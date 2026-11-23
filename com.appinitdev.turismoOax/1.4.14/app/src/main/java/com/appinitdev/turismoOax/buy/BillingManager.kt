
class BillingManager(
    private val context: Context,
    private val onPurchaseCompleted: (Purchase) -> Unit
) : PurchasesUpdatedListener {

    private val TAG = "BillingManager"

    // Crea el BillingClient habilitando compras pendientes
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
                    // Al conectar, revisamos si hay compras que no se confirmaron antes
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

    // Método para consultar detalles del producto
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

    // Lanza el flujo de compra
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

    // 1. REVISIÓN DE COMPRAS (Evita reembolsos si la app se cerró)
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

    // 2. PROCESO DE CONFIRMACIÓN (Acknowledge)
    // ... importaciones existentes

    private fun processPurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // En productos consumibles, NO se usa acknowledgePurchase.
            // Se usa consumeAsync para permitir que se pueda volver a comprar.

            val consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

            billingClient.consumeAsync(consumeParams) { billingResult, purchaseToken ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Producto consumido con éxito, ya puede volver a comprarse.")
                    // Aquí notificas a tu app que entregue el beneficio (ej. sumar monedas)
                    onPurchaseCompleted(purchase)
                } else {
                    Log.e(TAG, "Error consumiendo producto: ${billingResult.debugMessage}")
                }
            }
        }
    }

    // Callback de Google Play cuando cambia el estado de una compra
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