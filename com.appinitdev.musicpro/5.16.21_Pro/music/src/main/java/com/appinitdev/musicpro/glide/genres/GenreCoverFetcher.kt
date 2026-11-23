
class GenreCoverFetcher internal constructor(private val context: Context, private val genre: Genre) : DataFetcher<Bitmap> {
    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Bitmap>) {
        val coverFromFiles = GenreCover.load(context, genre)
        callback.onDataReady(coverFromFiles)
    }

    override fun cleanup() {}
    override fun cancel() {}
    override fun getDataClass() = Bitmap::class.java
    override fun getDataSource() = DataSource.LOCAL
}