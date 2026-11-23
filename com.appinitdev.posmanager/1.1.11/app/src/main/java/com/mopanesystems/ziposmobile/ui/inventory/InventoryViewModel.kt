@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val productRepository: ProductRepository
) : ViewModel() {
    private val _lowStockProducts = MutableLiveData<List<Product>>()
    val lowStockProducts: LiveData<List<Product>> = _lowStockProducts
    private val _stockAlerts = MutableLiveData<List<StockAlert>>()
    val stockAlerts: LiveData<List<StockAlert>> = _stockAlerts
    private val _recentAdjustments = MutableLiveData<List<InventoryAdjustment>>()
    val recentAdjustments: LiveData<List<InventoryAdjustment>> = _recentAdjustments
    private val _totalInventoryValue = MutableStateFlow(BigDecimal.ZERO)
    val totalInventoryValue: StateFlow<BigDecimal> get() = _totalInventoryValue
    private val _productsInventoryValue = MutableStateFlow<List<ProductWithValue>>(emptyList())
    val productsInventoryValue: StateFlow<List<ProductWithValue>> get() = _productsInventoryValue
    private val _lowStockCount = MutableLiveData<Int>()
    val lowStockCount: LiveData<Int> = _lowStockCount
    private val _outOfStockCount = MutableLiveData<Int>()
    val outOfStockCount: LiveData<Int> = _outOfStockCount
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    private val _productCount = MutableLiveData<Int>()
    val productCount: LiveData<Int> = _productCount
    val allCategories: StateFlow<List<Category>> =
        productRepository.getAllCategories()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    init {
        loadInventoryData()
    }
    private fun loadInventoryData() {
        _isLoading.value = true
        viewModelScope.launch {
            productRepository.getLowStockProducts().collect { products ->
                _lowStockProducts.value = products
            }
        }
        viewModelScope.launch {
            try {
                productRepository.getLowStockCount().collect {count ->
                    _lowStockCount.value = count
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error loading stock alerts: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
        viewModelScope.launch {
            try {
                productRepository.getOutOfStockProductsCount().collect {count ->
                    _outOfStockCount.value = count
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error loading stock alerts: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
        viewModelScope.launch {
            _totalInventoryValue.value = productRepository.getTotalInventoryValue()
        }
        viewModelScope.launch {
            try {
                productRepository.getActiveProductCount().collect {count ->
                    _productCount.value = count
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error loading stock alerts: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
        viewModelScope.launch {
            try {
                inventoryRepository.getAllAdjustments().collect { adjustmentList ->
                    _recentAdjustments.value = adjustmentList.take(10)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error loading adjustments: ${e.message}"
            }
        }
    }
    fun updateProduct(product: Product) {
        viewModelScope.launch {
            try {
                val updatedProduct = product.copy(updatedAt = LocalDateTime.now())
                productRepository.updateProduct(updatedProduct)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }
    private fun updateStockCounts(alerts: List<StockAlert>) {
        val lowStock = alerts.count { it.alertType == StockAlertType.LOW_STOCK }
        val outOfStock = alerts.count { it.alertType == StockAlertType.OUT_OF_STOCK }
        _lowStockCount.value = lowStock
        _outOfStockCount.value = outOfStock
    }
    fun acknowledgeAlert(alert: StockAlert) {
        viewModelScope.launch {
            try {
                inventoryRepository.acknowledgeAlert(alert.id, LocalDateTime.now())
                loadInventoryData() 
            } catch (e: Exception) {
                _errorMessage.value = "Error acknowledging alert: ${e.message}"
            }
        }
    }
    fun createStockAdjustment(
        productId: String,
        quantity: Int,
        reason: AdjustmentReason,
        notes: String?,
        adjustedBy: String,
        storeId: String
    ) {
        viewModelScope.launch {
            try {
                val adjustment = InventoryAdjustment(
                    id = UUID.randomUUID().toString(),
                    productId = productId,
                    quantity = quantity,
                    reason = reason,
                    notes = notes,
                    userId = adjustedBy,
                    storeId = storeId,
                    createdAt = LocalDateTime.now()
                )
                inventoryRepository.insertAdjustment(adjustment)
                updateProductStock(productId, quantity)
                checkStockAlerts(productId)
                loadInventoryData() 
            } catch (e: Exception) {
                _errorMessage.value = "Error creating stock adjustment: ${e.message}"
            }
        }
    }
    private suspend fun updateProductStock(productId: String, quantityChange: Int) {
        val product = productRepository.getProductById(productId)
        if (product != null) {
            val newQuantity = product.stockQuantity + quantityChange
            productRepository.updateStockQuantity(productId, newQuantity)
        }
    }
    private suspend fun checkStockAlerts(productId: String) {
        val product = productRepository.getProductById(productId)
        if (product != null) {
            if (product.stockQuantity <= 0) {
                createStockAlertInternal(
                    productId = productId,
                    currentStock = product.stockQuantity,
                    minThreshold = product.minStockLevel,
                    alertType = StockAlertType.OUT_OF_STOCK,
                    storeId = product.storeId ?: "default_store"
                )
            } else if (product.stockQuantity <= product.minStockLevel) {
                createStockAlertInternal(
                    productId = productId,
                    currentStock = product.stockQuantity,
                    minThreshold = product.minStockLevel,
                    alertType = StockAlertType.LOW_STOCK,
                    storeId = product.storeId ?: "default_store"
                )
            } else {
                inventoryRepository.deactivateAlertsForProduct(productId)
            }
        }
    }
    private suspend fun createStockAlertInternal(
        productId: String,
        currentStock: Int,
        minThreshold: Int,
        alertType: StockAlertType,
        storeId: String
    ) {
        val alert = StockAlert(
            id = UUID.randomUUID().toString(),
            productId = productId,
            currentStock = currentStock,
            threshold = minThreshold,
            alertType = alertType,
            isActive = true,
            createdAt = LocalDateTime.now(),
            storeId = storeId
        )
        inventoryRepository.insertAlert(alert)
    }
    fun createStockAlert(
        productId: String,
        currentStock: Int,
        minThreshold: Int,
        alertType: StockAlertType,
        storeId: String
    ) {
        viewModelScope.launch {
            try {
                val alert = StockAlert(
                    id = UUID.randomUUID().toString(),
                    productId = productId,
                    currentStock = currentStock,
                    threshold = minThreshold,
                    alertType = alertType,
                    isActive = true,
                    createdAt = LocalDateTime.now(),
                    storeId = storeId
                )
                inventoryRepository.insertAlert(alert)
                loadInventoryData() 
            } catch (e: Exception) {
                _errorMessage.value = "Error creating stock alert: ${e.message}"
            }
        }
    }
    private val _suppliers = MutableStateFlow<List<Supplier>>(emptyList())
    val suppliers: StateFlow<List<Supplier>> get() = _suppliers
    fun loadSuppliers() {
        viewModelScope.launch {
            inventoryRepository.getAllActiveSuppliers().collect { list ->
                _suppliers.value = list
            }
        }
    }
    fun insertSupplier(supplier: Supplier) {
        viewModelScope.launch {
            try {
                inventoryRepository.insertSupplier(supplier)
                loadSuppliers()
            } catch (e: Exception) {
                _errorMessage.value = "Error inserting supplier: ${e.message}"
            }
        }
    }
    fun updateSupplier(supplier: Supplier) {
        viewModelScope.launch {
            try {
                inventoryRepository.updateSupplier(supplier)
                loadSuppliers()
            } catch (e: Exception) {
                _errorMessage.value = "Error updating supplier: ${e.message}"
            }
        }
    }
    fun deactivateSupplier(supplierId: String) {
        viewModelScope.launch {
            try {
                inventoryRepository.deactivateSupplier(supplierId)
                loadSuppliers()
            } catch (e: Exception) {
                _errorMessage.value = "Error deactivating supplier: ${e.message}"
            }
        }
    }
    private val _purchaseOrders = MutableStateFlow<List<PurchaseOrder>>(emptyList())
    val purchaseOrders: StateFlow<List<PurchaseOrder>> get() = _purchaseOrders
    fun loadPurchaseOrders() {
        viewModelScope.launch {
            inventoryRepository.getAllOrders().collect { list ->
                _purchaseOrders.value = list
            }
        }
    }
    fun insertPurchaseOrder(order: PurchaseOrder, items: List<PurchaseOrderItem>) {
        viewModelScope.launch {
            try {
                inventoryRepository.insertOrder(order)
                inventoryRepository.insertOrderItems(items)
                loadPurchaseOrders()
            } catch (e: Exception) {
                _errorMessage.value = "Error creating purchase order: ${e.message}"
            }
        }
    }
    fun updatePurchaseOrderStatus(orderId: String, status: OrderStatus) {
        viewModelScope.launch {
            try {
                inventoryRepository.updateOrderStatus(orderId, status)
                loadPurchaseOrders()
            } catch (e: Exception) {
                _errorMessage.value = "Error updating order status: ${e.message}"
            }
        }
    }
    fun filterAdjustmentsByReason(reason: AdjustmentReason) {
        viewModelScope.launch {
            inventoryRepository.getAdjustmentsByReason(reason).collect { list ->
                _recentAdjustments.value = list
            }
        }
    }
    fun filterAdjustmentsByProduct(productId: String) {
        viewModelScope.launch {
            inventoryRepository.getAdjustmentsByProduct(productId).collect { list ->
                _recentAdjustments.value = list
            }
        }
    }
    fun refreshData() {
        loadInventoryData()
        loadSuppliers()
        loadPurchaseOrders()
    }
}