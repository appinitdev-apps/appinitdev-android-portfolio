
interface Identifiable {
    val id: String
}

interface Auditable {
    val createdAt: LocalDateTime
    val updatedAt: LocalDateTime
}

interface Syncable {
    val isSynced: Boolean
}

interface StoreScoped {
    val storeId: String?
}
