 

















@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivedLabelsScreen(
    onNavigateBack: () -> Unit,
    viewModel: LabelsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val labels = uiState.archivedLabels

    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var labelToDelete by remember { mutableStateOf("") }

    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(R.string.labels_archived_labels_title),
                onNavigateBack = onNavigateBack,
                showSeparator = listState.canScrollBackward,
            )
        },
        content = {
            LazyColumn(
                state = listState,
                modifier =
                    Modifier
                        .padding(it)
                        .fillMaxSize(),
            ) {
                items(labels, key = Label::name) { label ->
                    ArchivedLabelListItem(
                        Modifier.animateItem(),
                        label = label,
                        onUnarchive = { viewModel.setArchived(label.name, false) },
                        onDelete = {
                            labelToDelete = label.name
                            showDeleteConfirmationDialog = true
                        },
                        enableUnarchive = uiState.isPro,
                        onLastItemUnarchive =
                            if (labels.size == 1) {
                                onNavigateBack
                            } else {
                                null
                            },
                    )
                }
            }
            if (showDeleteConfirmationDialog) {
                ConfirmationDialog(
                    title = stringResource(R.string.labels_delete, labelToDelete),
                    subtitle = stringResource(R.string.labels_delete_desc),
                    onConfirm = {
                        val lastLabelDeleted = labels.size == 1
                        viewModel.deleteLabel(labelToDelete)
                        showDeleteConfirmationDialog = false
                        if (lastLabelDeleted) {
                            onNavigateBack()
                        }
                    },
                    onDismiss = { showDeleteConfirmationDialog = false },
                )
            }
        },
    )
}

@Composable
fun ArchivedLabelListItem(
    modifier: Modifier,
    enableUnarchive: Boolean,
    label: Label,
    onUnarchive: () -> Unit,
    onDelete: () -> Unit,
    onLastItemUnarchive: (() -> Unit)? = null,
) {
    val labelName = label.name

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(4.dp),
    ) {
        Icon(
            modifier =
                Modifier
                    .padding(8.dp),
            imageVector = Icons.AutoMirrored.Outlined.Label,
            contentDescription = null,
            tint = MaterialTheme.getLabelColor(label.colorIndex),
        )
        Text(
            text = label.name,
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.weight(1f))

        var dropDownMenuExpanded by remember { mutableStateOf(false) }
        Box {
            IconButton(onClick = { dropDownMenuExpanded = true }) {
                Icon(
                    EvaIcons.Outline.MoreVertical,
                    contentDescription = stringResource(R.string.labels_more_about, labelName),
                )
            }

            BetterDropdownMenu(
                expanded = dropDownMenuExpanded,
                onDismissRequest = { dropDownMenuExpanded = false },
            ) {
                val paddingModifier = Modifier.padding(end = 32.dp)
                DropdownMenuItem(
                    enabled = enableUnarchive,
                    modifier = firstMenuItemModifier,
                    text = {
                        Text(
                            modifier = paddingModifier,
                            text = stringResource(R.string.labels_unarchive),
                        )
                    },
                    onClick = {
                        onUnarchive()
                        dropDownMenuExpanded = false
                        onLastItemUnarchive?.invoke()
                    },
                    leadingIcon = {
                        Icon(
                            EvaIcons.Outline.Undo,
                            contentDescription = "${stringResource(R.string.labels_unarchive)} $labelName",
                        )
                    },
                )
                DropdownMenuItem(
                    modifier = lastMenuItemModifier,
                    text = {
                        Text(
                            modifier = paddingModifier,
                            text = stringResource(R.string.main_delete),
                        )
                    },
                    onClick = {
                        onDelete()
                        dropDownMenuExpanded = false
                    },
                    leadingIcon = {
                        Icon(
                            EvaIcons.Outline.Trash,
                            contentDescription = "${stringResource(R.string.main_delete)} $labelName",
                        )
                    },
                )
            }
        }
    }
}
