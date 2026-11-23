
class PdfDocumentAdapter(
    private val context: Context,
    private val file: File
) : PrintDocumentAdapter() {

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: android.os.CancellationSignal?,
        layoutResultCallback: LayoutResultCallback?,
        extras: android.os.Bundle?
    ) {
        if (cancellationSignal?.isCanceled == true) {
            layoutResultCallback?.onLayoutCancelled()
            return
        }

        val info = PrintDocumentInfo.Builder(file.name)
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .build()
        layoutResultCallback?.onLayoutFinished(info, true)
    }

    override fun onWrite(
        pages: Array<out android.print.PageRange>?,
        destination: ParcelFileDescriptor?,
        cancellationSignal: android.os.CancellationSignal?,
        writeResultCallback: WriteResultCallback?
    ) {
        try {
            FileInputStream(file).use { input ->
                FileOutputStream(destination?.fileDescriptor).use { output ->
                    input.copyTo(output)
                }
            }
            writeResultCallback?.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
        } catch (e: Exception) {
            writeResultCallback?.onWriteFailed(e.message)
        }
    }
}