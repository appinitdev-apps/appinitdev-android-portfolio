

class PhotoEditorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🌟 SOPORTE DOBLE: Lee tu extra interno O el dato nativo de "Editar con" del sistema
        val imageUri: Uri? = if (intent.hasExtra("EXTRA_IMAGE_URI")) {
            intent.getStringExtra("EXTRA_IMAGE_URI")?.let { Uri.parse(it) }
        } else {
            intent.data // Aquí cae la foto cuando viene de la galería externa u otra app
        }

        // Si no se detectó ninguna URI por ninguna vía, cerramos de forma segura
        if (imageUri == null) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {

                    // Invocamos tu pantalla de la librería
                    PhotoEditorScreen(
                        imageUri = imageUri,
                        onBackClicked = {
                            setResult(Activity.RESULT_CANCELED)
                            finish()
                        },
                        onSavedSuccessfully = { newUri ->
                            // Si se abrió de forma externa, devolvemos el resultado de la misma manera
                            val resultIntent = Intent().apply {
                                data = newUri // Formato nativo
                                putExtra("EXTRA_EDITED_IMAGE_URI", newUri.toString()) // Tu formato interno
                            }
                            setResult(Activity.RESULT_OK, resultIntent)
                            finish()
                        }
                    )

                }
            }
        }
    }
}