object DrawableUtils {
    fun @receiver:DrawableRes Int.toDrawable(context: Context): Drawable {
        return ContextCompat.getDrawable(context, this)!!
    }
}
