class InsertFilenameDialog(
    val activity: BaseSimpleActivity, var path: String, val callback: (filename: String) -> Unit
) {
    init {
        val binding = DialogInsertFilenameBinding.inflate(activity.layoutInflater)
        activity.getAlertDialogBuilder()
            .setPositiveButton(C.string.ok, null)
            .setNegativeButton(C.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this, C.string.filename) { alertDialog ->
                    alertDialog.showKeyboard(binding.insertFilenameTitle)
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val filename = binding.insertFilenameTitle.value
                        val extension = binding.insertFilenameExtensionTitle.value
                        if (filename.isEmpty()) {
                            activity.toast(C.string.filename_cannot_be_empty)
                            return@setOnClickListener
                        }
                        var newFilename = filename
                        if (extension.isNotEmpty()) {
                            newFilename += ".$extension"
                        }
                        val newPath = "$path/$newFilename"
                        if (!newFilename.isAValidFilename()) {
                            activity.toast(C.string.filename_invalid_characters)
                            return@setOnClickListener
                        }
                        if (activity.getDoesFilePathExist(newPath)) {
                            val msg = String.format(activity.getString(C.string.file_already_exists), newFilename)
                            activity.toast(msg)
                        } else {
                            callback(newFilename)
                            alertDialog.dismiss()
                        }
                    }
                }
            }
    }
}
