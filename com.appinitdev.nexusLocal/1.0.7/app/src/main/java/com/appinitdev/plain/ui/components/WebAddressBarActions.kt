fun persistMdnsHostname(
    context: Context,
    scope: CoroutineScope,
    hostname: String,
) {
    scope.launch {
        withIO { MdnsHostnamePreference.putAsync(hostname) }
    }
}
fun persistPort(
    context: Context,
    scope: CoroutineScope,
    isHttps: Boolean,
    port: Int,
) {
    scope.launch(Dispatchers.IO) {
        if (isHttps) {
            HttpsPortPreference.putAsync(port)
        } else {
            HttpPortPreference.putAsync(port)
        }
    }
}
fun showRestartAppDialog(context: Context) {
    androidx.appcompat.app.AlertDialog.Builder(context)
        .setTitle(LocaleHelper.getString(Res.string.restart_app_title))
        .setMessage(LocaleHelper.getString(Res.string.restart_app_message))
        .setPositiveButton(LocaleHelper.getString(Res.string.relaunch_app)) { _, _ ->
            AppHelper.relaunch(context)
        }
        .setCancelable(false)
        .create()
        .show()
}
