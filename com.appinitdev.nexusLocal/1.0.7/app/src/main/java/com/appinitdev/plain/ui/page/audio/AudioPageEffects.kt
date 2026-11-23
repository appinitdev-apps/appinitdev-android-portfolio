@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AudioPageEffects(
    audioState: AudioPageState,
    audioVM: AudioViewModel,
    audioPlaylistVM: AudioPlaylistViewModel,
    tagsVM: TagsViewModel,
    mediaFoldersVM: MediaFoldersViewModel,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sharedFlow = Channel.sharedFlow
    LaunchedEffect(Unit) {
        audioVM.hasPermission.value = AppFeatureType.FILES.hasPermission(context)
        if (audioVM.hasPermission.value) {
            scope.launch(Dispatchers.IO) {
                audioVM.sortBy.value = AudioSortByPreference.getValueAsync()
                audioVM.loadAsync(context, tagsVM)
                audioPlaylistVM.loadAsync(context)
                mediaFoldersVM.loadAsync(context)
            }
        }
    }
    LaunchedEffect(sharedFlow) {
        sharedFlow.collect { event ->
            if (event is PermissionsResultEvent) {
                audioVM.hasPermission.value = AppFeatureType.FILES.hasPermission(context)
                scope.launch(Dispatchers.IO) {
                    audioVM.sortBy.value = AudioSortByPreference.getValueAsync()
                    audioVM.loadAsync(context, tagsVM)
                }
            }
        }
    }
    LaunchedEffect(audioState.dragSelectState.selectMode, !context.isGestureInteractionMode()) {
        if (audioState.dragSelectState.selectMode || !context.isGestureInteractionMode()) {
            audioState.scrollBehavior.reset()
        }
    }
}
