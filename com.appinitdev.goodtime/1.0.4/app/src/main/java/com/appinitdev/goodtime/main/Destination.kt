@Serializable
data object OnboardingDest
@Serializable
data object MainDest
val MainDest.route: String
    get() = "com.apps.adrcotfas.goodtime.main.MainDest"
@Serializable
data object LabelsDest
@Serializable
data class AddEditLabelDest(
    val name: String,
)
@Serializable
data object ArchivedLabelsDest
@Serializable
data object StatsDest
@Serializable
data object SettingsDest
@Serializable
data object UserInterfaceDest
@Serializable
data object TimerDurationsDest
@Serializable
data object NotificationSettingsDest
@Serializable
data object BackupDest
@Serializable
data object AboutDest
@Serializable
data object LicensesDest
@Serializable
data object AcknowledgementsDest
@Serializable
data object ProDest
