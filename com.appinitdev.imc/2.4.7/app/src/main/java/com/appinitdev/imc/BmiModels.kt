
enum class Gender {
    FEMALE, MALE
}

enum class UnitSystem {
    METRIC, // kg, cm
    IMPERIAL // lbs, inches
}

enum class BmiCategoryType(val labelResId: Int, val color: Color) {
    UNDERWEIGHT(R.string.riesgo_de_desnutrici_n, Color(0xFF0288D1)), // Blue
    NORMAL(R.string.normal1, Color(0xFF10B981)),                      // Emerald Green
    OVERWEIGHT(R.string.sobrepeso1, Color(0xFFF59E0B)),                  // Amber Yellow
    OBESITY_1(R.string.obesidad1, Color(0xFFF97316)),                   // Orange
    OBESITY_2(R.string.obesidad_ii1, Color(0xFFEF4444)),                // Coral Red
    OBESITY_3(R.string.obesidad_iii1, Color(0xFF8B5CF6))                 // Violet Purple
}

data class AgeCategory(
    val id: Int,
    val titleResId: Int,
    val drawableResId: Int,
    val ageRangeTextResId: Int,
    val descriptionResId: Int
)

data class HealthRecommendation(
    val hydrationTitle: String,
    val hydrationValue: String,
    val hydrationNote: String,
    val energyTitle: String,
    val energyValue: String,
    val energyNote: String
)

data class BmiResult(
    val pesoFormatted: String,
    val estaturaFormatted: String,
    val pesoKg: Double,
    val estaturaCm: Double,
    val categoryId: Int,
    val bmiType: Int, // 1 to 6
    val bmiValue: Double,
    val expectedWeightKg: Double,
    val minHealthyWeightKg: Double = 0.0,
    val maxHealthyWeightKg: Double = 0.0,
    val statusText: String,
    val heightStatusText: String = "",
    val referenceTable: DoubleArray = doubleArrayOf(),
    val healthRecommendation: HealthRecommendation
)

data class BmiRecord(
    val id: Long = System.currentTimeMillis(),
    val timestamp: Long = System.currentTimeMillis(),
    val categoryId: Int,
    val categoryName: String,
    val weightKg: Double,
    val heightCm: Double,
    val bmiValue: Double,
    val statusText: String,
    val gender: Gender
)
