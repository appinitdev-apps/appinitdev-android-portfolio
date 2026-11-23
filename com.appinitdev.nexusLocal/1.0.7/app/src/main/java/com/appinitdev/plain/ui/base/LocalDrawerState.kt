
val LocalDrawerState = compositionLocalOf<DrawerState?> { null }

/**
 * When pager is on the first page, forward rightward drags to the drawer.
 * Leftward drags (to next tab) are consumed normally by the pager.
 */
class DrawerNestedScrollConnection(
    private val pagerState: PagerState,
    private val drawerState: DrawerState,
    private val scope: CoroutineScope,
) : NestedScrollConnection {

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        // Only intercept when on first page and dragging right (opening drawer)
        if (source == NestedScrollSource.UserInput &&
            pagerState.currentPage == 0 &&
            !pagerState.isScrollInProgress &&
            available.x > 0 &&
            drawerState.currentValue == DrawerValue.Closed
        ) {
            scope.launch { drawerState.open() }
            return available
        }
        return Offset.Zero
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        return Velocity.Zero
    }
}
