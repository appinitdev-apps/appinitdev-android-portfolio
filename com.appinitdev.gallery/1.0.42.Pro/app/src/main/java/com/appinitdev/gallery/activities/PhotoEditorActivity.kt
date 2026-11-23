

class PhotoEditorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        
        val imageUri: Uri? = if (intent.hasExtra("EXTRA_IMAGE_URI")) {
            intent.getStringExtra("EXTRA_IMAGE_URI")?.let { Uri.parse(it) }
        } else {
            intent.data 
        }

        
        if (imageUri == null) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {

                    
                    PhotoEditorScreen(
                        imageUri = imageUri,
                        onBackClicked = {
                            setResult(Activity.RESULT_CANCELED)
                            finish()
                        },
                        onSavedSuccessfully = { newUri ->
                            
                            val resultIntent = Intent().apply {
                                data = newUri 
                                putExtra("EXTRA_EDITED_IMAGE_URI", newUri.toString()) 
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