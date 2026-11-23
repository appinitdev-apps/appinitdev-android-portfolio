internal class RefreshLayoutNestedScrollConnection(
    private val composePosition: ComposePosition,
    private val refreshLayoutState: RefreshLayoutState,
    private val dragEfficiency: Float,
    private val orientationIsHorizontal: Boolean,
    private val refreshingCanScroll: Boolean = false,
) : NestedScrollConnection {
    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        if (source == NestedScrollSource.UserInput) {
            when (composePosition) {
                ComposePosition.Start -> {
                    val value = available.x
                    if (value > 0) {
                        if (value > 0.01f)
                            refreshLayoutState.offset(value * dragEfficiency)
                        return Offset(value, 0f)
                    }
                }
                ComposePosition.End -> {
                    val value = available.x
                    if (value < 0) {
                        if (value < -0.01f)
                            refreshLayoutState.offset(value * dragEfficiency)
                        return Offset(value, 0f)
                    }
                }
                ComposePosition.Top -> {
                    val value = available.y
                    if (value > 0) {
                        if (value > 0.01f)
                            refreshLayoutState.offset(value * dragEfficiency)
                        return Offset(0f, value)
                    }
                }
                ComposePosition.Bottom -> {
                    val value = available.y
                    if (value < 0) {
                        if (value < -0.01f)
                            refreshLayoutState.offset(value * dragEfficiency)
                        return Offset(0f, value)
                    }
                }
            }
        }
        return Offset.Zero
    }
    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        if (!refreshingCanScroll && refreshLayoutState.refreshContentState.value == RefreshContentState.Refreshing) {
            return if (orientationIsHorizontal)
                Offset(available.x, 0f)
            else
                Offset(0f, available.y)
        }
        val refreshOffset = refreshLayoutState.refreshContentOffsetState.value
        if (source == NestedScrollSource.UserInput) {
            when (composePosition) {
                ComposePosition.Start -> {
                    if (available.x < 0 && refreshOffset > 0) {
                        var consumptive = available.x
                        if (-available.x > refreshOffset) {
                            consumptive = available.x - refreshOffset
                        }
                        refreshLayoutState.offset(consumptive * dragEfficiency)
                        return Offset(consumptive, 0f)
                    }
                }
                ComposePosition.End -> {
                    if (available.x > 0 && refreshOffset < 0) {
                        var consumptive = available.x
                        if (-available.x > refreshOffset) {
                            consumptive = available.x - refreshOffset
                        }
                        refreshLayoutState.offset(consumptive * dragEfficiency)
                        return Offset(consumptive, 0f)
                    }
                }
                ComposePosition.Top -> {
                    if (available.y < 0 && refreshOffset > 0) {
                        var consumptive = available.y
                        if (-available.y > refreshOffset) {
                            consumptive = available.y - refreshOffset
                        }
                        refreshLayoutState.offset(consumptive * dragEfficiency)
                        return Offset(0f, consumptive)
                    }
                }
                ComposePosition.Bottom -> {
                    if (available.y > 0 && refreshOffset < 0) {
                        var consumptive = available.y
                        if (-available.y < refreshOffset) {
                            consumptive = available.y - refreshOffset
                        }
                        refreshLayoutState.offset(consumptive * dragEfficiency)
                        return Offset(0f, consumptive)
                    }
                }
            }
        }
        return Offset.Zero
    }
    override suspend fun onPreFling(available: Velocity): Velocity {
        if (!refreshingCanScroll && refreshLayoutState.refreshContentState.value == RefreshContentState.Refreshing) {
            return available
        }
        if (refreshLayoutState.refreshContentOffsetState.value != 0f) {
            refreshLayoutState.offsetHoming()
            return available
        }
        return Velocity.Zero
    }
}
