
@Composable
internal fun <IndicatorValue> ElementScrollbar(
    stateController: StateController<IndicatorValue>,
    modifier: Modifier,
    settings: ScrollbarSettings,
    indicatorContent: (@Composable (indicatorValue: IndicatorValue, isThumbSelected: Boolean) -> Unit)?,
) {
    val view = LocalView.current
    val layoutSettings = remember(settings) {
        ScrollbarLayoutSettings(
            scrollbarPadding = settings.scrollbarPadding,
            selectionActionable = settings.selectionActionable,
            hideEasingAnimation = settings.hideEasingAnimation,
            hideDisplacement = settings.hideDisplacement,
            hideDelayMillis = settings.hideDelayMillis,
            durationAnimationMillis = settings.durationAnimationMillis,
        )
    }

    BoxWithConstraints(modifier) {
        val maxLengthPixels = constraints.maxHeight.toFloat()

        VerticalScrollbarLayout(
            thumbOffsetNormalized = stateController.thumbOffsetNormalized.value,
            thumbIsInAction = stateController.thumbIsInAction.value,
            thumbIsSelected = stateController.isSelected.value,
            settings = layoutSettings,
            indicator = indicatorContent?.let {
                { it(stateController.indicatorValue(), stateController.isSelected.value) }
            },
            draggableModifier = Modifier.draggable(
                state = rememberDraggableState { deltaPixel ->
                    stateController.onDraggableState(
                        deltaPixels = deltaPixel,
                        maxLengthPixels = maxLengthPixels
                    )
                },
                orientation = Orientation.Vertical,
                enabled = settings.selectionMode != ScrollbarSelectionMode.Disabled,
                startDragImmediately = true,
                onDragStarted = { offsetPixel ->
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    stateController.onDragStarted(
                        offsetPixels = offsetPixel.y,
                        maxLengthPixels = maxLengthPixels
                    )
                },
                onDragStopped = {
                    stateController.onDragStopped()
                }
            )
        )
    }
}
