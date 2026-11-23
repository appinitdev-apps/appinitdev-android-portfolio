fun Modifier.gridDragSelect(
    items: List<IData>,
    state: DragSelectState,
    enableAutoScroll: Boolean = true,
    autoScrollThreshold: Float? = null,
    enableHaptics: Boolean = true,
    hapticFeedback: HapticFeedback? = null,
): Modifier = composed {
    val scrollThreshold: Float = autoScrollThreshold ?: GridDragSelectDefaults.autoScrollThreshold
    if (enableAutoScroll) {
        LaunchedEffect(state.autoScrollSpeed.floatValue) {
            if (state.autoScrollSpeed.floatValue == 0f) return@LaunchedEffect
            while (isActive) {
                state.gridState()?.scrollBy(state.autoScrollSpeed.floatValue)
                delay(10)
            }
        }
    }
    val haptics: HapticFeedback? =
        if (!enableHaptics) null
        else hapticFeedback ?: GridDragSelectDefaults.hapticsFeedback
    if (!state.selectMode) {
        return@composed this
    }
    pointerInput(Unit) {
        fun findItemIndex(gridState: LazyGridState, position: Offset): Int? {
            val found = gridState.layoutInfo.visibleItemsInfo.find { itemInfo ->
                itemInfo.size.toIntRect().contains(position.round() - itemInfo.offset)
            }
            if (found != null) {
                val key = found.key
                val idx = items.indexOfFirst { it.id == key }
                return if (idx >= 0) idx else null
            }
            val lastItem = gridState.layoutInfo.visibleItemsInfo.lastOrNull()
                ?.takeIf { it.index == gridState.layoutInfo.totalItemsCount - 1 }
            if (lastItem != null && position.y > lastItem.offset.y) {
                return items.size - 1
            }
            return null
        }
        detectDragGestures(
            onDragStart = { offset ->
                state.gridState()?.let { gridState ->
                    findItemIndex(gridState, offset)?.let { startIndex ->
                        haptics?.performHapticFeedback(HapticFeedbackType.LongPress)
                        state.startDrag(items[startIndex].id, startIndex)
                    }
                }
            },
            onDragCancel = state::stopDrag,
            onDragEnd = state::stopDrag,
            onDrag = { change, _ ->
                state.whenDragging { dragState ->
                    val gridState = gridState() ?: return@whenDragging
                    autoScrollSpeed.value = gridState.calculateScrollSpeed(change, scrollThreshold)
                    val itemPosition = findItemIndex(gridState, change.position)
                        ?: return@whenDragging
                    if (itemPosition == dragState.current) {
                        return@whenDragging
                    }
                    val inRangeIds = items.getWithinRangeIds(itemPosition, dragState)
                    val shouldSelect = state.isSelected(dragState.initialId)
                    inRangeIds.forEach {
                        if (shouldSelect) {
                            state.addSelected(it)
                        } else {
                            state.removeSelected(it)
                        }
                    }
                    this.dragState = dragState.copy(current = itemPosition)
                }
            },
        )
    }
}
private fun LazyGridState.calculateScrollSpeed(
    change: PointerInputChange,
    scrollThreshold: Float,
): Float {
    val distanceFromTop: Float = change.position.y
    val distanceFromBottom: Float = layoutInfo.viewportSize.height - distanceFromTop
    return when {
        distanceFromBottom < scrollThreshold -> scrollThreshold - distanceFromBottom
        distanceFromTop < scrollThreshold -> -(scrollThreshold - distanceFromTop)
        else -> 0f
    }
}
private fun List<IData>.getWithinRangeIds(
    itemPosition: Int,
    dragState: DragState,
): List<String> {
    val initial = dragState.initial
    return filterIndexed { index, _ ->
        index in initial..itemPosition || index in itemPosition..initial
    }.map { it.id }
}
