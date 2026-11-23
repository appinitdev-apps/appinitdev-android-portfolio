/**
 *     Goodtime Productivity
 *     Copyright (C) 2025 Adrian Cotfas
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditLabelScreen(
    viewModel: AddEditLabelViewModel = koinViewModel(),
    labelName: String,
    onNavigateToDefault: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val isEditMode = labelName.isNotEmpty()
    val context = LocalContext.current

    val listState = rememberScrollState()

    LaunchedEffect(labelName) {
        val defaultLabelDisplayName = context.getString(R.string.labels_default_label_name)
        viewModel.init(labelName, defaultLabelDisplayName)
    }

    if (uiState.isLoading) return

    val label = uiState.tmpLabel
    val labelNameToDisplay = label.name

    val followDefault = label.useDefaultTimeProfile
    var showBreakBudgetInfoDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopBar(
                onNavigateBack = onNavigateBack,
                icon = Icons.AutoMirrored.Default.ArrowBack,
                title = {
                    LabelNameRow(
                        isAddingNewLabel = !isEditMode,
                        labelName = labelNameToDisplay,
                        onValueChange = {
                            val newLabelName = it
                            viewModel.updateTmpLabel(
                                uiState.tmpLabel.copy(name = newLabelName),
                                resetProfile = false,
                            )
                        },
                        showError = !uiState.labelNameIsValid(),
                    )
                },
                showSeparator = listState.canScrollBackward,
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(listState)
                    .background(MaterialTheme.colorScheme.background)
                    .imePadding(),
        ) {
            ColorSelectRow(
                selectedIndex = label.colorIndex.toInt(),
            ) {
                viewModel.updateTmpLabel(label.copy(colorIndex = it.toLong()), resetProfile = false)
            }
            Spacer(modifier = Modifier.height(16.dp))
            ListItem(
                modifier =
                    Modifier.clickable {
                        viewModel.updateTmpLabel(
                            label.copy(useDefaultTimeProfile = !followDefault),
                            resetProfile = false,
                        )
                    },
                leadingContent = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilledTonalIconButton(
                            onClick = {
                                onNavigateToDefault()
                            },
                        ) {
                            Icon(
                                imageVector = EvaIcons.Outline.Navigation2,
                                contentDescription = stringResource(R.string.labels_default_label_name),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        VerticalDivider(modifier = Modifier.height(32.dp))
                    }
                },
                headlineContent = {
                    Text(stringResource(R.string.labels_follow_default_time_profile))
                },
                trailingContent = {
                    Switch(
                        checked = followDefault,
                        onCheckedChange = {
                            viewModel.updateTmpLabel(
                                label.copy(useDefaultTimeProfile = it),
                                resetProfile = false,
                            )
                        },
                    )
                },
            )
            AnimatedVisibility(!followDefault) {
                Column {
                    TimerProfileSettings(
                        timerProfile = label.timerProfile,
                        timerProfiles = uiState.timerProfiles,
                        onTimerProfileChange = { updated ->
                            viewModel.updateTmpLabel(label.copy(timerProfile = updated))
                        },
                        onTimerProfileSelect = { selected ->
                            viewModel.updateTmpLabel(label.copy(timerProfile = selected), resetProfile = false)
                        },
                        onBreakBudgetInfo = { showBreakBudgetInfoDialog = true },
                    )
                }
            }
            AnimatedVisibility(visible = uiState.labelToEdit != label) {
                Button(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    enabled = uiState.labelNameIsValid(),
                    onClick = {
                        if (isEditMode) {
                            viewModel.updateLabel(labelName, label)
                        } else {
                            viewModel.addLabel(label)
                        }
                        onNavigateBack()
                    },
                ) {
                    Text(stringResource(R.string.main_save))
                }
            }
        }
        if (showBreakBudgetInfoDialog) {
            BreakBudgetInfoDialog(
                onDismiss = { showBreakBudgetInfoDialog = false },
            )
        }
    }
}

@Composable
private fun LabelNameRow(
    isAddingNewLabel: Boolean,
    labelName: String,
    onValueChange: (String) -> Unit,
    showError: Boolean,
) {
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier =
            Modifier
                .animateContentSize(),
    ) {
        val internalModifier =
            if (isAddingNewLabel) {
                Modifier.focusRequester(focusRequester)
            } else {
                Modifier
            }

        Box {
            BasicTextField(
                modifier =
                    internalModifier
                        .fillMaxWidth()
                        .clearFocusOnKeyboardDismiss(),
                textStyle =
                    MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        textDecoration = TextDecoration.Underline,
                    ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                singleLine = true,
                value = labelName,
                onValueChange = {
                    if (it.length <= LABEL_NAME_MAX_LENGTH) {
                        onValueChange(it)
                    }
                },
            )
            if (labelName.isEmpty()) {
                Text(
                    text =
                        stringResource(
                            R.string.labels_add_label_name,
                        ),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        AnimatedVisibility(showError && labelName.isNotEmpty()) {
            Text(
                text = stringResource(R.string.labels_label_name_already_exists),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        LaunchedEffect(labelName) {
            if (isAddingNewLabel) focusRequester.requestFocus()
        }
    }
}
