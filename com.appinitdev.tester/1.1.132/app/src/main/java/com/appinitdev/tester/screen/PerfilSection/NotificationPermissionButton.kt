
@Composable
fun NotificationPermissionButton() {
    val context = LocalContext.current
    val activity = context as Activity

    // 1. Creamos un estado que Compose pueda "observar"
    var hasPermission by remember {
        mutableStateOf(notificationsEnabled(context))
    }

    // 2. Observer para actualizar el estado cuando el usuario vuelve a la app
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            // Si la app vuelve al primer plano (ON_RESUME), verificamos el permiso
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
        // 3. Actualizamos el estado inmediatamente después de la respuesta del sistema
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
        // Aplicamos los colores dinámicos aquí
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (hasPermission)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.primary // Color del texto/icono
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
            modifier = Modifier.size(18.dp) // Un tamaño ligeramente menor para que quepa bien en 34.dp
        )

//        Spacer(Modifier.width(8.dp))
//        Text(
//            color = MaterialTheme.colorScheme.primary,
//            text = if (hasPermission) stringResource(R.string.notificaciones_activas) else stringResource(
//                R.string.activar_notificaciones
//            )
//        )
    }
}