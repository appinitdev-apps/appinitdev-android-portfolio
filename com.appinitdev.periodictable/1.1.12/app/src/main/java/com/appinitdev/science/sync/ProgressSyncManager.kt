
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
        val streakLastPlay: String? = null,  // ISO date string (yyyy-MM-dd)
        val notes: String? = null  // Element notes data
    )


    /**
     * Load cloud progress for uid and call callback. If document not present, callback with null.
     * Also attempts to read achievements (list of maps), statistics (map of id->progress), streak, and notes.
     */

    /**
     * Write progress and optionally achievements/statistics/streak/streakLastPlay/notes to cloud (merge). lastUpdated will be server timestamp.
     */


    /**
     * Merge strategy (improved):
     * - For xp: keep the larger xp (cloud or local).
     * - For achievements/statistics: per-item, take the maximum progress between local and cloud.
     * - For streak: take the maximum of local and cloud.
     *
     * After computing merged state, write it to cloud and then update local storage:
     * - XP/local level will be updated to mergedXp (only increase local if merged is greater).
     * - Achievements/statistics are increased locally to match merged values (never reduced).
     * - Streak is increased locally if merged is greater.
     */
    fun mergeAndUploadLocalProgress(context: Context, uid: String, onComplete: ((Boolean) -> Unit)? = null) {
        // read local xp/level
        val localXp = XpManager.getXp(context)
        val localLevel = XpManager.getLevel(localXp)

        // read local achievements & statistics using existing model getters
        val localAchievements = ArrayList<Achievement>()
        AchievementModel.getList(context, localAchievements)
        // Ensure each item's progress is loaded (in case getList doesn't)
        localAchievements.forEach { it.loadProgress(context) }

        val localStatistics = ArrayList<Statistics>()
        StatisticsModel.getList(context, localStatistics)
        localStatistics.forEach { it.loadProgress(context) }

        // read local streak and last play date
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

    /**
     * Merge notes from local and cloud sources.
     * Strategy: For each element, keep the longer content (more detailed notes).
     * Notes format: "<element_code>note text</element_code>"
     */
    private fun mergeNotes(localNotes: String, cloudNotes: String?): String {
        if (cloudNotes == null || cloudNotes.isEmpty()) {
            return localNotes
        }
        if (localNotes.isEmpty()) {
            return cloudNotes
        }

        // Parse both notes strings to extract element-specific notes
        val localElementNotes = parseElementNotes(localNotes)
        val cloudElementNotes = parseElementNotes(cloudNotes)

        // Merge: for each element, take the longer content
        val allElements = (localElementNotes.keys + cloudElementNotes.keys).toSet()
        val mergedElementNotes = mutableMapOf<String, String>()

        for (element in allElements) {
            val localNote = localElementNotes[element] ?: ""
            val cloudNote = cloudElementNotes[element] ?: ""
            
            // Take the longer note (more content means more user edits)
            mergedElementNotes[element] = if (localNote.length >= cloudNote.length) localNote else cloudNote
        }

        // Reconstruct the notes string
        return buildNotesString(mergedElementNotes)
    }

    /**
     * Parse notes string into a map of element code to note content.
     */
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

    /**
     * Build notes string from element notes map.
     */
    private fun buildNotesString(elementNotes: Map<String, String>): String {
        return elementNotes.entries
            .sortedBy { it.key }
            .joinToString("") { (element, content) ->
                "<$element>$content</$element>"
            }
    }
}