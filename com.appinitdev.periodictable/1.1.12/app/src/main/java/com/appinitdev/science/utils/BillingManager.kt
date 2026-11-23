
class BillingManager(
    private val activity: Activity,
    private val scope: CoroutineScope,
    private val listener: Listener
) : BillingClientStateListener {

    interface Listener {
        fun onProductsUpdated()
        fun onPurchasesUpdated()
        fun onPurchaseCompleted(productId: String)
        fun onError(message: String)
    }

    companion object {
        const val PRO_VERSION_ID = "pro_version"
        const val PRO_PLUS_VERSION_ID = "pro_version_plus"
        const val PRO_PLUS_UPGRADE_ID = "pro_version_plus_upgrade"
    }

    private val productIds = listOf(PRO_VERSION_ID, PRO_PLUS_VERSION_ID, PRO_PLUS_UPGRADE_ID)

    private var billingClient: BillingClient? = null
    private val productDetailMap = mutableMapOf<String, ProductDetails>()

    @Volatile
    private var ownsProVersion = false

    @Volatile
    private var ownsProPlusVersion = false

    fun initialize() {
        val pendingPurchasesParams = PendingPurchasesParams.newBuilder()
            .enableOneTimeProducts()
            .build()

        billingClient = BillingClient.newBuilder(activity)
            .enablePendingPurchases(pendingPurchasesParams)
            .setListener(purchasesUpdatedListener)
            .build()

        billingClient?.startConnection(this)
    }

    fun endConnection() {
        billingClient?.endConnection()
    }

    
    fun getProductDetail(productId: String): ProductDetails? = productDetailMap[productId]
    fun isOwnsProVersion(): Boolean = ownsProVersion
    fun isOwnsProPlusVersion(): Boolean = ownsProPlusVersion
    fun isReady(): Boolean = billingClient?.isReady == true

    
    override fun onBillingServiceDisconnected() {
        
        billingClient?.startConnection(this)
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            
            queryProducts()
            queryPurchases()
        } else {
            listener.onError("Billing setup failed: ${billingResult.responseCode}")
        }
    }

    
    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            if (purchases?.isNotEmpty() == true) {
                for (purchase in purchases) {
                    
                    scope.launch {
                        handlePurchase(purchase)
                    }
                }
            }
        } else {
            
            listener.onError("Purchase update: ${billingResult.responseCode}")
        }
    }

    
    private fun queryProducts() {
        scope.launch {
            queryProductsAsync(BillingClient.ProductType.INAPP, productIds)
        }
    }

    private suspend fun queryProductsAsync(productType: String, products: List<String>) {
        val productListForQuery = products.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(productType)
                .build()
        }
        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(productListForQuery)
            .build()

        val productDetailsResult = withContext(Dispatchers.IO) {
            billingClient?.queryProductDetails(queryProductDetailsParams)
        }

        if (productDetailsResult?.billingResult?.responseCode == BillingClient.BillingResponseCode.OK) {
            productDetailsResult.productDetailsList?.forEach { productDetails ->
                productDetailMap[productDetails.productId] = productDetails
            }
            Handler(Looper.getMainLooper()).post {
                listener.onProductsUpdated()
            }
        } else {
            listener.onError("Query products failed: ${productDetailsResult?.billingResult?.responseCode}")
        }
    }

    fun queryPurchases() {
        queryPurchases(BillingClient.ProductType.INAPP)
    }

    private fun queryPurchases(type: String) {
        val queryPurchaseParams = QueryPurchasesParams.newBuilder()
            .setProductType(type)
            .build()

        billingClient?.queryPurchasesAsync(queryPurchaseParams) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                ownsProVersion = false
                ownsProPlusVersion = false
                if (purchasesList.isNotEmpty()) {
                    for (purchase in purchasesList) {
                        try {
                            val jsonObject = JSONObject(purchase.originalJson)
                            val productId = jsonObject.getString("productId")
                            if (productId == PRO_VERSION_ID) ownsProVersion = true
                            if (productId == PRO_PLUS_VERSION_ID || productId == PRO_PLUS_UPGRADE_ID) ownsProPlusVersion = true
                        } catch (e: Exception) {
                            
                        }
                    }
                }
                Handler(Looper.getMainLooper()).post {
                    listener.onPurchasesUpdated()
                }
            } else {
                listener.onError("Query purchases failed: ${billingResult.responseCode}")
            }
        }
    }

    
    private suspend fun handlePurchase(purchase: Purchase) {
        val productId = try {
            val jsonObject = JSONObject(purchase.originalJson)
            jsonObject.getString("productId")
        } catch (e: Exception) {
            ""
        }

        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

            withContext(Dispatchers.IO) {
                billingClient?.acknowledgePurchase(params) { result ->
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        
                        Handler(Looper.getMainLooper()).post {
                            updateOwnershipFlags(productId)
                            listener.onPurchaseCompleted(productId)
                        }
                    } else {
                        listener.onError("Acknowledge failed: ${result.responseCode}")
                    }
                }
            }
        } else {
            
            Handler(Looper.getMainLooper()).post {
                updateOwnershipFlags(productId)
                listener.onPurchaseCompleted(productId)
            }
        }
    }

    private fun updateOwnershipFlags(productId: String) {
        when (productId) {
            PRO_VERSION_ID -> {
                ownsProVersion = true
                ownsProPlusVersion = false
            }
            PRO_PLUS_VERSION_ID, PRO_PLUS_UPGRADE_ID -> {
                ownsProPlusVersion = true
                ownsProVersion = true
            }
        }
    }

    
    fun launchPurchase(productDetails: ProductDetails) {
        if (billingClient?.isReady != true) {
            listener.onError("Billing client not ready")
            return
        }

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                )
            )
            .build()

        billingClient?.launchBillingFlow(activity, billingFlowParams)
    }
}