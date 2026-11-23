
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : IData> SortAndBrowseDialog(
    context: Context,
    mediaVM: BaseMediaViewModel<T>,
    tagsVM: TagsViewModel,
    sortByEntries: List<FileSortBy>,
    onSortSelected: (FileSortBy) -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val isDocs = mediaVM.dataType == DataType.DOC
    val docsVM = (mediaVM as? DocsViewModel)

    AlertDialog(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = onDismiss,
        title = null,
        text = {
            LazyColumn {
                // View Mode section (only for docs)
                if (isDocs && docsVM != null) {
                    item {
                        Text(
                            text = stringResource(Res.string.view_mode),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    // Browse by tags option
                    item {
                        PDialogRadioRow(
                            selected = docsVM.tabsShowTags.value,
                            onClick = {
                                scope.launch(Dispatchers.IO) {
                                    DocTabsModePreference.putAsync(true)
                                    docsVM.tabsShowTags.value = true
                                    docsVM.fileType.value = ""
                                    docsVM.tag.value = null
                                    docsVM.trash.value = false
                                    docsVM.loadAsync(context, tagsVM)
                                }
                            },
                            text = stringResource(Res.string.browse_by_tags)
                        )
                    }
                    // Browse by file type option
                    item {
                        PDialogRadioRow(
                            selected = !docsVM.tabsShowTags.value,
                            onClick = {
                                scope.launch(Dispatchers.IO) {
                                    DocTabsModePreference.putAsync(false)
                                    docsVM.tabsShowTags.value = false
                                    docsVM.fileType.value = ""
                                    docsVM.tag.value = null
                                    docsVM.trash.value = false
                                    docsVM.loadAsync(context, tagsVM)
                                }
                            },
                            text = stringResource(Res.string.browse_by_file_type)
                        )
                    }
                    // Spacing between sections
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Sort rules section
                item {
                    Text(
                        text = stringResource(Res.string.sort),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Sort options
                items(sortByEntries) { sortByOption ->
                    PDialogRadioRow(
                        selected = mediaVM.sortBy.value == sortByOption,
                        onClick = {
                            mediaVM.sortBy.value = sortByOption
                            onSortSelected(sortByOption)
                        },
                        text = stringResource(sortByOption.getTextId())
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(text = stringResource(Res.string.close))
            }
        },
        dismissButton = {},
    )
}
