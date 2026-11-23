@Parcelize
data class Reminder(var id: Long, var dateTime: Date, var repetition: Repetition?) : Parcelable
@Parcelize data class Repetition(var value: Int, var unit: RepetitionTimeUnit) : Parcelable
enum class RepetitionTimeUnit {
    MINUTES,
    HOURS,
    DAYS,
    WEEKS,
    MONTHS,
    YEARS,
}
