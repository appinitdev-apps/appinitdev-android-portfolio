
object NearbyPairManager {
    private val activePairingSessions = ConcurrentHashMap<String, DPairingSession>()

    
    private const val MAX_TIMESTAMP_DIFF_MS = 5 * 60 * 1000L

    private fun verifyPairingRequestSignature(request: DPairingRequest): Boolean {
        return try {
            val signatureData = request.toSignatureData()
            val signatureBytes = Base64.decode(request.signature, Base64.NO_WRAP)
            val rawPublicKey = Base64.decode(request.signaturePublicKey, Base64.NO_WRAP)
            CryptoHelper.verifySignatureWithRawEd25519PublicKey(rawPublicKey, signatureData.toByteArray(), signatureBytes)
        } catch (e: Exception) {
            LogCat.e("Failed to verify pairing request signature: ${e.message}")
            false
        }
    }

    private fun verifyPairingResponseSignature(response: DPairingResponse): Boolean {
        return try {
            val signatureData = response.toSignatureData()
            val signatureBytes = Base64.decode(response.signature, Base64.NO_WRAP)
            val rawPublicKey = Base64.decode(response.signaturePublicKey, Base64.NO_WRAP)
            CryptoHelper.verifySignatureWithRawEd25519PublicKey(rawPublicKey, signatureData.toByteArray(), signatureBytes)
        } catch (e: Exception) {
            LogCat.e("Failed to verify pairing response signature: ${e.message}")
            false
        }
    }

    suspend fun startPairingAsync(device: DNearbyDevice) {
        try {
            val context = MainApp.instance
            val deviceName = TempData.deviceName.value

            
            val keyPair = CryptoHelper.generateECDHKeyPair()

            
            val signaturePublicKey = SignatureHelper.getRawPublicKeyBase64Async()
            
            val bestIp = device.getBestIp()
            val session = DPairingSession(
                deviceId = device.id,
                deviceName = device.name,
                deviceIp = bestIp,
                keyPair = keyPair,
            )
            activePairingSessions[device.id] = session

            val currentTimestamp = System.currentTimeMillis()
            val ecdhPublicKeyBytes = CryptoHelper.getPublicKeyBytes(keyPair)
            val ecdhPublicKey = Base64.encodeToString(ecdhPublicKeyBytes, Base64.NO_WRAP)

            val request = DPairingRequest(
                fromId = TempData.clientId,
                fromName = deviceName,
                port = TempData.httpsPort.value,
                deviceType = PhoneHelper.getDeviceType(context),
                ecdhPublicKey = ecdhPublicKey,
                signaturePublicKey = signaturePublicKey,
                timestamp = currentTimestamp,
                ips = NetworkHelper.getDeviceIP4s().toList(),
            )
            request.signature = SignatureHelper.signTextAsync(request.toSignatureData())

            sendPairingMessage(NearbyMessageType.PAIR_REQUEST, JsonHelper.jsonEncode(request), bestIp)
        } catch (e: Exception) {
            LogCat.e("Error starting pairing: ${e.message}")
            val event = PairingFailedEvent(device.id, "Failed to send pairing request")
            sendEvent(event)
            sendEvent(WebSocketEvent(EventType.PAIRING_FAILED, JsonHelper.jsonEncode(DPairingResult(event.deviceId, event.reason))))
        }
    }

