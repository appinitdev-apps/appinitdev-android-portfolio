
@Keep
data class BugReport(
    val id: String = "",
    val appId: String = "",
    val userId: String = "",
    val userName: String = "",
    val deviceInfo: String = "", // Modelo, Versión Android
    val stepsToReproduce: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    val status: String = "Open", // Open, In Progress, Resolved, Closed
    val timestamp: Long = System.currentTimeMillis()
)

@Keep
data class Survey(
    val id: String = "",
    val appId: String = "",
    val title: String = "",
    val questions: List<String> = emptyList(),
    val isActive: Boolean = true
)

@Keep
data class SurveyResponse(
    val id: String = "",
    val surveyId: String = "",
    val userId: String = "",
    val answers: Map<Int, String> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)

@Keep
data class Achievement(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val iconResId: Int = 0,
    val pointsReward: Int = 0
)

@Keep
data class UserNotification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val body: String = "",
    val type: String = "general", // general, bug_update, new_app, survey, points
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val data: Map<String, String> = emptyMap(),
    val appId: String? = null
)

@Keep
data class FeedbackReport14Days(
    val id: String = "",
    val appId: String = "",
    val userId: String = "",
    val userName: String = "",
    val ratingEstabilidad: Float = 5f,
    val ratingDiseno: Float = 5f,
    val modeloDispositivo: String = "",
    val versionAndroid: String = "",
    val comentarios: String = "",
    val sugerencias: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Keep
data class ChecklistItem(
    val id: String = "",
    val task: String = "",
    val isCompleted: Boolean = false
)

@Keep
data class PointsTransaction(
    val id: String = "",
    val userId: String = "",
    val amount: Int = 0,
    val reason: String = "", // Ejemplo: "Check-in diario", "Referido", "Boost App"
    val timestamp: Long = System.currentTimeMillis()
)

@Keep
data class UserReport(
    val id: String = "",
    val reporterId: String = "",
    val reportedId: String = "", // ID del usuario o de la App
    val type: String = "app", // app, user, message
    val reason: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
