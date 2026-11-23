
@AndroidEntryPoint
class POSFragment : Fragment() {
    private val viewModel: POSViewModel by viewModels()
    private val customerViewModel: CustomerViewModel by viewModels()
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
    private lateinit var receiptService: ReceiptService

    // Barcode scanner launcher
    private val barcodeScannerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val barcode = result.data?.getStringExtra(BarcodeScannerActivity.EXTRA_BARCODE_RESULT)
            barcode?.let { handleScannedBarcode(it) }
        }
    }

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
                        POSScreenCompose(
                            viewModel = viewModel,
                            onScanBarcode = { startBarcodeScanner() },
                            onProcessPayment = { result ->
                                viewModel.processTransaction(result.paymentMethod,  paymentResultInfo = result)
                            },
                            onClearCart = { viewModel.clearCart() },
                            currencyFormatter = currencyFormatter,
                            viewModelCustomerViewModel = customerViewModel
                        )
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize receipt service
        receiptService = ReceiptService(requireContext())
    }


    private fun startBarcodeScanner() {
        val intent = Intent(requireContext(), BarcodeScannerActivity::class.java)
        barcodeScannerLauncher.launch(intent)
    }

    private fun handleScannedBarcode(barcode: String) {
        // Search for product by barcode

        viewModel.findProductByBarcode(barcode)
    }
}
