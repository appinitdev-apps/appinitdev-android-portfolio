@Composable
internal fun rememberScrollStateController(
    state: ScrollState,
    visibleLengthDp: Dp,
    thumbMinLength: Float,
    alwaysShowScrollBar: Boolean,
    selectionMode: ScrollbarSelectionMode
): ScrollStateController {
    val coroutineScope = rememberCoroutineScope()
    val visibleLengthDpUpdated = rememberUpdatedState(visibleLengthDp)
    val thumbMinLengthUpdated = rememberUpdatedState(thumbMinLength)
    val alwaysShowScrollBarUpdated = rememberUpdatedState(alwaysShowScrollBar)
    val selectionModeUpdated = rememberUpdatedState(selectionMode)
    val isSelected = remember { mutableStateOf(false) }
    val dragOffset = remember { mutableFloatStateOf(0f) }
    val fullLengthDp = with(LocalDensity.current) {
        remember {
            derivedStateOf {
                state.maxValue.toDp() + visibleLengthDpUpdated.value
            }
        }
    }
    val thumbSizeNormalizedReal = remember {
        derivedStateOf {
            if (fullLengthDp.value == 0.dp) 1f else {
                val normalizedDp = visibleLengthDpUpdated.value / fullLengthDp.value
                normalizedDp.coerceIn(0f, 1f)
            }
        }
    }
    val thumbSizeNormalized = remember {
        derivedStateOf {
            thumbSizeNormalizedReal.value.coerceAtLeast(thumbMinLengthUpdated.value)
        }
    }
    fun offsetCorrection(top: Float): Float {
        val topRealMax = 1f
        val topMax = (1f - thumbSizeNormalized.value).coerceIn(0f, 1f)
        return top * topMax / topRealMax
    }
    val thumbOffsetNormalized = remember {
        derivedStateOf {
            if (state.maxValue == 0) return@derivedStateOf 0f
            val normalized = state.value.toFloat() / state.maxValue.toFloat()
            offsetCorrection(normalized)
        }
    }
    val thumbIsInAction = remember {
        derivedStateOf {
            state.isScrollInProgress || isSelected.value || alwaysShowScrollBarUpdated.value
        }
    }
    return remember {
        ScrollStateController(
            thumbSizeNormalized = thumbSizeNormalized,
            thumbOffsetNormalized = thumbOffsetNormalized,
            thumbIsInAction = thumbIsInAction,
            _isSelected = isSelected,
            dragOffset = dragOffset,
            state = state,
            selectionMode = selectionModeUpdated,
            coroutineScope = coroutineScope
        )
    }
}
