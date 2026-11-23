class DeleteWithRememberDialog(
    private val activity: Activity,
    private val message: String,
    private val showSkipRecycleBinOption: Boolean,
    private val callback: (remember: Boolean, skipRecycleBin: Boolean) -> Unit
) {
    private var dialog: AlertDialog? = null
    private val binding = DialogDeleteWithRememberBinding.inflate(activity.layoutInflater)
    init {
        binding.deleteRememberTitle.text = message
        binding.skipTheRecycleBinCheckbox.beGoneIf(!showSkipRecycleBinOption)
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
        callback(binding.deleteRememberCheckbox.isChecked, binding.skipTheRecycleBinCheckbox.isChecked)
    }
}
