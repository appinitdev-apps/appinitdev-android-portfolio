
@Composable
internal fun ReorderableCollectionItem(
    state: ReorderableLazyCollectionState<*>,
    key: Any,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    dragging: Boolean,
    content: @Composable ReorderableCollectionItemScope.(isDragging: Boolean) -> Unit,
) {
    var itemPosition by remember { mutableStateOf(Offset.Zero) }
    Box(modifier.onGloballyPositioned { itemPosition = it.positionInRoot() }) {
        val itemScope = remember(state, key) {
            ReorderableCollectionItemScopeImpl(
                reorderableLazyCollectionState = state, key = key, itemPositionProvider = { itemPosition },
            )
        }
        itemScope.content(dragging)
    }
    LaunchedEffect(state.reorderableKeys, enabled) {
        if (enabled) state.reorderableKeys.add(key) else state.reorderableKeys.remove(key)
    }
}
