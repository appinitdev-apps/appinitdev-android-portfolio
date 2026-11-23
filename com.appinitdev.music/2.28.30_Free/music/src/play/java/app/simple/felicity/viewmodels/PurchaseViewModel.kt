@HiltViewModel
class PurchaseViewModel @Inject constructor(
        application: Application
) : WrappedViewModel(application) {
    private val productId = "full_version"
    private val offers = listOf("early-access", "sale-discount")
    sealed class BillingState {
        data object Connecting : BillingState()
        data class Ready(
                val productDetails: ProductDetails,
                val basePrice: String,
                val offerPrice: String?,
                val offerToken: String?
        ) : BillingState()
        data object AlreadyPurchased : BillingState()
        data class Error(val message: String) : BillingState()
        data object Unavailable : BillingState()
    }
    private val _billingState = MutableLiveData<BillingState>(BillingState.Connecting)
    val billingState: LiveData<BillingState> = _billingState
    private val _purchaseSuccess = MutableLiveData<Boolean>()
    val purchaseSuccess: LiveData<Boolean> = _purchaseSuccess
    private var billingClient: BillingClient? = null
    private var productDetails: ProductDetails? = null
    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK if !purchases.isNullOrEmpty() -> {
                purchases.forEach { handlePurchase(it) }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "User canceled the purchase flow — maybe next time!")
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                restoreExistingPurchases()
            }
            else -> {
                Log.w(TAG, "Billing update failed: ${billingResult.debugMessage}")
                _billingState.postValue(BillingState.Error(billingResult.debugMessage))
            }
        }
    }
    init {
        connectToBillingService()
    }
    private fun connectToBillingService() {
        _billingState.postValue(BillingState.Connecting)
        billingClient = BillingClient.newBuilder(applicationContext())
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases(
                    PendingPurchasesParams.newBuilder()
                        .enableOneTimeProducts()
                        .build()
            )
            .build()
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing service connected — let's go shopping!")
                    queryProductDetails()
                    restoreExistingPurchases()
                } else {
                    Log.w(TAG, "Billing setup failed: ${billingResult.debugMessage}")
                    _billingState.postValue(BillingState.Error(billingResult.debugMessage))
                }
            }
            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected — will try again on next action.")
            }
        })
    }
    private fun queryProductDetails() {
        val productList = listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
        )
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()
        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val details: ProductDetails? = productDetailsList.productDetailsList.firstOrNull()
                if (details != null) {
                    productDetails = details
                    val basePrice = details.oneTimePurchaseOfferDetails?.formattedPrice ?: ""
                    val bestOffer = details.oneTimePurchaseOfferDetailsList
                        ?.filter { it.offerId in offers }
                        ?.minByOrNull { it.priceAmountMicros }
                    val offerPrice = bestOffer?.formattedPrice
                    val offerToken = bestOffer?.offerToken
                    if (offerPrice != null) {
                        Log.d(TAG, "Active offer found: '$offerPrice' (offer: ${bestOffer.offerId})")
                    } else {
                        Log.d(TAG, "No recognized offer active — showing base price.")
                    }
                    if (_billingState.value !is BillingState.AlreadyPurchased) {
                        _billingState.postValue(BillingState.Ready(details, basePrice, offerPrice, offerToken))
                    }
                } else {
                    Log.w(TAG, "No product details found for '$productId'. Check Play Console.")
                    _billingState.postValue(BillingState.Error("Product not found. Please check back later."))
                }
            } else {
                Log.w(TAG, "Product details query failed: ${billingResult.debugMessage}")
                _billingState.postValue(BillingState.Error(billingResult.debugMessage))
            }
        }
    }
    private val _restoreFailed = MutableLiveData<Boolean>()
    val restoreFailed: LiveData<Boolean> = _restoreFailed
    fun restorePurchase() {
        val client = billingClient
        if (client == null || !client.isReady) {
            connectToBillingService()
            return
        }
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        client.queryPurchasesAsync(params) { _, purchases ->
            val ownedPurchase = purchases.firstOrNull { it.products.contains(productId) }
            if (ownedPurchase != null) {
                handlePurchase(ownedPurchase)
            } else {
                Log.w(TAG, "Restore failed — no matching purchase on this account.")
                _restoreFailed.postValue(true)
            }
        }
    }
    private fun restoreExistingPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        billingClient?.queryPurchasesAsync(params) { _, purchases ->
            val ownedPurchase = purchases.firstOrNull { purchase ->
                purchase.products.contains(productId)
            }
            if (ownedPurchase != null) {
                handlePurchase(ownedPurchase)
            }
        }
    }
    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            TrialPreferences.setFullVersion(true)
            _billingState.postValue(BillingState.AlreadyPurchased)
            _purchaseSuccess.postValue(true)
            if (!purchase.isAcknowledged) {
                val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient?.acknowledgePurchase(acknowledgeParams) { ackResult ->
                    if (ackResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d(TAG, "Purchase acknowledged — the user is officially a full version owner!")
                    } else {
                        Log.w(TAG, "Acknowledgement failed: ${ackResult.debugMessage}")
                    }
                }
            }
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            Log.d(TAG, "Purchase is pending — waiting for payment to clear.")
        }
    }
    fun launchPurchaseFlow(activity: Activity) {
        val details = productDetails
        if (details == null) {
            Log.w(TAG, "Cannot launch purchase flow — product details not loaded yet.")
            connectToBillingService()
            return
        }
        val client = billingClient
        if (client == null || !client.isReady) {
            Log.w(TAG, "Billing client not ready — reconnecting.")
            connectToBillingService()
            return
        }
        val productDetailsParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
        val readyState = _billingState.value as? BillingState.Ready
        val offerToken = readyState?.offerToken
            ?: details.oneTimePurchaseOfferDetailsList
                ?.filter { it.offerId in offers }
                ?.minByOrNull { it.priceAmountMicros }
                ?.offerToken
        if (!offerToken.isNullOrEmpty()) {
            Log.d(TAG, "Attaching offer token to purchase flow.")
            productDetailsParamsBuilder.setOfferToken(offerToken)
        }
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParamsBuilder.build()))
            .build()
        val billingResult = client.launchBillingFlow(activity, billingFlowParams)
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            Log.w(TAG, "Launch billing flow failed: ${billingResult.debugMessage}")
            _billingState.postValue(BillingState.Error(billingResult.debugMessage))
        }
    }
    override fun onCleared() {
        super.onCleared()
        billingClient?.endConnection()
        billingClient = null
    }
    companion object {
        private const val TAG = "PurchaseViewModel"
    }
}
