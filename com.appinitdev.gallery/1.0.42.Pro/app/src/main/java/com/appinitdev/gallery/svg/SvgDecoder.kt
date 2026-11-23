
class SvgDecoder : ResourceDecoder<InputStream, SVG> {

    override fun handles(source: InputStream, options: Options) = true

    @Throws(IOException::class)
    override fun decode(source: InputStream, width: Int, height: Int, options: Options): Resource<SVG> {
        try {
            val svg = SVG.getFromInputStream(source)
            return SimpleResource(svg)
        } catch (ex: SVGParseException) {
            throw IOException("Cannot load SVG from stream", ex)
        }
    }
}
