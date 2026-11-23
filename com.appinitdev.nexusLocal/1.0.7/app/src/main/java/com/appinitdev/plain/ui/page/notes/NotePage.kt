@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun NotePage(
    navController: NavHostController, initId: String, tagId: String,
    notesVM: NotesViewModel, tagsVM: TagsViewModel,
    noteVM: NoteViewModel = viewModel(), mdEditorVM: MdEditorViewModel = viewModel(),
) {
    val context = LocalContext.current
    val id = remember { mutableStateOf(initId) }
    val previewerState = rememberPreviewerState()
    val tagsState by tagsVM.itemsFlow.collectAsState()
    val tagsMapState by tagsVM.tagsMapFlow.collectAsState()
    val mdListState = rememberLazyListState()
    val editorScrollState = rememberScrollState()
    val focusRequester = remember { FocusRequester() }
    val shouldRequestFocus = remember { mutableStateOf(true) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(canScroll = { !noteVM.editMode })
    val tagIds = tagsMapState[id.value]?.map { it.tagId } ?: emptyList()
    NotePageEffects(id, tagId, noteVM, notesVM, tagsVM, mdEditorVM, previewerState)
    if (noteVM.showSelectTagsDialog.value) {
        val m = noteVM.item.value
        if (m != null) SelectTagsDialog(tagsVM, tagsState, tagsMapState, data = m) { noteVM.showSelectTagsDialog.value = false }
    }
    PScaffold(topBar = {
        PTopAppBar(navController = navController, title = "", scrollBehavior = scrollBehavior, actions = {
            if (noteVM.editMode) {
                PIconButton(icon = Res.drawable.undo, contentDescription = stringResource(Res.string.undo), enabled = mdEditorVM.textFieldState.undoState.canUndo,
                    tint = MaterialTheme.colorScheme.onSurface) { mdEditorVM.textFieldState.undoState.undo() }
                PIconButton(icon = Res.drawable.redo, contentDescription = stringResource(Res.string.redo), enabled = mdEditorVM.textFieldState.undoState.canRedo,
                    tint = MaterialTheme.colorScheme.onSurface) { mdEditorVM.textFieldState.undoState.redo() }
                PIconButton(icon = Res.drawable.wrap_text, contentDescription = stringResource(Res.string.wrap_content),
                    tint = MaterialTheme.colorScheme.onSurface) { mdEditorVM.toggleWrapContent(navController.context) }
            } else if (id.value.isNotEmpty()) {
                ActionButtonTags { noteVM.showSelectTagsDialog.value = true }
                PIconButton(
                    icon = Res.drawable.share_2,
                    contentDescription = stringResource(Res.string.share),
                    tint = MaterialTheme.colorScheme.onSurface,
                ) {
                    ShareHelper.shareText(context, noteVM.content)
                }
            }
            PIconButton(icon = if (noteVM.editMode) Res.drawable.markdown else Res.drawable.square_pen,
                contentDescription = stringResource(if (noteVM.editMode) Res.string.view else Res.string.edit),
                tint = MaterialTheme.colorScheme.onSurface) { noteVM.editMode = !noteVM.editMode }
        })
    }, modifier = Modifier.imePadding(), bottomBar = {
        AnimatedVisibility(visible = noteVM.editMode, enter = slideInVertically { it }, exit = slideOutVertically { it }) {
            MdEditorBottomAppBar(mdEditorVM)
        }
    }, content = { paddingValues ->
        if (noteVM.editMode) {
            MdEditor(modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding(), top = paddingValues.calculateTopPadding()),
                mdEditorVM = mdEditorVM, scrollState = editorScrollState, focusRequester = focusRequester,
                shouldRequestFocus = shouldRequestFocus.value, onFocusRequested = { shouldRequestFocus.value = false })
        } else {
            LazyColumn(modifier = Modifier.padding(top = paddingValues.calculateTopPadding()).nestedScroll(scrollBehavior.nestedScrollConnection), state = mdListState) {
                item {
                    val tags = tagsState.filter { tagIds.contains(it.id) }
                    if (tags.isNotEmpty()) {
                        PCard {
                            FlowRow(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                tags.forEach { tag ->
                                    Text(text = AnnotatedString("#" + tag.name), modifier = Modifier.wrapContentHeight().align(Alignment.Bottom),
                                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp, color = MaterialTheme.colorScheme.primary))
                                }
                            }
                        }
                        VerticalSpace(dp = 16.dp)
                    }
                }
                item { MarkdownText(text = noteVM.content, modifier = Modifier.padding(horizontal = PlainTheme.PAGE_HORIZONTAL_MARGIN), previewerState = previewerState) }
                item { BottomSpace(paddingValues) }
            }
        }
    })
    MediaPreviewer(state = previewerState)
}
