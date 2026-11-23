object PrintHelper {
    fun printText(context: Context, textMeasurer: TextMeasurer, jobName: String, content: String) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        printManager.print(
            jobName,
            ComposeTextPrintAdapter(
                context = context,
                textMeasurer = textMeasurer,
                title = "",
                content = content
            ),
            PrintAttributes.Builder()
                .setMediaSize(
                    PrintAttributes.MediaSize.ISO_A4
                )
                .setColorMode(
                    PrintAttributes.COLOR_MODE_COLOR
                )
                .build()
        )
    }
}
