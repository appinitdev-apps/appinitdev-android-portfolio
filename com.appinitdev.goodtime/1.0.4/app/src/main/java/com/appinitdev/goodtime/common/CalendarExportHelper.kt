
object CalendarExportHelper {

     


    fun addSessionToCalendar(context: Context, session: Session) {
        val startMs = session.timestamp
        val endMs = startMs + (session.duration * 60 * 1000)

        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMs)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMs)
            putExtra(CalendarContract.Events.TITLE, "Focus Session: ${session.label}")
            putExtra(
                CalendarContract.Events.DESCRIPTION,
                "Completed ${session.duration} minutes of focus time in Goodtime Productivity.\nNotes: ${session.notes}"
            )
            putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }
}
