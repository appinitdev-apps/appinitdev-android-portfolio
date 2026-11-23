@Composable
fun ReceiptDialog(
    pdfPath: String?,
    onDismiss: () -> Unit,
) {
    if (pdfPath == null) return
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss,  properties = DialogProperties(
        usePlatformDefaultWidth = false,
        decorFitsSystemWindows = false
    )) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 48.dp, horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    stringResource(R.string.administrar_transacci_n),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                val bitmap = remember(pdfPath) { pdfToBitmap(File(pdfPath)) }
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Receipt",
                                modifier = Modifier
                            .fillMaxWidth()
                            .height(500.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    IconButton(
                        onClick = { sharePdf(File(pdfPath), context) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    IconButton(
                        onClick = { printPdf(File(pdfPath), context) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = "Print",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.weight(1f) ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(34.dp))
            }
        }
    }
}
fun printPdf(file: File, context: Context) {
    val printManager = context.getSystemService(Context.PRINT_SERVICE) as android.print.PrintManager
    val printAdapter = PdfDocumentAdapter(context, file)
    val attributes = PrintAttributes.Builder()
        .setMediaSize(PrintAttributes.MediaSize("CUSTOM", "TICKET", 165, 5000)) 
        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
        .build()
    printManager.print("Receipt", printAdapter, attributes)
}
fun sharePdf(file: File, context: Context) {
    val uri = androidx.core.content.FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share Receipt"))
}
fun pdfToBitmap(file: File): Bitmap? {
    return try {
        val renderer = android.graphics.pdf.PdfRenderer(
            android.os.ParcelFileDescriptor.open(
                file,
                android.os.ParcelFileDescriptor.MODE_READ_ONLY
            )
        )
        val page = renderer.openPage(0)
        val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
        page.render(
            bitmap,
            null,
            null,
            android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
        )
        page.close()
        renderer.close()
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
