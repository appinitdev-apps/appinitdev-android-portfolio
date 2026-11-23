 
















internal fun initQrScanner() = QrProcessor.setProcessor(::prepareBitmap)

private fun prepareBitmap(bitmap: Bitmap): Bitmap =
    QrPrepareHelper.prepareQrForDecode(bitmap.applyPadding(100))