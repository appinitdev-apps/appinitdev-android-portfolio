@Composable
fun PClickableText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    softWrap: Boolean = true,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    onClick: (Int) -> Unit = {},
    onDoubleClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
) {
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
    val pressIndicator =
        Modifier.pointerInput(onClick) {
            detectTapGestures(onDoubleTap = { onDoubleClick() }, onTap = {
                val layout = layoutResult.value
                if (layout != null) {
                    val offset = layout.getOffsetForPosition(it)
                    onClick(offset)
                }
            }, onLongPress = { onLongClick() })
        }
    BasicText(
        text = text,
        modifier = modifier.then(pressIndicator),
        style = style,
        softWrap = softWrap,
        overflow = overflow,
        maxLines = maxLines,
        onTextLayout = {
            layoutResult.value = it
            onTextLayout(it)
        },
    )
}
@Composable
fun PClickableText(
    text: String,
    clickTexts: List<VClickText>,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
) {
    val fullText = text.linkify(clickTexts)
    androidx.compose.foundation.text.ClickableText(
        text = fullText,
        modifier = modifier,
        style = style,
        onClick = { position ->
            fullText.clickAt(position, clickTexts)
        },
    )
}