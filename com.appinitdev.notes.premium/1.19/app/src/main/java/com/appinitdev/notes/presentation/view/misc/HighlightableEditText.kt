open class HighlightableEditText(context: Context, attrs: AttributeSet) :
    EditTextWithWatcher(context, attrs) {
    fun getSpanRange(span: CharacterStyle): Pair<Int, Int> {
        val text = super.getText()!!
        return Pair(text.getSpanStart(span), text.getSpanEnd(span))
    }
    protected fun removeSpan(span: CharacterStyle, removeText: Boolean = false) {
        val (start, end) = getSpanRange(span)
        text?.removeSelectionFromSpans(start, end)
        if (removeText) {
            text?.delete(start, end)
        }
    }
    protected fun applySpan(
        span: CharacterStyle,
        start: Int = selectionStart,
        end: Int = selectionEnd,
    ) {
        text?.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
    private val highlightedSpans: MutableList<CharacterStyle> = mutableListOf()
    private var selectedHighlightedSpan: CharacterStyle? = null
    fun clearHighlights() {
        highlightedSpans.apply {
            forEach { span -> removeSpan(span) }
            clear()
        }
        selectedHighlightedSpan = null
    }
    fun highlight(startIdx: Int, endIdx: Int, selected: Boolean): Int? {
        if (selected) {
            selectedHighlightedSpan?.unselect()
        }
        highlightedSpans
            .filter { getSpanRange(it) == Pair(startIdx, endIdx) }
            .forEach {
                removeSpan(it)
                highlightedSpans.remove(it)
            }
        val span = HighlightSpan(if (selected) highlightColor else highlightColor.withAlpha(0.1f))
        applySpan(span, startIdx, endIdx)
        highlightedSpans.add(span)
        if (selected) {
            selectedHighlightedSpan = span
        }
        return layout?.let {
            val line = layout.getLineForOffset(startIdx)
            layout.getLineTop(line)
        }
    }
    fun unselectHighlight() {
        selectedHighlightedSpan?.unselect()
    }
    private fun CharacterStyle.unselect() {
        val (previousHighlightedStartIdx, previousHighlightedEndIdx) = getSpanRange(this)
        if (previousHighlightedStartIdx != -1) {
            removeSpan(this)
            highlight(previousHighlightedStartIdx, previousHighlightedEndIdx, false)
        }
    }
}
class HighlightSpan(@ColorInt color: Int) : BackgroundColorSpan(color)
