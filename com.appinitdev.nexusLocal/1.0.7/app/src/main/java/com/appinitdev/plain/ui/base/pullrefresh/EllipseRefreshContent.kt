
@Composable
fun RefreshLayoutState.EllipseRefreshContent(
    min: Dp = minDp,
    color: Color = Color.Black,
    content: @Composable (BoxScope.(RefreshLayoutState) -> Unit)? = null,
    innerContent: @Composable (BoxScope.(RefreshLayoutState) -> Unit)? = null,
) {
    val isHorizontal =
        remember(getComposePositionState()) { getComposePositionState().value.isHorizontal() }
    val density = LocalDensity.current
    val min_2 = remember(min) { min / 2 }
    val min_4 = remember(min) { min / 4 }
    Box(
        modifier =
            if (isHorizontal) {
                Modifier
                    .fillMaxHeight()
                    .padding(horizontal = min_4)
            } else {
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = min_4)
            },
    ) {
        Box(
            modifier =
                Modifier
                    .let {
                        if (isHorizontal) {
                            it
                                .height(min)
                                .width(
                                    maxOf(
                                        density.run { abs(getRefreshContentOffset()).toDp() } - min_2,
                                        min,
                                    ),
                                )
                        } else {
                            it
                                .width(min)
                                .height(
                                    maxOf(
                                        density.run { abs(getRefreshContentOffset()).toDp() } - min_2,
                                        min,
                                    ),
                                )
                        }
                    }
                    .border(
                        border =
                            BorderStroke(
                                width = 2.dp,
                                color = color,
                            ),
                        shape = CircleShape,
                    )
                    .align(Alignment.Center),
        ) {
            innerContent?.invoke(this, this@EllipseRefreshContent)
        }
        content?.invoke(this, this@EllipseRefreshContent)
    }
}

private val minDp = 40.dp
