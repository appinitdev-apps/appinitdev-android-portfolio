object MdEditorHighlightHelper {
    @Composable
    fun highlight(text: String, fileExtension: String, firstColoredIndex: Int): AnnotatedString {
        return buildAnnotatedString {
            val highlightDriver = HighlightDriver(fileExtension)
            val highlights = highlightDriver.highlightText(text, firstColoredIndex)
            append(text)
            for ((color, start, end) in highlights) {
                addStyle(style = SpanStyle(color = color), start = start, end = end)
            }
        }
    }
}