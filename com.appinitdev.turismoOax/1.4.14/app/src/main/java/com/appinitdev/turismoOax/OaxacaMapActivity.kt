class OaxacaMapActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOaxacaMapBinding
    private lateinit var dbHelper: DatabaseHelper
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOaxacaMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbHelper = DatabaseHelper(this)
        binding.wvMap.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            userAgentString = "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Mobile Safari/537.36 TurismoOaxacaApp/1.0"
        }
        binding.wvMap.webViewClient = WebViewClient()
        binding.wvMap.webChromeClient = WebChromeClient()
        binding.wvMap.addJavascriptInterface(WebAppInterface(), "Android")
        val selectedTourId = intent.getStringExtra("tour_id") ?: "2"
        val htmlContent = """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="utf-8">
                <title>Mapa Turístico de Oaxaca</title>
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
                <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
                <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                <style>
                    html, body, #map {
                        width: 100vw;
                        height: 100vh;
                        margin: 0;
                        padding: 0;
                        background-color: #f4f3f0;
                    }
                    .btn-tour {
                        background-color: #1976D2;
                        color: white;
                        border: none;
                        padding: 8px 12px;
                        border-radius: 8px;
                        font-weight: bold;
                        cursor: pointer;
                        margin-top: 8px;
                        width: 100%;
                        font-size: 13px;
                        box-shadow: 0 2px 4px rgba(0,0,0,0.2);
                    }
                    .btn-tour:active {
                        background-color: #1565C0;
                    }
                    .leaflet-popup-content-wrapper {
                        border-radius: 12px;
                        padding: 4px;
                        box-shadow: 0 4px 12px rgba(0,0,0,0.25);
                    }
                </style>
            </head>
            <body>
                <div id="map"></div>
                <script>
                    var map = L.map('map', {
                        center: [16.8, -96.6],
                        zoom: 8,
                        zoomControl: true,
                        attributionControl: false
                    });

                    // Capa de mapa Esri World Street Map (Rápida, limpia y sin marcas de agua)
                    L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/{z}/{y}/{x}', {
                        maxZoom: 18
                    }).addTo(map);

                    var destinations = [
                        { id: '1', name: 'Ruta del Mezcal & Hierve el Agua', lat: 16.8661, lon: -96.2758, desc: 'Hierve el Agua, Tule, Teotitlán y Mitla' },
                        { id: '2', name: 'Ruta Zapoteca', lat: 17.0439, lon: -96.7678, desc: 'Monte Albán, Alebrijes y Barro Negro' },
                        { id: '3', name: 'Ruta San José del Pacífico', lat: 16.1711, lon: -96.5022, desc: 'Senderismo, Columpio Extremo y Temazcal' },
                        { id: '4', name: 'Ruta Grutas Mágicas', lat: 16.5, lon: -96.8, desc: 'Río Subterráneo, Cenote y Cavernas' },
                        { id: '5', name: 'Ruta Sierra Juárez', lat: 17.3045, lon: -96.4468, desc: 'Mirador de Cristal y Guelatao' },
                        { id: '6', name: 'Ruta Tour Playero', lat: 15.8614, lon: -97.0664, desc: 'Ballenas, Tortugas y Bioluminiscencia' },
                        { id: '7', name: 'Ruta Tour Apoala', lat: 17.6481, lon: -97.1352, desc: 'Peñas Gemelas y Cascada Azul' }
                    ];

                    var markersMap = {};

                    destinations.forEach(function(dest) {
                        var marker = L.marker([dest.lat, dest.lon]).addTo(map);
                        var popupContent = '<div style="min-width:180px;">' +
                            '<b style="font-size:14px; color:#1976D2;">' + dest.name + '</b><br>' +
                            '<span style="color:#555; font-size:12px;">' + dest.desc + '</span><br>' +
                            '<button class="btn-tour" onclick="openTour(\'' + dest.id + '\')">Ver Tour</button>' +
                            '</div>';
                        marker.bindPopup(popupContent);
                        markersMap[dest.id] = marker;
                    });

                    setTimeout(function() {
                        var targetMarker = markersMap['$selectedTourId'] || markersMap['2'] || markersMap['1'];
                        if (targetMarker) {
                            targetMarker.openPopup();
                            map.panTo(targetMarker.getLatLng());
                        }
                        map.invalidateSize();
                    }, 500);

                    function openTour(id) {
                        if (window.Android && window.Android.openTourDetails) {
                            window.Android.openTourDetails(id);
                        }
                    }
                </script>
            </body>
            </html>
        """.trimIndent()
        binding.wvMap.loadDataWithBaseURL("https://unpkg.com/", htmlContent, "text/html", "UTF-8", null)
        binding.btnOpenExternalMap.setOnClickListener {
            val gmmIntentUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=Oaxaca+Turismo+Monte+Alban+Hierve+el+Agua")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            } else {
                startActivity(Intent(Intent.ACTION_VIEW, gmmIntentUri))
            }
        }
    }
    inner class WebAppInterface {
        @JavascriptInterface
        fun openTourDetails(tourId: String) {
            runOnUiThread {
                val tours = dbHelper.getCacheTours()
                val tourEncontrado = tours.find { it.id == tourId }
                if (tourEncontrado != null) {
                    val intent = Intent(this@OaxacaMapActivity, DetalleTourActivity::class.java)
                    intent.putExtra("tour", tourEncontrado)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@OaxacaMapActivity, getString(R.string.msg_abriendo_detalles_tour), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
