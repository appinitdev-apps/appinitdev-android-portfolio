
 













@OptIn(UnstableApi::class)
class WidgetActionReceiver : BroadcastReceiver() {

    private val TAG = "WidgetActionReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        
        
        
        val pendingResult = goAsync()

        val sessionToken = SessionToken(
                context.applicationContext,
                ComponentName(context.applicationContext, FelicityPlayerService::class.java)
        )

        val controllerFuture = MediaController.Builder(
                context.applicationContext,
                sessionToken
        ).buildAsync()

        Futures.addCallback(controllerFuture, object : FutureCallback<MediaController> {
            override fun onSuccess(controller: MediaController) {
                handleAction(context, controller, action) {
                    
                    
                    Handler(Looper.getMainLooper()).postDelayed({
                                                                    controller.release()
                                                                    pendingResult.finish()
                                                                }, 600L)
                }
            }

            override fun onFailure(t: Throwable) {
                Log.e(TAG, "Failed to connect to FelicityPlayerService: ${t.message}")
                pendingResult.finish()
            }
        }, MoreExecutors.directExecutor())
    }

     








    private fun handleAction(context: Context, controller: MediaController, action: String, onDone: () -> Unit) {
        when (action) {
            ACTION_PLAY_PAUSE -> {
                if (controller.mediaItemCount == 0) {
                    
                    
                    restoreQueueAndPlay(context, controller, onDone)
                } else {
                    
                    if (controller.isPlaying) {
                        controller.pause()
                    } else {
                        controller.play()
                    }
                    onDone()
                }
            }

            ACTION_NEXT -> {
                if (controller.mediaItemCount == 0) {
                    restoreQueueAndPlay(context, controller, onDone)
                } else {
                    controller.seekToNextMediaItem()
                    onDone()
                }
            }

            ACTION_PREV -> {
                if (controller.mediaItemCount == 0) {
                    restoreQueueAndPlay(context, controller, onDone)
                } else {
                    controller.seekToPreviousMediaItem()
                    onDone()
                }
            }

            else -> onDone()
        }
    }

     






    private fun restoreQueueAndPlay(context: Context, controller: MediaController, onDone: () -> Unit) {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val db = AudioDatabase.getInstance(context.applicationContext)
                val playbackState = PlaybackStateManager.fetchPlaybackState(db)
                val savedQueue = PlaybackStateManager.getAudiosFromQueueIDs(db)

                if (!savedQueue.isNullOrEmpty() && playbackState != null) {
                    val restoredIndex = when {
                        playbackState.currentHash == 0L -> {
                            playbackState.index.coerceIn(0, savedQueue.size - 1)
                        }
                        else -> {
                            val byHash = savedQueue.indexOfFirst { it.hash == playbackState.currentHash }
                            if (byHash >= 0) byHash else 0
                        }
                    }

                    withContext(Dispatchers.Main) {
                        
                        
                        MediaPlaybackManager.setMediaController(controller)
                        MediaPlaybackManager.setSongs(
                                audios = savedQueue,
                                position = restoredIndex,
                                startPositionMs = playbackState.position,
                                autoPlay = true
                        )
                    }
                } else {
                    Log.w(TAG, "No saved queue found — nothing to play from widget.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error restoring queue from database: ${e.message}", e)
            } finally {
                withContext(Dispatchers.Main) { onDone() }
            }
        }
    }

    companion object {
        const val ACTION_PLAY_PAUSE = "app.simple.felicity.ACTION_WIDGET_PLAY_PAUSE"
        const val ACTION_NEXT = "app.simple.felicity.ACTION_WIDGET_NEXT"
        const val ACTION_PREV = "app.simple.felicity.ACTION_WIDGET_PREV"
    }
}

