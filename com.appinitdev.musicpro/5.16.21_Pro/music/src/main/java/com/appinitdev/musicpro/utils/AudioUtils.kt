
object AudioUtils {
     










    fun getSampling(context: Context, fileUri: Uri): String {
        val mex = MediaExtractor()
        try {
            context.contentResolver.openAssetFileDescriptor(fileUri, "r")!!.use {
                mex.setDataSource(it)
            }
            return "${
                mex.getTrackFormat(0).getInteger(MediaFormat.KEY_SAMPLE_RATE).toFloat() / 1000
            } kHz"
        } catch (_: IOException) {
        } catch (_: IllegalArgumentException) {
        } catch (_: NullPointerException) {
        } finally {
            mex.release()
        }

        return context.getString(R.string.not_available)
    }

    fun Int.toBitrate(): String {
        return when {
            this / 1000 < 1024 -> {
                "${this / 1000} kbit/s"
            }
            this / 1000 > 1024 -> {
                "${this / 1000} mbit/s"
            }
            else -> {
                "exceeding bitrate"
            }
        }
    }
}