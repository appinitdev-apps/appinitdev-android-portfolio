@Composable
fun RefreshLayoutState.PullToRefreshContent(
    createText: @Composable (RefreshContentState) -> String = {
        when (it) {
            RefreshContentState.Failed -> stringResource(Res.string.srl_header_failed)
            RefreshContentState.Finished -> stringResource(Res.string.srl_header_finish)
            RefreshContentState.Refreshing -> stringResource(Res.string.srl_header_refreshing)
            RefreshContentState.Dragging -> {
                if (abs(getRefreshContentOffset()) < getRefreshContentThreshold()) {
                    stringResource(Res.string.srl_header_pulling)
                } else {
                    stringResource(Res.string.srl_header_release)
                }
            }
        }
    }
) {
    val refreshContentState by remember {
        getRefreshContentState()
    }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = createText(refreshContentState),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.secondaryTextColor,
        )
    }
}
