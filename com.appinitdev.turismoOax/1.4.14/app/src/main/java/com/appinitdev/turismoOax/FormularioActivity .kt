
class FormularioActivity : AppCompatActivity() {
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var binding: ActivityFormularioBinding
    private lateinit var preferenceManager: PreferenceManager
    private val motivos by lazy {
        listOf(
            getString(com.appinitdev.turismoOax.R.string.informaci_n_general),
            getString(com.appinitdev.turismoOax.R.string.cotizar_servicio),
            getString(com.appinitdev.turismoOax.R.string.paquete_tur_stico),
            getString(com.appinitdev.turismoOax.R.string.transportaci_n),
            getString(com.appinitdev.turismoOax.R.string.colaboraciones),
            getString(com.appinitdev.turismoOax.R.string.duda),
            getString(com.appinitdev.turismoOax.R.string.reservaci_n),
            getString(com.appinitdev.turismoOax.R.string.otro)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormularioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(this)
        databaseHelper = DatabaseHelper(this)
        binding.etTelefonoLada.setText("52")
        setupSpinner()
        setupButton()
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(
            this,
            R.layout.simple_dropdown_item_1line,
            motivos
        )

        (binding.actvMotivo as? AutoCompleteTextView)?.apply {
            setAdapter(adapter)
            setOnItemClickListener { _, _, position, _ ->
                
                if (position == -1) {
                    binding.actvMotivo.error =
                        context.getString(com.appinitdev.turismoOax.R.string.seleccione_un_motivo)
                } else {
                    binding.actvMotivo.error = null
                }
            }
        }
    }

    private fun setupButton() {
        binding.btnEnviar.setOnClickListener {
            if (validarFormulario()) {
                mostrarConfirmacion()
            }
        }
    }

    private fun validarFormulario(): Boolean {
        var valido = true

        
        if (binding.etNombre.text.toString().trim().length < 3) {
            binding.etNombre.error =
                getString(com.appinitdev.turismoOax.R.string.m_nimo_3_caracteres)
            valido = false
        }

        
        val telefono = binding.etTelefono.text.toString().trim()
        if (telefono.isEmpty()) {
            binding.etTelefono.error = getString(com.appinitdev.turismoOax.R.string.campo_requerido)
            valido = false
        } else if (!Pattern.matches("^[0-9]{7,11}\$", telefono)) {
            binding.etTelefono.error = getString(com.appinitdev.turismoOax.R.string.formato_inv_lido_ej_9511231235)
            valido = false
        }

        
        val telefonoLada = binding.etTelefonoLada.text.toString().trim()
        if (telefonoLada.isEmpty()) {
            binding.etTelefonoLada.error = getString(com.appinitdev.turismoOax.R.string.campo_requerido)
            valido = false
        } else if (!Pattern.matches("^[0-9]{1,4}\$", telefonoLada)) {
            binding.etTelefonoLada.error = getString(com.appinitdev.turismoOax.R.string.formato_inv_lido_ej_m_xico_52)
            valido = false
        }

        
        val email = binding.etEmail.text.toString().trim()
        if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = getString(com.appinitdev.turismoOax.R.string.formato_inv_lido)
            valido = false
        }

        
        if (binding.actvMotivo.text.toString().trim().isEmpty()) {
            binding.actvMotivo.error = getString(com.appinitdev.turismoOax.R.string.seleccione_un_motivo)
            valido = false
        }

        
        if (binding.etDescripcion.text.toString().trim().length < 10) {
            binding.etDescripcion.error =
                getString(com.appinitdev.turismoOax.R.string.m_nimo_10_caracteres)
            valido = false
        }

        return valido
    }

    private fun mostrarConfirmacion() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(com.appinitdev.turismoOax.R.string.confirmar_env_o))
            .setMessage(getString(com.appinitdev.turismoOax.R.string.desea_enviar_el_formulario))
            .setPositiveButton(getString(com.appinitdev.turismoOax.R.string.enviar)) { _, _ -> enviarFormulario() }
            .setNegativeButton(getString(com.appinitdev.turismoOax.R.string.cancelar), null)
            .show()
    }

    fun generarTicketID(): String {
        val tiempoCorto = (System.currentTimeMillis() % 10000).toInt().toString(36).uppercase()
        val aleatorio = (100..999).random().toString(36).uppercase()
        return "R-"+tiempoCorto + aleatorio
    }

    private fun enviarFormulario() {
        val id = generarTicketID()
        val datos = """
            Nombre: ${binding.etNombre.text}
            Teléfono: +(${binding.etTelefonoLada.text}) ${binding.etTelefono.text}
            Email: ${binding.etEmail.text}
            Motivo: ${binding.actvMotivo.text}
            Ticket: $id
            Menbresia: ${if (preferenceManager.esMembresiaComprada()) "Si" else "No"}
            Descripción: ${binding.etDescripcion.text}
        """.trimIndent()
        val desc = "Tel: +(${binding.etTelefonoLada.text}) ${binding.etTelefono.text}\nContent: ${binding.etDescripcion.text}"

        enviarEmailCotizacion(datos, binding.actvMotivo.text.toString(),
            desc,
            id
        )
    }

    private fun enviarEmailCotizacion(cotizacion: String, motivo:String, desc:String, id: String) {
        val sender = GmailSender()
        val asunto = "[$id]Pregunta o duda: $motivo "
        val destinatario = "cruzyoloxochitl@gmail.com"
        binding.btnEnviar.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        sender.enviarCorreo(
            destinatario = destinatario,
            asunto = asunto,
            cuerpo = cotizacion
        ) { exito ->
            runOnUiThread {
                binding.progressBar.visibility = View.GONE
                binding.btnEnviar.isEnabled = true

                if (exito) {
                    databaseHelper.insertNotificaciones(asunto, desc + " Ticket: " + id)
                    binding.cvResultado.visibility = View.VISIBLE
                    binding.btnEnviar.visibility = View.GONE

                    MaterialAlertDialogBuilder(this)
                        .setTitle(getString(com.appinitdev.turismoOax.R.string.solicitud_enviada_exito_titulo))
                        .setMessage(getString(com.appinitdev.turismoOax.R.string.solicitud_enviada_exito_mensaje, id))
                        .setPositiveButton("Entendido") { dialog, _ ->
                            dialog.dismiss()
                            finish()
                        }
                        .setCancelable(false)
                        .show()
                } else {
                    binding.cvResultado.visibility = View.VISIBLE
                    binding.tvResultado.text = getString(com.appinitdev.turismoOax.R.string.error_al_enviar_la_cotizaci_n_intente_nuevamente)
                    Toast.makeText(this, getString(com.appinitdev.turismoOax.R.string.error_al_enviar_email), Toast.LENGTH_SHORT).show()
                }
            }
        }

    }


}
