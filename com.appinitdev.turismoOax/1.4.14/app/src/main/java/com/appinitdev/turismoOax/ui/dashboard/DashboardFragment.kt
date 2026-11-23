
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var tourAdapter: TourAdapter
    private lateinit var dbHelper: DatabaseHelper

    private var listaOriginal: List<Tour> = emptyList()
    private var listaFiltrada: MutableList<Tour> = mutableListOf()

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        dbHelper = DatabaseHelper(requireContext())

        if (!estaConectado(requireContext())) {
            mostrarMensajeSinConexion(requireContext())
        }

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        setupSearchAndFilters()
        setupExtraActions()
        cargarClimaDestinos()
        cargarTours()

        return root
    }

    private fun cargarClimaDestinos() {
        val destinations = listOf(
            WeatherDestination("1", "Oaxaca Centro", "Oaxaca City", "25°C", "☀️", "Cálido y Soleado", "Warm & Sunny", "Clima agradable para recorrer el Centro Histórico y mercados.", "Great weather for exploring downtown.", 17.0604, -96.7253),
            WeatherDestination("2", "Hierve el Agua", "Petrified Waterfalls", "23°C", "🌤️", "Templado Agraz", "Mild & Scenic", "Usa bloqueador solar biodegradable y traje de baño.", "Bring sunscreen and a swimsuit.", 16.8661, -96.2758),
            WeatherDestination("3", "Monte Albán", "Monte Alban Site", "24°C", "☀️", "Soleado y Ventoso", "Sunny & Windy", "Lleva sombrero, agua y tenis cómodos.", "Wear a hat, bring water and walking shoes.", 17.0439, -96.7678),
            WeatherDestination("4", "San José del Pacífico", "Sierra Mountains", "16°C", "🌲", "Fresco de Montaña", "Fresh Mountain Air", "Lleva chamarra o suéter abrigador para la tarde.", "Bring a warm jacket or sweater.", 16.1711, -96.5022),
            WeatherDestination("5", "Puerto Escondido", "Pacific Coast", "29°C", "🌊", "Cálido Tropical", "Warm Tropical Coast", "Día perfecto para playa, surf y avistamiento marítimo.", "Perfect beach day.", 15.8614, -97.0664)
        )

        val weatherAdapter = WeatherAdapter(destinations)
        binding.rvWeather.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvWeather.adapter = weatherAdapter

        lifecycleScope.launch(Dispatchers.IO) {
            for (dest in destinations) {
                try {
                    val url = "https://api.open-meteo.com/v1/forecast?latitude=${dest.lat}&longitude=${dest.lon}&current_weather=true"
                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.connectTimeout = 3000
                    connection.readTimeout = 3000
                    val stream = connection.inputStream
                    val json = stream.bufferedReader().use { it.readText() }
                    val jsonObject = JSONObject(json)
                    if (jsonObject.has("current_weather")) {
                        val currentWeather = jsonObject.getJSONObject("current_weather")
                        val tempValue = currentWeather.getDouble("temperature").toInt()
                        val code = if (currentWeather.has("weathercode")) currentWeather.getInt("weathercode") else 0
                        
                        dest.temp = "$tempValue°C"
                        when (code) {
                            0 -> {
                                dest.icon = "☀️"
                                dest.statusEs = "Soleado y Despejado"
                                dest.statusEn = "Sunny & Clear"
                            }
                            1, 2 -> {
                                dest.icon = "🌤️"
                                dest.statusEs = "Parcialmente Nublado"
                                dest.statusEn = "Partly Cloudy"
                            }
                            3 -> {
                                dest.icon = "☁️"
                                dest.statusEs = "Nublado"
                                dest.statusEn = "Overcast"
                            }
                            45, 48 -> {
                                dest.icon = "🌫️"
                                dest.statusEs = "Neblina"
                                dest.statusEn = "Foggy"
                            }
                            51, 53, 55, 56, 57 -> {
                                dest.icon = "🌦️"
                                dest.statusEs = "Llovizna Ligera"
                                dest.statusEn = "Light Drizzle"
                            }
                            61, 63, 65, 66, 67, 80, 81, 82 -> {
                                dest.icon = "🌧️"
                                dest.statusEs = "Lluvia"
                                dest.statusEn = "Rainy"
                            }
                            95, 96, 99 -> {
                                dest.icon = "🌩️"
                                dest.statusEs = "Tormenta Eléctrica"
                                dest.statusEn = "Thunderstorm"
                            }
                            else -> {
                                dest.icon = "🌤️"
                                dest.statusEs = "Templado"
                                dest.statusEn = "Mild"
                            }
                        }
                    }
                } catch (_: Exception) {
                    // Mantiene los valores estimados locales si está sin conexión
                }
            }
            withContext(Dispatchers.Main) {
                weatherAdapter.updateData(destinations)
            }
        }
    }

    private fun setupSearchAndFilters() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                aplicarFiltros()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.chipGroupFilters.setOnCheckedStateChangeListener { _, _ ->
            aplicarFiltros()
        }
    }

    private fun setupExtraActions() {
        // Selector de Idioma
        binding.btnLanguage.setOnClickListener {
            val opciones = arrayOf("Español 🇲🇽", "English 🇺🇸")
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Seleccionar Idioma / Select Language")
                .setItems(opciones) { _, which ->
                    val tag = if (which == 0) "es" else "en"
                    val locales = LocaleListCompat.forLanguageTags(tag)
                    AppCompatDelegate.setApplicationLocales(locales)
                }
                .show()
        }


    }

    private fun aplicarFiltros() {
        val query = binding.etSearch.text.toString().trim().lowercase()
        val checkedChipId = binding.chipGroupFilters.checkedChipId

        val baseLista = when (checkedChipId) {
            R.id.chipFavoritos -> dbHelper.getAllFavoritos()
            R.id.chipDirectos -> listaOriginal.filter { it.id.startsWith("directo_") }
            else -> listaOriginal
        }

        listaFiltrada = if (query.isEmpty()) {
            baseLista.toMutableList()
        } else {
            baseLista.filter { tour ->
                tour.getNombreActual().lowercase().contains(query) ||
                        tour.getDescripcionActual().lowercase().contains(query)
            }.toMutableList()
        }

        if (::tourAdapter.isInitialized) {
            tourAdapter.updateData(listaFiltrada)
        }
    }

    private fun mostrarMensajeSinConexion(context: Context) {
        MaterialAlertDialogBuilder(context)
            .setTitle(getString(R.string.sin_conexi_n_a_internet))
            .setMessage(getString(R.string.la_app_requiere_una_conexi_n_de_internet_para_ver_la_informaci_n_e_imagenes_actualizadas))
            .setPositiveButton(getString(R.string.cerrar)) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun estaConectado(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            @Suppress("DEPRECATION")
            val activeNetwork = connectivityManager.activeNetworkInfo
            return activeNetwork != null && activeNetwork.isConnected
        } else {
            @Suppress("DEPRECATION")
            val activeNetwork = connectivityManager.activeNetworkInfo
            return activeNetwork?.isConnected == true
        }
    }

    private fun cargarTours() {
        lifecycleScope.launch {
            val toursBase = withContext(Dispatchers.IO) {
                var tours: List<Tour> = emptyList()
                try {
                    tours = RetrofitClient.apiService.getTours()
                    if (tours.isNotEmpty()) {
                        dbHelper.saveCacheTours(tours)
                    }
                } catch (e: Exception) {
                    Log.e("API Error", "Error al cargar de API, intentando cache local: ${e.message}")
                }

                if (tours.isEmpty()) {
                    tours = dbHelper.getCacheTours()
                }

                if (tours.isEmpty()) {
                    tours = cargarToursDesdeRaw()
                }

                tours
            }

            val serviciosDirectos = listOf(
                Tour(
                    id = "directo_transporte",
                    nombre = getString(R.string.servicio_de_transporte),
                    imagenPrincipal = "https://appinitdev.com/oaxaca/otros/directo_transporte.png",
                    imagenes = arrayListOf(), lugares = arrayListOf(),
                    descripcion = "Renta de transporte privado y traslados directos.",
                    costoColectivo = "", costoPrivado = "",
                    flayer = "https://appinitdev.com/oaxaca/otros/directo_transporte.png"
                ),
                Tour(
                    id = "directo_privado",
                    nombre = getString(R.string.tours_privados),
                    imagenPrincipal = "https://appinitdev.com/oaxaca/otros/directo_privado.png",
                    imagenes = arrayListOf(), lugares = arrayListOf(),
                    descripcion = "Un guía y transporte exclusivo solo para ti y tus acompañantes.",
                    costoColectivo = "", costoPrivado = "",
                    flayer = "https://appinitdev.com/oaxaca/otros/directo_privado.png"
                ),
                Tour(
                    id = "directo_personalizado",
                    nombre = getString(R.string.tour_personalizado),
                    imagenPrincipal = "https://appinitdev.com/oaxaca/otros/directo_personalizado.png",
                    imagenes = arrayListOf(), lugares = arrayListOf(),
                    descripcion = "Diseña tu propio itinerario por Oaxaca a tu medida.",
                    costoColectivo = "", costoPrivado = "",
                    flayer = "https://appinitdev.com/oaxaca/otros/directo_personalizado.png"
                ),
                Tour(
                    id = "directo_grupos",
                    nombre = getString(R.string.grupo_de_personas),
                    imagenPrincipal = "https://appinitdev.com/oaxaca/otros/directo_grupos.png",
                    imagenes = arrayListOf(), lugares = arrayListOf(),
                    descripcion = "Planes, camiones y logística especial para grupos grandes o eventos.",
                    costoColectivo = "", costoPrivado = "",
                    flayer = "https://appinitdev.com/oaxaca/otros/directo_grupos.png"
                ),
                Tour(
                    id = "directo_colaboraciones",
                    nombre = getString(R.string.colaboraciones),
                    imagenPrincipal = "https://appinitdev.com/oaxaca/otros/directo_colaboraciones.png",
                    imagenes = arrayListOf(), lugares = arrayListOf(),
                    descripcion = "Trabaja con nosotros, agencias de viajes, hoteles o creadores de contenido.",
                    costoColectivo = "", costoPrivado = "",
                    flayer = "https://appinitdev.com/oaxaca/otros/directo_colaboraciones.png"
                )
            )

            val listaCompleta = toursBase.toMutableList()
            listaCompleta.addAll(serviciosDirectos)
            listaOriginal = listaCompleta
            listaFiltrada = listaCompleta.toMutableList()

            tourAdapter = TourAdapter(listaFiltrada) { tour ->
                if (tour.id.startsWith("directo_")) {
                    val intent = Intent(requireContext(), CotizacionActivity::class.java)
                    intent.putExtra("tour", tour.getNombreActual())
                    startActivity(intent)
                } else {
                    val intent = Intent(requireContext(), DetalleTourActivity::class.java)
                    intent.putExtra("tour", tour)
                    startActivity(intent)
                }
            }

            recyclerView.adapter = tourAdapter

            // Sincronización y descarga en segundo plano para uso Offline
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    com.appinitdev.turismoOax.network.OfflineImageManager.syncImages(requireContext().applicationContext, listaCompleta)
                } catch (e: Exception) {
                    Log.e("OfflineImages", "Error en la sincronización de imágenes: ${e.message}")
                }
            }
        }
    }

    private fun cargarToursDesdeRaw(): List<Tour> {
        return try {
            val inputStream = resources.openRawResource(R.raw.tours)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<Tour>>() {}.type
            Gson().fromJson(jsonString, listType) ?: emptyList()
        } catch (e: Exception) {
            Log.e("DashboardFragment", "Error al leer res/raw/tours.json", e)
            emptyList()
        }
    }
}
