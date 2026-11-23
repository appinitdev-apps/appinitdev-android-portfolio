
@HiltViewModel
class POSViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val customerRepository: CustomerRepository,
    private val transactionRepository: TransactionRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val storeRepository: StoreRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {


    private val _currentStore = MutableStateFlow<Store?>(null)
    val currentStore: StateFlow<Store?> = _currentStore.asStateFlow()


    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer.asStateFlow()

    private val _transactionStatus = MutableStateFlow(TransactionStatus.PENDING)
    val transactionStatus: StateFlow<TransactionStatus> = _transactionStatus.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _currentTransaction = MutableLiveData<Transaction?>()
    val currentTransaction: LiveData<Transaction?> = _currentTransaction

    private val _lowStockCount = MutableLiveData<Int>()
    val lowStockCount: LiveData<Int> = _lowStockCount

    private val _outOfStockCount = MutableLiveData<Int>()
    val outOfStockCount: LiveData<Int> = _outOfStockCount

    // Cart calculations
    /*val subtotal: StateFlow<BigDecimal>
        get() = MutableStateFlow(_cartItems.value.sumOf { it.totalPrice })

    val taxAmount: StateFlow<BigDecimal>
        get() = MutableStateFlow(_cartItems.value.sumOf { it.taxAmount })

    val totalAmount: StateFlow<BigDecimal>
        get() = MutableStateFlow(_cartItems.value.sumOf { it.totalPrice + it.taxAmount })

    val itemCount: StateFlow<Int>
        get() = MutableStateFlow(_cartItems.value.sumOf { it.quantity })*/


    init {
        // Restaurar carrito al iniciar
        viewModelScope.launch {
            loadCart(context).collect { items ->
             _cartItems.value = items
                recalculateTotals()
            }

        }


        viewModelScope.launch {
            storeRepository.getStore().collect { store ->
                _currentStore.value = store
                store?.let {
                    Log.d("POSStore", "Current store loaded: ${it.name}")
                } ?: Log.d("POSStore", "No store found")
            }
        }

        viewModelScope.launch {
            try {
                productRepository.getOutOfStockProductsCount().collect {count ->
                    _outOfStockCount.value = count

                }
            } catch (e: Exception) {

            } finally {

            }
        }

        viewModelScope.launch {
            try {
                productRepository.getLowStockCount().collect {count ->
                    _lowStockCount.value = count

                }
            } catch (e: Exception) {

            } finally {

            }
        }

    }

    private val _subtotal = MutableStateFlow(BigDecimal.ZERO)
    val subtotal: StateFlow<BigDecimal> = _subtotal.asStateFlow()

    private val _taxAmount = MutableStateFlow(BigDecimal.ZERO)
    val taxAmount: StateFlow<BigDecimal> = _taxAmount.asStateFlow()

    private val _totalAmount = MutableStateFlow(BigDecimal.ZERO)
    val totalAmount: StateFlow<BigDecimal> = _totalAmount.asStateFlow()

    private val _itemCount = MutableStateFlow(0)
    val itemCount: StateFlow<Int> = _itemCount.asStateFlow()


    private val _receiptPath = MutableStateFlow<String?>(null)
    val receiptPath: StateFlow<String?> = _receiptPath

    private fun recalculateTotals() {
        val items = _cartItems.value
        _subtotal.value = items.fold(BigDecimal.ZERO) { acc, item -> acc + item.totalPrice }
        _taxAmount.value = items.fold(BigDecimal.ZERO) { acc, item -> acc + item.taxAmount }
        _totalAmount.value = _subtotal.value + _taxAmount.value
        _itemCount.value = items.sumOf { it.quantity }
    }

    fun addProductToCart(product: Product, quantity: Int = 1) {
        viewModelScope.launch {
            val currentItems = _cartItems.value.toMutableList()
            val existingItemIndex = currentItems.indexOfFirst { it.product.id == product.id }

            if (existingItemIndex != -1) {
                // Update existing item
                val existingItem = currentItems[existingItemIndex]
                val newQuantity = existingItem.quantity + quantity
                currentItems[existingItemIndex] = existingItem.copy(
                    quantity = newQuantity,
                    totalPrice = product.price.multiply(BigDecimal(newQuantity)),
                    taxAmount = calculateTax(product.price.multiply(BigDecimal(newQuantity)), product.taxRate)
                )
            } else {
                // Add new item
                val totalPrice = product.price.multiply(BigDecimal(quantity))
                val taxAmount = calculateTax(totalPrice, product.taxRate)
                val newItem = CartItem(
                    id = UUID.randomUUID().toString(),
                    product = product,
                    quantity = quantity,
                    unitPrice = product.price,
                    totalPrice = totalPrice,
                    taxAmount = taxAmount
                )
                currentItems.add(newItem)

            }

            _cartItems.value = currentItems
            saveCart(context, currentItems)
            updateStockQuantity(product.id, -quantity)
            recalculateTotals()
        }
    }

    fun removeProductFromCart(productId: String) {
        viewModelScope.launch {
            val currentItems = _cartItems.value.toMutableList()
            val itemToRemove = currentItems.find { it.product.id == productId }
            
            if (itemToRemove != null) {
                currentItems.remove(itemToRemove)
                _cartItems.value = currentItems
                saveCart(context, currentItems)
                updateStockQuantity(productId, itemToRemove.quantity)
                recalculateTotals()
            }
        }
    }

    fun updateCartItemQuantity(productId: String, newQuantity: Int) {
        viewModelScope.launch {
            val currentItems = _cartItems.value.toMutableList()
            val itemIndex = currentItems.indexOfFirst { it.product.id == productId }

            if (itemIndex != -1) {
                val item = currentItems[itemIndex]
                val quantityDifference = newQuantity - item.quantity
                
                if (newQuantity <= 0) {
                    currentItems.removeAt(itemIndex)
                } else {
                    val totalPrice = item.unitPrice.multiply(BigDecimal(newQuantity))
                    val taxAmount = calculateTax(totalPrice, item.product.taxRate)
                    currentItems[itemIndex] = item.copy(
                        quantity = newQuantity,
                        totalPrice = totalPrice,
                        taxAmount = taxAmount
                    )
                }

                _cartItems.value = currentItems
                saveCart(context, currentItems)
                updateStockQuantity(productId, -quantityDifference)
                recalculateTotals()
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            // Restore stock quantities
          _cartItems.value.forEach { item ->
                updateStockQuantity(item.product.id, item.quantity)
            }
             saveCart(context, emptyList())
            _cartItems.value = emptyList()
            _selectedCustomer.value = null
            _transactionStatus.value = TransactionStatus.PENDING
            recalculateTotals()
        }
    }
    fun clearCartComplete() {
        viewModelScope.launch {
            // Restore stock quantities
            /*_cartItems.value.forEach { item ->
                updateStockQuantity(item.product.id, item.quantity)
            }*/
            _cartItems.value = emptyList()
            saveCart(context, emptyList())
            _selectedCustomer.value = null
            _transactionStatus.value = TransactionStatus.PENDING
            recalculateTotals()
        }
    }

    fun selectCustomer(customer: Customer?) {
        _selectedCustomer.value = customer
    }

    fun findProductByBarcode(barcode: String) {
        viewModelScope.launch {
            try {
                val product = productRepository.getProductByBarcode(barcode)
                if (product != null) {
                    addProductToCart(product, 1)
                } else {
                    // TODO: Show error message - product not found
                }
            } catch (e: Exception) {
                // TODO: Handle error
            }
        }
    }

    /*fun processTransaction(paymentMethod: PaymentMethod, paidAmount: BigDecimal? = null) {
        viewModelScope.launch {
            if (_cartItems.value.isEmpty()) return@launch

            _isProcessing.value = true
            _transactionStatus.value = TransactionStatus.IN_PROGRESS

            try {
                val transaction = createTransaction(paymentMethod, paidAmount)
                val transactionId = transaction.id

                // Insert transaction
                transactionRepository.insertTransaction(transaction)

                // Insert transaction items
                val transactionItems = _cartItems.value.map { cartItem ->
                    TransactionItem(
                        id = UUID.randomUUID().toString(),
                        transactionId = transactionId,
                        productId = cartItem.product.id,
                        quantity = cartItem.quantity,
                        unitPrice = cartItem.unitPrice,
                        totalPrice = cartItem.totalPrice,
                        taxAmount = cartItem.taxAmount
                    )
                }
                transactionRepository.insertTransactionItems(transactionItems)

                // Insert payment
                val payment = Payment(
                    id = UUID.randomUUID().toString(),
                    transactionId = transactionId,
                    amount = transaction.totalAmount,
                    method = paymentMethod,
                    status = PaymentStatus.COMPLETED,
                    processedAt = LocalDateTime.now()
                )
                transactionRepository.insertPayment(payment)

                // Update customer if selected
                _selectedCustomer.value?.let { customer ->
                    updateCustomerAfterTransaction(customer, transaction)
                }

                _transactionStatus.value = TransactionStatus.COMPLETED
                _currentTransaction.value = transaction

            } catch (e: Exception) {
                _transactionStatus.value = TransactionStatus.CANCELLED
                // Handle error
            } finally {
                _isProcessing.value = false
            }
        }
    }*/
    fun processTransaction(paymentMethod: PaymentMethod, paidAmount: BigDecimal? = null, paymentResultInfo: PaymentResultInfo) {
        viewModelScope.launch {
            if (_cartItems.value.isEmpty()) {
                Log.d("POS", "Cart is empty, aborting transaction")
                return@launch
            }

            _isProcessing.value = true
            _transactionStatus.value = TransactionStatus.IN_PROGRESS
            Log.d("POS", "Transaction started. Status set to IN_PROGRESS")

            try {
                val transaction = createTransaction(paymentMethod, paidAmount, paymentResultInfo)
                val transactionId = transaction.id
                Log.d("POS", "Transaction created with ID: $transactionId")

                val transactionItems = _cartItems.value.map { cartItem ->

                    TransactionItem(
                        id = UUID.randomUUID().toString(),
                        transactionId = transactionId,
                        productId = cartItem.product.id,
                        quantity = cartItem.quantity,
                        unitPrice = cartItem.unitPrice,
                        totalPrice = cartItem.totalPrice,
                        taxAmount = cartItem.taxAmount,
                        notes ="${cartItem.product.name} ${cartItem.product.sku}".replace(Regex("\\s{2,}")," ")
                    )

                }



                val payment = Payment(
                    id = UUID.randomUUID().toString(),
                    transactionId = transactionId,
                    amount = transaction.totalAmount,
                    method = paymentMethod,
                    status = PaymentStatus.COMPLETED,
                    processedAt = LocalDateTime.now()
                )

                // Ejecutar todo en IO para evitar bloqueos
                withContext(Dispatchers.IO) {
                    Log.d("POS", "Inserting transaction into DB...")
                    transactionRepository.insertTransaction(transaction)
                    Log.d("POS", "Transaction inserted successfully")

                    Log.d("POS", "Inserting ${transactionItems.size} transaction items into DB...")
                    transactionRepository.insertTransactionItems(transactionItems)
                    Log.d("POS", "Transaction items inserted successfully")

                    Log.d("POS", "Inserting payment into DB...")
                    transactionRepository.insertPayment(payment)
                    Log.d("POS", "Payment inserted successfully")
                }

                val transactionDate = transaction.createdAt.toLocalDate()
                val year = transactionDate.year
                val dbDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val periods = listOf(
                    "daily" to Pair(
                        transactionDate.atStartOfDay(),
                        transactionDate.atTime(23, 59, 59)
                    ),
                    "weekly" to Pair(
                        transactionDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay(),
                        transactionDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).atTime(23, 59, 59)
                    ),
                    "monthly" to Pair(
                        transactionDate.withDayOfMonth(1).atStartOfDay(),
                        transactionDate.withDayOfMonth(transactionDate.lengthOfMonth()).atTime(23, 59, 59)
                    ),
                    "yearly" to Pair(
                        LocalDateTime.of(year, 1, 1, 0, 0, 0),
                        LocalDateTime.of(year, 12, 31, 23, 59, 59)
                    )
                )

                Log.e("datosGuardar", periods.toString())

                _cartItems.value.forEach { cartItem ->
                    val profit = cartItem.totalPrice - (cartItem.unitPrice.multiply(BigDecimal(cartItem.quantity)))

                    periods.forEach { (periodName, dates) ->
                        addOrUpdateProductPerformance(
                            productId = cartItem.product.id,
                            data="${cartItem.product.name} ${cartItem.product.sku}".replace(Regex("\\s{2,}")," "),
                            storeId = transaction.storeId,
                            period = periodName,
                            periodStart = dates.first,
                            periodEnd = dates.second,
                            quantitySold = cartItem.quantity,
                            revenue = cartItem.totalPrice,
                            profit = profit
                        )
                    }
                }

                updateDailySalesAnalytics(transaction, transactionItems)

                // Actualizar cliente si hay seleccionado
                _selectedCustomer.value?.let { customer ->
                    Log.d("POS", "Updating customer ${customer.id} after transaction")
                    updateCustomerAfterTransaction(customer, transaction,paymentResultInfo)
                    Log.d("POS", "Customer updated successfully")
                } ?: Log.d("POS", "No customer selected for transaction")




                _transactionStatus.value = TransactionStatus.COMPLETED
                _currentTransaction.value = transaction
                try {
                    when (val result = generateReceipt(transaction, transactionItems)) {
                        is ReceiptResult.Success -> {
                        _receiptPath.value = result.filePath // aquí sí es la ruta al PDF
                            try {
                                transactionRepository.updateTransaction(transaction.copy(
                                    notes = result.filePath
                                ))
                            }catch (_: Exception){}
                    }
                        is ReceiptResult.Error -> {
                        Log.e("POS", result.message)
                        _receiptPath.value = null
                    }
                    }
                }catch (e: Exception){}
                completeTransaction()
            } catch (e: Exception) {

                _transactionStatus.value = TransactionStatus.CANCELLED
                Log.e("POS", "Transaction failed: ${e.message}", e)
            } finally {
                _isProcessing.value = false
                Log.d("POS", "Processing flag reset to false")
            }
        }
    }

    fun completeTransaction() {
        viewModelScope.launch {
            Log.d("POS", "Completing transaction")
            _transactionStatus.value = TransactionStatus.COMPLETED

            // ✅ Aquí solo vacías carrito, no muevas stock
            clearCartComplete()
            Log.d("POS", "Cart cleared and transaction marked as COMPLETED")
        }
    }

    fun clearReceipt() {
        _receiptPath.value = null
    }

    fun cancelTransaction() {
        viewModelScope.launch {
            Log.d("POS", "Cancelling transaction, restoring stock quantities")

            // ✅ Revertir cambios de stock (sumar lo quitado en addProductToCart)
            _cartItems.value.forEach { item ->
                withContext(Dispatchers.IO) {
                    updateStockQuantity(item.product.id, item.quantity)
                    Log.d("POS", "Restored stock for product ${item.product.id} by ${item.quantity}")
                }
            }

            _transactionStatus.value = TransactionStatus.CANCELLED
            Log.d("POS", "Cart cleared and transaction cancelled")
        }
    }



    /*fun completeTransaction() {
        _transactionStatus.value = TransactionStatus.COMPLETED
        clearCart()
    }

    fun cancelTransaction() {
        viewModelScope.launch {
            // Restore stock quantities
            _cartItems.value.forEach { item ->
                updateStockQuantity(item.product.id, item.quantity)
            }
            clearCart()
        }
    }*/

    private suspend fun updateStockQuantity(productId: String, quantityChange: Int) {
        val product = productRepository.getProductById(productId)
        product?.let {
            val newQuantity = it.stockQuantity + quantityChange
            productRepository.updateStockQuantity(productId, newQuantity)
        }
    }

    private fun calculateTax(amount: BigDecimal, taxRate: BigDecimal): BigDecimal {
        return amount.multiply(taxRate).divide(BigDecimal(100))
    }

    private fun createTransaction(paymentMethod: PaymentMethod, paidAmount: BigDecimal?, paymentResultInfo: PaymentResultInfo): Transaction {
        val subtotal = _cartItems.value.sumOf { it.totalPrice }
        val taxAmount = _cartItems.value.sumOf { it.taxAmount }
        val totalAmount = subtotal + taxAmount
        val paid = paidAmount ?: totalAmount
        val changeAmount = paid - totalAmount

        return Transaction(
            id = UUID.randomUUID().toString(),
            transactionNumber = generateTransactionNumber(),
            customerId = _selectedCustomer.value?.id,
            cashierId = "admin_user",
            storeId = _currentStore.value?.id ?: "default_store",
            type = TransactionType.PURCHASE,
            status = TransactionStatus.PENDING,
            subtotal = subtotal,
            taxAmount = taxAmount,
            totalAmount = totalAmount,
            paidAmount = paid,
            changeAmount = changeAmount,
            paymentMethod = paymentMethod,
            notes = paymentResultInfo.nota
        )
    }

    private fun updateCustomerAfterTransaction(customer: Customer, transaction: Transaction, paymentResultInfo: PaymentResultInfo) {
        viewModelScope.launch {
            val updatedCustomer = customer.copy(
                totalSpent = customer.totalSpent + transaction.totalAmount,
                loyaltyPoints = customer.loyaltyPoints + calculateLoyaltyPoints(transaction.totalAmount),
                balance = customer.balance + paymentResultInfo.remaining.multiply(BigDecimal(-1))
            )
            customerRepository.updateCustomer(updatedCustomer)

            // Add customer transaction record
            val customerTransaction = CustomerTransaction(
                id = UUID.randomUUID().toString(),
                customerId = customer.id,
                transactionId = transaction.id,
                type = TransactionType.PURCHASE,
                amount = transaction.totalAmount,
                balance = updatedCustomer.balance
            )
            customerRepository.insertCustomerTransaction(customerTransaction)
        }
    }

    private fun calculateLoyaltyPoints(amount: BigDecimal): Int {
        val pointsPerDollar = _currentStore.value?.settings?.loyaltyPointsPerDollar ?: 1.0
        return amount.multiply(BigDecimal(pointsPerDollar)).toInt()
    }

    private fun generateTransactionNumber(): String {
        return "TXN-${System.currentTimeMillis()}"
    }

    fun generateReceipt(transaction: Transaction, transactionItems: List<TransactionItem>): ReceiptResult {
        return try {
            val receiptService = ReceiptService(context)
            /*val transactionItems = _cartItems.value.map { cartItem ->
                TransactionItem(
                    id = UUID.randomUUID().toString(),
                    transactionId = transaction.id,
                    productId = cartItem.product.id,
                    quantity = cartItem.quantity,
                    unitPrice = cartItem.unitPrice,
                    totalPrice = cartItem.totalPrice,
                    taxAmount = cartItem.taxAmount,
                    notes ="${cartItem.product.name} ${cartItem.product.sku}".replace(Regex("\\s{2,}")," ")
                )
            }*/

            receiptService.generateReceipt(
                transaction = transaction,
                transactionItems = transactionItems,
                store = _currentStore.value, // TODO: Get current store
                customer = _selectedCustomer.value
            )
        } catch (e: Exception) {
            ReceiptResult.Error("Error generating receipt: ${e.message}")
        }
    }

    fun generateReceiptBitmap(transaction: Transaction): android.graphics.Bitmap? {
        val receiptService = ReceiptService(context)
        val transactionItems = _cartItems.value.map { cartItem ->
            TransactionItem(
                id = UUID.randomUUID().toString(),
                transactionId = transaction.id,
                productId = cartItem.product.id,
                quantity = cartItem.quantity,
                unitPrice = cartItem.unitPrice,
                totalPrice = cartItem.totalPrice,
                taxAmount = cartItem.taxAmount
            )
        }

        return receiptService.generateReceiptBitmap(
            transaction = transaction,
            transactionItems = transactionItems,
            store = _currentStore.value, // TODO: Get current store
            customer = _selectedCustomer.value
        )
    }

    suspend fun addOrUpdateProductPerformance(
        productId: String,
        data: String,
        storeId: String,
        period: String,
        periodStart: LocalDateTime,
        periodEnd: LocalDateTime,
        quantitySold: Int,
        revenue: BigDecimal,
        profit: BigDecimal = BigDecimal.ZERO,

    ) {
        val existingListPerformance = analyticsRepository.getProductPerformanceByProductAndPeriod(productId, period, periodStart, periodEnd).first()
        if (existingListPerformance.isNotEmpty()) {
            val existing = existingListPerformance.first()
            val updated = existing.copy(
                quantitySold = existing.quantitySold + quantitySold,
                revenue = existing.revenue + revenue,
                profit = existing.profit + profit,
                data=data,
                averagePrice = if (existing.quantitySold + quantitySold != 0) {
                    (existing.revenue + revenue)
                        .divide(BigDecimal(existing.quantitySold + quantitySold), 2, java.math.RoundingMode.HALF_UP)
                } else BigDecimal.ZERO
            )
            analyticsRepository.updateProductPerformance(updated)
        } else {
            val newPerformance = ProductPerformance(
                id = UUID.randomUUID().toString(),
                productId = productId,
                data=data,
                storeId = storeId,
                period = period,
                periodStart = periodStart,
                periodEnd = periodEnd,
                quantitySold = quantitySold,
                revenue = revenue,
                profit = profit,
                averagePrice =  if (quantitySold != 0) {
                    revenue.divide(BigDecimal(quantitySold), 2, java.math.RoundingMode.HALF_UP)
                } else BigDecimal.ZERO,
                rank = 0 // opcional
            )

            analyticsRepository.insertProductPerformance(newPerformance)
        }
    }


    private suspend fun updateDailySalesAnalytics(transaction: Transaction, items: List<TransactionItem>) {
        val today = transaction.createdAt.toLocalDate().atStartOfDay()
        val existing = analyticsRepository.getDailySalesByDate(today)

        if (existing != null) {
            val updated = existing.copy(
                totalSales = existing.totalSales + transaction.totalAmount,
                totalTransactions = existing.totalTransactions + 1,
                totalItems = existing.totalItems + items.sumOf { it.quantity },
                averageTransactionValue =
                    (existing.totalSales + transaction.totalAmount)
                        .divide((existing.totalTransactions + 1).toBigDecimal(), 2, java.math.RoundingMode.HALF_UP),
                cashSales = existing.cashSales + if (transaction.paymentMethod == PaymentMethod.CASH) transaction.totalAmount else BigDecimal.ZERO,
                cardSales = existing.cardSales + if (transaction.paymentMethod == PaymentMethod.CARD) transaction.totalAmount else BigDecimal.ZERO,
                mobileSales = existing.mobileSales + if (transaction.paymentMethod == PaymentMethod.MOBILE_PAYMENT) transaction.totalAmount else BigDecimal.ZERO,
                otherSales = existing.otherSales,
                refunds = existing.refunds,
                discounts = existing.discounts + transaction.discountAmount,
                taxes = existing.taxes + transaction.taxAmount
            )
            analyticsRepository.updateDailySales(updated)
        } else {
            val newDaily = DailySales(
                id = UUID.randomUUID().toString(),
                date = today,
                storeId = transaction.storeId,
                totalSales = transaction.totalAmount,
                totalTransactions = 1,
                totalItems = items.sumOf { it.quantity },
                averageTransactionValue = transaction.totalAmount,
                cashSales = if (transaction.paymentMethod == PaymentMethod.CASH) transaction.totalAmount else BigDecimal.ZERO,
                cardSales = if (transaction.paymentMethod == PaymentMethod.CARD) transaction.totalAmount else BigDecimal.ZERO,
                mobileSales = if (transaction.paymentMethod == PaymentMethod.MOBILE_PAYMENT) transaction.totalAmount else BigDecimal.ZERO,
                otherSales = BigDecimal.ZERO,
                refunds = BigDecimal.ZERO,
                discounts = transaction.discountAmount,
                taxes = transaction.taxAmount
            )
            analyticsRepository.insertDailySales(newDaily)
        }
    }

}

data class CartItem(
    val id: String,
    val product: Product,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val totalPrice: BigDecimal,
    val taxAmount: BigDecimal
)

