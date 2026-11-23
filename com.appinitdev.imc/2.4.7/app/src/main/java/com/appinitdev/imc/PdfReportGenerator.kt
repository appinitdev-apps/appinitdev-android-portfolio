
object PdfReportGenerator {

    fun generateAndSharePdf(
        context: Context,
        memberName: String,
        memberAge: String,
        memberGender: String,
        latestBmiResult: BmiResult?,
        growthRecords: List<GrowthRecord>
    ) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 Size
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint()
        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

        // 1. Header Banner
        paint.color = Color.parseColor("#00B894")
        canvas.drawRect(0f, 0f, 595f, 90f, paint)

        paint.color = Color.WHITE
        paint.textSize = 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("INFORME NUTRICIONAL Y DE SALUD IMC (OMS)", 30f, 45f, paint)

        paint.textSize = 12f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Generado el: $dateStr • Estándar OMS 2024", 30f, 70f, paint)

        // 2. Patient / Member Information Box
        paint.color = Color.parseColor("#F4F9F8")
        canvas.drawRoundRect(30f, 110f, 565f, 180f, 15f, 15f, paint)

        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("PACIENTE: $memberName", 45f, 138f, paint)

        paint.textSize = 12f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Edad: $memberAge   |   Sexo: $memberGender", 45f, 160f, paint)

        // 3. Current BMI Status Summary
        var startY = 200f
        if (latestBmiResult != null) {
            paint.color = Color.parseColor("#E0F2FE")
            canvas.drawRoundRect(30f, startY, 565f, startY + 90f, 15f, 15f, paint)

            paint.color = Color.parseColor("#0288D1")
            paint.textSize = 13f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("DIAGNÓSTICO NUTRICIONAL ACTUAL (OMS)", 45f, startY + 25f, paint)

            paint.color = Color.parseColor("#0F172A")
            paint.textSize = 20f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("IMC: ${String.format(Locale.US, "%.1f", latestBmiResult.bmiValue)} kg/m²", 45f, startY + 55f, paint)

            paint.textSize = 14f
            canvas.drawText("Estado: ${latestBmiResult.statusText}", 220f, startY + 55f, paint)

            paint.textSize = 11f
            paint.typeface = Typeface.DEFAULT
            canvas.drawText("Rango Saludable Esperado: ${String.format(Locale.US, "%.1f", latestBmiResult.minHealthyWeightKg)} kg - ${String.format(Locale.US, "%.1f", latestBmiResult.maxHealthyWeightKg)} kg", 45f, startY + 75f, paint)

            startY += 110f
        }

        // 4. Growth History Table
        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("HISTORIAL DE MEDICIONES RECIENTES", 30f, startY + 15f, paint)
        startY += 25f

        // Table Header Row
        paint.color = Color.parseColor("#00B894")
        canvas.drawRect(30f, startY, 565f, startY + 25f, paint)

        paint.color = Color.WHITE
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Fecha", 40f, startY + 17f, paint)
        canvas.drawText("Peso (kg)", 150f, startY + 17f, paint)
        canvas.drawText("Estatura (cm)", 250f, startY + 17f, paint)
        canvas.drawText("IMC", 370f, startY + 17f, paint)
        canvas.drawText("Estado Nutricional", 440f, startY + 17f, paint)

        startY += 25f
        paint.typeface = Typeface.DEFAULT
        paint.color = Color.parseColor("#0F172A")

        val displayRecords = growthRecords.takeLast(10).reversed()
        for (rec in displayRecords) {
            val recordDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(rec.timestamp))

            canvas.drawText(recordDate, 40f, startY + 18f, paint)
            canvas.drawText("${rec.weightKg} kg", 150f, startY + 18f, paint)
            canvas.drawText("${rec.heightCm.toInt()} cm", 250f, startY + 18f, paint)
            canvas.drawText(String.format(Locale.US, "%.1f", rec.bmiValue), 370f, startY + 18f, paint)
            canvas.drawText(rec.statusText, 440f, startY + 18f, paint)

            paint.color = Color.parseColor("#E2E8F0")
            canvas.drawLine(30f, startY + 24f, 565f, startY + 24f, paint)
            paint.color = Color.parseColor("#0F172A")

            startY += 25f
            if (startY > 780f) break
        }

        // Footer Note
        paint.color = Color.GRAY
        paint.textSize = 9f
        canvas.drawText("Nota: Este informe es orientativo según las guías de la OMS. Consulte a un profesional de la salud o nutrición.", 30f, 820f, paint)

        document.finishPage(page)

        // Save to File & Launch Share Intent
        try {
            val cacheDir = File(context.cacheDir, "pdf_reports")
            if (!cacheDir.exists()) cacheDir.mkdirs()

            val file = File(cacheDir, "Informe_Nutricional_${memberName.replace(" ", "_")}.pdf")
            val outputStream = FileOutputStream(file)
            document.writeTo(outputStream)
            document.close()
            outputStream.close()

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Compartir Informe PDF"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
