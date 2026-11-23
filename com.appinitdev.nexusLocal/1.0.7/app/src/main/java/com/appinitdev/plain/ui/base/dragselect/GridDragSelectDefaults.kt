
object GridDragSelectDefaults {

    val hapticsFeedback: HapticFeedback
        @Composable get() = LocalHapticFeedback.current

    val autoScrollThreshold: Float
        @Composable get() = with(LocalDensity.current) { DEFAULT_THRESHOLD_DP.dp.toPx() }

    private const val DEFAULT_THRESHOLD_DP = 40
}
