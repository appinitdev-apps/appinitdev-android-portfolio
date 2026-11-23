@AndroidEntryPoint
class CustomersFragment : Fragment() {
    private val viewModel: CustomerViewModel by viewModels()
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
                        CustomersScreenCompose(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
