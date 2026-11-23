object ProgressSyncManager {
    private const val TAG = "ProgressSyncManager"
    data class CloudAchievement(val id: Int = 0, val progress: Int = 0, val maxProgress: Int? = null)
    data class CloudProgress(
        val xp: Long = 0L,
        val level: Int = 1,
        val lastUpdated: Timestamp? = null,
        val achievements: List<CloudAchievement>? = null,
        val statistics: Map<Int, Long>? = null,
        val streak: Long? = null,
        val streakLastPlay: String? = null,  
        val notes: String? = null  
    )
    fun mergeAndUploadLocalProgress(context: Context, uid: String, onComplete: ((Boolean) -> Unit)? = null) {
        val localXp = XpManager.getXp(context)
        val localLevel = XpManager.getLevel(localXp)
        val localAchievements = ArrayList<Achievement>()
        AchievementModel.getList(context, localAchievements)
        localAchievements.forEach { it.loadProgress(context) }
        val localStatistics = ArrayList<Statistics>()
        StatisticsModel.getList(context, localStatistics)
        localStatistics.forEach { it.loadProgress(context) }
        val localStreak = try {
            StreakManager.getCurrentStreak(context)
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to read local streak", t)
            0
        }
        val localLastPlayDate = try {
            StreakManager.getLastPlayDate(context)
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to read local last play date", t)
            null
        }
    }
    private fun mergeNotes(localNotes: String, cloudNotes: String?): String {
        if (cloudNotes == null || cloudNotes.isEmpty()) {
            return localNotes
        }
        if (localNotes.isEmpty()) {
            return cloudNotes
        }
        val localElementNotes = parseElementNotes(localNotes)
        val cloudElementNotes = parseElementNotes(cloudNotes)
        val allElements = (localElementNotes.keys + cloudElementNotes.keys).toSet()
        val mergedElementNotes = mutableMapOf<String, String>()
        for (element in allElements) {
            val localNote = localElementNotes[element] ?: ""
            val cloudNote = cloudElementNotes[element] ?: ""
            mergedElementNotes[element] = if (localNote.length >= cloudNote.length) localNote else cloudNote
        }
        return buildNotesString(mergedElementNotes)
    }
    private fun parseElementNotes(notes: String): Map<String, String> {
        val elementNotes = mutableMapOf<String, String>()
        val regex = Regex("<([^>]+)>(.*?)</\\1>", RegexOption.DOT_MATCHES_ALL)
        regex.findAll(notes).forEach { match ->
            val elementCode = match.groups[1]?.value ?: return@forEach
            val noteContent = match.groups[2]?.value ?: ""
            elementNotes[elementCode] = noteContent
        }
        return elementNotes
    }
    private fun buildNotesString(elementNotes: Map<String, String>): String {
        return elementNotes.entries
            .sortedBy { it.key }
            .joinToString("") { (element, content) ->
                "<$element>$content</$element>"
            }
    }
}