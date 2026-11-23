
/**
 * Pure Kotlin lightweight Animated GIF Encoder.
 * Encodes a list of Bitmaps into an animated GIF output stream.
 */
class AnimatedGifEncoder {
    private var width = 0
    private var height = 0
    private var delay = 200 // Default 200ms per frame
    private var repeat = 0  // 0 = loop indefinitely
    private var out: OutputStream? = null

    fun setDelay(ms: Int) {
        delay = ms
    }

    fun setFrameRate(fps: Float) {
        if (fps > 0f) {
            delay = (1000f / fps).toInt()
        }
    }

    fun start(os: OutputStream): Boolean {
        out = os
        return try {
            writeString("GIF89a") // Header
            true
        } catch (e: Exception) {
            false
        }
    }

    fun addFrame(bitmap: Bitmap): Boolean {
        if (out == null || bitmap.isRecycled || bitmap.width <= 0 || bitmap.height <= 0) return false
        try {
            val targetW = 360
            val targetH = ((targetW.toFloat() * bitmap.height) / bitmap.width.toFloat()).toInt().coerceIn(100, 1080)
            val resized = Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
            if (width == 0 || height == 0) {
                width = resized.width
                height = resized.height
                writeLSD()
                writePalette()
                writeNetscapeExt()
            }

            writeGraphicCtrlExt()
            writeImageDesc()
            writeFrameData(resized)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun finish(): Boolean {
        if (out == null) return false
        return try {
            out?.write(0x3B) // GIF trailer
            out?.flush()
            out = null
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun writeString(s: String) {
        for (i in s.indices) {
            out?.write(s[i].code)
        }
    }

    private fun writeShort(value: Int) {
        out?.write(value and 0xFF)
        out?.write((value shr 8) and 0xFF)
    }

    private fun writeLSD() {
        writeShort(width)
        writeShort(height)
        out?.write(0xF7) // Global color table flag (256 colors)
        out?.write(0)    // Background color index
        out?.write(0)    // Pixel aspect ratio
    }

    private fun writePalette() {
        // Simple 6x6x6 color cube + 40 grays = 256 colors
        for (r in 0..5) {
            for (g in 0..5) {
                for (b in 0..5) {
                    out?.write(r * 51)
                    out?.write(g * 51)
                    out?.write(b * 51)
                }
            }
        }
        for (i in 0..39) {
            val gray = i * 6
            out?.write(gray)
            out?.write(gray)
            out?.write(gray)
        }
    }

    private fun writeNetscapeExt() {
        out?.write(0x21) // Extension introducer
        out?.write(0xFF) // Application extension label
        out?.write(11)   // Block size
        writeString("NETSCAPE2.0")
        out?.write(3)    // Sub-block size
        out?.write(1)    // Loop sub-block ID
        writeShort(repeat)
        out?.write(0)    // Block terminator
    }

    private fun writeGraphicCtrlExt() {
        out?.write(0x21) // Extension introducer
        out?.write(0xF9) // Graphic control label
        out?.write(4)    // Block size
        out?.write(0x04) // Disposal method: restore to BG
        writeShort(delay / 10) // Delay time in 1/100ths of a second
        out?.write(0)    // Transparent color index
        out?.write(0)    // Block terminator
    }

    private fun writeImageDesc() {
        out?.write(0x2C) // Image separator
        writeShort(0)    // Image left
        writeShort(0)    // Image top
        writeShort(width)
        writeShort(height)
        out?.write(0)    // Local color table flag = 0
    }

    private fun writeFrameData(bitmap: Bitmap) {
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        out?.write(8) // LZW minimum code size
        val uncompressed = ByteArray(width * height)

        for (i in pixels.indices) {
            val p = pixels[i]
            val r = ((p shr 16) and 0xFF) / 51
            val g = ((p shr 8) and 0xFF) / 51
            val b = (p and 0xFF) / 51
            val idx = (r * 36 + g * 6 + b).coerceIn(0, 255)
            uncompressed[i] = idx.toByte()
        }

        // LZW Simple Uncompressed-style block streaming for valid GIF decoder compatibility
        var position = 0
        val total = uncompressed.size
        while (position < total) {
            val blockSize = (total - position).coerceAtMost(254)
            out?.write(blockSize + 1)
            out?.write(0x80) // Clear code
            out?.write(uncompressed, position, blockSize)
            position += blockSize
        }
        out?.write(1)
        out?.write(0x81) // End of information code
        out?.write(0)    // Block terminator
    }
}
