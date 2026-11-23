 

















@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelsScreen(
    onNavigateToLabel: (Any) -> Unit,
    onNavigateToArchivedLabels: () -> Unit,
    onNavigateToPro: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: LabelsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    if (uiState.isLoading) return
    val labels = uiState.unarchivedLabels
    val activeLabelName = uiState.activeLabelName
    val defaultLabelName = stringResource(id = R.string.labels_default_label_name)

    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var labelToDelete by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    val dragDropState =
        rememberDragDropState(listState) { fromIndex, toIndex ->
            viewModel.rearrangeLabel(fromIndex, toIndex)
        }

    val activeLabelIndex = labels.indexOfFirst { it.name == activeLabelName }
    if (labels.isNotEmpty()) {
        LaunchedEffect(Unit) {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index?.let {
                if (activeLabelIndex > it) {
                    listState.scrollToItem(activeLabelIndex)
                }
            }
        }
    }

    val showFab = listState.isScrollingUp()

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(R.string.labels_title),
                onNavigateBack = { onNavigateBack() },
                showSeparator = listState.canScrollBackward,
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                enter = slideInVertically(initialOffsetY = { it * 2 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it * 2 }) + fadeOut(),
                visible = showFab,
            ) {
                FloatingActionButton(
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    onClick = {
                        if (uiState.isPro) {
                            onNavigateToLabel(AddEditLabelDest(name = ""))
                        } else {
                            onNavigateToPro()
                        }
                    },
                ) {
                    Icon(EvaIcons.Outline.Plus, stringResource(R.string.labels_add_label))
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            AnimatedVisibility(uiState.archivedLabelCount > 0) {
                ListItem(
                    modifier =
                        Modifier.clickable {
                            onNavigateToArchivedLabels()
                        },
                    leadingContent = {
                        Icon(
                            imageVector = EvaIcons.Outline.Archive,
                            tint = MaterialTheme.colorScheme.secondary,
                            contentDescription = stringResource(R.string.labels_navigate_to_archived),
                        )
                    },
                    headlineContent = {
                        Text(
                            text = stringResource(R.string.labels_archived_labels_title),
                            style =
                                MaterialTheme.typography.labelLarge.copy(
                                    color = MaterialTheme.colorScheme.secondary,
                                ),
                        )
                    },
                    trailingContent = {
                        Text(
                            text = "${uiState.archivedLabelCount}",
                            style =
                                MaterialTheme.typography.labelLarge.copy(
                                    color = MaterialTheme.colorScheme.secondary,
                                ),
                        )
                    },
                )
            }
            if (labels.isEmpty()) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        modifier = Modifier.size(96.dp),
                        imageVector = Icons.AutoMirrored.Outlined.Label,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        contentDescription = stringResource(R.string.stats_no_items),
                    )
                    Text(
                        text = stringResource(R.string.stats_no_items),
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                    )
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
            ) {
                itemsIndexed(
                    labels,
                    key = { _, item -> item.name },
                ) { index, label ->
                    DraggableItem(dragDropState, index) { isDragging ->
                        LabelListItem(
                            label = label,
                            isDragging = isDragging,
                            dragModifier =
                                Modifier.dragContainer(
                                    dragDropState = dragDropState,
                                    key = label.name,
                                    onDragFinished = { viewModel.rearrangeLabelsToDisk() },
                                ),
                            onEdit = {
                                onNavigateToLabel(AddEditLabelDest(label.name))
                            },
                            onDuplicate = {
                                viewModel.duplicateLabel(
                                    if (label.isDefault()) defaultLabelName else label.name,
                                    label.isDefault(),
                                )
                            },
                            onArchive = { viewModel.setArchived(label.name, true) },
                            onDelete = {
                                labelToDelete = label.name
                                showDeleteConfirmationDialog = true
                            },
                        )
                    }
                }
            }
        }
        if (showDeleteConfirmationDialog) {
            ConfirmationDialog(
                title = stringResource(R.string.labels_delete, labelToDelete),
                subtitle = stringResource(R.string.labels_delete_label_confirmation),
                onConfirm = {
                    viewModel.deleteLabel(labelToDelete)
                    showDeleteConfirmationDialog = false
                },
                onDismiss = { showDeleteConfirmationDialog = false },
            )
        }
    }
}

@Composable
private fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableIntStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableIntStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}
