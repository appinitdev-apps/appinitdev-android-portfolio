class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this) {}
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val application = context.applicationContext as Application
            val viewModel: MathViewModel = remember { MathViewModel(application) }
            val themeMode by viewModel.themeMode.collectAsState()
            MathAppTheme(themeMode = themeMode) {
                MainMathApp(viewModel = viewModel)
            }
        }
    }
}
@Composable
fun MainMathApp(
    viewModel: MathViewModel
) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    BackHandler(enabled = true) {
        val handled = viewModel.navigateBack()
        if ((!handled) && context is Activity) {
            context.finish()
        }
    }
    Crossfade(
        targetState = currentScreen,
        modifier = Modifier.fillMaxSize(),
        label = "ScreenTransition"
    ) { screen ->
        when (screen) {
            Screen.HOME -> HomeScreen(
                viewModel = viewModel,
                onNavigateToSearch = { viewModel.navigateTo(Screen.SEARCH) }
            )
            Screen.CATEGORY_DETAIL -> CategoryDetailScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateBack() }
            )
            Screen.TOPIC_DETAIL -> TopicDetailScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateBack() }
            )
            Screen.SEARCH -> SearchScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateBack() }
            )
            Screen.FAVORITES -> FavoritesScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateBack() }
            )
            Screen.FLASHCARDS -> FlashcardsScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateBack() }
            )
            Screen.GLOSSARY -> GlossaryScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateBack() }
            )
            Screen.UNIT_CONVERTER -> UnitConverterScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateBack() }
            )
            Screen.PLOTTER -> HomeScreen(
                viewModel = viewModel,
                onNavigateToSearch = { viewModel.navigateTo(Screen.SEARCH) }
            )
        }
    }
}
