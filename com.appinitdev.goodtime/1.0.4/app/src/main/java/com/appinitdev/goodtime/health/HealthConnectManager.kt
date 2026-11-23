
object HealthConnectManager {

    /**
     * Prepares an intent to insert/record a completed focus session as a Mindfulness activity.
     */
    fun createMindfulnessIntent(session: Session): Intent {
        val startMs = session.timestamp
        val endMs = startMs + (session.duration * 60 * 1000)

        return Intent("androidx.health.ACTION_INSERT_MINDFULNESS_SESSION").apply {
            putExtra("EXTRA_START_TIME", startMs)
            putExtra("EXTRA_END_TIME", endMs)
            putExtra("EXTRA_TITLE", "Goodtime Focus: ${session.label}")
            putExtra("EXTRA_NOTES", session.notes)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun recordMindfulnessSession(context: Context, session: Session): Boolean {
        try {
            val intent = createMindfulnessIntent(session)
            context.startActivity(intent)
            return true
        } catch (_: Exception) {
            return false
        }
    }
}
