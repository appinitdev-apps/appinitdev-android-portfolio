
val LocalDrawerState = compositionLocalOf<DrawerState?> { null }

 



class DrawerNestedScrollConnection(
    private val pagerState: PagerState,
    private val drawerState: DrawerState,
    private val scope: CoroutineScope,
) : NestedScrollConnection {

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        
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
