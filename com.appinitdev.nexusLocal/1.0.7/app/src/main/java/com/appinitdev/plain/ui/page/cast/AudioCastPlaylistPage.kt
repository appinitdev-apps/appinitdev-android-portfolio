@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioCastPlaylistPage(castVM: CastViewModel, onDismissRequest: () -> Unit) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val castItems by CastPlayer.items.collectAsState()
    val currentUri by CastPlayer.currentUri.collectAsState()
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        scope.launch(Dispatchers.IO) { CastPlayer.reorderItems(from.index, to.index) }
    }
    if (showClearConfirmDialog) {
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text(stringResource(Res.string.clear_all)) },
            text = { Text(stringResource(Res.string.clear_all_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = { scope.launch(Dispatchers.IO) { CastPlayer.clearItems(); showClearConfirmDialog = false } },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(Res.string.confirm)) }
            },
            dismissButton = { TextButton(onClick = { showClearConfirmDialog = false }) { Text(stringResource(Res.string.cancel)) } })
    }
    PModalBottomSheet(onDismissRequest = onDismissRequest, sheetState = sheetState) {
        Column {
            PBottomSheetTopAppBar(
                title = if (castItems.isNotEmpty()) LocaleHelper.getStringF(Res.string.playlist_title, "total", castItems.size) else stringResource(Res.string.cast_playlist),
                subtitle = if (castItems.isEmpty()) "" else stringResource(Res.string.drag_number_to_reorder_list),
                actions = {
                    if (castItems.isNotEmpty()) {
                        IconButton(onClick = { showClearConfirmDialog = true }) {
                            Icon(painter = painterResource(Res.drawable.delete_forever), contentDescription = "Clear playlist", tint = MaterialTheme.colorScheme.red)
                        }
                    }
                })
            if (castItems.isEmpty()) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), contentAlignment = Alignment.Center) {
                    Text(text = stringResource(Res.string.cast_playlist_empty), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.secondaryTextColor)
                }
            } else {
                LazyColumn(state = lazyListState, modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), contentPadding = PaddingValues(bottom = 96.dp)) {
                    itemsIndexed(castItems, { _, item -> item.path }) { index, audio ->
                        val isPlaying = currentUri == audio.path
                        ReorderableItem(reorderableLazyListState, key = audio.path) { isDragging ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { castVM.cast(audio) },
                                colors = CardDefaults.cardColors(containerColor = if (isPlaying) MaterialTheme.colorScheme.cardBackgroundActive else MaterialTheme.colorScheme.cardBackgroundNormal)
                            ) {
                                with(this@ReorderableItem) {
                                    AudioCastPlaylistItemContent(
                                        item = audio, index = index, isPlaying = isPlaying,
                                        isLoading = castVM.isLoading.value && isPlaying, onRemove = { scope.launch(Dispatchers.IO) { CastPlayer.removeItemAt(index) } })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
