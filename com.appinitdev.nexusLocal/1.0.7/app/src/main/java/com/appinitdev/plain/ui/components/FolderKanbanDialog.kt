@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderKanbanDialog(filesVM: FilesViewModel, onDismiss: () -> Unit = {}) {
    val context = LocalContext.current
    val options = remember { mutableStateListOf<FolderOption>() }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val recentsText = stringResource(Res.string.recents)
    val internalStorageText = stringResource(Res.string.internal_storage)
    val sdcardText = stringResource(Res.string.sdcard)
    val usbStorageText = stringResource(Res.string.usb_storage)
    val fileTransferAssistantText = stringResource(Res.string.app_data)
    LaunchedEffect(Unit) {
        val items = buildFolderOptions(context, filesVM, recentsText, internalStorageText, sdcardText, usbStorageText, fileTransferAssistantText)
        options.clear()
        options.addAll(items)
    }
    PModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column {
            PBottomSheetTopAppBar(title = stringResource(Res.string.folders))
            if (options.isNotEmpty()) {
                LazyColumn(Modifier.fillMaxSize()) {
                    item { TopSpace() }
                    items(options) { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { sendEvent(FolderKanbanSelectEvent(item)); onDismiss() }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = item.isChecked, onClick = { sendEvent(FolderKanbanSelectEvent(item)); onDismiss() })
                            HorizontalSpace(8.dp)
                            Text(text = item.title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                            if (item.isFavoriteFolder) {
                                PIconButton(icon = Res.drawable.delete_forever, tint = MaterialTheme.colorScheme.red, contentDescription = stringResource(Res.string.delete), click = {
                                    DialogHelper.confirmToDelete {
                                        scope.launch(Dispatchers.IO) { FavoriteFoldersPreference.removeAsync(item.fullPath); options.remove(item) }
                                    }
                                })
                            }
                        }
                    }
                    item { BottomSpace() }
                }
            } else {
                NoDataColumn(loading = true)
            }
        }
    }
}
