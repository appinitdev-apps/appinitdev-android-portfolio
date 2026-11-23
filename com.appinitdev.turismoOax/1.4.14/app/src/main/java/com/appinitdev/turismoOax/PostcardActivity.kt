
class PostcardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostcardBinding
    private var selectedBitmap: Bitmap? = null

    private val selectImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                try {
                    @Suppress("DEPRECATION")
                    selectedBitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
                    binding.ivPostcardPreview.setImageBitmap(selectedBitmap)
                } catch (_: Exception) {
                    Toast.makeText(this, getString(R.string.msg_error_cargar_imagen), Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostcardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupStampSelection()

        binding.btnSelectPhoto.setOnClickListener {
            selectImageLauncher.launch("image/*")
        }

        binding.btnSharePostcard.setOnClickListener {
            compartirPostal()
        }
    }

    private fun setupStampSelection() {
        binding.cgStamps.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val text = when (checkedIds.first()) {
                    R.id.chipStamp2 -> "🌵 ¡Ruta del Mezcal! | Turismo Oaxaca 🇲🇽"
                    R.id.chipStamp3 -> "✨ ¡Guelaguetza Mágica! | Turismo Oaxaca 🇲🇽"
                    R.id.chipStamp4 -> "🌊 ¡Costa Oaxaqueña! | Turismo Oaxaca 🇲🇽"
                    else -> "❤️ ¡Mi Recuerdo de Oaxaca! | Turismo Oaxaca 🇲🇽"
                }
                binding.tvPostcardStamp.text = text
            }
        }
    }

    private fun compartirPostal() {
        try {
            val cardView = binding.ivPostcardPreview.parent as android.view.View
            val resultBitmap = Bitmap.createBitmap(cardView.width, cardView.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(resultBitmap)
            cardView.draw(canvas)

            val cachePath = File(cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "postal_oaxaca.png")
            val stream = FileOutputStream(file)
            resultBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            val contentUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)

            if (contentUri != null) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    setDataAndType(contentUri, contentResolver.getType(contentUri))
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    putExtra(Intent.EXTRA_TEXT, getString(R.string.postcard_share_text))
                }
                startActivity(Intent.createChooser(shareIntent, getString(R.string.postcard_chooser_title)))
            }
        } catch (_: Exception) {
            Toast.makeText(this, getString(R.string.msg_error_generar_postal), Toast.LENGTH_SHORT).show()
        }
    }
}
