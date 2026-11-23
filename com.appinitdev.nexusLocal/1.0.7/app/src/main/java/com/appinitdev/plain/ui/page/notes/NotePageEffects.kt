@OptIn(FlowPreview::class)
@Composable
internal fun NotePageEffects(
    id: MutableState<String>, tagId: String, noteVM: NoteViewModel,
    notesVM: NotesViewModel, tagsVM: TagsViewModel, mdEditorVM: MdEditorViewModel,
    previewerState: MediaPreviewerState,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val view = LocalView.current
    val window = (view.context as Activity).window
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val insetsController = WindowCompat.getInsetsController(window, view)
    LaunchedEffect(Unit) {
        tagsVM.dataType.value = DataType.NOTE
        noteVM.editMode = id.value.isEmpty()
        mdEditorVM.load(context)
        scope.launch(Dispatchers.IO) {
            if (id.value.isNotEmpty()) {
                val item = NoteHelper.getById(id.value)
                noteVM.item.value = item
                noteVM.content = item?.content ?: ""
                mdEditorVM.textFieldState.edit { append(noteVM.content); setSelection(0) }
            }
            snapshotFlow { mdEditorVM.textFieldState.text }.debounce(200).collectLatest { t ->
                val isNew = id.value.isEmpty()
                val text = t.toString()
                if (noteVM.content == text) return@collectLatest
                scope.launch(Dispatchers.IO) {
                    val newItem = NoteHelper.addOrUpdateAsync(id.value) {
                        title = text.cut(250).replace("\n", ""); content = text; noteVM.content = text
                    }
                    id.value = newItem.id
                    if (isNew && tagId.isNotEmpty()) {
                        TagHelper.addTagRelations(arrayListOf(TagRelationStub(id.value).toTagRelation(tagId, DataType.NOTE)))
                    }
                    if (isNew) tagsVM.loadAsync(setOf(id.value))
                    notesVM.updateItem(newItem)
                }
            }
        }
    }
    DisposableEffect(Unit) { onDispose { insetsController.show(WindowInsetsCompat.Type.navigationBars()) } }
    BackHandler(previewerState.visible) { scope.launch { previewerState.close() } }
    LaunchedEffect(noteVM.editMode) {
        if (noteVM.editMode) keyboardController?.show()
        else { keyboardController?.hide(); focusManager.clearFocus() }
    }
    SideEffect {
        if (noteVM.editMode && context.isGestureInteractionMode()) insetsController.hide(WindowInsetsCompat.Type.navigationBars())
        else insetsController.show(WindowInsetsCompat.Type.navigationBars())
    }
}
