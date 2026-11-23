
class VoiceRecorder(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null

    fun startRecording() {
        audioFile = File(context.cacheDir, "voice_note_${System.currentTimeMillis()}.m4a")
        
        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(audioFile?.absolutePath)
            
            try {
                prepare()
                start()
            } catch (e: Exception) {
                Log.e("VoiceRecorder", "Recording failed", e)
            }
        }
    }

    fun stopRecording(): File? {
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            audioFile
        } catch (e: Exception) {
            Log.e("VoiceRecorder", "Stop recording failed", e)
            null
        }
    }
}
