class DetalleTourActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetalleTourBinding
    private lateinit var dbHelper: DatabaseHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetalleTourBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbHelper = DatabaseHelper(this)
        val tour = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("tour", Tour::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("tour")
        }
        tour?.let { setupUI(it) }
    }
    private fun setupUI(tour: Tour) {
        val images = tour.imagenes ?: emptyList()
        val list = mutableListOf<CarouselItem>()
        val rawList = if (images.isNotEmpty()) images else listOf(tour.imagenPrincipal)
        for (imagen in rawList) {
            val fileOrUrl = com.appinitdev.turismoOax.network.OfflineImageManager.getFileOrUrl(this, imagen)
            val pathOrUrl = if (fileOrUrl is java.io.File) fileOrUrl.absolutePath else imagen
            list.add(CarouselItem(imageUrl = pathOrUrl))
        }
        binding.carousel.setData(list)
        binding.carousel.carouselListener = object : org.imaginativeworld.whynotimagecarousel.listener.CarouselListener {
            override fun onClick(position: Int, carouselItem: CarouselItem) {
                val intent = Intent(this@DetalleTourActivity, FullImageActivity::class.java).apply {
                    putStringArrayListExtra("images", ArrayList(if (images.isNotEmpty()) images else listOf(tour.imagenPrincipal)))
                }
                startActivity(intent)
            }
        }
        binding.tvTourName.text = tour.getNombreActual()
        binding.tvDescription.text = tour.getDescripcionActual()
        binding.tvPriceCollective.text = "Colectivo: ${tour.costoColectivo}"
        binding.tvPricePrivate.text = "Privado: ${tour.costoPrivado}"
        val flyerSource = com.appinitdev.turismoOax.network.OfflineImageManager.getFileOrUrl(this, tour.flayer)
        Glide.with(this)
            .load(flyerSource)
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .into(binding.flayer)
        binding.flayer.setOnClickListener {
            val intent = Intent(this@DetalleTourActivity, FullImageActivity::class.java).apply {
                putExtra("image_url", tour.flayer)
            }
            startActivity(intent)
        }
        updateFavIcon(dbHelper.isFavorito(tour.id))
        binding.btnFavDetalle.setOnClickListener {
            val isFavNow = dbHelper.toggleFavorito(tour)
            updateFavIcon(isFavNow)
            val msg = if (isFavNow) getString(R.string.msg_anadido_favoritos) else getString(R.string.msg_removido_favoritos)
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
        val places = tour.getLugaresActuales()
        binding.rvPlaces.layoutManager = LinearLayoutManager(this)
        binding.rvPlaces.adapter = PlacesAdapter(places)
        binding.btnReservar.setOnClickListener {
            val intent = Intent(this, CotizacionActivity::class.java)
            intent.putExtra("tour", tour.nombre)
            startActivity(intent)
        }
        binding.btnWhatsApp.setOnClickListener {
            val phoneNumber = "529511234567"
            val message = "Hola, me interesa solicitar más información sobre el tour: ${tour.nombre}"
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}")
                }
                startActivity(intent)
            } catch (_: Exception) {
                Toast.makeText(this, getString(R.string.msg_no_whatsapp), Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnCompartir.setOnClickListener {
            val shareText = "¡Mira este increíble tour en Oaxaca!\n\n*${tour.nombre}*\n${tour.descripcion}\n\nColectivo: ${tour.costoColectivo} | Privado: ${tour.costoPrivado}\n\n¡Descubre Oaxaca con nosotros!"
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, tour.nombre)
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(intent, getString(R.string.btn_compartir)))
        }
        binding.btnCalendario.setOnClickListener {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, "Tour Oaxaca: ${tour.nombre}")
                putExtra(CalendarContract.Events.DESCRIPTION, tour.descripcion)
                putExtra(CalendarContract.Events.EVENT_LOCATION, "Oaxaca, México")
            }
            try {
                startActivity(intent)
            } catch (_: Exception) {
                Toast.makeText(this, getString(R.string.msg_no_calendario), Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnMapa.setOnClickListener {
            val lugarQuery = "Oaxaca " + (tour.lugares?.firstOrNull() ?: tour.nombre)
            val mapUri = Uri.parse("geo:0,0?q=${Uri.encode(lugarQuery)}")
            val intent = Intent(Intent.ACTION_VIEW, mapUri)
            try {
                startActivity(intent)
            } catch (_: Exception) {
                val webMapUri = Uri.parse("https://www.openstreetmap.org/search?query=${Uri.encode(lugarQuery)}")
                startActivity(Intent(Intent.ACTION_VIEW, webMapUri))
            }
        }
    }
    private fun updateFavIcon(isFavorite: Boolean) {
        val colorRes = if (isFavorite) R.color.red_500 else R.color.grey_400
        val color = ContextCompat.getColor(this, colorRes)
        ImageViewCompat.setImageTintList(binding.btnFavDetalle, ColorStateList.valueOf(color))
    }
}
