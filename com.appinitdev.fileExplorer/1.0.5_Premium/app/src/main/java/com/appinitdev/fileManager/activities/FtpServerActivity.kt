class FtpServerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FtpServerScreen(onBack = { finish() })
        }
    }
}
