class ReceiptService(private val context: Context) {
    private val dateFormatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    fun generateReceipt(
        transaction: Transaction,
        transactionItems: List<TransactionItem>,
        store: Store?,
        customer: Customer?,
    ): ReceiptResult {
        return try {
            try {
                val bitmap = generateReceiptBitmap(
                    context = context,
                    transaction = transaction,
                    transactionItems = transactionItems,
                    store = store,
                    customer = customer,
                )
                val fileName  =
                    "POSManager_${transaction.transactionNumber}_${dateFormatter.format(Date())}.pdf"
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = String.format("%02d", calendar.get(Calendar.MONTH) + 1) 
                val day = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))
                val receiptDir = File(
                    context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                    "POSManager/${transaction.paymentMethod}/$year/$month/$day"
                )
                if (!receiptDir.exists()) {
                    receiptDir.mkdirs()
                }
                val pdfFile = File(receiptDir, fileName)
                if (bitmap != null) {
                    val pdfDocument = android.graphics.pdf.PdfDocument()
                    val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(
                        bitmap.width,
                        bitmap.height,
                        1
                    ).create()
                    val page = pdfDocument.startPage(pageInfo)
                    val canvas = page.canvas
                    canvas.drawBitmap(bitmap, 0f, 0f, null)
                    pdfDocument.finishPage(page)
                    FileOutputStream(pdfFile).use { out ->
                        pdfDocument.writeTo(out)
                    }
                    pdfDocument.close()
                    val save = pdfFile.absolutePath
                    Log.e("Save: ", save)
                    ReceiptResult.Success(save)
                }else{
                    ReceiptResult.Error("Error generating receipt Bitmap")
                }
            } catch (e: Exception) {
                ReceiptResult.Error("Error generating receipt: ${e.message}")
            }
        } catch (e: Exception) {
            ReceiptResult.Error("Error generating receipt: ${e.message}")
        }
    }
    fun generateReceiptBitmap(
        transaction: Transaction,
        transactionItems: List<TransactionItem>,
        store: Store?,
        customer: Customer?,
        width: Int = 400,
        height: Int = 600,
    ): Bitmap? {
        return null 
    }
    fun getReceiptsDirectory(): File {
        val receiptDir =
            File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "receipts")
        if (!receiptDir.exists()) {
            receiptDir.mkdirs()
        }
        return receiptDir
    }
    fun getReceiptFile(transactionNumber: String): File? {
        val receiptDir = getReceiptsDirectory()
        val files = receiptDir.listFiles { file ->
            file.name.startsWith("receipt_${transactionNumber}_") && file.name.endsWith(".pdf")
        }
        return files?.firstOrNull()
    }
    fun deleteReceipt(transactionNumber: String): Boolean {
        val receiptFile = getReceiptFile(transactionNumber)
        return receiptFile?.delete() ?: false
    }
    fun getAllReceipts(): List<File> {
        val receiptDir = getReceiptsDirectory()
        return receiptDir.listFiles { file ->
            file.name.startsWith("receipt_") && file.name.endsWith(".pdf")
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
}
sealed class ReceiptResult {
    data class Success(val filePath: String) : ReceiptResult()
    data class Error(val message: String) : ReceiptResult()
}
