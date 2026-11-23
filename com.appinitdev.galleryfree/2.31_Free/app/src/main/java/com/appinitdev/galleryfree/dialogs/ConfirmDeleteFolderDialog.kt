
class ConfirmDeleteFolderDialog(activity: Activity, message: String, warningMessage: String, val callback: () -> Unit) {
    private var dialog: AlertDialog? = null

    init {
        val binding = DialogConfirmDeleteFolderBinding.inflate(activity.layoutInflater)
        binding.message.text = message
        binding.messageWarning.text = warningMessage

        activity.getAlertDialogBuilder()
            .setPositiveButton(org.appinitdev.commons.R.string.yes) { dialog, which -> dialogConfirmed() }
            .setNegativeButton(org.appinitdev.commons.R.string.no, null)
            .apply {
                activity.setupDialogStuff(binding.root, this) { alertDialog ->
                    dialog = alertDialog
                }
            }
    }

    private fun dialogConfirmed() {
        dialog?.dismiss()
        callback()
    }
}
