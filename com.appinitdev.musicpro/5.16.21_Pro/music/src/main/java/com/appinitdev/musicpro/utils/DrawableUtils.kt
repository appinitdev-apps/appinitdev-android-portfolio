
object DrawableUtils {

    /**
     * Converts drawable resource to drawable
     * @param context is the context
     * @return drawable
     * @receiver drawable resource
     */
    fun @receiver:DrawableRes Int.toDrawable(context: Context): Drawable {
        return ContextCompat.getDrawable(context, this)!!
    }
}
