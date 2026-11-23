
@RequiresApi(24)
class AudioRecordService : Service() {

    var status = NotNullLiveData(READY)
    private var lastStart = 0L
    private var audioDuration = 0L

    private lateinit var recorder: MediaRecorder

    override fun onCreate() {
        recorder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else MediaRecorder()

        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

            val output = this@AudioRecordService.getTempAudioFile()
            setOutputFile(output.path)
            prepare()
        }
    }

    override fun onDestroy() {
        recorder.release()
    }

    override fun onBind(intent: Intent?) = LocalBinder(this)

    fun start() {
        recorder.start()
        status.value = RECORDING
        lastStart = SystemClock.elapsedRealtime()
    }

    fun resume() {
        recorder.resume()
        status.value = RECORDING
        lastStart = SystemClock.elapsedRealtime()
    }

    fun pause() {
        recorder.pause()
        status.value = PAUSED
        audioDuration += SystemClock.elapsedRealtime() - lastStart
        lastStart = 0L
    }

    fun stop() {
        recorder.stop()
        stopSelf()
    }

    fun getBase(): Long {
        return if (lastStart != 0L) {
            lastStart - audioDuration
        } else SystemClock.elapsedRealtime() - audioDuration
    }
}
