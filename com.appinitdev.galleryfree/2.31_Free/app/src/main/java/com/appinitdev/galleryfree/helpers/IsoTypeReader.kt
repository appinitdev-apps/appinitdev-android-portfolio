

object IsoTypeReader {
    fun readUInt32(bb: ByteBuffer): Long {
        var i = bb.int.toLong()
        if (i < 0) {
            i += 1L shl 32
        }
        return i
    }

    fun read4cc(bb: ByteBuffer): String? {
        val codeBytes = ByteArray(4)
        bb.get(codeBytes)

        return try {
            String(codeBytes, Charset.forName("ISO-8859-1"))
        } catch (e: UnsupportedEncodingException) {
            null
        }
    }
}
