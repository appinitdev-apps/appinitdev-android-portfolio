@Composable
fun rememberScroller(
    scrollableState: ScrollableState,
    pixelAmount: Float,
    duration: Long = 100,
): Scroller {
    return rememberScroller(scrollableState, { pixelAmount }, duration)
}
@Composable
fun rememberScroller(
    scrollableState: ScrollableState,
    pixelPerSecond: Float,
): Scroller {
    val scope = rememberCoroutineScope()
    val pixelPerSecondUpdated = rememberUpdatedState(pixelPerSecond)
    return remember(scrollableState, scope) {
        Scroller(
            scrollableState,
            scope,
            pixelPerSecondProvider = { pixelPerSecondUpdated.value },
        )
    }
}
@Composable
fun rememberScroller(
    scrollableState: ScrollableState,
    pixelAmountProvider: () -> Float,
    duration: Long = 100,
): Scroller {
    val scope = rememberCoroutineScope()
    val pixelAmountProviderUpdated = rememberUpdatedState(pixelAmountProvider)
    val durationUpdated = rememberUpdatedState(duration)
    return remember(scrollableState, scope, duration) {
        Scroller(
            scrollableState,
            scope,
            pixelPerSecondProvider = {
                pixelAmountProviderUpdated.value() / (durationUpdated.value / 1000f)
            },
        )
    }
}
