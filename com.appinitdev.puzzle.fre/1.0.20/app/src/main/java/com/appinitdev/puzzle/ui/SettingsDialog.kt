data class AppSettings(
    val isSoundEnabled: Boolean = true,
    val isHapticEnabled: Boolean = true,
    val isHighlightCorrectEnabled: Boolean = true,
    val isTimeAttackEnabled: Boolean = false,
    val timeAttackLimitSeconds: Int = 120,
    val boardSkin: String = "Clásico",
    val tileStyle: String = "Relieve 3D",
    val isAccessibilityPatternEnabled: Boolean = false,
    val customBackgroundPath: String? = null
)
data class BoardSkinStyle(
    val name: String,
    val containerColor: Color,
    val textColor: Color,
    val borderColor: Color
)
fun getBoardSkinStyle(skinName: String): BoardSkinStyle {
    return when (skinName) {
        "Madera" -> BoardSkinStyle(
            name = "Madera",
            containerColor = Color(0xFFD7CCC8),
            textColor = Color(0xFF3E2723),
            borderColor = Color(0xFF8D6E63)
        )
        "Neón" -> BoardSkinStyle(
            name = "Neón",
            containerColor = Color(0xFF12005E),
            textColor = Color(0xFF00E5FF),
            borderColor = Color(0xFFD500F9)
        )
        "OLED" -> BoardSkinStyle(
            name = "OLED",
            containerColor = Color(0xFF121212),
            textColor = Color(0xFFEEEEEE),
            borderColor = Color(0xFF424242)
        )
        else -> BoardSkinStyle(
            name = "Clásico",
            containerColor = Color(0xFFE1F5FE),
            textColor = Color(0xFF0277BD),
            borderColor = Color(0xFF81D4FA)
        )
    }
}
val ACCESSIBILITY_PATTERNS = listOf("▲", "●", "■", "★", "✦", "⬢", "✚", "♦", "▼", "◆", "▲", "●", "■", "★", "✦", "⬢")
fun loadAppSettings(context: Context): AppSettings {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    return AppSettings(
        isSoundEnabled = prefs.getBoolean("sound_enabled", true),
        isHapticEnabled = prefs.getBoolean("haptic_enabled", true),
        isHighlightCorrectEnabled = prefs.getBoolean("highlight_correct_enabled", true),
        isTimeAttackEnabled = prefs.getBoolean("time_attack_enabled", false),
        timeAttackLimitSeconds = prefs.getInt("time_attack_limit", 120),
        boardSkin = prefs.getString("board_skin", "Clásico") ?: "Clásico",
        tileStyle = prefs.getString("tile_style", "Relieve 3D") ?: "Relieve 3D",
        isAccessibilityPatternEnabled = prefs.getBoolean("accessibility_pattern_enabled", false),
        customBackgroundPath = prefs.getString("custom_bg_path", null)
    )
}
fun saveAppSettings(context: Context, settings: AppSettings) {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    prefs.edit()
        .putBoolean("sound_enabled", settings.isSoundEnabled)
        .putBoolean("haptic_enabled", settings.isHapticEnabled)
        .putBoolean("highlight_correct_enabled", settings.isHighlightCorrectEnabled)
        .putBoolean("time_attack_enabled", settings.isTimeAttackEnabled)
        .putInt("time_attack_limit", settings.timeAttackLimitSeconds)
        .putString("board_skin", settings.boardSkin)
        .putString("tile_style", settings.tileStyle)
        .putBoolean("accessibility_pattern_enabled", settings.isAccessibilityPatternEnabled)
        .putString("custom_bg_path", settings.customBackgroundPath)
        .apply()
    SoundEffectsManager.isSoundEnabled = settings.isSoundEnabled
}
@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    onSettingsChanged: (AppSettings) -> Unit
) {
    val context = LocalContext.current
    var settings by remember { mutableStateOf(loadAppSettings(context)) }
    val skinOptions = listOf("Clásico", "Madera", "Neón", "OLED")
    val tileStyleOptions = listOf("Relieve 3D", "Soft Round", "Marco Foto", "Plano")
    val timeOptions = listOf(60, 90, 120, 180)
    val bgImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val bitmap = if (android.os.Build.VERSION.SDK_INT < 28) {
                @Suppress("DEPRECATION")
                android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            } else {
                val source = android.graphics.ImageDecoder.createSource(context.contentResolver, it)
                android.graphics.ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.isMutableRequired = true
                }
            }
            val path = saveBitmapToInternalStorage(context, bitmap)
            settings = settings.copy(customBackgroundPath = path)
            saveAppSettings(context, settings)
            onSettingsChanged(settings)
        }
    }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 10.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.ajustes_de_juego),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.tema_de_colores), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(skinOptions) { skin ->
                                    FilterChip(
                                        selected = settings.boardSkin == skin,
                                        onClick = {
                                            settings = settings.copy(boardSkin = skin)
                                            saveAppSettings(context, settings)
                                            onSettingsChanged(settings)
                                        },
                                        label = { Text(skin) },
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.ViewInAr,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.forma_y_relieve_3d), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(tileStyleOptions) { style ->
                                    FilterChip(
                                        selected = settings.tileStyle == style,
                                        onClick = {
                                            settings = settings.copy(tileStyle = style)
                                            saveAppSettings(context, settings)
                                            onSettingsChanged(settings)
                                        },
                                        label = { Text(style) },
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }
                        }
                    }
                    SettingSwitchRow(
                        title = stringResource(R.string.modo_accesibilidad),
                        subtitle = stringResource(R.string.simbolos_de_alto_contraste),
                        icon = Icons.Default.AccessibilityNew,
                        checked = settings.isAccessibilityPatternEnabled,
                        onCheckedChange = {
                            settings = settings.copy(isAccessibilityPatternEnabled = it)
                            saveAppSettings(context, settings)
                            onSettingsChanged(settings)
                        }
                    )
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Image,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.fondo_de_la_app), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { bgImageLauncher.launch("image/*") },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(stringResource(R.string.elegir_de_galeria), style = MaterialTheme.typography.labelMedium)
                                }
                                if (settings.customBackgroundPath != null) {
                                    OutlinedButton(
                                        onClick = {
                                            settings = settings.copy(customBackgroundPath = null)
                                            saveAppSettings(context, settings)
                                            onSettingsChanged(settings)
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(stringResource(R.string.quitar), style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }
                    SettingSwitchRow(
                        title = stringResource(R.string.efectos_de_sonido),
                        subtitle = stringResource(R.string.audio_al_mover_mezclar),
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        checked = settings.isSoundEnabled,
                        onCheckedChange = {
                            settings = settings.copy(isSoundEnabled = it)
                            saveAppSettings(context, settings)
                            onSettingsChanged(settings)
                        }
                    )
                    SettingSwitchRow(
                        title = stringResource(R.string.vibracion_haptica),
                        subtitle = stringResource(R.string.respuesta_al_tacto),
                        icon = Icons.Default.Vibration,
                        checked = settings.isHapticEnabled,
                        onCheckedChange = {
                            settings = settings.copy(isHapticEnabled = it)
                            saveAppSettings(context, settings)
                            onSettingsChanged(settings)
                        }
                    )
                    SettingSwitchRow(
                        title = stringResource(R.string.resaltar_piezas_correctas),
                        subtitle = stringResource(R.string.borde_verde_en_piezas),
                        icon = Icons.Default.CheckCircle,
                        checked = settings.isHighlightCorrectEnabled,
                        onCheckedChange = {
                            settings = settings.copy(isHighlightCorrectEnabled = it)
                            saveAppSettings(context, settings)
                            onSettingsChanged(settings)
                        }
                    )
                    SettingSwitchRow(
                        title = stringResource(R.string.modo_contrarreloj),
                        subtitle = stringResource(R.string.reloj_en_cuenta_regresiva),
                        icon = Icons.Default.Speed,
                        checked = settings.isTimeAttackEnabled,
                        onCheckedChange = {
                            settings = settings.copy(isTimeAttackEnabled = it)
                            saveAppSettings(context, settings)
                            onSettingsChanged(settings)
                        }
                    )
                    if (settings.isTimeAttackEnabled) {
                        Column(modifier = Modifier.padding(start = 12.dp, top = 4.dp)) {
                            Text(
                                text = "Tiempo Límite: ${settings.timeAttackLimitSeconds}s",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                timeOptions.forEach { seconds ->
                                    FilterChip(
                                        selected = settings.timeAttackLimitSeconds == seconds,
                                        onClick = {
                                            settings = settings.copy(timeAttackLimitSeconds = seconds)
                                            saveAppSettings(context, settings)
                                            onSettingsChanged(settings)
                                        },
                                        label = { Text("${seconds}s") },
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onDismiss,
                    shape = CircleShape,
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text(stringResource(R.string.cerrar), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
@Composable
fun SettingSwitchRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}
