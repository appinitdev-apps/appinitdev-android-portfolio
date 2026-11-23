
// https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-savedstate#savedstate-compose-state
@OptIn(androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi::class)
class MainViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    var httpServerError by savedStateHandle.saveable { mutableStateOf("") }
    var httpServerState by savedStateHandle.saveable {
        mutableStateOf(HttpServerState.OFF)
    }
    var isVPNConnected by savedStateHandle.saveable { mutableStateOf(false) }
    var ip4s by savedStateHandle.saveable { mutableStateOf(emptyList<String>()) }
    var ip4 by savedStateHandle.saveable { mutableStateOf("") }
    var currentRootTab by savedStateHandle.saveable { mutableIntStateOf(0) }

    fun enableHttpServer(
        context: Context,
        enable: Boolean,
    ) {
        viewModelScope.launch {
            withIO { WebPreference.putAsync(enable) }
            if (enable) {
                httpServerError = ""
                if (!httpServerState.isProcessing() && httpServerState != HttpServerState.ON) {
                    httpServerState = HttpServerState.STARTING
                }
                val permission = Permission.POST_NOTIFICATIONS
                if (permission.can(context)) {
                    sendEvent(StartHttpServerEvent())
                } else {
                    DialogHelper.showConfirmDialog(
                        LocaleHelper.getStringAsync(Res.string.confirm),
                        LocaleHelper.getStringAsync(Res.string.foreground_service_notification_prompt)
                    ) {
                        coIO {
                            Permissions.ensureNotificationAsync(context)
                            while (!AppHelper.foregrounded()) {
                                LogCat.d("Waiting for foreground")
                                delay(800)
                            }
                            sendEvent(StartHttpServerEvent())
                        }
                    }
                }
            } else {
                withIO {
                    HttpServerManager.stopServiceAsync(context)
                }
            }
        }
    }

    fun syncHttpServerState(context: Context) {
        viewModelScope.launch {
            val webEnabled = withIO { WebPreference.getAsync() }
            if (!webEnabled) {
                if (!httpServerState.isProcessing()) {
                    httpServerState = HttpServerState.OFF
                }
                return@launch
            }

            if (httpServerState == HttpServerState.ERROR) {
                return@launch
            }

            if (!httpServerState.isProcessing() && httpServerState != HttpServerState.ON) {
                httpServerState = HttpServerState.STARTING
            }

            val serverUp = withIO { HttpServerManager.checkServerAsync() }
            if (serverUp) {
                httpServerError = ""
                httpServerState = HttpServerState.ON
            } else {
                enableHttpServer(context, true)
            }
        }
    }
}
