class BillingManager(
    private val context: Context,
    private val onPurchaseCompleted: (Purchase) -> Unit
) : PurchasesUpdatedListener {
    private val TAG = "BillingManager"
    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()
    init {
        startConnection()
    }
    private fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing Client conectado con éxito")
                    queryPurchases()
                } else {
                    Log.e(TAG, "Error en setup: ${billingResult.debugMessage}")
                }
            }
            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Servicio desconectado, reintentando...")
                startConnection()
            }
        })
    }
    fun queryProductDetails(productId: String, onResult: (List<ProductDetails>) -> Unit) {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            )
            .build()
        billingClient.queryProductDetailsAsync(params) { billingResult, queryProductDetailsResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                onResult(queryProductDetailsResult.productDetailsList as List<ProductDetails>)
            } else {
                onResult(emptyList())
            }
        }
    }
    fun launchPurchaseFlow(activity: Activity, productDetails: ProductDetails) {
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        billingClient.launchBillingFlow(activity, billingFlowParams)
    }
    fun queryPurchases() {
        if (!billingClient.isReady) return
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases.forEach { processPurchase(it) }
            }
        }
    }
    private fun processPurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            val consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.consumeAsync(consumeParams) { billingResult, purchaseToken ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Producto consumido con éxito, ya puede volver a comprarse.")
                    onPurchaseCompleted(purchase)
                } else {
                    Log.e(TAG, "Error consumiendo producto: ${billingResult.debugMessage}")
                }
            }
        }
    }
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { processPurchase(it) }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.i(TAG, "El usuario canceló la compra")
            }
            else -> {
                Log.e(TAG, "Error en compra: ${billingResult.debugMessage}")
            }
        }
    }
}