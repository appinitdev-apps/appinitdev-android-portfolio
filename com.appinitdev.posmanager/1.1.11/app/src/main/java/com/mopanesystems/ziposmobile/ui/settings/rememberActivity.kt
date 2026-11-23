
@Composable
fun rememberActivity(): Activity? {
    val context = LocalContext.current
    var currentContext = context
    while (currentContext is android.content.ContextWrapper) {
        if (currentContext is Activity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}