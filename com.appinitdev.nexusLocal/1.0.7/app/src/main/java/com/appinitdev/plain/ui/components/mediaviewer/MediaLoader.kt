
@OptIn(ExperimentalCoilApi::class)
@Composable
fun rememberCoilImagePainter(path: String): Painter {
    val imageRequest = ImageRequest
        .Builder(LocalContext.current)
        .data(path)
        .size(coil3.size.Size.ORIGINAL)
        .build()
    return rememberAsyncImagePainter(imageRequest)
}


@Composable
fun rememberDecoderImagePainter(
    inputStream: InputStream,
    rotation: Int,
): ImageDecoder? {
    var imageDecoder by remember { mutableStateOf<ImageDecoder?>(null) }
    LaunchedEffect(inputStream) {
        launch {
            imageDecoder = withIO {
                try {
                    val decoder = if (isSPlus()) {
                        BitmapRegionDecoder.newInstance(inputStream)
                    } else {
                        BitmapRegionDecoder.newInstance(inputStream, false)
                    }
                    if (decoder == null) {
                        null
                    } else {
                        ImageDecoder(decoder = decoder, rotation = rotation)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            imageDecoder?.release()
        }
    }
    return imageDecoder
}