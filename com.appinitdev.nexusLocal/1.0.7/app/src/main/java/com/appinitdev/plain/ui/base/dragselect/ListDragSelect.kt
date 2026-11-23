
fun Modifier.listDragSelect(
    items: List<IData>,
    state: DragSelectState,
    enableAutoScroll: Boolean = true,
    autoScrollThreshold: Float? = null,
    enableHaptics: Boolean = true,
    hapticFeedback: HapticFeedback? = null,
): Modifier = composed {
    val localHapticFeedback = LocalHapticFeedback.current
    val scrollThreshold = autoScrollThreshold ?: DragSelectDefaults.autoScrollThreshold
    
    if (enableAutoScroll) {
        LaunchedEffect(state.autoScrollSpeed.floatValue) {
            if (state.autoScrollSpeed.floatValue == 0f) return@LaunchedEffect

            val listState = state.listState() ?: return@LaunchedEffect
            
            while (isActive) {
                listState.scrollBy(state.autoScrollSpeed.floatValue)
                delay(10)
            }
        }
    }

    val haptics = if (enableHaptics) hapticFeedback ?: localHapticFeedback else null

    if (!state.selectMode) {
        return@composed this
    }
    
    pointerInput(state.selectMode) {
        detectDragGestures(
            onDragStart = { offset ->
                val lazyListState = state.listState() ?: return@detectDragGestures
                val itemIndex = findItemAt(lazyListState, offset.y)
                if (itemIndex != -1) {
                    val item = items.getOrNull(itemIndex)
                    if (item != null) {
                        haptics?.performHapticFeedback(HapticFeedbackType.LongPress)
                        state.startDrag(item.id, itemIndex)
                        state.addSelected(item.id)
                    }
                }
            },
            onDragCancel = state::stopDrag,
            onDragEnd = state::stopDrag,
            onDrag = { change, _ ->
                state.whenDragging { dragState ->
                    val lazyListState = state.listState() ?: return@whenDragging
                    
                    
                    val viewportHeight = lazyListState.layoutInfo.viewportSize.height
                    autoScrollSpeed.value = when {
                        change.position.y < scrollThreshold -> -scrollThreshold * 0.2f
                        change.position.y > viewportHeight - scrollThreshold -> scrollThreshold * 0.2f
                        else -> 0f
                    }
                    
                    
                    val itemIndex = findItemAt(lazyListState, change.position.y)
                    if (itemIndex == -1 || itemIndex == dragState.current) {
                        return@whenDragging
                    }
                    
                    
                    val start = minOf(itemIndex, dragState.initial)
                    val end = maxOf(itemIndex, dragState.initial)
                    val inRangeIndices = (start..end).toList()
                    
                    
                    val shouldSelect = true
                    for (index in inRangeIndices) {
                        val id = items.getOrNull(index)?.id ?: continue
                        if (shouldSelect) {
                            state.addSelected(id)
                        } else {
                            state.removeSelected(id)
                        }
                    }
                    
                    this.dragState = dragState.copy(current = itemIndex)
                }
            }
        )
    }
}

private fun findItemAt(listState: LazyListState, y: Float): Int {
    val items = listState.layoutInfo.visibleItemsInfo
    if (items.isEmpty()) return -1
    
    return items.minByOrNull { abs((it.offset + it.size / 2f) - y) }?.index ?: -1
} 