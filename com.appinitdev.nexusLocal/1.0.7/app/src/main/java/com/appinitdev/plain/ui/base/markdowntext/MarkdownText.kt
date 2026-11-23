
@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    truncateOnTextOverflow: Boolean = false,
    isTextSelectable: Boolean = true,
    style: TextStyle = TextStyle(
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    previewerState: MediaPreviewerState,
) {
    val defaultColor = MaterialTheme.colorScheme.onSurface
    val linkTextColor = MaterialTheme.colorScheme.primary
    var mText by remember { mutableStateOf("") }

    AndroidView(
        modifier = modifier,
        factory = { factoryContext ->
            TextView(factoryContext).apply {
                setLinkTextColor(linkTextColor.toArgb())

                setTextIsSelectable(isTextSelectable)

                // Disable smart text selection to prevent SmartSelectSprite crash
                // when the view has scrolled and bounding rectangles become invalid
                setTextClassifier(TextClassifier.NO_OP)

                movementMethod = LinkMovementMethod.getInstance()

                if (truncateOnTextOverflow) enableTextOverflow()
            }
        },
        update = { textView ->
            if (text != mText) {
                mText = text
                with(textView) {
                    applyTextColor(style.color.takeOrElse { defaultColor }.toArgb())
                    applyFontSize(style)
                    applyLineHeight(style)
                    applyLineSpacing(style)
                    applyTextDecoration(style)

                    with(style) {
                        applyTextAlign(textAlign)
                        fontStyle?.let { applyFontStyle(it) }
                        fontWeight?.let { applyFontWeight(it) }
                    }
                }
                textView.markdown(text, previewerState)
            }
        }
    )
}
