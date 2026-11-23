
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TagsBottomSheet(
    tagsVM: TagsViewModel,
    onDismissRequest: () -> Unit
) {
    val itemsState by tagsVM.itemsFlow.collectAsState()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = rememberCoroutineScope()

    PModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        Column {
            PBottomSheetTopAppBar(
                title = stringResource(Res.string.tags),
                actions = {
                    ActionButtonRefresh(
                        loading = tagsVM.showLoading.value,
                        onClick = {
                            tagsVM.showLoading.value = true
                            scope.launch {
                                withIO { tagsVM.loadAsync() }
                            }
                        }
                    )
                    ActionButtonAdd { tagsVM.showAddDialog() }
                }
            )

            if (itemsState.isNotEmpty()) {
                LazyColumn(
                    Modifier.fillMaxSize()
                ) {
                    item {
                        TopSpace()
                    }
                    items(itemsState) { tag ->
                        PListItem(
                            modifier = PlainTheme
                                .getCardModifier()
                                .clickable {
                                    tagsVM.showEditDialog(tag)
                                },
                            title = tag.name,
                            action = {
                                PIconButton(
                                    icon = Res.drawable.delete_forever,
                                    tint = MaterialTheme.colorScheme.red,
                                    contentDescription = stringResource(Res.string.delete),
                                    click = {
                                        DialogHelper.confirmToDelete {
                                            tagsVM.deleteTag(tag.id)
                                        }
                                    }
                                )
                            }
                        )
                        VerticalSpace(dp = 8.dp)
                    }
                }
            } else {
                NoDataColumn(loading = tagsVM.showLoading.value)
            }
        }
    }

    TagNameDialog(tagsVM)
} 