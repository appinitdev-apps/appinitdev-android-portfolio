class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var billingManager: BillingManager
    private var productDetails: ProductDetails? = null
    private val PRODUCT_ID = "membresiad"
    private val binding get() = _binding!!
    private lateinit var databaseHelper: DatabaseHelper
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        ViewModelProvider(this).get(HomeViewModel::class.java)
        databaseHelper = DatabaseHelper(requireContext())
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        preferenceManager = PreferenceManager(requireContext())
        billingManager = BillingManager(requireContext()) {
            preferenceManager.guardarMembresia(true)
            databaseHelper.insertNotificaciones(
                getString(R.string.compra_exitosa),
                getString(R.string.has_adquirido_la_membres_a_membresiad_correctamente)
            )
            Toast.makeText(requireContext(),
                getString(R.string.compra_registrada), Toast.LENGTH_SHORT).show()
            binding.txtMem.visibility = View.VISIBLE
        }
        if (preferenceManager.esMembresiaComprada()) {
            binding.txtMem.visibility = View.VISIBLE
        }
        binding.btnBuy.setOnClickListener {
            billingManager.queryProductDetails(PRODUCT_ID) { products ->
                if (products.isNotEmpty()) {
                    productDetails = products.first()
                }
            }
            mostrarDialogoLista()
        }
        binding.btnMapGeneral.setOnClickListener {
            startActivity(Intent(requireContext(), com.appinitdev.turismoOax.OaxacaMapActivity::class.java))
        }
        binding.btnChecklist.setOnClickListener {
            startActivity(Intent(requireContext(), com.appinitdev.turismoOax.PackingChecklistActivity::class.java))
        }
        binding.btnPostcard.setOnClickListener {
            startActivity(Intent(requireContext(), com.appinitdev.turismoOax.PostcardActivity::class.java))
        }
        binding.btnGastronomia.setOnClickListener {
            mostrarGuiaGastronomica()
        }
        binding.btnTips.setOnClickListener {
            mostrarTipsViajero()
        }
        binding.btnEmergencias.setOnClickListener {
            mostrarDirectorioEmergencias()
        }
        binding.btnWhatsApp.setOnClickListener {
            abrirWhatsApp()
        }
        binding.btnFacebook.setOnClickListener {
            abrirFacebook()
        }
        binding.btnAsesor.setOnClickListener {
            abrirActividadAsesor()
        }
        return binding.root
    }
    private fun mostrarGuiaGastronomica() {
        val platillos = arrayOf(
            "🫓 Tlayuda Oaxaqueña\nLa icónica tortilla gigante dorada untada con asiento de cerdo, frijoles refritos, quesillo, aguacate y carne a elegir (tasajo, cecina o chorizo).",
            "🥘 Mole Negro Oaxaqueño\nEl rey de los moles elaborado con más de 30 ingredientes tradicionales como chiles secos, chocolate criollo, plátano, pasas y especias.",
            "🧀 Quesillo (Queso Oaxaca)\nQueso de hebra artesanal elaborado tradicionalmente en Etla, famoso por su textura suave y cremosidad incomparable.",
            "🥤 Tejate (\"Bebida de los Dioses\")\nBebida ceremonial prehispánica elaborada a base de maíz, cacao, hueso de mamey y la flor de cacao (rosita de cacao).",
            "🌵 Mezcal con Sal de Gusano y Naranja\nDestilado 100% artesanal de agave oaxaqueño acompañado del maridaje tradicional de rodajas de naranja dulce y sal de gusano de maguey.",
            "🦗 Chapulines al Ajillo\nInsectos comestibles tostados en comal con chile, ajo y limón, una botana crujiente y milenaria llena de proteína."
        )
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("🍲 Sabores de Oaxaca (Guía Gastronómica)")
            .setItems(platillos, null)
            .setPositiveButton("¡Se me antojó!", null)
            .show()
    }
    private fun mostrarTipsViajero() {
        val tips = """
            |💵 1. Dinero en Efectivo:
            |Lleva pesos en efectivo para comunidades como Hierve el Agua, Teotitlán y mercados tradicionales donde no aceptan tarjeta de crédito.
            |
            |🎓 2. Descuentos Oficiales:
            |Si eres mexicano, presenta tu credencial vigente de Estudiante, Maestro o INAPAM para entrar GRATIS a zonas arqueológicas como Monte Albán y Mitla.
            |
            |🧥 3. Clima y Ropa:
            |Ropa ligera y bloqueador solar para Valles Centrales y la Costa; chamarra abrigadora para la Sierra Sur (San José del Pacífico).
            |
            |👟 4. Calzado Adecuado:
            |Usa tenis con buen agarre para subir pirámides y caminar por senderos empedrados.
            |
            |🧢 5. Hidratación:
            |Lleva una botella reutilizable de agua y sombrero/gorra para protegerte del sol en los recorridos al aire libre.
        """.trimMargin()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("💡 Consejos para el Viajero (Oaxaca Travel Tips)")
            .setMessage(tips)
            .setPositiveButton("Entendido", null)
            .show()
    }
    private fun mostrarDirectorioEmergencias() {
        val opciones = arrayOf(
            "🚨 911 - Emergencias Generales",
            "🛠️ 078 - Ángeles Verdes (Auxilio Vial Turístico)",
            "👮‍♂️ +52 951 516 0123 - Policía Turística de Oaxaca",
            "ℹ️ +52 951 514 2124 - Módulo Información Turística (SECTUR)",
            "📲 +52 951 286 3746 - Atención Agencia de Viajes"
        )
        val telefonos = arrayOf("911", "078", "+529515160123", "+529515142124", "+529512863746")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("🚨 Directorio de Emergencias y Asistencia")
            .setItems(opciones) { _, which ->
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${telefonos[which]}"))
                startActivity(intent)
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }
    private fun mostrarDialogoLista() {
        val mensaje =
            getString(R.string.descuentos_de_hasta_el_10_comunicaci_n_constante_asesor_a_personalizada_ofertas_exclusivas_asignaci_n_de_asesor_asistente).trimIndent()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.beneficios))
            .setMessage(mensaje)
            .setPositiveButton(getString(R.string.comprar)) { dialog, _ ->
                if (productDetails != null) {
                    if (isInternetAvailable(requireContext())) {
                        billingManager.launchPurchaseFlow(requireActivity(), productDetails!!)
                    } else {
                        Toast.makeText(requireContext(),
                            getString(R.string.error_de_compra), Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.producto_no_disponible_intente_m_s_tarde),
                        Toast.LENGTH_LONG
                    ).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancelar)) { dialog, _ -> dialog.dismiss() }
            .show()
    }
    private fun abrirWhatsApp() {
        val phoneNumber = "+529512863746"
        val mensaje = getString(R.string.hola_estoy_interesad_en_su_servicio)
        val url = "https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(mensaje)}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.setPackage("com.whatsapp")
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(requireContext(),
                getString(R.string.whatsapp_no_est_instalado), Toast.LENGTH_SHORT)
                .show()
        }
    }
    private fun abrirFacebook() {
        val facebookUrl = "https://www.facebook.com/ViajesTuristicosOaxacaMagico"
        val facebookId = "109176774832742"
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/$facebookId"))
            startActivity(intent)
        } catch (_: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl)))
        }
    }
    private fun abrirActividadAsesor() {
        val intent = Intent(requireContext(), FormularioActivity::class.java)
        startActivity(intent)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
