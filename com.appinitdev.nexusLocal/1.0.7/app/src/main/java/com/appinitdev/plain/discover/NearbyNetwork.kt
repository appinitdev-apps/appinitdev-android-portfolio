
 





object NearbyNetwork {
    const val PORT = 52352
    private const val MULTICAST_ADDRESS = "224.0.0.100"
    private const val RECEIVE_TIMEOUT_MS = 10_000
    private const val BUFFER_SIZE = 2048
    private const val RESTART_DELAY_MS = 2000L

    private var receiverJob: Job? = null
    private var multicastLock: WifiManager.MulticastLock? = null

    

     
    fun sendMulticast(message: String) {
        coIO {
            var socket: MulticastSocket? = null
            try {
                socket = MulticastSocket()
                socket.timeToLive = 1 
                val address = InetAddress.getByName(MULTICAST_ADDRESS)
                val bytes = message.toByteArray()
                socket.send(DatagramPacket(bytes, bytes.size, address, PORT))
            } catch (e: Exception) {
                LogCat.e("Multicast send error: ${e.message}")
            } finally {
                socket?.close()
            }
        }
    }

     
    fun sendUnicast(message: String, targetIP: String) {
        coIO {
            var socket: DatagramSocket? = null
            try {
                socket = DatagramSocket()
                val address = InetAddress.getByName(targetIP)
                val bytes = message.toByteArray()
                socket.send(DatagramPacket(bytes, bytes.size, address, PORT))
            } catch (e: Exception) {
                LogCat.e("Unicast send error to $targetIP: ${e.message}")
            } finally {
                socket?.close()
            }
        }
    }

    

     








    fun startReceiver(onMessage: (message: String, senderIP: String) -> Unit) {
        if (receiverJob?.isActive == true) {
            return
        }

        
        
        
        if (multicastLock == null) {
            runCatching {
                val wifiManager = MainApp.instance.applicationContext
                    .getSystemService(Context.WIFI_SERVICE) as WifiManager
                multicastLock = wifiManager.createMulticastLock("PlainApp:discover").apply {
                    setReferenceCounted(false)
                    acquire()
                }
            }.onFailure { LogCat.e("Multicast lock acquire error: ${it.message}") }
        } else if (multicastLock?.isHeld == false) {
            runCatching { multicastLock?.acquire() }
        }

        receiverJob = coIO {
            while (isActive) {
                try {
                    receiveLoop(onMessage)
                } catch (e: Exception) {
                    LogCat.e("Multicast receiver error: ${e.message}")
                }
                delay(RESTART_DELAY_MS)
            }
        }
        LogCat.d("Multicast receiver started")
    }

     
    fun stopReceiver() {
        receiverJob?.cancel()
        receiverJob = null
        runCatching {
            if (multicastLock?.isHeld == true) multicastLock?.release()
        }
        multicastLock = null
        LogCat.d("Multicast receiver stopped")
    }

    

     





    private suspend fun receiveLoop(
        onMessage: (message: String, senderIP: String) -> Unit,
    ) {
        var socket: MulticastSocket? = null
        try {
            socket = MulticastSocket(null as SocketAddress?).apply {
                reuseAddress = true
                soTimeout = RECEIVE_TIMEOUT_MS
            }
            socket.bind(InetSocketAddress(PORT))

            val group = InetAddress.getByName(MULTICAST_ADDRESS)
            socket.joinGroup(group)

            val buffer = ByteArray(BUFFER_SIZE)
            while (receiverJob?.isActive == true) {
                try {
                    val packet = DatagramPacket(buffer, buffer.size)
                    socket.receive(packet)
                    val message = String(packet.data, 0, packet.length)
                    val senderIP = packet.address.hostAddress ?: ""
                    onMessage(message, senderIP)
                } catch (_: SocketTimeoutException) {
                    
                }
            }
        } finally {
            runCatching {
                socket?.leaveGroup(InetAddress.getByName(MULTICAST_ADDRESS))
                socket?.close()
            }
        }
    }
}
