val DEFAULT_SOFT_ANIMATION_SPEC = tween<Float>(320)
val DEFAULT_PREVIEWER_ENTER_TRANSITION = scaleIn(tween(180)) + fadeIn(tween(240))
val DEFAULT_PREVIEWER_EXIT_TRANSITION = scaleOut(tween(320)) + fadeOut(tween(240))
val DEFAULT_CROSS_FADE_ANIMATE_SPEC: AnimationSpec<Float> = tween(80)
val DEFAULT_PLACEHOLDER_ENTER_TRANSITION = fadeIn(tween(200))
val DEFAULT_PLACEHOLDER_EXIT_TRANSITION = fadeOut(tween(200))
val DEFAULT_PREVIEWER_PLACEHOLDER_CONTENT = @Composable {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Color.White.copy(0.2F))
    }
}
private val fileExistsCache = ConcurrentHashMap<String, Boolean>()
class PreviewerPlaceholder(
    var enterTransition: EnterTransition = DEFAULT_PLACEHOLDER_ENTER_TRANSITION,
    var exitTransition: ExitTransition = DEFAULT_PLACEHOLDER_EXIT_TRANSITION,
    var content: @Composable () -> Unit = DEFAULT_PREVIEWER_PLACEHOLDER_CONTENT,
)
@Composable
fun getModel(item: PreviewItem): Any {
    if (item.isVideo() || item.path.isUrl()) return item
    if (item.size <= 2000 * 1000) return item
    val imageType = remember { ImageHelper.getImageType(item.path, item.path) }
    if (imageType.isApplicableAnimated() || imageType == ImageType.SVG) return item
    val rotation = remember {
        if (item.rotation == -1) item.rotation = ImageHelper.getRotation(item.path)
        item.rotation
    }
    val fileExists = remember(item.path) { fileExistsCache.getOrPut(item.path) { File(item.path).exists() } }
    if (!fileExists) return item
    val inputStream = remember(item.path) {
        try { File(item.path).inputStream() } catch (e: Exception) { fileExistsCache.remove(item.path); null }
    }
    val decoder = if (inputStream != null) rememberDecoderImagePainter(inputStream = inputStream, rotation = rotation) else null
    if (decoder != null) item.intrinsicSize = IntSize(decoder.decoderWidth, decoder.decoderHeight)
    return decoder ?: item
}
