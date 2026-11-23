class StoragePermissionRequiredDialog(
    val activity: SimpleActivity,
    val onOkay: () -> Unit,
    val onCancel: () -> Unit,
    val callback: (dialog: AlertDialog) -> Unit
) {
    init {
        val binding = DialogStoragePermissionRequiredBinding.inflate(activity.layoutInflater)
        activity.getAlertDialogBuilder()
            .setPositiveButton(org.appinitdev.commons.R.string.go_to_settings) { dialog, _ ->
                dialog.dismiss()
                onOkay()
            }
            .setNegativeButton(org.appinitdev.commons.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
                onCancel()
            }
            .apply {
                activity.setupDialogStuff(
                    view = binding.root,
                    dialog = this,
                    cancelOnTouchOutside = false,
                    callback = callback
                )
            }
    }
}
