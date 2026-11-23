
object AnimationUtils {

     










    inline fun <reified T : View> T.animateToZeroScale(duration: Long = 500L, delay: Long = 0L, crossinline action: () -> Unit) {
        animate()
            .alpha(0f)
            .scaleX(0f)
            .scaleY(0f)
            .setDuration(duration)
            .setStartDelay(delay)
            .withEndAction {
                action()
            }
    }
}