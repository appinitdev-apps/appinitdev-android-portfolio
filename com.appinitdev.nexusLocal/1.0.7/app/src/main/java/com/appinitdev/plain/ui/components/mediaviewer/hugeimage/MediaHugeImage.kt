@Composable
fun MediaHugeImage(
    modifier: Modifier = Modifier, imageDecoder: ImageDecoder,
    scale: Float = DEFAULT_SCALE, offsetX: Float = DEFAULT_OFFSET_X,
    offsetY: Float = DEFAULT_OFFSET_Y, rotation: Float = DEFAULT_ROTATION,
    gesture: RawGesture = RawGesture(), onMounted: () -> Unit = {},
    onSizeChange: suspend (SizeChangeContent) -> Unit = {}, boundClip: Boolean = true,
) {
    val scope = rememberCoroutineScope()
    val state = remember { HugeImageState(imageDecoder, scope) }
    state.scale = scale; state.offsetX = offsetX; state.offsetY = offsetY
    LaunchedEffect(state.bSize, state.rSize) {
        onSizeChange(SizeChangeContent(defaultSize = state.uSize, containerSize = state.bSize, maxScale = state.maxScale))
    }
    LaunchedEffect(state.rSize) { state.updateSampleSize() }
    LaunchedEffect(state.zeroInSampleSize, state.inSampleSize, state.needRenderHeightTexture) { state.updateBackgroundBitmap() }
    DisposableEffect(Unit) { onDispose { state.bitmap?.recycle(); state.bitmap = null } }
    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            imageDecoder.startRenderQueueAsync { scope.launch { state.renderUpdateTimeStamp = System.currentTimeMillis() } }
        }
    }
    LaunchedEffect(state.renderHeightTexture) {
        if (!state.renderHeightTexture) { imageDecoder.renderQueue.clear(); imageDecoder.clearAllBitmap() }
    }
    LaunchedEffect(state.rSize, state.rectW, state.rectH) { state.updateBlockDivider(state.rectW, state.rectH) }
    LaunchedEffect(state.bitmap) {
        val bmp = state.bitmap
        if (bmp != null && bmp.width > 1 && bmp.height > 1 && state.canvasAlpha.value == 0F) {
            scope.launch { state.canvasAlpha.animateTo(1F, DEFAULT_CROSS_FADE_ANIMATE_SPEC); onMounted() }
        }
    }
    Canvas(
        modifier = modifier.alpha(state.canvasAlpha.value).fillMaxSize()
            .graphicsLayer { clip = boundClip }
            .onSizeChanged { state.bSize = it }
            .pointerInput(Unit) { detectTapGestures(onLongPress = gesture.onLongPress) }
            .pointerInput(Unit) {
                detectTransformGestures(onTap = gesture.onTap, onDoubleTap = gesture.onDoubleTap,
                    gestureStart = gesture.gestureStart, gestureEnd = gesture.gestureEnd, onGesture = gesture.onGesture)
            },
    ) {
        withTransform({ rotate(degrees = rotation, pivot = state.rotationCenter) }) {
            state.bitmap?.let {
                drawImage(image = it.asImageBitmap(), dstSize = IntSize(state.rSize.width, state.rSize.height),
                    dstOffset = IntOffset(state.deltaX.toInt(), state.deltaY.toInt()))
            }
            if (state.renderUpdateTimeStamp >= 0) state.updateRenderList()
            if (state.renderHeightTexture && !state.calcMaxCountPending) {
                imageDecoder.forEachBlock { block, _, _ ->
                    block.bitmap?.let {
                        try { drawImage(image = it.asImageBitmap(), dstSize = block.renderSize, dstOffset = block.renderOffset) }
                        catch (ex: Exception) { ex.printStackTrace(); LogCat.e(ex.toString()) }
                    }
                }
            }
        }
    }
}
