
 









@RunWith(AndroidJUnit4::class)
class ThumbnailBenchmarkTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private lateinit var testJpeg: File
    private val TAG = "ThumbBench"

    
    private val thumbW = 1024
    private val thumbH = 1024

    @Before
    fun setUp() {
        
        
        
        testJpeg = File(tmp.newFolder("bench"), "photo.jpg")
        if (!testJpeg.exists()) {
            createSyntheticJpeg(testJpeg, width = 3024, height = 4032)
        }
        Log.i(TAG, "Test image: ${testJpeg.absolutePath} (${testJpeg.length() / 1024} KB)")
    }

    

    @Test
    fun benchmark_naive_fullDecode() {
        
        repeat(3) { naiveDecode(testJpeg, thumbW, thumbH) }

        val times = LongArray(5) { measureMs { naiveDecode(testJpeg, thumbW, thumbH) } }
        val avg = times.average().toLong()
        Log.i(TAG, "Naive full-decode avg=${avg}ms runs=${times.toList()}")
        
    }

    

    @Test
    fun benchmark_optimised_decode() {
        
        repeat(3) { decodeSampledBitmapFromFile(testJpeg.absolutePath, thumbW, thumbH, true) }

        val times = LongArray(5) { measureMs { decodeSampledBitmapFromFile(testJpeg.absolutePath, thumbW, thumbH, true) } }
        val avg = times.average().toLong()
        Log.i(TAG, "Optimised decode avg=${avg}ms runs=${times.toList()}")
        Assert.assertTrue(
            "Optimised decode avg ${avg}ms should be < 300 ms (target <200 ms with cache)",
            avg < 300,
        )
    }

    

    @Test
    fun benchmark_full_pipeline_nocache() {
        
        repeat(3) { fullPipeline(testJpeg, thumbW, thumbH) }

        val times = LongArray(5) { measureMs { fullPipeline(testJpeg, thumbW, thumbH) } }
        val avg = times.average().toLong()
        Log.i(TAG, "Full pipeline (no cache) avg=${avg}ms runs=${times.toList()}")
    }

    

    @Test
    fun benchmark_disk_cache_roundtrip() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        
        val bytes = fullPipeline(testJpeg, thumbW, thumbH) ?: return
        val cacheFile = File(context.cacheDir, "thumbs/bench_test.jpg")
        cacheFile.parentFile?.mkdirs()
        val writeMs = measureMs { cacheFile.writeBytes(bytes) }
        Log.i(TAG, "Cache write ${bytes.size / 1024} KB in ${writeMs}ms")

        
        val times = LongArray(5) { measureMs { cacheFile.readBytes() } }
        val avg = times.average().toLong()
        Log.i(TAG, "Cache read avg=${avg}ms runs=${times.toList()}")
        Assert.assertTrue("Cache read avg ${avg}ms should be < 30 ms", avg < 30)

        cacheFile.delete()
    }

    

    @Test
    fun correctness_dimensions_centerCrop() {
        val bmp = decodeSampledBitmapFromFile(testJpeg.absolutePath, thumbW, thumbH, true)
        Assert.assertNotNull("Bitmap must not be null", bmp)
        bmp!!
        Log.i(TAG, "CenterCrop result: ${bmp.width}×${bmp.height}")
        
        Assert.assertTrue(
            "Width (${bmp.width}) should be ≤ $thumbW",
            bmp.width <= thumbW,
        )
        Assert.assertTrue(
            "Height (${bmp.height}) should be ≤ $thumbH",
            bmp.height <= thumbH,
        )
        bmp.recycle()
    }

    @Test
    fun correctness_dimensions_fit() {
        val bmp = decodeSampledBitmapFromFile(testJpeg.absolutePath, 200, 200, false)
        Assert.assertNotNull("Bitmap must not be null", bmp)
        bmp!!
        Log.i(TAG, "FIT result: ${bmp.width}×${bmp.height}")
        Assert.assertTrue("Width should be ≤ 200", bmp.width <= 200)
        Assert.assertTrue("Height should be ≤ 200", bmp.height <= 200)
        bmp.recycle()
    }

    

    private inline fun measureMs(block: () -> Unit): Long {
        val start = System.currentTimeMillis()
        block()
        return System.currentTimeMillis() - start
    }

     
    private fun naiveDecode(file: File, w: Int, h: Int): Bitmap? {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return null
        val scaled = Bitmap.createScaledBitmap(bitmap, w, h, true)
        if (scaled !== bitmap) bitmap.recycle()
        return scaled
    }

     
    private fun fullPipeline(file: File, w: Int, h: Int): ByteArray? {
        val bitmap = decodeSampledBitmapFromFile(file.absolutePath, w, h, true) ?: return null
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        bitmap.recycle()
        return out.toByteArray()
    }

     



    private fun createSyntheticJpeg(dest: File, width: Int, height: Int) {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val r = (x * 255 / width)
                val g = (y * 255 / height)
                val b = 128
                pixels[y * width + x] = (255 shl 24) or (r shl 16) or (g shl 8) or b
            }
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        dest.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        bitmap.recycle()
        Log.i(TAG, "Synthetic JPEG created: ${width}×${height} → ${dest.length() / 1024} KB")
    }
}