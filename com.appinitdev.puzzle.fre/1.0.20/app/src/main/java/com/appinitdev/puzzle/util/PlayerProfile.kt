
data class PlayerProfile(
    val xp: Int,
    val level: Int,
    val rankTitle: String,
    val xpInCurrentLevel: Int,
    val xpForNextLevel: Int,
    val progressRatio: Float
)

object PlayerProfileManager {

    fun getXp(context: Context): Int {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getInt("player_xp", 0)
    }

    fun addXp(context: Context, earnedXp: Int) {
        val currentXp = getXp(context)
        val newXp = currentXp + earnedXp
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .edit()
            .putInt("player_xp", newXp)
            .apply()
    }

    fun getProfile(context: Context): PlayerProfile {
        val xp = getXp(context)
        val level = (xp / 250) + 1
        val xpInCurrentLevel = xp % 250
        val xpForNextLevel = 250
        val progressRatio = xpInCurrentLevel.toFloat() / xpForNextLevel.toFloat()

        val rankTitle = when {
            level >= 10 -> "👑 Leyenda Suprema"
            level >= 6 -> "🥇 Gran Maestro"
            level >= 3 -> "🥈 Estratega"
            else -> "🥉 Principiante"
        }

        return PlayerProfile(
            xp = xp,
            level = level,
            rankTitle = rankTitle,
            xpInCurrentLevel = xpInCurrentLevel,
            xpForNextLevel = xpForNextLevel,
            progressRatio = progressRatio.coerceIn(0f, 1f)
        )
    }
}
