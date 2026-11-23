
fun Modifier.dragSelectToggleableItem(
    state: DragSelectState,
    id: String,
    semanticsLabel: String = DEFAULT_LABEL,
    interactionSource: MutableInteractionSource? = null,
): Modifier = then(
    Modifier
        .dragSelectSemantics(state, id, semanticsLabel)
        .dragSelectToggleable(state, id, interactionSource)
)