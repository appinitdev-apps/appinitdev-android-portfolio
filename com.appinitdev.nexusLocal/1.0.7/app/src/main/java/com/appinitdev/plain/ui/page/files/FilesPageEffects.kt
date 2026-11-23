
@Composable
internal fun FilesPageEffects(
    filesVM: FilesViewModel, context: Context, scope: CoroutineScope,
    folderPath: String, previewerState: MediaPreviewerState,
    audioPlaylistVM: AudioPlaylistViewModel,
) {
    BackHandler(enabled = previewerState.visible || filesVM.selectMode.value || filesVM.showSearchBar.value || filesVM.showPasteBar.value || filesVM.canNavigateBack()) {
        when {
            previewerState.visible -> scope.launch { previewerState.closeTransform() }
            filesVM.selectMode.value -> filesVM.exitSelectMode()
            filesVM.showSearchBar.value -> {
                filesVM.exitSearchMode()
                scope.launch(Dispatchers.IO) { filesVM.loadAsync(context) }
            }
            filesVM.showPasteBar.value -> { filesVM.cutFiles.clear(); filesVM.copyFiles.clear(); filesVM.showPasteBar.value = false }
            filesVM.canNavigateBack() -> {
                filesVM.navigateBack()
                scope.launch(Dispatchers.IO) { filesVM.loadAsync(context) }
            }
        }
    }

    LaunchedEffect(folderPath) {
        scope.launch(Dispatchers.IO) {
            if (folderPath.isNotEmpty()) {
                val targetDir = File(folderPath)
                if (targetDir.exists()) {
                    val appDataPath = context.appDir()
                    val type = if (folderPath.startsWith(appDataPath)) FilesType.APP else FilesType.INTERNAL_STORAGE
                    val rootPath = when (type) { FilesType.APP -> appDataPath; else -> FileSystemHelper.getInternalStoragePath() }
                    filesVM.initSelectedPath(rootPath, type, folderPath, folderPath)
                } else {
                    filesVM.loadLastPathAsync(context)
                }
            } else {
                filesVM.loadLastPathAsync(context)
            }
            filesVM.loadAsync(context)
            audioPlaylistVM.loadAsync(context)
        }
    }

    LaunchedEffect(Channel.sharedFlow) {
        Channel.sharedFlow.collect { event ->
            when (event) {
                is PermissionsResultEvent -> scope.launch(Dispatchers.IO) { filesVM.loadAsync(context) }
                is FolderKanbanSelectEvent -> {
                    val m = event.data
                    filesVM.offset = 0
                    filesVM.initSelectedPath(m.rootPath, m.type, m.fullPath, m.fullPath)
                    scope.launch(Dispatchers.IO) { filesVM.loadAsync(context) }
                }
                is ActionEvent -> if (event.source == ActionSourceType.FILE) scope.launch(Dispatchers.IO) { filesVM.loadAsync(context) }
            }
        }
    }
}
