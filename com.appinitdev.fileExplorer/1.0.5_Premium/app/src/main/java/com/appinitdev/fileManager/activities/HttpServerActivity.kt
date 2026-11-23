class HttpServerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HttpServerScreen(onBack = { finish() })
        }
    }
}
