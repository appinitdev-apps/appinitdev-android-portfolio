enum class NiceToastType {
    SUCCESS, INFO, WARNING, ERROR
}
fun showNiceToast(
    context: Context,
    message: String,
    type: NiceToastType = NiceToastType.SUCCESS
) {
    try {
        val density = context.resources.displayMetrics.density
        val paddingHorizPx = (18 * density).toInt()
        val paddingVertPx = (10 * density).toInt()
        val iconSizePx = (20 * density).toInt()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(paddingHorizPx, paddingVertPx, paddingHorizPx, paddingVertPx)
            val (bgColor, strokeColor) = when (type) {
                NiceToastType.SUCCESS -> Pair("#1B3322", "#4CAF50")
                NiceToastType.INFO -> Pair("#162638", "#2196F3")
                NiceToastType.WARNING -> Pair("#362512", "#FF9800")
                NiceToastType.ERROR -> Pair("#381919", "#F44336")
            }
            val bgDrawable = GradientDrawable().apply {
                setColor(Color.parseColor(bgColor))
                cornerRadius = 24 * density
                setStroke((1.5 * density).toInt(), Color.parseColor(strokeColor))
            }
            background = bgDrawable
        }
        val imageView = ImageView(context).apply {
            val iconRes = when (type) {
                NiceToastType.SUCCESS -> R.drawable.ic_launcher_foreground
                NiceToastType.INFO -> R.drawable.ic_launcher_foreground
                NiceToastType.WARNING -> android.R.drawable.ic_dialog_alert
                NiceToastType.ERROR -> android.R.drawable.ic_dialog_alert
            }
            setImageResource(iconRes)
            layoutParams = LinearLayout.LayoutParams(iconSizePx, iconSizePx).apply {
                rightMargin = (10 * density).toInt()
            }
        }
        val textView = TextView(context).apply {
            text = message
            setTextColor(Color.WHITE)
            textSize = 13f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        layout.addView(imageView)
        layout.addView(textView)
        val toast = Toast(context.applicationContext ?: context).apply {
            setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, (70 * density).toInt())
            duration = Toast.LENGTH_SHORT
            view = layout
        }
        toast.show()
    } catch (e: Exception) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}