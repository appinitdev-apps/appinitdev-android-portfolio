
class AllFilesPermissionDialog(
    val activity: BaseSimpleActivity, message: String = "", val callback: (result: Boolean) -> Unit, val neutralPressed: () -> Unit
) {
    private var dialog: AlertDialog? = null

    init {
        val view = activity.layoutInflater.inflate(org.appinitdev.commons.R.layout.dialog_message, null)
        view.findViewById<TextView>(R.id.message).text = message

        activity.getAlertDialogBuilder().setPositiveButton(R.string.media_only) { dialog, which ->  neutralPressed()  }
            /*.setNeutralButton(R.string.all_files) { dialog, which -> positivePressed() }*/
            .apply {
                activity.setupDialogStuff(view, this) { alertDialog ->
                    dialog = alertDialog
                }
            }
    }

    private fun positivePressed() {
        dialog?.dismiss()
        callback(true)
    }
}
