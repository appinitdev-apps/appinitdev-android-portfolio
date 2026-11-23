 
















@AndroidEntryPoint
class AppActivity : ComposeActivity() {

    @Inject
    lateinit var rootComponentFactory: RootComponent.Factory

    private val component: RootComponent by lazy {
        retainedComponent(factory = rootComponentFactory::invoke)
    }

    override fun handleIntent(intent: Intent) = component.handleDeeplinks(intent)

    @Composable
    override fun Content() = RootContent(component = component)

}