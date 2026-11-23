
@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()


    val allCategories: StateFlow<List<Category>> =
        productRepository.getAllCategories()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _filteredProducts = MutableStateFlow<List<Product>>(emptyList())
    val filteredProducts: StateFlow<List<Product>> = _filteredProducts.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _selectedMode = MutableStateFlow(1) // valor inicial
    val selectedMode: StateFlow<Int> = _selectedMode.asStateFlow()


    init {
        loadProducts()
        viewModelScope.launch {
            val existingCategories = productRepository.getAllCategories().first()
            if (existingCategories.isEmpty()) {
                insertDefaultCategories()
            }
        }

//
//       val product1 = createNewProduct(
//            name = "Coca Cola 500ml",
//            sku = "CC500",
//            price = BigDecimal("15.50"),
//            categoryId = "Beverages",
//            barcode = "5012345678900",
//            description = "Refresco de cola",
//            stockQuantity = 50,
//            minStockLevel = 5
//        )
//
//        val product2 = createNewProduct(
//            name = "Pan Baguette",
//            sku = "PB100",
//            price = BigDecimal("12.00"),
//            categoryId = "Bakery",
//            barcode = "2345678901234",
//            description = "Pan francés artesanal",
//            stockQuantity = 30,
//            minStockLevel = 10
//        )
//
//        val product3 = createNewProduct(
//            name = "Leche Entera 1L",
//            sku = "LE1L",
//            price = BigDecimal("22.75"),
//            categoryId = "Dairy",
//            barcode = "3456789012345",
//            description = "Leche fresca entera",
//            stockQuantity = 40,
//            minStockLevel = 8
//        )
//
//        val product4 = createNewProduct(
//            name = "Huevos de Gallina x12",
//            sku = "H12",
//            price = BigDecimal("35.00"),
//            categoryId = "Eggs",
//            barcode = "4567890123456",
//            description = "Caja de 12 huevos frescos",
//            stockQuantity = 60,
//            minStockLevel = 12
//        )
//
//        val product5 = createNewProduct(
//            name = "Cereal Corn Flakes 500g",
//            sku = "CF500",
//            price = BigDecimal("48.90"),
//            categoryId = "Cereals",
//            barcode = "5678901234567",
//            description = "Cereal de maíz clásico",
//            stockQuantity = 25,
//            minStockLevel = 5
//        )
//        addProduct(product1)
//        addProduct(product2)
//        addProduct(product3)
//        addProduct(product4)
//        addProduct(product5)


    }


    private suspend fun insertDefaultCategories() {
        val defaultCategories = listOf(
            Category(id = "Default1", name = "All")
        )
        productRepository.insertCategories(defaultCategories)
    }

    fun setMode(mode: Int) {
        _selectedMode.value = mode
    }
    fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                productRepository.getAllActiveProducts().collect { productList ->
                    _products.value = productList
                    applyFilters()
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }


    }

    fun searchProducts(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    fun filterByCategory(categoryId: String?) {
        _selectedCategory.value = categoryId
        applyFilters()
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedCategory.value = null
        applyFilters()
    }

    private fun applyFilters() {
        val query = _searchQuery.value.lowercase()
        val categoryId = _selectedCategory.value

        val filtered = _products.value.filter { product ->
            val matchesSearch = query.isEmpty() ||
                    product.name.lowercase().contains(query) ||
                    product.sku.lowercase().contains(query) ||
                    product.barcode?.lowercase()?.contains(query) == true

            val matchesCategory = categoryId == null || product.categoryId == categoryId

            matchesSearch && matchesCategory
        }

        _filteredProducts.value = filtered
    }

    fun addProduct(product: Product) {
        viewModelScope.launch {
            try {
                productRepository.insertProduct(product)
            } catch (e: Exception) {
                _errorMessage.value = e.message
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

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            try {
                productRepository.deactivateProduct(product.id, false)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun activeProduct(product: Product) {
        viewModelScope.launch {
            try {
                productRepository.deactivateProduct(product.id, true)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }


    fun updateStockQuantity(productId: String, quantity: Int) {
        viewModelScope.launch {
            try {
                productRepository.updateStockQuantity(productId, quantity)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun getProductByBarcode(barcode: String) {
        viewModelScope.launch {
            try {
                val product = productRepository.getProductByBarcode(barcode)
                if (product != null) {
                    // Handle found product (e.g., add to cart in POS)
                    _errorMessage.value = "Product found: ${product.name}"
                } else {
                    _errorMessage.value = "Product not found for barcode: $barcode"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun getLowStockProducts() {
        viewModelScope.launch {
            try {
                productRepository.getLowStockProducts().collect { lowStockProducts ->
                    _filteredProducts.value = lowStockProducts
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun getOutOfStockProducts() {
        viewModelScope.launch {
            try {
                productRepository.getOutOfStockProducts().collect { outOfStockProducts ->
                    _filteredProducts.value = outOfStockProducts
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun getProductBySku(sku: String, onResult: (Product?) -> Unit) {
        viewModelScope.launch {
            try {
                val product = productRepository.getProductBySku(sku)
                onResult(product)
            } catch (e: Exception) {
                _errorMessage.value = e.message
                onResult(null)
            }
        }
    }

    fun insertProducts(products: List<Product>) {
        viewModelScope.launch {
            try {
                productRepository.insertProducts(products)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    // Helper function to create a new product
    fun createNewProduct(
        name: String,
        sku: String,
        price: BigDecimal,
        categoryId: String? = null,
        barcode: String? = null,
        description: String? = null,
        cost: BigDecimal? = null,
        stockQuantity: Int = 0,
        minStockLevel: Int = 0,
        isTaxable: Boolean = true,
        taxRate: BigDecimal = BigDecimal.ZERO
    ): Product {
        return Product(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            sku = sku,
            barcode = barcode,
            price = price,
            cost = cost,
            categoryId = categoryId,
            stockQuantity = stockQuantity,
            minStockLevel = minStockLevel,
            isActive = true,
            isTaxable = isTaxable,
            taxRate = taxRate,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
}