
object MediaShortcutHelper {
    fun addToDesktop(context: Context, path: String, label: String, iconRes: Int) {
        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            DialogHelper.showMessage(Res.string.shortcut_not_supported)
            return
        }
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            action = Constants.ACTION_PLAY_MEDIA
            putExtra(Constants.EXTRA_MEDIA_PATH, path)
            `package` = context.packageName
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val shortcutId = "media_${path.hashCode()}"
        val shortcut = ShortcutInfoCompat.Builder(context, shortcutId)
            .setShortLabel(label)
            .setLongLabel(label)
            .setIcon(IconCompat.createWithResource(context, iconRes))
            .setIntent(launchIntent)
            .build()

        
        
        val callbackIntent = Intent(context, MainActivity::class.java).apply {
            action = "${context.packageName}.action.SHORTCUT_PINNED"
            `package` = context.packageName
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val successCallback = PendingIntent.getActivity(
            context,
            shortcutId.hashCode(),
            callbackIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        ShortcutManagerCompat.requestPinShortcut(context, shortcut, successCallback.intentSender)
        
        
        DialogHelper.showMessage(Res.string.shortcut_added_to_home)
    }
}
