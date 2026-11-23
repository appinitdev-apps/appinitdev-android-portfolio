 

















data class TorchManagerData(
    val enabled: Boolean = false,
    val loop: Boolean = false,
)

 


class TorchManager(
    context: Context,
    ioScope: CoroutineScope,
    private val playerScope: CoroutineScope,
    private val settingsRepo: SettingsRepository,
    private val logger: Logger,
) {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraId: String? = null
    private var data: TorchManagerData = TorchManagerData()

    private var job: Job? = null

    init {
        for (id in cameraManager.cameraIdList) {
            val flashAvailable =
                cameraManager.getCameraCharacteristics(id).get(FLASH_INFO_AVAILABLE)
            if (flashAvailable == true) {
                cameraId = id
                break
            }
        }
        ioScope.launch {
            settingsRepo.settings
                .map {
                    TorchManagerData(
                        enabled = it.enableTorch,
                        loop = it.insistentNotification,
                    )
                }.collect {
                    data = it
                }
        }
    }

    fun isTorchAvailable() = cameraId != null

    fun start() {
        if (!data.enabled) return
        job =
            playerScope.launch {
                cameraId?.let {
                    try {
                        val pattern = listOf(100L, 50L, 100L)
                        if (data.loop) {
                            while (isActive) {
                                cameraManager.lightUp(pattern, it)
                                delay(1000)
                            }
                        } else {
                            cameraManager.lightUp(pattern, it)
                        }
                    } catch (e: CameraAccessException) {
                        logger.e(e) { "Failed to access the camera" }
                    }
                }
            }
    }

    fun stop() {
        if (!data.enabled) return
        job?.cancel()
        cameraId?.let {
            try {
                cameraManager.setTorchMode(it, false)
            } catch (e: CameraAccessException) {
                logger.e(e) { "Failed to access the camera" }
            }
        }
    }
}

private suspend fun CameraManager.lightUp(
    listOf: List<Long>,
    cameraId: String,
) {
    listOf.forEachIndexed { index, i ->
        if (index % 2 == 0) {
            setTorchMode(cameraId, true)
        } else {
            setTorchMode(cameraId, false)
        }
        delay(i)
    }
    setTorchMode(cameraId, false)
}
