fun showProUpgradeDialog(context: Context) {
    val message = buildString {
        appendLine("• ${context.getString(C.string.why_upgrade_no_ads)}")
        appendLine("• ${context.getString(C.string.why_upgrade_no_internet_access)}")
        appendLine("• ${context.getString(C.string.why_upgrade_improved_design)}")
        appendLine("• ${context.getString(C.string.why_upgrade_many_other_improvements)}")
        appendLine()
        appendLine(context.getString(C.string.why_upgrade_footer))
        appendLine(context.getString(C.string.why_upgrade_footer_one_time_payment))
    }
    AlertDialog.Builder(context)
        .setTitle(context.getString(C.string.why_upgrade))
        .setMessage(message)
        .setPositiveButton(context.getString(C.string.upgrade_to_pro)) { dialogInterface, _ ->
            try {
                val intent = context.packageManager.getLaunchIntentForPackage("com.appinitdev.fileExplorer")
                if (intent != null) {
                    context.startActivity(intent)
                } else {
                    val playIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=com.appinitdev.fileExplorer")
                    )
                    context.startActivity(playIntent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            dialogInterface.dismiss()
        }
        .setNegativeButton(context.getString(C.string.close)) { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        .show()
}
