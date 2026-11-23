@Composable
fun LanguageSettingsButton() {
    val context = LocalContext.current
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        OutlinedButton(
            onClick = {
                try {
                    val intent = Intent(Settings.ACTION_APP_LOCALE_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
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
