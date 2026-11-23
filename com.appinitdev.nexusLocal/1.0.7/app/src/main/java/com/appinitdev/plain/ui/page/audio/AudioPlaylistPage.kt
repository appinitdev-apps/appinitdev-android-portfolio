
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlaylistPage(audioPlaylistVM: AudioPlaylistViewModel, onDismissRequest: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isAudioPlaying by AudioPlayer.isPlayingFlow.collectAsState()
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        scope.launch(Dispatchers.IO) { audioPlaylistVM.reorder(context, from.index, to.index) }
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text(stringResource(Res.string.clear_all)) },
            text = { Text(stringResource(Res.string.clear_all_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = { scope.launch(Dispatchers.IO) { audioPlaylistVM.clearAsync(context); showClearConfirmDialog = false } },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(Res.string.confirm)) }
            },
            dismissButton = { TextButton(onClick = { showClearConfirmDialog = false }) { Text(stringResource(Res.string.cancel)) } },
        )
    }

    PModalBottomSheet(onDismissRequest = onDismissRequest, sheetState = sheetState) {
        Column {
            PBottomSheetTopAppBar(
                title = if (audioPlaylistVM.playlistItems.value.isNotEmpty())
                    LocaleHelper.getStringF(Res.string.playlist_title, "total", audioPlaylistVM.playlistItems.value.size)
                else stringResource(Res.string.playlist),
                subtitle = if (audioPlaylistVM.playlistItems.value.isEmpty()) "" else stringResource(Res.string.drag_number_to_reorder_list),
                actions = {
                    if (audioPlaylistVM.playlistItems.value.isNotEmpty()) {
                        IconButton(onClick = { showClearConfirmDialog = true }) {
                            Icon(painter = painterResource(Res.drawable.delete_forever), contentDescription = "Clear", tint = MaterialTheme.colorScheme.red)
                        }
                    }
                }
            )
            VerticalSpace(8.dp)
            if (audioPlaylistVM.playlistItems.value.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), contentAlignment = Alignment.Center
                ) {
                    Text(text = stringResource(Res.string.empty_playlist), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.secondaryTextColor)
                }
            } else {
                LazyColumn(
                    state = lazyListState, modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    itemsIndexed(audioPlaylistVM.playlistItems.value, { _, item -> item.path }) { index, audio ->
                        val isPlaying = isAudioPlaying && audioPlaylistVM.selectedPath.value == audio.path
                        ReorderableItem(reorderableLazyListState, key = audio.path) { isDragging ->
                            AudioPlaylistItemRow(
                                audio = audio, index = index, isPlaying = isPlaying,
                                audioPlaylistVM = audioPlaylistVM, scope = scope
                            )
                        }
                    }
                }
            }
        }
    }
}