    suspend fun respondToPairing(request: DPairingRequest, accepted: Boolean) {
        try {
            
            val currentTime = System.currentTimeMillis()
            if (abs(currentTime - request.timestamp) > MAX_TIMESTAMP_DIFF_MS) {
                LogCat.e("Pairing request timestamp is too old or in the future")
                val event = PairingFailedEvent(request.fromId, "Invalid timestamp")
                sendEvent(event)
                sendEvent(WebSocketEvent(EventType.PAIRING_FAILED, JsonHelper.jsonEncode(DPairingResult(event.deviceId, event.reason))))
                return
            }

            
            if (!verifyPairingRequestSignature(request)) {
                LogCat.e("Pairing request signature verification failed")
                val event = PairingFailedEvent(request.fromId, "Signature verification failed")
                sendEvent(event)
                sendEvent(WebSocketEvent(EventType.PAIRING_FAILED, JsonHelper.jsonEncode(DPairingResult(event.deviceId, event.reason))))
                return
            }

            LogCat.d("Pairing request signature verified successfully")

            if (accepted) {
                
                val keyPair = CryptoHelper.generateECDHKeyPair()

                
                val signaturePublicKey = SignatureHelper.getRawPublicKeyBase64Async()
                
                val session = DPairingSession(
                    deviceId = request.fromId,
                    deviceName = request.fromName,
                    deviceIp = request.fromIp,
                    keyPair = keyPair,
                )
                activePairingSessions[request.fromId] = session

                val responseTimestamp = System.currentTimeMillis()
                val ecdhPublicKeyBytes = CryptoHelper.getPublicKeyBytes(keyPair)
                val ecdhPublicKey = Base64.encodeToString(ecdhPublicKeyBytes, Base64.NO_WRAP)

                val response = DPairingResponse(
                    fromId = TempData.clientId,
                    toId = request.fromId,
                    port = TempData.httpsPort.value,
                    deviceType = PhoneHelper.getDeviceType(MainApp.instance),
                    ecdhPublicKey = ecdhPublicKey,
                    signaturePublicKey = signaturePublicKey,
                    accepted = true,
                    timestamp = responseTimestamp,
                    ips = NetworkHelper.getDeviceIP4s().toList(),
                )

                response.signature = SignatureHelper.signTextAsync(response.toSignatureData())

                val requestEcdhPublicKey = Base64.decode(request.ecdhPublicKey, Base64.NO_WRAP)
                val encryptKey = CryptoHelper.computeECDHSharedKey(keyPair.private, requestEcdhPublicKey)
                if (encryptKey != null) {
                    
                    val peerIps = (listOf(request.fromIp) + request.ips).distinct()
                    storePeerInDatabase(
                        request.fromId,
                        request.fromName,
                        peerIps,
                        request.port,
                        request.deviceType,
                        encryptKey,
                        request.signaturePublicKey
                    )
                    sendPairingMessage(NearbyMessageType.PAIR_RESPONSE, JsonHelper.jsonEncode(response), request.fromIp)
                    sendEvent(PairingSuccessEvent(request.fromId, request.fromName, request.fromIp, encryptKey))
                    sendEvent(WebSocketEvent(EventType.PAIRING_SUCCESS, JsonHelper.jsonEncode(DPairingResult(response.fromId))))
                } else {
                    throw Exception("Failed to compute shared key")
                }
            } else {
                
                val signaturePublicKey = SignatureHelper.getRawPublicKeyBase64Async()
                val rejectionTimestamp = System.currentTimeMillis()

                val response = DPairingResponse(
                    fromId = TempData.clientId,
                    toId = request.fromId,
                    port = TempData.httpsPort.value,
                    deviceType = request.deviceType,
                    ecdhPublicKey = "",
                    signaturePublicKey = signaturePublicKey,
                    accepted = false,
                    timestamp = rejectionTimestamp,
                )

                response.signature = SignatureHelper.signTextAsync(response.toSignatureData())

                sendPairingMessage(NearbyMessageType.PAIR_RESPONSE, JsonHelper.jsonEncode(response), request.fromIp)
                LogCat.d("Signed pairing rejection response sent to ${request.fromName}")
            }
        } catch (e: Exception) {
            LogCat.e("Error responding to pairing: ${e.message}")
            val event = PairingFailedEvent(request.fromId, "Failed to respond to pairing request")
            sendEvent(event)
            sendEvent(WebSocketEvent(EventType.PAIRING_FAILED, JsonHelper.jsonEncode(DPairingResult(event.deviceId, event.reason))))
        } finally {
            
            if (!accepted) {
                activePairingSessions.remove(request.fromId)
            }
        }
    }

    private fun sendPairingMessage(type: NearbyMessageType, message: String, targetIp: String) {
        NearbyNetwork.sendUnicast("${type.toPrefix()}${message}", targetIp)
    }

    fun cancelPairing(deviceId: String) {
        val session = activePairingSessions[deviceId]
        if (session != null) {
            
            try {
                val cancelMessage = DPairingCancel(
                    fromId = TempData.clientId,
                    toId = deviceId
                )
                sendPairingMessage(NearbyMessageType.PAIR_CANCEL, JsonHelper.jsonEncode(cancelMessage), session.deviceIp)
                LogCat.d("Pairing cancel message sent to ${session.deviceName}")
            } catch (e: Exception) {
                LogCat.e("Error sending pairing cancel message: ${e.message}")
            }
        }

        activePairingSessions.remove(deviceId)
        val event = PairingFailedEvent(deviceId, "Pairing cancelled by user")
        sendEvent(event)
        sendEvent(WebSocketEvent(EventType.PAIRING_FAILED, JsonHelper.jsonEncode(DPairingResult(event.deviceId, event.reason))))
        LogCat.d("Pairing cancelled for device: $deviceId")
    }

