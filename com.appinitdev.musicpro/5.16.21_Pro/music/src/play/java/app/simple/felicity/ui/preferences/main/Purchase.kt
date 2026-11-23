@AndroidEntryPoint
class Purchase : PreferenceFragment() {
    private var _binding: FragmentPurchaseBinding? = null
    private val binding get() = _binding!!
    private lateinit var headerBinding: HeaderPreferencesGenericBinding
    private val purchaseViewModel: PurchaseViewModel by viewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPurchaseBinding.inflate(inflater, container, false)
        headerBinding = HeaderPreferencesGenericBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        headerBinding.title.text = getString(R.string.purchase)
        headerBinding.icon.setImageResource(R.drawable.ic_sell)
        binding.header.setContentView(headerBinding.root)
        refreshTrialStatus()
        observeBillingState()
        setupBuyButton()
        setupRestoreButton()
    }
    private fun refreshTrialStatus() {
        binding.trialStatus.text = when {
            TrialPreferences.isFullVersion() -> getString(R.string.already_purchased)
            TrialPreferences.isWithinTrialPeriod() -> getString(R.string.trial_period_summary, TrialPreferences.getDaysLeft())
            else -> getString(R.string.trial_expired_short)
        }
    }
    private fun observeBillingState() {
        purchaseViewModel.billingState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is PurchaseViewModel.BillingState.Connecting -> {
                    binding.buyButton.text = getString(R.string.billing_connecting)
                    binding.buyButton.isEnabled = false
                    binding.buyButton.alpha = 0.6f
                    hidePriceRow()
                }
                is PurchaseViewModel.BillingState.Ready -> {
                    if (state.offerPrice != null) {
                        showPriceRow(basePrice = state.basePrice, offerPrice = state.offerPrice)
                        binding.buyButton.text = state.offerPrice
                    } else {
                        hidePriceRow()
                        binding.buyButton.text = state.basePrice.ifEmpty { getString(R.string.purchase_now) }
                    }
                    binding.buyButton.isEnabled = true
                    binding.buyButton.alpha = 1f
                }
                is PurchaseViewModel.BillingState.AlreadyPurchased -> {
                    binding.buyButton.text = getString(R.string.already_purchased)
                    binding.buyButton.isEnabled = false
                    binding.buyButton.alpha = 0.6f
                    binding.restoreButton.isEnabled = false
                    binding.restoreButton.gone()
                    hidePriceRow()
                    refreshTrialStatus()
                }
                is PurchaseViewModel.BillingState.Error -> {
                    binding.buyButton.text = getString(R.string.billing_error, state.message)
                    binding.buyButton.isEnabled = true
                    binding.buyButton.alpha = 0.8f
                    hidePriceRow()
                }
                is PurchaseViewModel.BillingState.Unavailable -> {
                    binding.buyButton.text = getString(R.string.billing_unavailable)
                    binding.buyButton.isEnabled = false
                    binding.buyButton.alpha = 0.5f
                    hidePriceRow()
                }
            }
        }
        purchaseViewModel.purchaseSuccess.observe(viewLifecycleOwner) { success ->
            if (success == true) {
                refreshTrialStatus()
            }
        }
        purchaseViewModel.restoreFailed.observe(viewLifecycleOwner) { failed ->
            if (failed == true) {
                showRestoreFailedWarning()
            }
        }
    }
    private fun showPriceRow(basePrice: String, offerPrice: String) {
        binding.originalPrice.text = basePrice
        binding.originalPrice.paintFlags =
            binding.originalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        binding.offerPriceBadge.text = offerPrice
        binding.priceRow.visibility = View.VISIBLE
    }
    private fun hidePriceRow() {
        binding.priceRow.visibility = View.GONE
    }
    private fun setupBuyButton() {
        binding.buyButton.setOnClickListener {
            activity?.let { act ->
                purchaseViewModel.launchPurchaseFlow(act)
            }
        }
    }
    private fun setupRestoreButton() {
        binding.restoreButton.setOnClickListener {
            binding.restoreButton.isEnabled = false
            binding.restoreButton.text = getString(R.string.restore_purchase_restoring)
            purchaseViewModel.restorePurchase()
        }
        purchaseViewModel.billingState.observe(viewLifecycleOwner) {
            if (it !is PurchaseViewModel.BillingState.AlreadyPurchased) {
                binding.restoreButton.isEnabled = true
                binding.restoreButton.text = getString(R.string.restore_purchase)
            }
        }
    }
    private fun showRestoreFailedWarning() {
        context?.let { ctx ->
            AlertDialog.Builder(ctx)
                .setTitle(R.string.restore_purchase)
                .setMessage(R.string.restore_purchase_failed)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
    }
    override val wantsMiniPlayerVisible: Boolean
        get() = false
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    companion object {
        fun newInstance(): Purchase {
            val args = Bundle()
            val fragment = Purchase()
            fragment.arguments = args
            return fragment
        }
        const val TAG = "Purchase"
    }
}
