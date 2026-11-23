@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AudioListItem(
    item: DAudio, audioVM: AudioViewModel, audioPlaylistVM: AudioPlaylistViewModel,
    tagsVM: TagsViewModel, castVM: CastViewModel, tags: List<DTag>,
    pagerState: PagerState, dragSelectState: DragSelectState,
    isCurrentlyPlaying: Boolean = false, isInPlaylist: Boolean = false,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var animatingButton by remember { mutableStateOf(false) }
    val castItems by CastPlayer.items.collectAsState()
    val currentUri by CastPlayer.currentUri.collectAsState()
    val castPlaying by CastPlayer.isPlaying.collectAsState()
    val isCurrentlyPlayingByCast = currentUri == item.path && castPlaying
    val isCurrentItemLoading = castVM.isLoading.value && currentUri == item.path
    val rotation by animateFloatAsState(
        targetValue = if (animatingButton) 90f else 0f,
        animationSpec = tween(durationMillis = 400), label = "icon_rotation"
    )
    val iconResource = if (isInPlaylist) Res.drawable.playlist_remove else Res.drawable.playlist_add
    val iconColor = if (isInPlaylist) MaterialTheme.colorScheme.red else MaterialTheme.colorScheme.primary
    val selected = remember(item.id, dragSelectState.selectedIds, audioVM.selectedItem.value) {
        dragSelectState.isSelected(item.id) || audioVM.selectedItem.value?.id == item.id
    }
    Surface(
        modifier = PlainTheme.getCardModifier(selected = selected)
            .combinedClickable(
                onClick = {
                    if (dragSelectState.selectMode) { dragSelectState.select(item.id) }
                    else if (castVM.castMode.value) { castVM.cast(item) }
                    else {
                        Permissions.checkNotification(context, Res.string.audio_notification_prompt) {
                            scope.launch(Dispatchers.IO) { audioPlaylistVM.playAsync(context, item) }
                        }
                    }
                },
                onLongClick = { if (!dragSelectState.selectMode) audioVM.selectedItem.value = item },
            ),
        color = Color.Unspecified,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp, 8.dp, 8.dp, 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AudioListItemLeadingIcon(
                item = item, dragSelectState = dragSelectState, castMode = castVM.castMode.value,
                isCurrentItemLoading = isCurrentItemLoading, isCurrentlyPlayingByCast = isCurrentlyPlayingByCast,
                isCurrentlyPlaying = isCurrentlyPlaying,
            )
            HorizontalSpace(dp = 12.dp)
            Column(modifier = Modifier.weight(1f).padding(vertical = 8.dp)) {
                Text(text = item.title, style = MaterialTheme.typography.listItemTitle())
                VerticalSpace(dp = 8.dp)
                Text(text = item.getSubtitle(), style = MaterialTheme.typography.listItemSubtitle())
                if (tags.isNotEmpty()) {
                    VerticalSpace(dp = 8.dp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        tags.forEach { tag ->
                            ClickableText(
                                text = AnnotatedString("#" + tag.name),
                                modifier = Modifier.wrapContentHeight().padding(end = 8.dp),
                                style = MaterialTheme.typography.listItemTag(),
                                onClick = {
                                    if (dragSelectState.selectMode) return@ClickableText
                                    val idx = tagsVM.itemsFlow.value.indexOfFirst { it.id == tag.id }
                                    if (idx != -1) scope.launch { pagerState.scrollToPage(idx + if (AppFeatureType.MEDIA_TRASH.has()) 2 else 1) }
                                }
                            )
                        }
                    }
                }
            }
            if (!dragSelectState.selectMode) {
                AudioListItemActions(
                    item = item, audioPlaylistVM = audioPlaylistVM, castMode = castVM.castMode.value,
                    castItems = castItems, isInPlaylist = isInPlaylist, iconResource = iconResource,
                    iconColor = iconColor, rotation = rotation,
                    onAnimStart = { animatingButton = true }, onAnimEnd = { animatingButton = false },
                )
            }
        }
    }
}
