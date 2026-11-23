@AndroidEntryPoint
class ProductsFragment : Fragment() {
    private val viewModel: ProductViewModel by viewModels()
    private val viewModelPOS: POSViewModel by viewModels()
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
                        ProductsScreenCompose(viewModel = viewModel, viewModelPOS = viewModelPOS)
                    }
                }
            }
        }
    }
}
