
@Composable
public fun rememberDragSelectState(
    lazyGridState: LazyGridState = rememberLazyGridState(),
    initialSelection: List<String> = emptyList(),
): DragSelectState {
    val dragState = rememberSaveable(saver = DragState.Saver) { DragState() }

    return remember(lazyGridState) {
        DragSelectState(
            initialSelection = initialSelection,
            gridState = { lazyGridState },
            listState = { null },
            dragState = dragState,
        )
    }
}

@Composable
public fun rememberDragSelectState(
    lazyGridState: () -> LazyGridState?,
    initialSelection: List<String> = emptyList(),
): DragSelectState {
    val dragState = rememberSaveable(saver = DragState.Saver) { DragState() }

    return remember(lazyGridState) {
        DragSelectState(
            initialSelection = initialSelection,
            gridState = { lazyGridState() },
            listState = { null },
            dragState = dragState,
        )
    }
}

@Composable
public fun rememberListDragSelectState(
    lazyListState: LazyListState = rememberLazyListState(),
    initialSelection: List<String> = emptyList(),
): DragSelectState {
    val dragState = rememberSaveable(saver = DragState.Saver) { DragState() }

    return remember(lazyListState) {
        DragSelectState(
            initialSelection = initialSelection,
            gridState = { null },
            listState = { lazyListState },
            dragState = dragState,
        )
    }
}

@Composable
public fun rememberListDragSelectState(
    lazyListState: () -> LazyListState?,
    initialSelection: List<String> = emptyList(),
): DragSelectState {
    val dragState = rememberSaveable(saver = DragState.Saver) { DragState() }

    return remember(lazyListState) {
        DragSelectState(
            initialSelection = initialSelection,
            gridState = { null },
            listState = { lazyListState() },
            dragState = dragState,
        )
    }
}
