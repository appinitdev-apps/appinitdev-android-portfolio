@Entity(tableName = "customer_transactions")
data class CustomerTransaction(
    @PrimaryKey
    override val id: String,
    val customerId: String,
    val transactionId: String,
    @ColumnInfo(name = "type")
    val type: TransactionType,
    val amount: BigDecimal,
    val balance: BigDecimal,
    val description: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
) : Identifiable {
    val isDeposit: Boolean
        get() = type == TransactionType.DEPOSIT || type == TransactionType.PAYMENT
    val isWithdrawal: Boolean
        get() = type == TransactionType.WITHDRAWAL || type == TransactionType.PURCHASE
}
