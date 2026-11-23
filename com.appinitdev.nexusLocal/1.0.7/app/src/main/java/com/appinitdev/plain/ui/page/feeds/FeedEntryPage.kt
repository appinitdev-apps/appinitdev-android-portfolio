@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun FeedEntryPage(navController: NavHostController, id: String, tagsVM: TagsViewModel, feedEntryVM: FeedEntryViewModel = viewModel()) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val tagsState by tagsVM.itemsFlow.collectAsState()
    val tagsMapState by tagsVM.tagsMapFlow.collectAsState()
    val tagIds = tagsMapState[id]?.map { it.tagId } ?: emptyList()
    val scrollState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(canScroll = { scrollState.firstVisibleItemIndex > 0 })
    val previewerState = rememberPreviewerState()
    val topRefreshLayoutState = rememberRefreshLayoutState {
        scope.launch {
            feedEntryVM.item.value?.let { m -> val r = withIO { m.fetchContentAsync() }; if (r.isOk()) { feedEntryVM.content.value = m.content; setRefreshState(RefreshContentState.Finished) } else { setRefreshState(RefreshContentState.Failed); DialogHelper.showErrorDialog(r.errorMessage()) } }
                .also { if (it == null) setRefreshState(RefreshContentState.Finished) }
        }
    }
    LaunchedEffect(Unit) {
        tagsVM.dataType.value = DataType.FEED_ENTRY
        scope.launch(Dispatchers.IO) { feedEntryVM.item.value = FeedEntryHelper.getAsync(id); val m = feedEntryVM.item.value ?: return@launch; feedEntryVM.content.value = m.content; feedEntryVM.feed.value = FeedHelper.getById(m.feedId) }
    }
    if (feedEntryVM.showSelectTagsDialog.value) { feedEntryVM.item.value?.let { m -> SelectTagsDialog(tagsVM, tagsState, tagsMapState, data = m) { feedEntryVM.showSelectTagsDialog.value = false } } }
    BackHandler(previewerState.visible) { scope.launch { previewerState.close() } }
    PScaffold(topBar = { FeedEntryTopBar(navController, feedEntryVM, scrollBehavior, scope, context) { scope.launch { scrollState.scrollToItem(0) } } }, modifier = Modifier.imePadding(),
        content = { paddingValues ->
            val m = feedEntryVM.item.value ?: return@PScaffold
            PullToRefresh(modifier = Modifier.padding(top = paddingValues.calculateTopPadding()), refreshLayoutState = topRefreshLayoutState,
                refreshContent = remember { { PullToRefreshContent(createText = { when (it) { RefreshContentState.Failed -> stringResource(Res.string.fetch_failed); RefreshContentState.Finished -> stringResource(Res.string.fetched); RefreshContentState.Refreshing -> stringResource(Res.string.fetching_content); RefreshContentState.Dragging -> if (abs(getRefreshContentOffset()) < getRefreshContentThreshold()) stringResource(Res.string.pull_down_to_fetch_content) else stringResource(Res.string.release_to_fetch) } }) } }) {
                LazyColumn(Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection), state = scrollState) {
                    item {
                        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(horizontal = 8.dp).clip(RoundedCornerShape(PlainTheme.CARD_RADIUS)).combinedClickable(onDoubleClick = { navController.navigateText("JSON", jsonEncode(m, pretty = true), "json") }, onClick = { WebHelper.open(context, m.url) })) {
                            Text(text = m.title, modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold))
                        }
                    }
                    item {
                        VerticalSpace(dp = 8.dp)
                        val tags = tagsState.filter { tagIds.contains(it.id) }
                        FlowRow(modifier = Modifier.padding(horizontal = PlainTheme.PAGE_HORIZONTAL_MARGIN), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = arrayOf(feedEntryVM.feed.value?.name ?: "", m.author, m.publishedAt.timeAgo()).filter { it.isNotEmpty() }.joinToString(" \u00b7 "), style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp, color = MaterialTheme.colorScheme.secondaryTextColor))
                            tags.forEach { tag -> Text(text = AnnotatedString("#" + tag.name), modifier = Modifier.wrapContentHeight().align(Alignment.Bottom), style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)) }
                        }
                        VerticalSpace(dp = 16.dp)
                    }
                    item { MarkdownText(text = feedEntryVM.content.value.ifEmpty { m.description }, modifier = Modifier.padding(horizontal = PlainTheme.PAGE_HORIZONTAL_MARGIN), previewerState = previewerState) }
                    if (feedEntryVM.content.value.isEmpty() && topRefreshLayoutState.refreshContentState.value == RefreshContentState.Finished) {
                        item {
                            VerticalSpace(dp = 32.dp)
                            if (feedEntryVM.fetchingContent.value) { Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) { CircularProgressIndicator(modifier = Modifier.size(32.dp), color = MaterialTheme.colorScheme.primary, strokeWidth = 3.dp) } }
                            else { POutlinedButton(text = stringResource(Res.string.load_full_content), block = true, modifier = Modifier.padding(horizontal = PlainTheme.PAGE_HORIZONTAL_MARGIN), enabled = !feedEntryVM.fetchingContent.value, onClick = { scope.launch { feedEntryVM.item.value?.let { mm -> feedEntryVM.fetchingContent.value = true; val r = withIO { mm.fetchContentAsync() }; feedEntryVM.fetchingContent.value = false; if (r.isOk()) feedEntryVM.content.value = mm.content else DialogHelper.showErrorDialog(r.errorMessage()) } } }) }
                        }
                    }
                    item { BottomSpace(paddingValues) }
                }
            }
        })
    MediaPreviewer(state = previewerState)
}
