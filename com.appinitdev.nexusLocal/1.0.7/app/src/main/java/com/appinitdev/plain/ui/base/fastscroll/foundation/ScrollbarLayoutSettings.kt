@Stable
internal data class ScrollbarLayoutSettings(
    val scrollbarPadding: Dp,
    val selectionActionable: ScrollbarSelectionActionable,
    val hideDisplacement: Dp,
    val hideDelayMillis: Int,
    val hideEasingAnimation: Easing,
    val durationAnimationMillis: Int
)