    suspend fun handlePairingResponse(response: DPairingResponse, senderIP: String) {
        val session = activePairingSessions[response.fromId]
        if (session == null) {
            LogCat.e("No active pairing session found for device ${response.fromId}")
            return
        }

        
        try {
            
            val currentTime = System.currentTimeMillis()
            if (abs(currentTime - response.timestamp) > MAX_TIMESTAMP_DIFF_MS) {
                LogCat.e("Pairing response timestamp is too old or in the future")
                val event = PairingFailedEvent(response.fromId, "Invalid timestamp")
                sendEvent(event)
                sendEvent(WebSocketEvent(EventType.PAIRING_FAILED, JsonHelper.jsonEncode(DPairingResult(event.deviceId, event.reason))))
                return
            }

            
            if (!verifyPairingResponseSignature(response)) {
                LogCat.e("Pairing response signature verification failed")
                val event = PairingFailedEvent(response.fromId, "Signature verification failed")
                sendEvent(event)
                sendEvent(WebSocketEvent(EventType.PAIRING_FAILED, JsonHelper.jsonEncode(DPairingResult(event.deviceId, event.reason))))
                return
            }

            LogCat.d("Pairing response signature verified successfully")

            if (response.accepted) {
                val responseEcdhPublicKey = Base64.decode(response.ecdhPublicKey, Base64.NO_WRAP)
                val encryptKey = CryptoHelper.computeECDHSharedKey(session.keyPair.private, responseEcdhPublicKey)
                if (encryptKey != null) {
                    
                    val peerIps = (listOf(senderIP) + response.ips).distinct()
                    storePeerInDatabase(
                        response.fromId,
                        session.deviceName,
                        peerIps,
                        response.port,
                        response.deviceType,
                        encryptKey,
                        response.signaturePublicKey
                    )
                    sendEvent(PairingSuccessEvent(response.fromId, session.deviceName, senderIP, encryptKey))
                    sendEvent(WebSocketEvent(EventType.PAIRING_SUCCESS, JsonHelper.jsonEncode(DPairingResult(response.fromId))))
                    LogCat.d("Pairing completed successfully with ${session.deviceName}")
                } else {
                    throw Exception("Failed to compute shared key")
                }
            } else {
                val event = PairingFailedEvent(response.fromId, "Pairing request was rejected")
                sendEvent(event)
                sendEvent(WebSocketEvent(EventType.PAIRING_FAILED, JsonHelper.jsonEncode(DPairingResult(event.deviceId, event.reason))))
                LogCat.d("Verified pairing rejection from ${session.deviceName}")
            }
        } catch (e: Exception) {
            LogCat.e("Error processing pairing response: ${e.message}")
            val event = PairingFailedEvent(response.fromId, "Failed to process pairing response")
            sendEvent(event)
            sendEvent(WebSocketEvent(EventType.PAIRING_FAILED, JsonHelper.jsonEncode(DPairingResult(event.deviceId, event.reason))))
        }

        
        activePairingSessions.remove(response.fromId)
    }

    fun handlePairingCancel(cancel: DPairingCancel) {
        LogCat.d("Pairing cancelled by remote device: ${cancel.fromId}")
        sendEvent(PairingCanceledEvent(cancel.fromId))
        sendEvent(WebSocketEvent(EventType.PAIRING_CANCELED, JsonHelper.jsonEncode(DPairingResult(cancel.fromId))))
        
        activePairingSessions.remove(cancel.fromId)
    }

    private suspend fun storePeerInDatabase(
        deviceId: String,
        deviceName: String,
        deviceIps: List<String>,
        port: Int,
        deviceType: DeviceType,
        key: String,
        signaturePublicKey: String
    ) {
        try {
            PeerManager.upsertPaired(
                deviceId = deviceId,
                deviceName = deviceName,
                deviceIps = deviceIps,
                port = port,
                deviceType = deviceType,
                key = key,
                signaturePublicKey = signaturePublicKey,
            )
        } catch (e: Exception) {
            LogCat.e("Error storing peer in database: ${e.message}")
            throw e
        }
    }
} 