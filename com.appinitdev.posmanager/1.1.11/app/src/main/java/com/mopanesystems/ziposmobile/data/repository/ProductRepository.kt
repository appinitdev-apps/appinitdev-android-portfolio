
@Singleton
class ProductRepository @Inject constructor(
    private val productDao: ProductDao
) {
    fun getAllActiveProducts(): Flow<List<Product>> = productDao.getAllActiveProducts()
    
    fun getProductsByStore(storeId: String): Flow<List<Product>> = productDao.getProductsByStore(storeId)
    
    suspend fun getProductById(id: String): Product? = productDao.getProductById(id)
    
    suspend fun getProductByBarcode(barcode: String): Product? = productDao.getProductByBarcode(barcode)
    
    suspend fun getProductBySku(sku: String): Product? = productDao.getProductBySku(sku)
    
    fun searchProducts(searchQuery: String): Flow<List<Product>> = productDao.searchProducts("%$searchQuery%")
    
    fun getProductsByCategory(categoryId: String): Flow<List<Product>> = productDao.getProductsByCategory(categoryId)
    
    fun getLowStockProducts(): Flow<List<Product>> = productDao.getLowStockProducts()
    
    fun getOutOfStockProducts(): Flow<List<Product>> = productDao.getOutOfStockProducts()
    
    suspend fun insertProduct(product: Product) = productDao.insertProduct(product)
    
    suspend fun insertProducts(products: List<Product>) = productDao.insertProducts(products)
    
    suspend fun updateProduct(product: Product) = productDao.updateProduct(product)
    
    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)
    
    suspend fun updateStockQuantity(productId: String, quantity: Int) = productDao.updateStockQuantity(productId, quantity)
    
    suspend fun deactivateProduct(productId: String, active: Boolean) = productDao.deactivateProduct(productId, active)
    
    fun getActiveProductCount(): Flow<Int> = productDao.getActiveProductCount()
    
    fun getLowStockCount(): Flow<Int> = productDao.getLowStockCount()

    suspend fun getTotalInventoryValue(): BigDecimal {
        val value = productDao.getTotalInventoryValue() ?: 0.0
        return BigDecimal.valueOf(value)
    }

    
    suspend fun getProductsInventoryValue(): List<ProductWithValue> {
        return productDao.getProductsInventoryValue()
    }
    fun getOutOfStockProductsCount(): Flow<Int> = productDao.getOutOfStockProductsCount()

    fun getAllCategories(): Flow<List<Category>> = productDao.getAllCategories()

    fun getActiveCategories(): Flow<List<Category>> = productDao.getActiveCategories()

    suspend fun getCategoryById(id: String): Category? = productDao.getCategoryById(id)

    suspend fun insertCategory(category: Category) {
        productDao.insertCategory(category)
    }

    suspend fun insertCategories(categories: List<Category>) {
        productDao.insertCategories(categories)
    }

    suspend fun updateCategory(category: Category) {
        productDao.updateCategory(category)
    }

    suspend fun deleteCategory(category: Category) {
        productDao.deleteCategory(category)
    }

}

