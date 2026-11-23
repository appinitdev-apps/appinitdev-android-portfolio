

@Composable
internal fun rememberScrollbarLayoutState(
    thumbIsInAction: Boolean,
    thumbIsSelected: Boolean,
    settings: ScrollbarLayoutSettings,
): ScrollbarLayoutState {
    val settingsUpdated by rememberUpdatedState(settings)
    val thumbIsInActionUpdated by rememberUpdatedState(thumbIsInAction)

    val isInActionSelectable = remember { mutableStateOf(thumbIsInAction) }

    LaunchedEffect(thumbIsInAction) {
        if (thumbIsInAction) {
            isInActionSelectable.value = true
        } else {
            delay(timeMillis = settingsUpdated.durationAnimationMillis.toLong() + settingsUpdated.hideDelayMillis.toLong())
            isInActionSelectable.value = false
        }
    }

    val activeDraggableModifier = remember {
        derivedStateOf {
            when (settingsUpdated.selectionActionable) {
                ScrollbarSelectionActionable.Always -> true
                ScrollbarSelectionActionable.WhenVisible -> isInActionSelectable.value
            }
        }
    }

    val thumbColor = animateColorAsState(
        targetValue = if (thumbIsSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
        animationSpec = tween(durationMillis = 50),
        label = "scrollbar thumb color value"
    )

    val currentDurationMillis = remember {
        derivedStateOf {
            val reductionRatio: Int = if (thumbIsInActionUpdated) 4 else 1
            settingsUpdated.durationAnimationMillis / reductionRatio
        }
    }

    val hideAlpha = animateFloatAsState(
        targetValue = if (thumbIsInActionUpdated) 1f else 0f,
        animationSpec = tween(
            durationMillis = currentDurationMillis.value,
            delayMillis = if (thumbIsInActionUpdated) 0 else settingsUpdated.hideDelayMillis,
            easing = settingsUpdated.hideEasingAnimation
        ),
        label = "scrollbar alpha value"
    )

    val hideDisplacement = animateDpAsState(
        targetValue = if (thumbIsInActionUpdated) 0.dp else settingsUpdated.hideDisplacement,
        animationSpec = tween(
            durationMillis = currentDurationMillis.value,
            delayMillis = if (thumbIsInActionUpdated) 0 else settingsUpdated.hideDelayMillis,
            easing = settingsUpdated.hideEasingAnimation
        ),
        label = "scrollbar displacement value"
    )

    return remember {
        ScrollbarLayoutState(
            activeDraggableModifier = activeDraggableModifier,
            thumbColor = thumbColor,
            hideDisplacement = hideDisplacement,
            hideAlpha = hideAlpha
        )
    }

}

internal data class ScrollbarLayoutState(
    val activeDraggableModifier: State<Boolean>,
    val thumbColor: State<Color>,
    val hideAlpha: State<Float>,
    val hideDisplacement: State<Dp>
)
