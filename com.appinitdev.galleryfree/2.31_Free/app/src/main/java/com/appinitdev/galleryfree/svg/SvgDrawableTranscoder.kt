
class SvgDrawableTranscoder : ResourceTranscoder<SVG, PictureDrawable> {
    override fun transcode(toTranscode: Resource<SVG>, options: Options): Resource<PictureDrawable> {
        val svg = toTranscode.get()
        val picture = svg.renderToPicture()
        val drawable = PictureDrawable(picture)
        return SimpleResource(drawable)
    }
}
