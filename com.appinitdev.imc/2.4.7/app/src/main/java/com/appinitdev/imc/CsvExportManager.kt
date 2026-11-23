
object CsvExportManager {

    fun exportAndShareCsv(
        context: Context,
        memberName: String,
        records: List<GrowthRecord>
    ) {
        val sb = StringBuilder()
        // CSV Header Row
        sb.append("\"Fecha\",\"Paciente\",\"Peso (kg)\",\"Estatura (cm)\",\"IMC\",\"Estado Nutricional\"\n")

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        for (rec in records) {
            val dateStr = dateFormat.format(Date(rec.timestamp))
            sb.append("\"$dateStr\",")
            sb.append("\"$memberName\",")
            sb.append("\"${rec.weightKg}\",")
            sb.append("\"${rec.heightCm}\",")
            sb.append("\"${String.format(Locale.US, "%.1f", rec.bmiValue)}\",")
            sb.append("\"${rec.statusText}\"\n")
        }

        try {
            val cacheDir = File(context.cacheDir, "csv_exports")
            if (!cacheDir.exists()) cacheDir.mkdirs()

            val file = File(cacheDir, "Historial_${memberName.replace(" ", "_")}.csv")
            val outputStream = FileOutputStream(file)
            outputStream.write(sb.toString().toByteArray(Charsets.UTF_8))
            outputStream.close()

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Exportar Historial a Excel (CSV)"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
