
@Entity(tableName = "daily_sales")
data class DailySales(
    @PrimaryKey
    override val id: String,
    val date: LocalDateTime,
    override val storeId: String,
    val totalSales: BigDecimal,
    val totalTransactions: Int,
    val totalItems: Int,
    val averageTransactionValue: BigDecimal,
    val cashSales: BigDecimal,
    val cardSales: BigDecimal,
    val mobileSales: BigDecimal,
    val otherSales: BigDecimal,
    val refunds: BigDecimal,
    val discounts: BigDecimal,
    val taxes: BigDecimal,
    val createdAt: LocalDateTime = LocalDateTime.now()
) : Identifiable, StoreScoped {

    val netSales: BigDecimal
        get() = totalSales - refunds - discounts

    val averageItemsPerTransaction: Double
        get() = if (totalTransactions > 0) totalItems.toDouble() / totalTransactions else 0.0
}

@Entity(tableName = "product_performance")
data class ProductPerformance(
    @PrimaryKey
    override val id: String,
    val data: String,
    val productId: String,
    override val storeId: String,
    val period: String, // daily, weekly, monthly
    val periodStart: LocalDateTime,
    val periodEnd: LocalDateTime,
    val quantitySold: Int,
    val revenue: BigDecimal,
    val profit: BigDecimal,
    val averagePrice: BigDecimal,
    val rank: Int,
    val createdAt: LocalDateTime = LocalDateTime.now()
) : Identifiable, StoreScoped

@Entity(tableName = "customer_analytics")
data class CustomerAnalytics(
    @PrimaryKey
    override val id: String,
    val customerId: String,
    override val storeId: String,
    val period: String,
    val periodStart: LocalDateTime,
    val periodEnd: LocalDateTime,
    val totalSpent: BigDecimal,
    val totalTransactions: Int,
    val averageTransactionValue: BigDecimal,
    val lastPurchaseDate: LocalDateTime? = null,
    val riskScore: Int = 0,
    val loyaltyTier: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
) : Identifiable, StoreScoped

@Entity(tableName = "refund_analytics")
data class RefundAnalytics(
    @PrimaryKey
    override val id: String,
    override val storeId: String,
    val period: String,
    val periodStart: LocalDateTime,
    val periodEnd: LocalDateTime,
    val totalRefunds: BigDecimal,
    val refundCount: Int,
    val averageRefundAmount: BigDecimal,
    val topRefundReasons: String, // JSON string of reasons
    val refundRate: Double, // percentage
    val createdAt: LocalDateTime = LocalDateTime.now()
) : Identifiable, StoreScoped

@Entity(tableName = "sync_logs")
data class SyncLog(
    @PrimaryKey
    override val id: String,
    val syncType: String,
    val status: SyncStatus,
    val recordsProcessed: Int,
    val recordsFailed: Int,
    val errorMessage: String? = null,
    val startedAt: LocalDateTime,
    val completedAt: LocalDateTime? = null,
    override val storeId: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
) : Identifiable, StoreScoped {

    val isSuccess: Boolean
        get() = status == SyncStatus.COMPLETED

    val isFailed: Boolean
        get() = status == SyncStatus.FAILED
}

enum class SyncType {
    PRODUCTS,
    CUSTOMERS,
    TRANSACTIONS,
    INVENTORY,
    ANALYTICS,
    FULL_SYNC
}

enum class SyncStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    CANCELLED
}
