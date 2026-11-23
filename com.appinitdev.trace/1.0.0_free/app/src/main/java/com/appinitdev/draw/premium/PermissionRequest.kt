@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PermissionRequest(
    onPermissionGranted: () -> Unit
) {
    val cameraPermission = Manifest.permission.CAMERA
    val storagePermission = Manifest.permission.READ_EXTERNAL_STORAGE
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[cameraPermission] == true &&
            permissions[storagePermission] == true) {
            onPermissionGranted()
        } else {
            Toast.makeText(context,
                context.getString(R.string.permisos_requeridos), Toast.LENGTH_SHORT).show()
        }
    }
    LaunchedEffect(Unit) {
        launcher.launch(arrayOf(cameraPermission, storagePermission))
    }
}
