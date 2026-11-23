
// 1. Pitch Black OLED Scheme
private val OledColorScheme = darkColorScheme(
    primary = Color(0xFF60A5FA),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF1E293B),
    onPrimaryContainer = Color(0xFF93C5FD),
    secondary = Color(0xFF388BFD),
    background = Color(0xFF000000),
    surface = Color(0xFF0D1117),
    surfaceVariant = Color(0xFF161B22),
    onBackground = Color(0xFFE6EDF3),
    onSurface = Color(0xFFE6EDF3)
)

// 2. Electric Blue Scheme
private val ElectricBlueColorScheme = darkColorScheme(
    primary = Color(0xFF3B82F6),
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFBFDBFE),
    secondary = Color(0xFF60A5FA),
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    surfaceVariant = Color(0xFF334155)
)

// 3. Emerald Green Scheme
private val EmeraldGreenColorScheme = darkColorScheme(
    primary = Color(0xFF10B981),
    primaryContainer = Color(0xFF064E3B),
    onPrimaryContainer = Color(0xFFA7F3D0),
    secondary = Color(0xFF34D399),
    background = Color(0xFF022C22),
    surface = Color(0xFF064E3B),
    surfaceVariant = Color(0xFF047857)
)

// 4. Deep Purple Scheme
private val DeepPurpleColorScheme = darkColorScheme(
    primary = Color(0xFF8B5CF6),
    primaryContainer = Color(0xFF4C1D95),
    onPrimaryContainer = Color(0xFFDDD6FE),
    secondary = Color(0xFFA78BFA),
    background = Color(0xFF2E1065),
    surface = Color(0xFF3B0764),
    surfaceVariant = Color(0xFF581C87)
)

// 5. Flame Orange Scheme
private val FlameOrangeColorScheme = darkColorScheme(
    primary = Color(0xFFF59E0B),
    primaryContainer = Color(0xFF78350F),
    onPrimaryContainer = Color(0xFFFDE68A),
    secondary = Color(0xFFFBBF24),
    background = Color(0xFF1C1917),
    surface = Color(0xFF292524),
    surfaceVariant = Color(0xFF44403C)
)

// 6. Crimson Red Scheme
private val CrimsonRedColorScheme = darkColorScheme(
    primary = Color(0xFFEF4444),
    primaryContainer = Color(0xFF7F1D1D),
    onPrimaryContainer = Color(0xFFFECACA),
    secondary = Color(0xFFF87171),
    background = Color(0xFF180808),
    surface = Color(0xFF2D0E0E),
    surfaceVariant = Color(0xFF450A0A)
)

// 7. Cyberpunk Pink Scheme
private val CyberpunkPinkColorScheme = darkColorScheme(
    primary = Color(0xFFEC4899),
    primaryContainer = Color(0xFF831843),
    onPrimaryContainer = Color(0xFFFBCFE8),
    secondary = Color(0xFFF472B6),
    background = Color(0xFF1A0A16),
    surface = Color(0xFF2D0E25),
    surfaceVariant = Color(0xFF500724)
)

// 8. Cyan Teal Scheme
private val CyanTealColorScheme = darkColorScheme(
    primary = Color(0xFF06B6D4),
    primaryContainer = Color(0xFF134E4A),
    onPrimaryContainer = Color(0xFFA5F3FC),
    secondary = Color(0xFF22D3EE),
    background = Color(0xFF041C1E),
    surface = Color(0xFF082F32),
    surfaceVariant = Color(0xFF0E4A4E)
)

// 9. Solar Gold Scheme
private val SolarGoldColorScheme = darkColorScheme(
    primary = Color(0xEAEAB308),
    primaryContainer = Color(0xFF713F12),
    onPrimaryContainer = Color(0xFFFEF08A),
    secondary = Color(0xFFFACC15),
    background = Color(0xFF1A170A),
    surface = Color(0xFF2A230E),
    surfaceVariant = Color(0xFF42320D)
)

// 10. Forest Green Scheme
private val ForestGreenColorScheme = darkColorScheme(
    primary = Color(0xFF22C55E),
    primaryContainer = Color(0xFF14532D),
    onPrimaryContainer = Color(0xFFBBF7D0),
    secondary = Color(0xFF4ADE80),
    background = Color(0xFF051C0D),
    surface = Color(0xFF0A2E16),
    surfaceVariant = Color(0xFF114723)
)

// 11. Vintage Sepia Scheme
private val VintageSepiaColorScheme = lightColorScheme(
    primary = Color(0xFF8B4513),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF5DEB3),
    onPrimaryContainer = Color(0xFF3E1F07),
    secondary = Color(0xFFA0522D),
    background = Color(0xFFFAF0E6),
    surface = Color(0xFFFFF8DC),
    surfaceVariant = Color(0xFFEEE8AA),
    onBackground = Color(0xFF291800),
    onSurface = Color(0xFF291800)
)

private val StandardDarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val StandardLightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun MathAppTheme(
    themeMode: String = "SYSTEM",
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        "OLED" -> OledColorScheme
        "BLUE" -> ElectricBlueColorScheme
        "GREEN" -> EmeraldGreenColorScheme
        "PURPLE" -> DeepPurpleColorScheme
        "ORANGE" -> FlameOrangeColorScheme
        "RED" -> CrimsonRedColorScheme
        "PINK" -> CyberpunkPinkColorScheme
        "CYAN" -> CyanTealColorScheme
        "GOLD" -> SolarGoldColorScheme
        "FOREST" -> ForestGreenColorScheme
        "SEPIA" -> VintageSepiaColorScheme
        else -> {
            if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (darkTheme) StandardDarkColorScheme else StandardLightColorScheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
