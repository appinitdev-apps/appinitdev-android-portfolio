class WebConsoleViewModel : ViewModel() {
    fun dig(context: Context) {
        viewModelScope.launch {
            DialogHelper.showLoading()
            val errorMessage = LocaleHelper.getStringAsync(Res.string.http_server_error)
            val serverUp = withIO { HttpServerManager.checkServerAsync() }
            DialogHelper.hideLoading()
            if (!serverUp) {
                AlertDialog.Builder(context)
                    .setTitle(LocaleHelper.getStringAsync(Res.string.error))
                    .setMessage(errorMessage)
                    .setPositiveButton(LocaleHelper.getStringAsync(Res.string.ok)) { _, _ -> }
                    .setNegativeButton(LocaleHelper.getStringAsync(Res.string.relaunch_app)) { _, _ -> AppHelper.relaunch(context) }
                    .create().show()
            } else {
                DialogHelper.showConfirmDialog(LocaleHelper.getStringAsync(Res.string.confirm), LocaleHelper.getStringAsync(Res.string.http_server_ok))
            }
        }
    }
    fun requestIgnoreBatteryOptimization() {
        if (!powerManager.isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID)) {
            sendEvent(IgnoreBatteryOptimizationEvent())
        }
    }
    fun enableKeepAwake(context: Context, enable: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            KeepAwakePreference.putAsync(enable)
            sendEvent(KeepAwakeChangedEvent(enable))
        }
    }
}
