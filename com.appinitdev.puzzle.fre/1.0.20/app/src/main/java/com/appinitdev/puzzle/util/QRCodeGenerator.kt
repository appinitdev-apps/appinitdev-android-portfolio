object QRCodeGenerator {
    fun generateQRCode(text: String, size: Int = 512): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        val paintDark = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        val paintAccent = Paint().apply {
            color = Color.parseColor("#3F51B5") 
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        val matrixSize = 25
        val cellSize = size.toFloat() / matrixSize
        val matrix = Array(matrixSize) { BooleanArray(matrixSize) }
        drawFinderPattern(matrix, 0, 0)
        drawFinderPattern(matrix, matrixSize - 7, 0)
        drawFinderPattern(matrix, 0, matrixSize - 7)
        for (i in 7 until matrixSize - 7) {
            if (i % 2 == 0) {
                matrix[6][i] = true
                matrix[i][6] = true
            }
        }
        val bytes = text.toByteArray(Charsets.UTF_8)
        var bitIndex = 0
        for (r in 0 until matrixSize) {
            for (c in 0 until matrixSize) {
                if (isReservedArea(r, c, matrixSize)) continue
                val byteVal = if (bytes.isNotEmpty()) bytes[bitIndex % bytes.size].toInt() and 0xFF else 0
                val bit = ((byteVal shr (bitIndex % 8)) and 1) == 1
                val pseudoRandom = ((r * 31 + c * 17 + text.hashCode()) % 3) == 0
                matrix[r][c] = bit xor pseudoRandom
                bitIndex++
            }
        }
        for (r in 0 until matrixSize) {
            for (c in 0 until matrixSize) {
                if (matrix[r][c]) {
                    val isFinder = isFinderCenter(r, c, matrixSize)
                    val p = if (isFinder) paintAccent else paintDark
                    val left = c * cellSize
                    val top = r * cellSize
                    canvas.drawRoundRect(
                        left + 1f, top + 1f,
                        left + cellSize - 1f, top + cellSize - 1f,
                        4f, 4f, p
                    )
                }
            }
        }
        return bitmap
    }
    private fun drawFinderPattern(matrix: Array<BooleanArray>, startR: Int, startC: Int) {
        for (r in 0 until 7) {
            for (c in 0 until 7) {
                val isOuter = r == 0 || r == 6 || c == 0 || c == 6
                val isInner = r in 2..4 && c in 2..4
                matrix[startR + r][startC + c] = isOuter || isInner
            }
        }
    }
    private fun isFinderCenter(r: Int, c: Int, size: Int): Boolean {
        return (r in 2..4 && c in 2..4) ||
                (r in 2..4 && c in (size - 5)..(size - 3)) ||
                (r in (size - 5)..(size - 3) && c in 2..4)
    }
    private fun isReservedArea(r: Int, c: Int, size: Int): Boolean {
        if (r < 8 && c < 8) return true 
        if (r < 8 && c >= size - 8) return true 
        if (r >= size - 8 && c < 8) return true 
        if (r == 6 || c == 6) return true 
        return false
    }
    fun shareQRCodeImage(context: Context, qrBitmap: Bitmap, shareText: String) {
        try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "puzzle_qr.png")
            FileOutputStream(file).use { out ->
                qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_TEXT, shareText)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Compartir Código QR"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
@Composable
fun QRCodeDialog(
    qrData: String,
    title: String = stringResource(R.string.codigo_qr_del_puzzle),
    subtitle: String = stringResource(R.string.escanea_este_codigo),
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val qrBitmap = remember(qrData) {
        QRCodeGenerator.generateQRCode(qrData, 600)
    }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode2,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Código QR",
                        modifier = Modifier
                            .size(220.dp)
                            .padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                    ) {
                        Text(stringResource(R.string.cerrar), maxLines = 1)
                    }
                    Button(
                        onClick = {
                            QRCodeGenerator.shareQRCodeImage(
                                context,
                                qrBitmap,
                                "¡Mira mi desáfio de puzzle! 🧩 $qrData"
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.compartir), maxLines = 1, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
