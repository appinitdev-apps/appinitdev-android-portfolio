class FullImageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFullImageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFullImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val imageUrls = intent.getStringArrayListExtra("images") ?: arrayListOf()
        val singleUrl = intent.getStringExtra("image_url")
        val list = mutableListOf<CarouselItem>()
        if (imageUrls.isNotEmpty()) {
            for (url in imageUrls) {
                val fileOrUrl = com.appinitdev.turismoOax.network.OfflineImageManager.getFileOrUrl(this, url)
                val pathOrUrl = if (fileOrUrl is java.io.File) fileOrUrl.absolutePath else url
                list.add(CarouselItem(imageUrl = pathOrUrl))
            }
        } else if (!singleUrl.isNullOrEmpty()) {
            val fileOrUrl = com.appinitdev.turismoOax.network.OfflineImageManager.getFileOrUrl(this, singleUrl)
            val pathOrUrl = if (fileOrUrl is java.io.File) fileOrUrl.absolutePath else singleUrl
            list.add(CarouselItem(imageUrl = pathOrUrl))
        }
        binding.fullCarousel.setData(list)
        binding.btnClose.setOnClickListener {
            finish()
        }
    }
}
