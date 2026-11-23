class PackingChecklistActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPackingChecklistBinding
    private val PREFS_NAME = "packing_checklist_prefs"
    data class ChecklistCategory(
        val nameEs: String,
        val nameEn: String,
        val items: List<ChecklistItem>
    )
    data class ChecklistItem(
        val key: String,
        val textEs: String,
        val textEn: String
    )
    private val categories = listOf(
        ChecklistCategory(
            "📄 Documentos e Identificación",
            "📄 Documents & Identification",
            listOf(
                ChecklistItem("doc_ine", "Credencial oficial (INE / Pasaporte)", "Official ID / Passport"),
                ChecklistItem("doc_inapam", "Credencial Estudiante / INAPAM (Entrada gratis a ruinas)", "Student / Senior ID (Free ruin entrance)"),
                ChecklistItem("doc_ticket", "Ticket / Confirmación de reservación del tour", "Tour reservation confirmation ticket")
            )
        ),
        ChecklistCategory(
            "💵 Dinero y Pagos",
            "💵 Money & Payments",
            listOf(
                ChecklistItem("money_cash", "Dinero en efectivo (pesos MXN) para artesanías y zonas sin cajero", "Cash (MXN pesos) for crafts and cash-only spots"),
                ChecklistItem("money_card", "Tarjeta de crédito/débito", "Credit/Debit Card")
            )
        ),
        ChecklistCategory(
            "👕 Ropa y Calzado por Zona",
            "👕 Clothing & Footwear",
            listOf(
                ChecklistItem("wear_shoes", "Tenis cómodos con buen agarre (para Monte Albán y caminatas)", "Comfortable walking shoes with good grip"),
                ChecklistItem("wear_jacket", "Chamarra abrigadora (para la Sierra / San José del Pacífico)", "Warm jacket/sweater for mountain areas"),
                ChecklistItem("wear_swim", "Traje de baño y toalla (para Hierve el Agua / Costa)", "Swimsuit & towel for waterfalls/coast"),
                ChecklistItem("wear_light", "Ropa ligera y fresca de algodón", "Lightweight breathable cotton clothes")
            )
        ),
        ChecklistCategory(
            "🧴 Sol y Salud",
            "🧴 Sun & Health Protection",
            listOf(
                ChecklistItem("sun_block", "Bloqueador solar biodegradable", "Biodegradable sunscreen"),
                ChecklistItem("sun_hat", "Sombrero / Gorra y lentes de sol", "Hat / Cap & Sunglasses"),
                ChecklistItem("sun_repellent", "Repelente de mosquitos ecológico", "Eco-friendly insect repellent"),
                ChecklistItem("sun_water", "Botella de agua reutilizable", "Reusable water bottle")
            )
        ),
        ChecklistCategory(
            "📷 Tecnología y Varios",
            "📷 Tech & Essentials",
            listOf(
                ChecklistItem("tech_phone", "Celular y cargador / Power Bank", "Smartphone & Charger / Power Bank"),
                ChecklistItem("tech_audio", "Audífonos para audios de la app", "Headphones for app audio guides"),
                ChecklistItem("tech_camera", "Cámara fotográfica", "Camera")
            )
        )
    )
    private val allCheckBoxes = mutableListOf<Pair<String, CheckBox>>()
    private var customItemsContainer: LinearLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPackingChecklistBinding.inflate(layoutInflater)
        setContentView(binding.root)
        renderChecklist()
        binding.btnResetChecklist.setOnClickListener {
            resetChecklist()
        }
        binding.btnAddItem.setOnClickListener {
            addCustomItemDialog()
        }
    }
    private fun renderChecklist() {
        binding.llChecklistContainer.removeAllViews()
        allCheckBoxes.clear()
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isEn = Locale.getDefault().language.equals("en", ignoreCase = true)
        val primaryColor = ContextCompat.getColor(this, R.color.blue_500)
        for (cat in categories) {
            val cardView = MaterialCardView(this).apply {
                radius = 16f * resources.displayMetrics.density
                cardElevation = 2f * resources.displayMetrics.density
                strokeWidth = (1 * resources.displayMetrics.density).toInt()
                setStrokeColor(ColorStateList.valueOf(0x33808080.toInt()))
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, (12 * resources.displayMetrics.density).toInt())
                }
                layoutParams = params
            }
            val cardContent = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(
                    (16 * resources.displayMetrics.density).toInt(),
                    (14 * resources.displayMetrics.density).toInt(),
                    (16 * resources.displayMetrics.density).toInt(),
                    (14 * resources.displayMetrics.density).toInt()
                )
            }
            val categoryTitle = TextView(this).apply {
                text = if (isEn) cat.nameEn else cat.nameEs
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(primaryColor)
                setPadding(0, 0, 0, (8 * resources.displayMetrics.density).toInt())
            }
            cardContent.addView(categoryTitle)
            for (item in cat.items) {
                val checkBox = CheckBox(this).apply {
                    text = if (isEn) item.textEn else item.textEs
                    isChecked = prefs.getBoolean(item.key, false)
                    textSize = 14f
                    buttonTintList = ColorStateList.valueOf(primaryColor)
                    setPadding(
                        (8 * resources.displayMetrics.density).toInt(),
                        (6 * resources.displayMetrics.density).toInt(),
                        (8 * resources.displayMetrics.density).toInt(),
                        (6 * resources.displayMetrics.density).toInt()
                    )
                    setOnCheckedChangeListener { _, isChecked ->
                        prefs.edit().putBoolean(item.key, isChecked).apply()
                        updateProgress()
                    }
                }
                allCheckBoxes.add(Pair(item.key, checkBox))
                cardContent.addView(checkBox)
            }
            cardView.addView(cardContent)
            binding.llChecklistContainer.addView(cardView)
        }
        val customCardView = MaterialCardView(this).apply {
            radius = 16f * resources.displayMetrics.density
            cardElevation = 2f * resources.displayMetrics.density
            strokeWidth = (1 * resources.displayMetrics.density).toInt()
            setStrokeColor(ColorStateList.valueOf(0x33808080.toInt()))
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, (12 * resources.displayMetrics.density).toInt())
            }
            layoutParams = params
        }
        val customCardContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                (16 * resources.displayMetrics.density).toInt(),
                (14 * resources.displayMetrics.density).toInt(),
                (16 * resources.displayMetrics.density).toInt(),
                (14 * resources.displayMetrics.density).toInt()
            )
        }
        val customTitle = TextView(this).apply {
            text = if (isEn) "📌 Custom Items" else "📌 Artículos Personalizados"
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(primaryColor)
            setPadding(0, 0, 0, (8 * resources.displayMetrics.density).toInt())
        }
        customCardContent.addView(customTitle)
        customItemsContainer = customCardContent
        customCardView.addView(customCardContent)
        binding.llChecklistContainer.addView(customCardView)
        updateProgress()
    }
    private fun updateProgress() {
        val total = allCheckBoxes.size
        if (total == 0) return
        val checkedCount = allCheckBoxes.count { it.second.isChecked }
        val percent = (checkedCount * 100) / total
        binding.progressBarChecklist.progress = percent
        binding.tvProgress.text = "Progreso: $percent% preparado ($checkedCount de $total artículos)"
    }
    private fun resetChecklist() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        for (pair in allCheckBoxes) {
            pair.second.isChecked = false
        }
        updateProgress()
        Toast.makeText(this, "Lista de empaque reiniciada", Toast.LENGTH_SHORT).show()
    }
    private fun addCustomItemDialog() {
        val input = EditText(this).apply {
            hint = "Ej: Medicamento personal, Impermeable..."
            setPadding(32, 24, 32, 24)
        }
        val primaryColor = ContextCompat.getColor(this, R.color.blue_500)
        MaterialAlertDialogBuilder(this)
            .setTitle("➕ Agregar Artículo")
            .setView(input)
            .setPositiveButton("Agregar") { _, _ ->
                val newItemText = input.text.toString().trim()
                if (newItemText.isNotEmpty()) {
                    val key = "" + System.currentTimeMillis()
                    val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    prefs.edit().putBoolean(key, true).apply()
                    val checkBox = CheckBox(this).apply {
                        text = newItemText
                        isChecked = true
                        textSize = 14f
                        buttonTintList = ColorStateList.valueOf(primaryColor)
                        setPadding(
                            (8 * resources.displayMetrics.density).toInt(),
                            (6 * resources.displayMetrics.density).toInt(),
                            (8 * resources.displayMetrics.density).toInt(),
                            (6 * resources.displayMetrics.density).toInt()
                        )
                        setOnCheckedChangeListener { _, isChecked ->
                            prefs.edit().putBoolean(key, isChecked).apply()
                            updateProgress()
                        }
                    }
                    allCheckBoxes.add(Pair(key, checkBox))
                    customItemsContainer?.addView(checkBox)
                    updateProgress()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
