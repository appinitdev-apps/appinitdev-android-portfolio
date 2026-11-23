@Stable
interface ReorderableCollectionItemScope {
    fun Modifier.draggableHandle(
        enabled: Boolean = true,
        interactionSource: MutableInteractionSource? = null,
        onDragStarted: (startedPosition: Offset) -> Unit = {},
        onDragStopped: () -> Unit = {},
    ): Modifier
    fun Modifier.longPressDraggableHandle(
        enabled: Boolean = true,
        interactionSource: MutableInteractionSource? = null,
        onDragStarted: (startedPosition: Offset) -> Unit = {},
        onDragStopped: () -> Unit = {},
    ): Modifier
}
internal class ReorderableCollectionItemScopeImpl(
    private val reorderableLazyCollectionState: ReorderableLazyCollectionState<*>,
    private val key: Any,
    private val itemPositionProvider: () -> Offset,
) : ReorderableCollectionItemScope {
    override fun Modifier.draggableHandle(
        enabled: Boolean,
        interactionSource: MutableInteractionSource?,
        onDragStarted: (startedPosition: Offset) -> Unit,
        onDragStopped: () -> Unit,
    ) = composed {
        var handleOffset by remember { mutableStateOf(Offset.Zero) }
        var handleSize by remember { mutableStateOf(IntSize.Zero) }
        val coroutineScope = rememberCoroutineScope()
        onGloballyPositioned {
            handleOffset = it.positionInRoot()
            handleSize = it.size
        }.draggable(
            key1 = reorderableLazyCollectionState,
            enabled = enabled && (reorderableLazyCollectionState.isItemDragging(key).value || !reorderableLazyCollectionState.isAnyItemDragging),
            interactionSource = interactionSource,
            onDragStarted = {
                coroutineScope.launch {
                    val rel = handleOffset - itemPositionProvider()
                    reorderableLazyCollectionState.onDragStart(key, Offset(rel.x + handleSize.width / 2f, rel.y + handleSize.height / 2f))
                }
                onDragStarted(it)
            },
            onDragStopped = { reorderableLazyCollectionState.onDragStop(); onDragStopped() },
            onDrag = { change, dragAmount -> change.consume(); reorderableLazyCollectionState.onDrag(dragAmount) },
        )
    }
    override fun Modifier.longPressDraggableHandle(
        enabled: Boolean,
        interactionSource: MutableInteractionSource?,
        onDragStarted: (startedPosition: Offset) -> Unit,
        onDragStopped: () -> Unit,
    ) = composed {
        var handleOffset by remember { mutableStateOf(Offset.Zero) }
        var handleSize by remember { mutableStateOf(IntSize.Zero) }
        val coroutineScope = rememberCoroutineScope()
        onGloballyPositioned {
            handleOffset = it.positionInRoot()
            handleSize = it.size
        }.longPressDraggable(
            key1 = reorderableLazyCollectionState,
            enabled = enabled && (reorderableLazyCollectionState.isItemDragging(key).value || !reorderableLazyCollectionState.isAnyItemDragging),
            interactionSource = interactionSource,
            onDragStarted = {
                coroutineScope.launch {
                    val rel = handleOffset - itemPositionProvider()
                    reorderableLazyCollectionState.onDragStart(key, Offset(rel.x + handleSize.width / 2f, rel.y + handleSize.height / 2f))
                }
                onDragStarted(it)
            },
            onDragStopped = { reorderableLazyCollectionState.onDragStop(); onDragStopped() },
            onDrag = { change, dragAmount -> change.consume(); reorderableLazyCollectionState.onDrag(dragAmount) },
        )
    }
}
