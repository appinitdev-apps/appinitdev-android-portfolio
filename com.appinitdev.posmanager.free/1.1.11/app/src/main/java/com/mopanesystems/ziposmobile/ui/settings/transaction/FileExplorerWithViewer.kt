
@Composable
fun FileExplorerWithViewer(context: Context, onBack: (() -> Unit)? = null) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedPdfPath by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize()) {
        FileExplorerScreen(
            onBack=onBack,
            context = context,
            startPath = "POSManager",
            onPdfClick = { file ->
                selectedPdfPath = file.absolutePath
                showDialog = true
            }
        )

        if (showDialog && selectedPdfPath != null) {
            ReceiptDialog(
                pdfPath = selectedPdfPath,
                onDismiss = {
                    showDialog = false
                    selectedPdfPath = null
                }
            )
        }
    }
}
