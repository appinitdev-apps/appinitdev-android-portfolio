
@Composable
fun LanguageSettingsButton() {
    val context = LocalContext.current

    // Condicional de versión: Solo aparece en Android 13 (API 33) o más
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        OutlinedButton(
            onClick = {
                try {
                    val intent = Intent(Settings.ACTION_APP_LOCALE_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Fallback silencioso en caso de error de sistema
                }
            },
            modifier = Modifier.padding(horizontal = 2.dp).height(34.dp),
            shape = RoundedCornerShape(8.dp),

            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        ) {
            Icon(
                imageVector = Icons.Default.Translate,
                contentDescription = "Cambiar idioma",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
