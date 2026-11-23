
@Composable
fun rememberReorderableLazyStaggeredGridState(
    lazyStaggeredGridState: LazyStaggeredGridState,
    scrollThresholdPadding: PaddingValues = PaddingValues(0.dp),
    scrollThreshold: Dp = ReorderableLazyCollectionDefaults.ScrollThreshold,
    scroller: Scroller = rememberScroller(
        scrollableState = lazyStaggeredGridState,
        pixelAmountProvider = { lazyStaggeredGridState.layoutInfo.mainAxisViewportSize * ScrollAmountMultiplier },
    ),
    onMove: suspend CoroutineScope.(from: LazyStaggeredGridItemInfo, to: LazyStaggeredGridItemInfo) -> Unit,
): ReorderableLazyStaggeredGridState {
    val density = LocalDensity.current
    val scrollThresholdPx = with(density) { scrollThreshold.toPx() }

    val scope = rememberCoroutineScope()
    val onMoveState = rememberUpdatedState(onMove)
    val layoutDirection = LocalLayoutDirection.current
    val absoluteScrollThresholdPadding = AbsolutePixelPadding(
        start = with(density) {
            scrollThresholdPadding.calculateStartPadding(layoutDirection).toPx()
        },
        end = with(density) {
            scrollThresholdPadding.calculateEndPadding(layoutDirection).toPx()
        },
        top = with(density) { scrollThresholdPadding.calculateTopPadding().toPx() },
        bottom = with(density) { scrollThresholdPadding.calculateBottomPadding().toPx() },
    )
    val state = remember(
        scope, lazyStaggeredGridState, scrollThreshold, scrollThresholdPadding, scroller,
    ) {
        ReorderableLazyStaggeredGridState(
            state = lazyStaggeredGridState,
            scope = scope,
            onMoveState = onMoveState,
            scrollThreshold = scrollThresholdPx,
            scrollThresholdPadding = absoluteScrollThresholdPadding,
            scroller = scroller,
            layoutDirection = layoutDirection,
        )
    }
    return state
}

@Stable
class ReorderableLazyStaggeredGridState internal constructor(
    state: LazyStaggeredGridState,
    scope: CoroutineScope,
    onMoveState: State<suspend CoroutineScope.(from: LazyStaggeredGridItemInfo, to: LazyStaggeredGridItemInfo) -> Unit>,

     




    scrollThreshold: Float,
    scrollThresholdPadding: AbsolutePixelPadding,
    scroller: Scroller,
    layoutDirection: LayoutDirection,
) : ReorderableLazyCollectionState<LazyStaggeredGridItemInfo>(
    state.toLazyCollectionState(),
    scope,
    onMoveState,
    scrollThreshold,
    scrollThresholdPadding,
    scroller,
    layoutDirection,
    lazyVerticalStaggeredGridRtlFix = true,
)

 







@Composable
fun LazyStaggeredGridItemScope.ReorderableItem(
    state: ReorderableLazyStaggeredGridState,
    key: Any,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    animateItemModifier: Modifier = Modifier.animateItem(),
    content: @Composable ReorderableCollectionItemScope.(isDragging: Boolean) -> Unit,
) {
    val dragging by state.isItemDragging(key)
    val offsetModifier = if (dragging) {
        Modifier
            .zIndex(1f)
            .graphicsLayer {
                translationY = state.draggingItemOffset.y
                translationX = state.draggingItemOffset.x
            }
    } else if (key == state.previousDraggingItemKey) {
        Modifier
            .zIndex(1f)
            .graphicsLayer {
                translationY =
                    state.previousDraggingItemOffset.value.y
                translationX =
                    state.previousDraggingItemOffset.value.x
            }
    } else {
        animateItemModifier
    }

    ReorderableCollectionItem(
        state = state,
        key = key,
        modifier = modifier.then(offsetModifier),
        enabled = enabled,
        dragging = dragging,
        content = content,
    )
}
