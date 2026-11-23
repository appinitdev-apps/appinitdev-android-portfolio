class AnimatedGifEncoder {
    private var width = 0
    private var height = 0
    private var delay = 200 
    private var repeat = 0  
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
            writeString("GIF89a") 
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
            out?.write(0x3B) 
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
        out?.write(0xF7) 
        out?.write(0)    
        out?.write(0)    
    }
    private fun writePalette() {
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
        out?.write(0x21) 
        out?.write(0xFF) 
        out?.write(11)   
        writeString("NETSCAPE2.0")
        out?.write(3)    
        out?.write(1)    
        writeShort(repeat)
        out?.write(0)    
    }
    private fun writeGraphicCtrlExt() {
        out?.write(0x21) 
        out?.write(0xF9) 
        out?.write(4)    
        out?.write(0x04) 
        writeShort(delay / 10) 
        out?.write(0)    
        out?.write(0)    
    }
    private fun writeImageDesc() {
        out?.write(0x2C) 
        writeShort(0)    
        writeShort(0)    
        writeShort(width)
        writeShort(height)
        out?.write(0)    
    }
    private fun writeFrameData(bitmap: Bitmap) {
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        out?.write(8) 
        val uncompressed = ByteArray(width * height)
        for (i in pixels.indices) {
            val p = pixels[i]
            val r = ((p shr 16) and 0xFF) / 51
            val g = ((p shr 8) and 0xFF) / 51
            val b = (p and 0xFF) / 51
            val idx = (r * 36 + g * 6 + b).coerceIn(0, 255)
            uncompressed[i] = idx.toByte()
        }
        var position = 0
        val total = uncompressed.size
        while (position < total) {
            val blockSize = (total - position).coerceAtMost(254)
            out?.write(blockSize + 1)
            out?.write(0x80) 
            out?.write(uncompressed, position, blockSize)
            position += blockSize
        }
        out?.write(1)
        out?.write(0x81) 
        out?.write(0)    
    }
}
