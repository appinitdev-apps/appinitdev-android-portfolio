@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey
    override val id: String,
    val firstName: String,
    val lastName: String,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val zipCode: String? = null,
    val country: String? = null,
    val dateOfBirth: LocalDateTime? = null,
    val loyaltyPoints: Int = 0,
    val totalSpent: BigDecimal = BigDecimal.ZERO,
    val balance: BigDecimal = BigDecimal.ZERO,
    val creditLimit: BigDecimal? = null,
    val isActive: Boolean = true,
    val riskScore: Int = 0,
    val notes: String? = null,
    override val createdAt: LocalDateTime = LocalDateTime.now(),
    override val updatedAt: LocalDateTime = LocalDateTime.now(),
    override val storeId: String? = null
) : Identifiable, Auditable, StoreScoped {
    val fullName: String
        get() = "$firstName $lastName".replace(Regex("\\s{2,}"), " ")
    val hasCreditLimit: Boolean
        get() = creditLimit != null && creditLimit > BigDecimal.ZERO
    val availableCredit: BigDecimal
        get() = (creditLimit ?: BigDecimal.ZERO) - balance
    val isHighRisk: Boolean
        get() = riskScore >= 7
    val hasPositiveBalance: Boolean
        get() = balance > BigDecimal.ZERO
    val hasDebt: Boolean
        get() = balance < BigDecimal.ZERO
    fun canAffordCreditSale(amount: BigDecimal): Boolean {
        if (!hasCreditLimit) return true 
        return availableCredit >= amount
    }
    fun calculateLoyaltyTier(): String {
        return when {
            loyaltyPoints >= 1000 -> "Gold"
            loyaltyPoints >= 500 -> "Silver"
            loyaltyPoints >= 100 -> "Bronze"
            else -> "Basic"
        }
    }
}
