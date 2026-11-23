
@SuppressLint("CheckResult")
internal fun MainActivity.initEvents() {
    lifecycleScope.launch {
        Channel.sharedFlow.collect { event ->
            if (isDestroyed || isFinishing) return@collect

            when (event) {
                is HttpServerStateChangedEvent -> {
                    mainVM.httpServerError = HttpServerManager.httpServerError
                    mainVM.httpServerState = event.state
                    if (event.state == HttpServerState.ON && !Permission.WRITE_EXTERNAL_STORAGE.can(this@initEvents)) {
                        DialogHelper.showConfirmDialog(LocaleHelper.getStringAsync(Res.string.confirm), LocaleHelper.getStringAsync(Res.string.storage_permission_confirm)) {
                            coIO { ApiPermissionsPreference.putAsync(Permission.WRITE_EXTERNAL_STORAGE, true); sendEvent(RequestPermissionsEvent(Permission.WRITE_EXTERNAL_STORAGE)) }
                        }
                    }
                }

                is PermissionsResultEvent -> {
                    
                }

                is HStartScreenMirrorEvent -> {
                    try {
                        if (event.audio && !Permission.RECORD_AUDIO.can(this@initEvents)) recordAudioForMirror.launch(android.Manifest.permission.RECORD_AUDIO)
                        else screenCapture.launch(mediaProjectionManager.createScreenCaptureIntent())
                    } catch (e: IllegalStateException) {
                        LogCat.e("Error launching screen capture: ${e.message}")
                    }
                }

                is HRequestScreenMirrorAudioEvent -> {
                    try {
                        if (Permission.RECORD_AUDIO.can(this@initEvents)) sendScreenMirrorAudioStatus(true)
                        else recordAudioForMirrorLate.launch(android.Manifest.permission.RECORD_AUDIO)
                    } catch (e: IllegalStateException) {
                        LogCat.e("Error requesting RECORD_AUDIO: ${e.message}")
                    }
                }

                is IgnoreBatteryOptimizationEvent -> {
                    try {
                        ignoreBatteryOptimizationActivityLauncher.launch(Intent().apply {
                            action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS; data = Uri.parse("package:$packageName")
                        })
                    } catch (e: IllegalStateException) {
                        LogCat.e("Error launching battery optimization: ${e.message}")
                    }
                }

                is RestartAppEvent -> {
                    startActivity(Intent(this@initEvents, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK })
                    Runtime.getRuntime().exit(0)
                }

                is HOpenAccessibilitySettingsEvent -> {
                    try {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    } catch (e: Exception) {
                        LogCat.e("Error opening accessibility settings: ${e.message}")
                    }
                }



















                is HOpenWebSettingsEvent -> {
                    try {
                        val nav = navControllerState.value
                        val alreadyThere = nav?.currentBackStackEntry?.destination?.hasRoute<Routing.WebSettings>() == true

                        if (AppHelper.foregrounded()) {
                            if (!alreadyThere) nav?.navigate(Routing.WebSettings)
                        } else {
                            
                            
                            val context = this@initEvents 

                            val intent = Intent(context, MainActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                putExtra("navigate_to_web_settings", true)
                            }

                            
                            val pendingIntent = android.app.PendingIntent.getActivity(
                                context,
                                1001,
                                intent,
                                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                            )

                            
                            
                            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                            val channelId = "web_access_channel" 

                            val notification = androidx.core.app.NotificationCompat.Builder(context, channelId)
                                .setContentTitle(getString(Res.string.permission_required))
                                .setContentText(getString(Res.string.web_settings))
                                .setSmallIcon(android.R.drawable.ic_dialog_info) 
                                .setAutoCancel(true)
                                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                                .setContentIntent(pendingIntent)
                                .build()

                            notificationManager.notify(999, notification)
                            LogCat.d("App en background: Se envió notificación para abrir WebSettings de forma segura.")
                        }
                    } catch (e: Exception) {
                        LogCat.e("Error navigating to WebSettings: ${e.message}")
                    }
                }

                is PickFileEvent -> handlePickFileEvent(event)
                is ExportFileEvent -> handleExportFileEvent(event)
                is ConfirmToAcceptLoginEvent -> {
                    pendingLoginEvent = event
                    openNew()
                }

                is PairingRequestReceivedEvent -> {
                    pendingPairingEvent = event
                    openNew()
                }

                is ChannelInviteReceivedEvent -> {
                    pendingChannelInviteEvent = event
                    openNew()
                }

                is PairingCanceledEvent -> {
                    pendingPairingEvent = null
                }
                is PairingSuccessEvent -> {
                    withIO { peerVM.loadPeers() }
                    PeerStatusManager.reconnectNow("post_pairing")
                    navControllerState.value?.navigate(Routing.Chat("peer:${event.deviceId}")) { popUpTo<Routing.Nearby> { inclusive = true } }
                }
            }
        }
    }
}

internal suspend fun MainActivity.doWhenReadyAsync() {
    val webEnabled = WebPreference.getAsync()
    val permEnabled = Permission.NOTIFICATION_LISTENER.isEnabledAsync(this)
    PNotificationListenerService.toggle(this, webEnabled && permEnabled)
}
