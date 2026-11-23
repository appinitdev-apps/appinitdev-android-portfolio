
enum class InterruptionCategory {
    SOCIAL_MEDIA,
    WORK_CALL,
    FATIGUE,
    ENVIRONMENT_NOISE,
    OTHER
}

data class InterruptionLog(
    val timestamp: Long,
    val category: InterruptionCategory,
    val note: String,
    val sessionLabel: String
)

class InterruptionReasonTracker(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("goodtime_interruptions", Context.MODE_PRIVATE)

    fun logInterruption(category: InterruptionCategory, note: String, sessionLabel: String) {
        val list = getLogs().toMutableList()
        list.add(
            InterruptionLog(
                timestamp = System.currentTimeMillis(),
                category = category,
                note = note,
                sessionLabel = sessionLabel
            )
        )
        saveLogs(list)
    }

    fun getLogs(): List<InterruptionLog> {
        val raw = prefs.getString("logs_json", null) ?: return emptyList()
        val result = mutableListOf<InterruptionLog>()
        try {
            val array = JSONArray(raw)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                result.add(
                    InterruptionLog(
                        timestamp = obj.getLong("timestamp"),
                        category = InterruptionCategory.valueOf(obj.getString("category")),
                        note = obj.optString("note", ""),
                        sessionLabel = obj.optString("sessionLabel", "default")
                    )
                )
            }
        } catch (_: Exception) {
        }
        return result
    }

    private fun saveLogs(logs: List<InterruptionLog>) {
        try {
            val array = JSONArray()
            for (item in logs) {
                val obj = JSONObject().apply {
                    put("timestamp", item.timestamp)
                    put("category", item.category.name)
                    put("note", item.note)
                    put("sessionLabel", item.sessionLabel)
                }
                array.put(obj)
            }
            prefs.edit().putString("logs_json", array.toString()).apply()
        } catch (_: Exception) {
        }
    }
}
