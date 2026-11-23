object MmsHelper {
    fun launchDefaultSmsApp(
        to: String,
        body: String,
        attachments: List<Pair<String, String>>, 
    ) {
        val context = MainApp.instance
        val activity = MainActivity.instance.get()
            ?: throw IllegalStateException("Activity not available")
        val defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(context)
        val attachmentUris = attachments.map { (filePath, _) ->
            FileProvider.getUriForFile(context, Constants.AUTHORITY, File(filePath))
        }
        val mimeType = attachments.firstOrNull()?.second ?: "*/*"
        val intent = if (attachmentUris.size == 1) {
            Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, attachmentUris.first())
            }
        } else {
            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = mimeType
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(attachmentUris))
            }
        }
        intent.apply {
            `package` = defaultSmsPackage
            putExtra("address", to)
            putExtra("sms_body", body)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        for (uri in attachmentUris) {
            context.grantUriPermission(defaultSmsPackage, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        activity.startActivity(intent)
    }
}
