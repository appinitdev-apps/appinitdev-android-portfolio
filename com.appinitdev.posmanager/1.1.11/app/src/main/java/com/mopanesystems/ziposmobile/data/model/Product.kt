
@Entity(tableName = "products")
data class Product(
    @PrimaryKey
    override val id: String,
    val name: String,
    val description: String? = null,
    val sku: String,
    val barcode: String? = null,
    val price: BigDecimal,
    val cost: BigDecimal? = null,
    val categoryId: String? = null,
    val subcategoryId: String? = null,
    val stockQuantity: Int = 0,
    val minStockLevel: Int = 0,
    val maxStockLevel: Int? = null,
    val isActive: Boolean = true,
    val isStatus: Boolean = true,
    val isTaxable: Boolean = true,
    val taxRate: BigDecimal = BigDecimal.ZERO,
    val imageUrl: String? = null,
    val weight: BigDecimal? = null,
    val dimensions: String? = null,
    val supplierId: String? = null,
    override val createdAt: LocalDateTime = LocalDateTime.now(),
    override val updatedAt: LocalDateTime = LocalDateTime.now(),
    override val storeId: String? = null
) : Identifiable, Auditable, StoreScoped {

    val hasLowStock: Boolean
        get() = stockQuantity in 1..minStockLevel

    val isOutOfStock: Boolean
        get() = stockQuantity <= 0

    val isAvailable: Boolean
        get() = isActive && stockQuantity > 0

    val totalStockValue: BigDecimal
        get() = price.multiply(BigDecimal(stockQuantity))

    val totalStockCost: BigDecimal
        get() = (cost ?: BigDecimal.ZERO).multiply(BigDecimal(stockQuantity))

    fun calculateTaxAmount(quantity: Int = 1): BigDecimal {
        if (!isTaxable || taxRate <= BigDecimal.ZERO) return BigDecimal.ZERO
        return price.multiply(BigDecimal(quantity))
            .multiply(taxRate)
            .divide(BigDecimal(100), 2, RoundingMode.HALF_UP)
    }

    fun calculateTotalPriceWithTax(quantity: Int = 1): BigDecimal {
        val basePrice = price.multiply(BigDecimal(quantity))
        return basePrice + calculateTaxAmount(quantity)
    }

    fun calculateProfitMargin(): BigDecimal {
        val unitCost = cost ?: BigDecimal.ZERO
        return price - unitCost
    }

    fun calculateProfitMarginPercentage(): Double {
        val unitCost = cost ?: return 100.0
        if (price <= BigDecimal.ZERO) return 0.0
        val profit = price - unitCost
        return profit.divide(price, 4, RoundingMode.HALF_UP).toDouble() * 100
    }
}

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey
    override val id: String,
    val name: String,
    val description: String? = null,
    val parentId: String? = null,
    val isActive: Boolean = true,
    override val createdAt: LocalDateTime = LocalDateTime.now(),
    override val updatedAt: LocalDateTime = LocalDateTime.now(),
    override val storeId: String? = null
) : Identifiable, Auditable, StoreScoped {

    val isSubcategory: Boolean
        get() = !parentId.isNullOrEmpty()
}

@Entity(tableName = "subcategories")
data class Subcategory(
    @PrimaryKey
    override val id: String,
    val name: String,
    val description: String? = null,
    val categoryId: String,
    val isActive: Boolean = true,
    override val createdAt: LocalDateTime = LocalDateTime.now(),
    override val updatedAt: LocalDateTime = LocalDateTime.now(),
    override val storeId: String? = null
) : Identifiable, Auditable, StoreScoped
