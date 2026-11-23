
@AndroidEntryPoint
class AnalyticsFragment : Fragment() {
    private val viewModel: AnalyticsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ZiPOSTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AnalyticsScreenCompose(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
