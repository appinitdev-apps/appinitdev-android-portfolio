enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}
enum class ThemePreset(
    val title: String,
    val primaryColorHex: Long,
    val containerColorHex: Long
) {
    EMERALD_MINT("🌿 Esmeralda Menta", 0xFF00A88F, 0xFFD0F5F0),
    OCEAN_CYAN("🌊 Océano Turquesa", 0xFF0288D1, 0xFFE0F2FE),
    NEON_ROSE("🌸 Rosa Neón", 0xFFE91E63, 0xFFFCE4EC),
    ROYAL_PURPLE("🟣 Violeta Real", 0xFF8B5CF6, 0xFFF3E8FF),
    SUNSET_ORANGE("🔥 Naranja Atardecer", 0xFFFF6D00, 0xFFFFE0B2)
}
class ThemeManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_theme_prefs", Context.MODE_PRIVATE)
    fun getThemeMode(): ThemeMode {
        val name = prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        return try {
            ThemeMode.valueOf(name)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }
    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
    }
    fun getThemePreset(): ThemePreset {
        val name = prefs.getString("theme_preset", ThemePreset.EMERALD_MINT.name) ?: ThemePreset.EMERALD_MINT.name
        return try {
            ThemePreset.valueOf(name)
        } catch (e: Exception) {
            ThemePreset.EMERALD_MINT
        }
    }
    fun setThemePreset(preset: ThemePreset) {
        prefs.edit().putString("theme_preset", preset.name).apply()
    }
}
