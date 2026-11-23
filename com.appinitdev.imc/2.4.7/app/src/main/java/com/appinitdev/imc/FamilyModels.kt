data class FamilyMember(
    val id: Long = System.currentTimeMillis(),
    val name: String,
    val birthTimestamp: Long,
    val gender: Gender,
    val relationship: String = "Hijo/a",
    val avatarDrawableResId: Int = R.drawable.uno
)
data class GrowthRecord(
    val id: Long = System.currentTimeMillis(),
    val memberId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val weightKg: Double,
    val heightCm: Double,
    val bmiValue: Double,
    val statusText: String
)
