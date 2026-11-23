@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveProducts(): Flow<List<Product>>
    @Query("SELECT * FROM products WHERE storeId = :storeId AND isActive = 1 ORDER BY name ASC")
    fun getProductsByStore(storeId: String): Flow<List<Product>>
    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: String): Product?
    @Query("SELECT * FROM products WHERE barcode = :barcode AND isActive = 1")
    suspend fun getProductByBarcode(barcode: String): Product?
    @Query("SELECT * FROM products WHERE sku = :sku AND isActive = 1")
    suspend fun getProductBySku(sku: String): Product?
    @Query("SELECT * FROM products WHERE name LIKE :searchQuery AND isActive = 1 ORDER BY name ASC")
    fun searchProducts(searchQuery: String): Flow<List<Product>>
    @Query("SELECT * FROM products WHERE categoryId = :categoryId AND isActive = 1 ORDER BY name ASC")
    fun getProductsByCategory(categoryId: String): Flow<List<Product>>
    @Query("SELECT * FROM products WHERE stockQuantity <= minStockLevel AND isActive = 1 ORDER BY stockQuantity ASC")
    fun getLowStockProducts(): Flow<List<Product>>
    @Query("SELECT * FROM products WHERE stockQuantity = 0 AND isActive = 1 ORDER BY name ASC")
    fun getOutOfStockProducts(): Flow<List<Product>>
    @Query("SELECT SUM(stockQuantity * price) FROM products WHERE isActive = 1")
    suspend fun getTotalInventoryValue(): Double?
    @Query("SELECT id, name, stockQuantity, price, (stockQuantity * price) as totalValue FROM products")
    suspend fun getProductsInventoryValue(): List<ProductWithValue>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)
    @Update
    suspend fun updateProduct(product: Product)
    @Delete
    suspend fun deleteProduct(product: Product)
    @Query("UPDATE products SET stockQuantity = :quantity WHERE id = :productId")
    suspend fun updateStockQuantity(productId: String, quantity: Int)
    @Query("UPDATE products SET isActive = :active WHERE id = :productId")
    suspend fun deactivateProduct(productId: String, active: Boolean )
    @Query("SELECT COUNT(*) FROM products WHERE isActive = 1")
    fun getActiveProductCount():  Flow<Int>
    @Query("SELECT COUNT(*) FROM products WHERE stockQuantity <= minStockLevel AND isActive = 1")
     fun getLowStockCount():  Flow<Int>
    @Query("SELECT COUNT(*)  FROM products WHERE stockQuantity = 0 AND isActive = 1 ORDER BY name ASC")
    fun getOutOfStockProductsCount(): Flow<Int>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<Category>)
    @Update
    suspend fun updateCategory(category: Category)
    @Delete
    suspend fun deleteCategory(category: Category)
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: String): Category?
    @Query(" SELECT * FROM categories ORDER BY CASE WHEN name = 'All' THEN 0 ELSE 1 END , name ASC")
    fun getAllCategories(): Flow<List<Category>>
    @Query("SELECT * FROM categories WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveCategories(): Flow<List<Category>>
}
data class ProductWithValue(
    val id: String,
    val name: String,
    val stockQuantity: Int,
    val price: Double,
    val totalValue: Double
)