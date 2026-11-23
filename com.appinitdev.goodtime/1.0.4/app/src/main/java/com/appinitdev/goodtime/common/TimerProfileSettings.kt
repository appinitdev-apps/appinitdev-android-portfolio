@Composable
fun TimerProfileSettings(
    timerProfile: TimerProfile,
    timerProfiles: List<TimerProfile>,
    onTimerProfileChange: (TimerProfile) -> Unit,
    onTimerProfileSelect: (TimerProfile) -> Unit,
    onEditProfiles: (() -> Unit)? = null,
    onBreakBudgetInfo: () -> Unit,
) {
    AnimatedVisibility(timerProfiles.isNotEmpty(), enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(modifier = Modifier.weight(1f)) {
                DropdownMenuBox(
                    contentModifier = Modifier.fillMaxWidth(),
                    colored = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    value = timerProfile.name ?: stringResource(R.string.labels_custom),
                    options = timerProfiles.mapNotNull { it.name },
                    onDismissRequest = {},
                    onDropdownMenuItemSelected = {
                        val selected = timerProfiles[it]
                        onTimerProfileSelect(selected)
                    },
                )
            }
            onEditProfiles?.let {
                IconButton(onClick = it) {
                    Icon(
                        imageVector = EvaIcons.Outline.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
    TimerTypeRow(
        isCountDown = timerProfile.isCountdown,
        onCountDownEnabled = {
            onTimerProfileChange(timerProfile.copy(isCountdown = it))
        },
    )
    AnimatedVisibility(timerProfile.isCountdown, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
        Column {
            EditableNumberListItem(
                title = stringResource(R.string.labels_focus_time),
                value = timerProfile.workDuration,
                onValueChange = {
                    onTimerProfileChange(timerProfile.copy(workDuration = it))
                },
            )
            EditableNumberListItem(
                title = stringResource(R.string.labels_break_time),
                value = timerProfile.breakDuration,
                onValueChange = {
                    onTimerProfileChange(timerProfile.copy(breakDuration = it))
                },
                enableSwitch = true,
                switchValue = timerProfile.isBreakEnabled,
                onSwitchChange = { enabled ->
                    val longEnabled = if (!enabled) false else timerProfile.isLongBreakEnabled
                    onTimerProfileChange(
                        timerProfile.copy(
                            isBreakEnabled = enabled,
                            isLongBreakEnabled = longEnabled,
                        ),
                    )
                },
            )
            EditableNumberListItem(
                title = stringResource(R.string.labels_long_break),
                value = timerProfile.longBreakDuration,
                onValueChange = {
                    onTimerProfileChange(timerProfile.copy(longBreakDuration = it))
                },
                enabled = timerProfile.isBreakEnabled,
                enableSwitch = true,
                switchValue = timerProfile.isLongBreakEnabled,
                onSwitchChange = { enabled ->
                    onTimerProfileChange(timerProfile.copy(isLongBreakEnabled = enabled))
                },
            )
            EditableNumberListItem(
                title = stringResource(R.string.labels_sessions_before_long_break),
                value = timerProfile.sessionsBeforeLongBreak,
                minValue = 2,
                maxValue = 8,
                enabled = timerProfile.isBreakEnabled && timerProfile.isLongBreakEnabled,
                onValueChange = {
                    onTimerProfileChange(timerProfile.copy(sessionsBeforeLongBreak = it))
                },
            )
        }
    }
    AnimatedVisibility(!timerProfile.isCountdown, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
        Column {
            val toggleBreak = {
                onTimerProfileChange(
                    timerProfile.copy(isBreakEnabled = !timerProfile.isBreakEnabled),
                )
            }
            ListItem(
                modifier =
                    Modifier.toggleable(
                        value = timerProfile.isBreakEnabled,
                        onValueChange = { toggleBreak() },
                    ),
                headlineContent = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                    ) {
                        Text(stringResource(R.string.labels_enable_break_budget))
                        IconButton(onClick = onBreakBudgetInfo) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = stringResource(R.string.labels_break_budget_info),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                },
                trailingContent = {
                    Checkbox(checked = timerProfile.isBreakEnabled, onCheckedChange = null)
                },
            )
            SliderListItem(
                title = stringResource(R.string.labels_focus_break_ratio),
                min = 2,
                max = 6,
                enabled = timerProfile.isBreakEnabled,
                value = timerProfile.workBreakRatio,
                showValue = true,
                onValueChange = {
                    onTimerProfileChange(timerProfile.copy(workBreakRatio = it))
                },
            )
        }
    }
}
@Composable
fun BreakBudgetInfoDialog(onDismiss: () -> Unit) {
    InfoDialog(
        title = stringResource(R.string.labels_break_budget_info),
        subtitle =
            "${stringResource(R.string.labels_break_budget_desc1)}\n" +
                stringResource(R.string.labels_break_budget_desc2),
    ) {
        onDismiss()
    }
}
