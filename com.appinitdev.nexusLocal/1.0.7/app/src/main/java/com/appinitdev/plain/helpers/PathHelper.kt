
object PathHelper {
    fun getPlainPublicDir(dirName: String): File {
        val dir = Environment.getExternalStoragePublicDirectory(dirName)
        return File(dir, "PlainApp")
    }
}