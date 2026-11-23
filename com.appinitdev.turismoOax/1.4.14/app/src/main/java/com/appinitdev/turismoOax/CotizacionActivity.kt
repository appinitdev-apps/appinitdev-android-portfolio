fun interface EmailCallback {
    fun onResult(success: Boolean)
}
class CotizacionActivity : AppCompatActivity() {
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var binding: ActivityCotizacionBinding
    private lateinit var calendar: Calendar
    private var selectedYear = 0
    private var selectedMonth = 0
    private var selectedDay = 0
    private lateinit var preferenceManager: PreferenceManager
    var tour = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCotizacionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        databaseHelper = DatabaseHelper(this)
        tour = intent.getStringExtra("tour") ?: "Sin especificar"
        binding.pack.text = tour
        preferenceManager = PreferenceManager(this)
        setupValidations()
        setupButton()
        binding.etTelefonoLada.setText("52")
    }
    private fun setupValidations() {
        binding.etNombre.filters = arrayOf<InputFilter>(InputFilter { source, _, _, _, _, _ ->
            source.toString().filter { it.isLetter() || it.isWhitespace() }
        })
        binding.etFecha.setOnClickListener {
            setupDatePicker()
        }
        binding.etTelefono.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val pattern = Pattern.compile("^[0-9]{7,11}\$")
                if (!pattern.matcher(s.toString()).matches()) {
                    binding.etTelefono.error = getString(R.string.formato_inv_lido_ej_9511231235)
                }
            }
        })
        binding.etTelefonoLada.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val pattern = Pattern.compile("^[0-9]{1,4}\$")
                if (!pattern.matcher(s.toString()).matches()) {
                    binding.etTelefonoLada.error = getString(R.string.formato_inv_lido_ej_m_xico_52)
                }
            }
        })
    }
    private fun setupButton() {
        binding.btnGenerarCotizacion.setOnClickListener {
            if (validateForm()) {
                generarCotizacion()
            }
        }
    }
    private fun setupDatePicker() {
        calendar = Calendar.getInstance().apply {
            selectedYear = get(Calendar.YEAR)
            selectedMonth = get(Calendar.MONTH)
            selectedDay = get(Calendar.DAY_OF_MONTH)
        }
        binding.etFecha.apply {
            setOnClickListener { showDatePicker() }
            keyListener = null
        }
    }
    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, day ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, day)
                }
                val minDate = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
                if (selectedDate.before(minDate)) {
                    binding.tilFecha.error =
                        getString(R.string.la_fecha_debe_ser_a_partir_de_ma_ana)
                } else {
                    binding.tilFecha.error = null
                    selectedYear = year
                    selectedMonth = month
                    selectedDay = day
                    updateDateInView()
                }
            },
            selectedYear,
            selectedMonth,
            selectedDay
        )
        datePickerDialog.datePicker.minDate = Calendar.getInstance().apply {
            val now = Calendar.getInstance()
            if (now.get(Calendar.HOUR_OF_DAY) < 20) {
                add(Calendar.DAY_OF_MONTH, 1)
            } else {
                add(Calendar.DAY_OF_MONTH, 2)
            }
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        datePickerDialog.show()
    }
    private fun updateDateInView() {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.etFecha.setText(sdf.format(Calendar.getInstance().apply {
            set(selectedYear, selectedMonth, selectedDay)
        }.time))
    }
    private fun validateForm(): Boolean {
        var isValid = true
        val selectedDate = Calendar.getInstance().apply {
            set(selectedYear, selectedMonth, selectedDay)
        }
        val minDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }
        if (selectedDate.before(minDate)) {
            binding.tilFecha.error = getString(R.string.seleccione_una_fecha_v_lida)
            isValid = false
        }
        if (binding.etNombre.text.toString().trim().isEmpty()) {
            binding.etNombre.error = getString(R.string.campo_obligatorio)
            isValid = false
        }
        val personas = binding.etPersonas.text.toString().toIntOrNull() ?: 0
        if (personas !in 1..200) {
            binding.etPersonas.error = getString(R.string.debe_ser_entre_1_y_200)
            isValid = false
        }
        if (binding.etTelefono.text.toString().trim().isEmpty()) {
            binding.etTelefono.error = getString(R.string.campo_obligatorio)
            isValid = false
        }
        val email = binding.etEmail.text.toString().trim()
        if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = getString(R.string.formato_inv_lido)
            isValid = false
        }
        if (binding.etTelefonoLada.text.toString().trim().isEmpty()) {
            binding.etTelefonoLada.setText("52")
            isValid = false
        }
        if (binding.rgTipoTour.checkedRadioButtonId == -1) {
            Toast.makeText(this,
                getString(R.string.seleccione_el_tipo_de_servicio), Toast.LENGTH_SHORT).show()
            isValid = false
        }
        return isValid
    }
    fun generarTicketID(): String {
        val tiempoCorto = (System.currentTimeMillis() % 10000).toInt().toString(36).uppercase()
        val aleatorio = (100..999).random().toString(36).uppercase()
        return "C-" + tiempoCorto + aleatorio
    }
    private fun enviarEmailCotizacion(cotizacion: String, id: String, cont: String) {
        val sender = GmailSender()
        val asunto = "[$id] Cotización de tour: $tour "
        val destinatario = "cruzyoloxochitl@gmail.com"
        binding.btnGenerarCotizacion.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE
        sender.enviarCorreo(
            destinatario = destinatario,
            asunto = asunto,
            cuerpo = cotizacion
        ) { exito ->
            runOnUiThread {
                binding.progressBar.visibility = View.GONE
                binding.btnGenerarCotizacion.isEnabled = true
                if (exito) {
                    databaseHelper.insertNotificaciones(asunto, cont)
                    binding.cvResultado.visibility = View.VISIBLE
                    binding.btnGenerarCotizacion.visibility = View.GONE
                    MaterialAlertDialogBuilder(this)
                        .setTitle(getString(R.string.solicitud_enviada_exito_titulo))
                        .setMessage(getString(R.string.solicitud_enviada_exito_mensaje, id))
                        .setPositiveButton("Entendido") { dialog, _ ->
                            dialog.dismiss()
                            finish()
                        }
                        .setCancelable(false)
                        .show()
                } else {
                    binding.cvResultado.visibility = View.VISIBLE
                    binding.tvResultado.text = getString(R.string.error_al_enviar_la_cotizaci_n_intente_nuevamente)
                    Toast.makeText(this, getString(R.string.error_al_enviar_email), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun generarCotizacion() {
        val tipoTour = if (binding.rbPrivado.isChecked) "Privado" else "Colectivo"
        val email = binding.etEmail.text.toString().takeIf { it.isNotBlank() } ?: "No proporcionado"
        val coment = binding.etComment.text.toString().takeIf { it.isNotBlank() } ?: " Ninguno"
        val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            .format(Calendar.getInstance().apply {
                set(selectedYear, selectedMonth, selectedDay)
            }.time)
        val id = generarTicketID()
        val cotizacion = """
            |** Cotización generada **
            |
            |Tour:  $tour
            |Nombre:  ${binding.etNombre.text}
            |Numero de Personas:  ${binding.etPersonas.text}
            |Teléfono:  +(${binding.etTelefonoLada.text}) ${binding.etTelefono.text}
            |Email:  $email
            |Tipo:  $tipoTour
            |Comentario:  $coment
            |Fecha: $fecha
            |Tiket:  [$id]
            |Membresia: ${if (preferenceManager.esMembresiaComprada()) "Si" else "No"}
        """.trimMargin()
        val cont = "Tipo:$tipoTour \nPersonas: ${binding.etPersonas.text} \nFecha: $fecha\n Tel: +(${binding.etTelefonoLada.text}) ${binding.etTelefono.text}"
        enviarEmailCotizacion(cotizacion, id, cont)
    }
}
