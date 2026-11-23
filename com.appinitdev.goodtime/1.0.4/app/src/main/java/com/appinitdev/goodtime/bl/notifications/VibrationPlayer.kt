data class VibrationData(
    val strength: Int,
    val loop: Boolean,
)
class VibrationPlayer(
    context: Context,
    private val settingsRepo: SettingsRepository,
    private val playerScope: CoroutineScope,
    ioScope: CoroutineScope,
) {
    private var data: VibrationData = VibrationData(3, false)
    private var job: Job? = null
    init {
        ioScope.launch {
            settingsRepo.settings
                .map {
                    VibrationData(
                        it.vibrationStrength,
                        it.insistentNotification,
                    )
                }.collect {
                    data = it
                }
        }
    }
    private val vibrator =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
    fun start() {
        start(data)
    }
    fun stop() {
        playerScope.launch {
            job?.cancelAndJoin()
            job =
                playerScope.launch {
                    vibrator.cancel()
                }
        }
    }
    fun start(strength: Int) {
        start(VibrationData(strength, false))
    }
    private fun start(data: VibrationData) {
        playerScope.launch {
            job?.cancelAndJoin()
            job =
                playerScope.launch innerLaunch@{
                    vibrator.cancel()
                    val (strength, loop) = data
                    val repeat = if (loop) 1 else -1
                    if (strength == 0 || !vibrator.hasVibrator()) {
                        return@innerLaunch
                    }
                    val pattern =
                        when (strength) {
                            1 -> {
                                longArrayOf(0, 100, 2000)
                            }
                            2 -> {
                                longArrayOf(0, 100, 50, 100, 1000)
                            }
                            3 -> {
                                longArrayOf(0, 200, 50, 200, 1000)
                            }
                            4 -> {
                                longArrayOf(0, 400, 100, 400, 1000)
                            }
                            5 -> {
                                longArrayOf(0, 400, 100, 400, 100, 400, 1000)
                            }
                            else -> longArrayOf()
                        }
                    delay(100)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val vibrationAttributes =
                            VibrationAttributes
                                .Builder()
                                .setUsage(VibrationAttributes.USAGE_ALARM)
                                .build()
                        vibrator.vibrate(
                            VibrationEffect.createWaveform(pattern, repeat),
                            vibrationAttributes,
                        )
                    } else {
                        val audioAttributes =
                            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build()
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(
                            VibrationEffect.createWaveform(pattern, repeat),
                            audioAttributes,
                        )
                    }
                }
        }
    }
}
