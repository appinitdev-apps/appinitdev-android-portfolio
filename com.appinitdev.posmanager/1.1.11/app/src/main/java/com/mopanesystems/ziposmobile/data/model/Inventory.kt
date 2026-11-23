
@Entity(tableName = "inventory_adjustments")
data class InventoryAdjustment(
    @PrimaryKey
    override val id: String,
    val productId: String,
    val quantity: Int,
    val reason: AdjustmentReason,
    val notes: String? = null,
    val userId: String,
    val storeId: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    override val isSynced: Boolean = false
) : Identifiable, Syncable {

    val isStockIncrease: Boolean
        get() = quantity > 0

    val isStockDecrease: Boolean
        get() = quantity < 0
}

@Entity(tableName = "stock_alerts")
data class StockAlert(
    @PrimaryKey
    override val id: String,
    val productId: String,
    val currentStock: Int,
    val threshold: Int,
    val alertType: StockAlertType,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val resolvedAt: LocalDateTime? = null,
    val acknowledgedAt: LocalDateTime? = null,
    val storeId: String
) : Identifiable {

    val isOutOfStockAlert: Boolean
        get() = alertType == StockAlertType.OUT_OF_STOCK

    val isLowStockAlert: Boolean
        get() = alertType == StockAlertType.LOW_STOCK
}

@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey
    override val id: String,
    val name: String,
    val contactPerson: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val zipCode: String? = null,
    val country: String? = null,
    val paymentTerms: String? = null,
    val isActive: Boolean = true,
    override val createdAt: LocalDateTime = LocalDateTime.now(),
    override val updatedAt: LocalDateTime = LocalDateTime.now(),
    override val storeId: String? = null
) : Identifiable, Auditable, StoreScoped

@Entity(tableName = "purchase_orders")
data class PurchaseOrder(
    @PrimaryKey
    override val id: String,
    val orderNumber: String,
    val supplierId: String,
    val status: OrderStatus,
    val totalAmount: BigDecimal,
    val notes: String? = null,
    val expectedDeliveryDate: LocalDateTime? = null,
    val actualDeliveryDate: LocalDateTime? = null,
    val createdBy: String,
    override val storeId: String,
    override val createdAt: LocalDateTime = LocalDateTime.now(),
    override val updatedAt: LocalDateTime = LocalDateTime.now()
) : Identifiable, Auditable, StoreScoped {

    val isFullyReceived: Boolean
        get() = status == OrderStatus.RECEIVED

    val canBeCancelled: Boolean
        get() = status == OrderStatus.DRAFT || status == OrderStatus.PENDING
}

@Entity(tableName = "purchase_order_items")
data class PurchaseOrderItem(
    @PrimaryKey
    override val id: String,
    val purchaseOrderId: String,
    val productId: String,
    val quantity: Int,
    val unitCost: BigDecimal,
    val totalCost: BigDecimal,
    val receivedQuantity: Int = 0
) : Identifiable {

    val remainingQuantity: Int
        get() = (quantity - receivedQuantity).coerceAtLeast(0)

    val isFullyReceived: Boolean
        get() = receivedQuantity >= quantity
}

enum class AdjustmentReason {
    STOCK_TAKE,
    DAMAGE,
    THEFT,
    RETURN,
    TRANSFER,
    CORRECTION,
    OTHER
}

enum class AlertType {
    LOW_STOCK,
    OUT_OF_STOCK,
    OVERSTOCK,
    EXPIRING_SOON
}

enum class StockAlertType {
    LOW_STOCK,
    OUT_OF_STOCK,
    OVERSTOCK,
    EXPIRING_SOON
}

enum class OrderStatus {
    DRAFT,
    PENDING,
    APPROVED,
    ORDERED,
    PARTIALLY_RECEIVED,
    RECEIVED,
    CANCELLED
}
