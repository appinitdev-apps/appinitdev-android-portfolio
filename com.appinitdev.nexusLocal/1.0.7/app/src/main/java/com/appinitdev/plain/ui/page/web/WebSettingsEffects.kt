
@Composable
internal fun WebSettingsEffects(
    permissionList: MutableState<List<PermissionItem>>,
    shouldIgnoreOptimize: MutableState<Boolean>,
    systemAlertWindow: MutableState<Boolean>,
    notificationListenerGranted: MutableState<Boolean>,
) {
    val context = LocalContext.current
    val sharedFlow = Channel.sharedFlow

    LaunchedEffect(sharedFlow) {
        sharedFlow.collect { event ->
            when (event) {
                is PermissionsResultEvent -> {
                    permissionList.value = Permissions.getWebList(context)
                    systemAlertWindow.value = Permission.SYSTEM_ALERT_WINDOW.can(context)
                    notificationListenerGranted.value = Permission.NOTIFICATION_LISTENER.can(context)
                    if (event.map[Permission.NOTIFICATION_LISTENER.toSysPermission()] == true) {
                        PNotificationListenerService.toggle(context, WebPreference.getAsync())
                    }
                }
                is WindowFocusChangedEvent -> {
                    shouldIgnoreOptimize.value = !powerManager.isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID)
                    notificationListenerGranted.value = Permission.NOTIFICATION_LISTENER.can(context)
                }
                is IgnoreBatteryOptimizationResultEvent -> {
                    if (shouldIgnoreOptimize.value) {
                        coIO {
                            DialogHelper.showLoading(); delay(1000); DialogHelper.hideLoading()
                            shouldIgnoreOptimize.value = !powerManager.isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID)
                        }
                    }
                }
            }
        }
    }
}

internal fun togglePermission(scope: CoroutineScope, context: android.content.Context, m: PermissionItem, enable: Boolean) {
    scope.launch {
        withIO { ApiPermissionsPreference.putAsync(m.permission, enable) }
        if (m.permission == Permission.NOTIFICATION_LISTENER) {
            val webEnabled = WebPreference.getAsync()
            PNotificationListenerService.toggle(context, enable && webEnabled)
        }
        if (enable) {
            val ps = m.permissions.filter { !it.can(context) }
            if (ps.isNotEmpty()) sendEvent(RequestPermissionsEvent(*ps.toTypedArray()))
        }
    }
}
