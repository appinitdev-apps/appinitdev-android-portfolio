
class MyGlideImageDecoder(val degrees: Int, val signature: ObjectKey) : ImageDecoder {

    override fun decode(context: Context, uri: Uri): Bitmap {
        val options = RequestOptions()
            .format(DecodeFormat.PREFER_ARGB_8888)
            .signature(signature)
            .fitCenter()

        val builder = Glide.with(context)
            .asBitmap()
            .load(uri.toString().substringAfter("file://"))
            .apply(options)
            .transform(RotateTransformation(-degrees))
            .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)

        return builder.get()
    }
}
