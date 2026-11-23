@Composable
fun SolicitarPermisoNotificaciones() {
    val context = LocalContext.current
    val notLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
        } else {
        }
    }
    LaunchedEffect(key1 = true) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}