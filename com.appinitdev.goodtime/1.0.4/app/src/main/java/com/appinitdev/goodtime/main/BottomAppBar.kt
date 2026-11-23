 

















@Composable
fun BottomAppBar(
    modifier: Modifier,
    badgeItemCount: Int,
    hide: Boolean,
    labelData: LabelData,
    sessionCountToday: Int,
    onShowSheet: () -> Unit,
    onLabelClick: () -> Unit,
    navController: NavController,
) {
    val haptic = LocalHapticFeedback.current
    AnimatedVisibility(
        modifier = modifier,
        visible = !hide,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        val isDefaultLabel = labelData.name == Label.DEFAULT_LABEL_NAME
        val color = MaterialTheme.getLabelColor(labelData.colorIndex)

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onShowSheet()
                },
                modifier = Modifier.size(56.dp)
            ) {
                BadgedBoxWithCount(count = badgeItemCount, modifier = Modifier.padding(4.dp)) {
                    Icon(
                        imageVector = EvaIcons.Outline.Menu2,
                        contentDescription = stringResource(R.string.main_open_app_menu),
                        modifier = Modifier.size(32.dp)

                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))

            val onNavigateToSelectLabelDialog = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onLabelClick()
            }

            if (isDefaultLabel) {
                IconButton(onClick = onNavigateToSelectLabelDialog) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Label,
                        contentDescription = stringResource(R.string.labels_title),
                        tint = color,
                        modifier = Modifier.size(32.dp)
                    )
                }
            } else {
                Row(modifier = Modifier.padding(horizontal = 4.dp)) {
                    LabelChip(
                        name = labelData.name,
                        color = color,
                        selected = true,
                        showIcon = true,
                    ) { onNavigateToSelectLabelDialog() }
                }
            }


            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    navController.navigate(StatsDest)
                },
                modifier = Modifier.size(56.dp)
            ) {
                BadgedBoxWithCountDown(count = sessionCountToday, modifier = Modifier.padding(4.dp)) {
                    Icon(
                        imageVector = EvaIcons.Outline.PieChart,
                        contentDescription = stringResource(R.string.stats_title),
                        modifier = Modifier.size(32.dp)

                    )
                }
            }








































        }
    }
}
