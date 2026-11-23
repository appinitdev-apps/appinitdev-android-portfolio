
@Entity(tableName = "stores")
data class Store(
    @PrimaryKey
    override val id: String,
    val name: String,
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String,
    val phone: String? = null,
    val email: String? = null,
    val website: String? = null,
    val taxId: String? = null,
    val currency: String = "USD",
    val timezone: String = "UTC",
    val isActive: Boolean = true,
    val settings: StoreSettings? = null,
    override val createdAt: LocalDateTime = LocalDateTime.now(),
    override val updatedAt: LocalDateTime = LocalDateTime.now()
) : Identifiable, Auditable

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    override val id: String,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: UserRole,
    override val storeId: String? = null,
    val isActive: Boolean = true,
    val lastLoginAt: LocalDateTime? = null,
    override val createdAt: LocalDateTime = LocalDateTime.now(),
    override val updatedAt: LocalDateTime = LocalDateTime.now()
) : Identifiable, Auditable, StoreScoped {

    val fullName: String
        get() = "$firstName $lastName".trim()

    val isAdmin: Boolean
        get() = role == UserRole.ADMIN

    val isManagerOrAdmin: Boolean
        get() = role == UserRole.ADMIN || role == UserRole.MANAGER

    val canProcessSales: Boolean
        get() = role == UserRole.ADMIN || role == UserRole.MANAGER || role == UserRole.CASHIER
}

@Entity(tableName = "user_permissions")
data class UserPermission(
    @PrimaryKey
    override val id: String,
    val userId: String,
    val permission: String,
    val isGranted: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now()
) : Identifiable

data class StoreSettings(
    val receiptHeader: String? = null,
    val receiptFooter: String? = null,
    val taxRate: Double = 0.0,
    val allowNegativeStock: Boolean = false,
    val requireManagerApproval: Boolean = false,
    val autoPrintReceipts: Boolean = true,
    val enableLoyaltyProgram: Boolean = false,
    val loyaltyPointsPerDollar: Double = 1.0,
    val lowStockThreshold: Int = 10,
    val enableOfflineMode: Boolean = true,
    val syncInterval: Int = 300 
)

enum class UserRole {
    ADMIN,
    MANAGER,
    CASHIER,
    VIEWER
}
