object DirectPrintManager {
    fun printClinicalReport(
        context: Context,
        memberName: String,
        memberAge: String,
        memberGender: String,
        latestBmiResult: BmiResult?,
        growthRecords: List<GrowthRecord>
    ) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = "Informe_IMC_${memberName.replace(" ", "_")}"
        PdfReportGenerator.generateAndSharePdf(
            context = context,
            memberName = memberName,
            memberAge = memberAge,
            memberGender = memberGender,
            latestBmiResult = latestBmiResult,
            growthRecords = growthRecords
        )
        val pdfFile = File(File(context.cacheDir, "pdf_reports"), "Informe_Clinico_${memberName.replace(" ", "_")}.pdf")
        if (!pdfFile.exists()) return
        val printAdapter = object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }
                val info = PrintDocumentInfo.Builder("Informe_Clinico_$memberName.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                    .build()
                callback?.onLayoutFinished(info, true)
            }
            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                try {
                    val input = FileInputStream(pdfFile)
                    val output = FileOutputStream(destination?.fileDescriptor)
                    val buf = ByteArray(1024)
                    var bytesRead: Int
                    while (input.read(buf).also { bytesRead = it } > 0) {
                        output.write(buf, 0, bytesRead)
                    }
                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                    input.close()
                    output.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                    callback?.onWriteFailed(e.message)
                }
            }
        }
        printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
    }
}
