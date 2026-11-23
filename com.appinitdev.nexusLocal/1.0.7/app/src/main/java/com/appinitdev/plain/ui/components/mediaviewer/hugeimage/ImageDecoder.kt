
class ImageDecoder(
    private val decoder: BitmapRegionDecoder,
    private val rotation: Int = RotationType.ROTATION_0.value,
    private val onRelease: () -> Unit = {},
) {
    var decoderWidth by mutableIntStateOf(0)
        private set

    var decoderHeight by mutableIntStateOf(0)
        private set

    
    var blockSize by mutableIntStateOf(0)
        private set

    
    var renderList: Array<Array<RenderBlock>> = emptyArray()
        private set

    
    val renderQueue = LinkedBlockingDeque<RenderBlock>()

    
    private var countW = 0

    
    private var countH = 0

    
    private var maxBlockCount = 0

    init {
        
        setMaxBlockCount(1)
    }

    
    @Synchronized
    fun setMaxBlockCount(count: Int): Boolean {
        if (maxBlockCount == count) return false
        if (decoder.isRecycled) return false

        when (rotation) {
            RotationType.ROTATION_0.value, RotationType.ROTATION_180.value -> {
                decoderWidth = decoder.width
                decoderHeight = decoder.height
            }

            RotationType.ROTATION_90.value, RotationType.ROTATION_270.value -> {
                decoderWidth = decoder.height
                decoderHeight = decoder.width
            }

            else -> throw RotationIllegalException()
        }

        maxBlockCount = count
        blockSize =
            (decoderWidth.coerceAtLeast(decoderHeight)).toFloat().div(count).toInt()
        countW = ceil(decoderWidth.toFloat().div(blockSize)).toInt()
        countH = ceil(decoderHeight.toFloat().div(blockSize)).toInt()
        renderList = buildRenderBlockList(countH, countW, blockSize, decoderWidth, decoderHeight)
        return true
    }

    
    fun forEachBlock(action: (block: RenderBlock, column: Int, row: Int) -> Unit) {
        for ((column, rows) in renderList.withIndex()) {
            for ((row, block) in rows.withIndex()) {
                action(block, column, row)
            }
        }
    }

    
    fun clearAllBitmap() {
        forEachBlock { block, _, _ ->
            block.release()
        }
    }

    
    fun release() {
        synchronized(decoder) {
            if (!decoder.isRecycled) {
                
                renderQueue.clear()
                
                decoder.recycle()
                
                renderQueue.putFirst(RenderBlock())
            }
            onRelease()
        }
    }

     


    fun decodeRegion(inSampleSize: Int, rect: Rect) =
        decodeRegionBitmap(decoder, rotation, decoderWidth, decoderHeight, inSampleSize, rect)

    fun startRenderQueueAsync(onUpdate: () -> Unit) {
        try {
            while (!decoder.isRecycled) {
                val block = renderQueue.take()
                if (decoder.isRecycled) {
                    break
                }
                val bitmap = decodeRegion(block.inSampleSize, block.sliceRect)
                if (bitmap != null) {
                    block.bitmap = bitmap
                }
                onUpdate()
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}