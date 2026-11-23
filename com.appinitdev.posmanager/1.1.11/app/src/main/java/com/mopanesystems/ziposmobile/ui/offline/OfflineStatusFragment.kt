
class OfflineStatusFragment : Fragment() {
    // private val viewModel: OfflineStatusViewModel by viewModels() // Temporarily disabled - depends on disabled services

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
                        OfflineStatusScreenCompose() // Temporarily disabled ViewModel
                    }
                }
            }
        }
    }
}
