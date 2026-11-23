private val DarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color.Black,
    primaryContainer = CardDark,
    onPrimaryContainer = TextPrimary,
    secondary = NeonPurple,
    onSecondary = Color.White,
    tertiary = BrightAmber,
    onTertiary = Color.Black,
    background = DarkObsidian,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = CardDark,
    onSurfaceVariant = TextSecondary
)
@Composable
fun VideoToPhotoTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
