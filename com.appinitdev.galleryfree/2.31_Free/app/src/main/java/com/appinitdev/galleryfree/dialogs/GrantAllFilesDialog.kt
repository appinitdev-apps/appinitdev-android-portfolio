class GrantAllFilesDialog(val activity: BaseSimpleActivity) {
    init {
        val binding = DialogGrantAllFilesBinding.inflate(activity.layoutInflater)
        binding.grantAllFilesImage.applyColorFilter(activity.getProperTextColor())
        activity.getAlertDialogBuilder()
            .setPositiveButton(org.appinitdev.commons.R.string.ok) { dialog, which -> activity.launchGrantAllFilesIntent() }
            .setNegativeButton(org.appinitdev.commons.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this) { alertDialog -> }
            }
    }
}
