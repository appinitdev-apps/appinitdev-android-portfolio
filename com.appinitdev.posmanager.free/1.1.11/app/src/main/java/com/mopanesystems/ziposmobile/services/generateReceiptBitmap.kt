@SuppressLint("ResourceType")
fun generateReceiptBitmap(
    context: Context,
    transaction: Transaction,
    transactionItems: List<TransactionItem>,
    store: Store?,
    customer: Customer?,
    width: Int = 384,  
    padding: Int = 10
): Bitmap? {
    Log.e("generateReceiptBitmap", transactionItems.toString())
    return try {
        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val lineHeight = 20f
        val estimatedHeight = 400 + transactionItems.size * 30 + 200 
        val bitmap = Bitmap.createBitmap(width, estimatedHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        val paint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
        }
        var y = padding.toFloat()
        try {
            val logoResId = R.drawable.logo
            val logoBitmap = decodeSampledBitmapFromResource(context.resources, logoResId, 100, 100)
            if (logoBitmap != null) {
                val scaledLogo = Bitmap.createScaledBitmap(logoBitmap, 100, 100, true)
                canvas.drawBitmap(scaledLogo, (width - scaledLogo.width) / 2f, y, paint)
                y += scaledLogo.height + padding
            }
        } catch (e: Exception) { }
        paint.textSize = 16f
        paint.isFakeBoldText = true
        val storeName = store?.name ?: "POSManager"
        canvas.drawText(storeName, (width - paint.measureText(storeName)) / 2f, y, paint)
        y += lineHeight
        paint.textSize = 12f
        paint.isFakeBoldText = false
        val storeAddress = store?.address ?: "Centro, México"
        canvas.drawText(storeAddress, (width - paint.measureText(storeAddress)) / 2f, y, paint)
        y += lineHeight
        paint.textSize = 12f
        paint.isFakeBoldText = false
        val storeGmail = store?.email ?: "appinitdev@gmail.com"
        canvas.drawText(storeGmail, (width - paint.measureText(storeGmail)) / 2f, y, paint)
        y += lineHeight
        val storePhone = store?.phone ?: "(555) 123-4567"
        canvas.drawText(storePhone, (width - paint.measureText(storePhone)) / 2f, y, paint)
        y += lineHeight + padding
        paint.isFakeBoldText = true
        canvas.drawText(context.getString(R.string.transaction)+" ${transaction.transactionNumber}", padding.toFloat(), y, paint)
        y += lineHeight
        paint.isFakeBoldText = false
        canvas.drawText(context.getString(R.string.date)+" ${transaction.createdAt.format(dateFormatter)}", padding.toFloat(), y, paint)
        y += lineHeight + padding
        canvas.drawText(context.getString(R.string.payment_method)+" ${transaction.paymentMethod}", padding.toFloat(), y, paint)
        y += lineHeight + padding
        transaction.notes?.let{
            canvas.drawText(context.getString(R.string.note)+" ${it.toString()}", padding.toFloat(), y, paint)
            y += lineHeight + padding
        }
        customer?.let {
            paint.isFakeBoldText = true
            canvas.drawText(context.getString(R.string.customer), padding.toFloat(), y, paint)
            y += lineHeight
            paint.isFakeBoldText = false
            canvas.drawText(context.getString(R.string.name)+" ${it.fullName}", padding.toFloat(), y, paint)
            y += lineHeight
            it.phone?.let { phone ->
                canvas.drawText(context.getString(R.string.phone)+" $phone", padding.toFloat(), y, paint)
                y += lineHeight
            }
            it.email?.let { email ->
                canvas.drawText(context.getString(R.string.email)+" $email", padding.toFloat(), y, paint)
                y += lineHeight
            }
            y += padding
        }
        paint.isFakeBoldText = true
        val colItemX = padding.toFloat()
        val colQtyX = colItemX + 150f 
        val colPriceX = colQtyX + 40f
        val colTotalX = colPriceX + 80f
        canvas.drawText(context.getString(R.string.item), colItemX, y, paint)
        canvas.drawText(context.getString(R.string.qty), colQtyX, y, paint)
        canvas.drawText(context.getString(R.string.price), colPriceX, y, paint)
        canvas.drawText(context.getString(R.string.total), colTotalX, y, paint)
        y += lineHeight
        paint.isFakeBoldText = false
        transactionItems.forEach { item ->
            val itemName = item.notes?.take(20)?:item.productId.take(20)
            val qty = item.quantity.toString()
            val price = currencyFormatter.format(item.unitPrice)
            val total = currencyFormatter.format(item.totalPrice)
            canvas.drawText(itemName, colItemX, y, paint)
            canvas.drawText(qty, colQtyX, y, paint)
            canvas.drawText(price, colPriceX, y, paint)
            canvas.drawText(total, colTotalX, y, paint)
            y += lineHeight
        }
        y += padding
        val labelPaint = Paint(paint).apply {
            isFakeBoldText = false
            textSize = 12f
        }
        val amountPaint = Paint(paint).apply {
            isFakeBoldText = true
            textSize = 12f
        }
        val labelX = padding.toFloat()
        val amountX = width - padding.toFloat() 
        canvas.drawText(context.getString(R.string.subtotal), labelX, y, labelPaint)
        canvas.drawText(currencyFormatter.format(transaction.subtotal),
            amountX - amountPaint.measureText(currencyFormatter.format(transaction.subtotal)), y, amountPaint)
        y += lineHeight
        canvas.drawText(context.getString(R.string.tax), labelX, y, labelPaint)
        canvas.drawText(currencyFormatter.format(transaction.taxAmount),
            amountX - amountPaint.measureText(currencyFormatter.format(transaction.taxAmount)), y, amountPaint)
        y += lineHeight
        if (transaction.discountAmount > BigDecimal.ZERO) {
            canvas.drawText(context.getString(R.string.discount), labelX, y, labelPaint)
            val discountText = "-${currencyFormatter.format(transaction.discountAmount)}"
            canvas.drawText(discountText,
                amountX - amountPaint.measureText(discountText), y, amountPaint)
            y += lineHeight
        }
        canvas.drawText(context.getString(R.string.total), labelX, y, amountPaint)
        val totalText = currencyFormatter.format(transaction.totalAmount)
        canvas.drawText(totalText,
            amountX - amountPaint.measureText(totalText), y, amountPaint)
        y += lineHeight
        if (transaction.paidAmount > transaction.totalAmount) {
            canvas.drawText(context.getString(R.string.paid), labelX, y, labelPaint)
            val paidText = currencyFormatter.format(transaction.paidAmount)
            canvas.drawText(paidText,
                amountX - amountPaint.measureText(paidText), y, amountPaint)
            y += lineHeight
            canvas.drawText(context.getString(R.string.change), labelX, y, labelPaint)
            val changeText = currencyFormatter.format(transaction.changeAmount)
            canvas.drawText(changeText,
                amountX - amountPaint.measureText(changeText), y, amountPaint)
            y += lineHeight
        }
        y += padding
        try {
            val qrData = "Transaction #: ${transaction.transactionNumber}\nTotal: ${currencyFormatter.format(transaction.totalAmount)}\nPayment method: ${transaction.paymentMethod}\nStore: ${storeName}\nDate: ${transaction.createdAt.format(dateFormatter)}\nMail: ${storeGmail}\nPhone: ${storePhone}"
            val qrWriter = com.google.zxing.qrcode.QRCodeWriter()
            val bitMatrix = qrWriter.encode(qrData, com.google.zxing.BarcodeFormat.QR_CODE, 120, 120)
            val qrBitmap = Bitmap.createBitmap(120, 120, Bitmap.Config.ARGB_8888)
            for (x in 0 until 120) {
                for (yPos in 0 until 120) {
                    qrBitmap.setPixel(x, yPos, if (bitMatrix.get(x, yPos)) Color.BLACK else Color.WHITE)
                }
            }
            canvas.drawBitmap(qrBitmap, (width - 120) / 2f, y, paint)
            y += 120 + padding
        } catch (e: Exception) { }
        canvas.drawText(context.getString(R.string.thank_you_for_your_business), padding.toFloat(), y, paint)
        y += lineHeight
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1
    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}
private fun decodeSampledBitmapFromResource(res: android.content.res.Resources, resId: Int, reqWidth: Int, reqHeight: Int): Bitmap? {
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    BitmapFactory.decodeResource(res, resId, options)
    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
    options.inJustDecodeBounds = false
    return BitmapFactory.decodeResource(res, resId, options)
}
