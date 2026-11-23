
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey
    override val id: String,
    val transactionNumber: String,
    val customerId: String? = null,
    val cashierId: String,
    val storeId: String,
    val type: TransactionType,
    val status: TransactionStatus,
    val subtotal: BigDecimal,
    val taxAmount: BigDecimal,
    val discountAmount: BigDecimal = BigDecimal.ZERO,
    val totalAmount: BigDecimal,
    val paidAmount: BigDecimal = BigDecimal.ZERO,
    val changeAmount: BigDecimal = BigDecimal.ZERO,
    val paymentMethod: PaymentMethod? = null,
    val notes: String? = null,
    val isRefund: Boolean = false,
    val originalTransactionId: String? = null,
    val refundAmount: BigDecimal? = null,
    val refundReason: String? = null,
    val managerApprovalId: String? = null,
    override val isSynced: Boolean = false,
    override val createdAt: LocalDateTime = LocalDateTime.now(),
    override val updatedAt: LocalDateTime = LocalDateTime.now(),
    val completedAt: LocalDateTime? = null
) : Identifiable, Auditable, Syncable {

    val isCompleted: Boolean
        get() = status == TransactionStatus.COMPLETED

    val isPending: Boolean
        get() = status == TransactionStatus.PENDING

    val isCancelled: Boolean
        get() = status == TransactionStatus.CANCELLED

    val isRefunded: Boolean
        get() = status == TransactionStatus.REFUNDED || status == TransactionStatus.PARTIALLY_REFUNDED

    val canBeRefunded: Boolean
        get() = isCompleted && !isRefund

    val hasDiscount: Boolean
        get() = discountAmount > BigDecimal.ZERO
}

@Entity(tableName = "transaction_items")
data class TransactionItem(
    @PrimaryKey
    override val id: String,
    val transactionId: String,
    val productId: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val totalPrice: BigDecimal,
    val discountAmount: BigDecimal = BigDecimal.ZERO,
    val taxAmount: BigDecimal = BigDecimal.ZERO,
    val notes: String? = null
) : Identifiable {

    val itemTotalWithTax: BigDecimal
        get() = totalPrice + taxAmount

    val unitPriceWithTax: BigDecimal
        get() = if (quantity > 0) itemTotalWithTax.divide(BigDecimal(quantity), 2, RoundingMode.HALF_UP) else unitPrice
}

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey
    override val id: String,
    val transactionId: String,
    val amount: BigDecimal,
    val method: PaymentMethod,
    val reference: String? = null,
    val status: PaymentStatus,
    val processedAt: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
) : Identifiable {

    val isCompleted: Boolean
        get() = status == PaymentStatus.COMPLETED

    val isFailed: Boolean
        get() = status == PaymentStatus.FAILED
}

enum class TransactionType {
    SALE,
    REFUND,
    RETURN,
    EXCHANGE,
    VOID,
    PURCHASE,
    DEPOSIT,
    WITHDRAWAL,
    PAYMENT,
    ADJUSTMENT
}

enum class TransactionStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    REFUNDED,
    PARTIALLY_REFUNDED
}

enum class PaymentMethod {
    CASH,
    CARD,
    MOBILE_PAYMENT,
    STORE_CREDIT,
    CHECK,
    BANK_TRANSFER,
    OTHER,
    CREDIT_SALE
}

enum class PaymentStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED,
    REFUNDED
}
