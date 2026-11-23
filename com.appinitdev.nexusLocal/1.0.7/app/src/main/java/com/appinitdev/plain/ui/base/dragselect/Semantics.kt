
internal const val DEFAULT_LABEL = "Select"

fun Modifier.dragSelectSemantics(
    dragSelectState: DragSelectState,
    id: String,
    label: String = DEFAULT_LABEL,
): Modifier = dragSelectSemantics(dragSelectState, label) {
    dragSelectState.addSelected(id)
}

fun Modifier.dragSelectSemantics(
    dragSelectState: DragSelectState,
    label: String = DEFAULT_LABEL,
    onLongClick: () -> Unit,
): Modifier = dragSelectSemantics(dragSelectState.selectMode, label, onLongClick)

fun Modifier.dragSelectSemantics(
    inSelectionMode: Boolean,
    label: String = DEFAULT_LABEL,
    onLongClick: () -> Unit,
): Modifier = then(
    Modifier.semantics {
        if (!inSelectionMode) {
            onLongClick(label = label) {
                onLongClick()
                true
            }
        }
    },
)
