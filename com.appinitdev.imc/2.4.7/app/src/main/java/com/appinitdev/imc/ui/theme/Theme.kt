
@Composable
fun IMCTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    preset: ThemePreset = ThemePreset.EMERALD_MINT,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val primaryColor = Color(preset.primaryColorHex)
    val containerColor = Color(preset.containerColorHex)

    val lightColorScheme = lightColorScheme(
        primary = primaryColor,
        onPrimary = Color.White,
        primaryContainer = containerColor,
        onPrimaryContainer = Color(0xFF0F172A),
        secondary = primaryColor,
        onSecondary = Color.White,
        background = BackgroundLight,
        onBackground = OnBackgroundLight,
        surface = SurfaceLight,
        onSurface = OnSurfaceLight,
        surfaceVariant = Color(0xFFEAF4F2),
        onSurfaceVariant = Color(0xFF334155)
    )

    val darkColorScheme = darkColorScheme(
        primary = primaryColor,
        onPrimary = Color.Black,
        primaryContainer = primaryColor.copy(alpha = 0.25f),
        onPrimaryContainer = Color.White,
        secondary = primaryColor,
        onSecondary = Color.Black,
        background = BackgroundDark,
        onBackground = OnBackgroundDark,
        surface = SurfaceDark,
        onSurface = OnSurfaceDark,
        surfaceVariant = Color(0xFF1E294B),
        onSurfaceVariant = Color(0xFFCBD5E1)
    )

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme
        else -> lightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
