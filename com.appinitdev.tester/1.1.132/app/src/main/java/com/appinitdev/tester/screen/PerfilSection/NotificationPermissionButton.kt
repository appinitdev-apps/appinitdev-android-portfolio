@Composable
fun NotificationPermissionButton() {
    val context = LocalContext.current
    val activity = context as Activity
    var hasPermission by remember {
        mutableStateOf(notificationsEnabled(context))
    }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                hasPermission = notificationsEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }
    OutlinedButton(
        onClick = {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return@OutlinedButton
            when {
                hasPermission -> openAppNotificationSettings(context)
                ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) -> launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                else -> openAppNotificationSettings(context)
            }
        },
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .height(34.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (hasPermission)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.primary 
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
    ) {
        Icon(
            imageVector = if (hasPermission)
                Icons.Default.NotificationsActive
            else
                Icons.Default.NotificationsOff,
            contentDescription = null,
            modifier = Modifier.size(18.dp) 
        )
    }
}