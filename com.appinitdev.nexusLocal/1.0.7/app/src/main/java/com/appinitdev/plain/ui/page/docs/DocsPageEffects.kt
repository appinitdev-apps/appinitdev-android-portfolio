@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DocsPageEffects(
    docsState: DocsPageState,
    docsVM: DocsViewModel,
    tagsVM: TagsViewModel,
    mediaFoldersVM: MediaFoldersViewModel,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sharedFlow = Channel.sharedFlow
    LaunchedEffect(Unit) {
        docsVM.hasPermission.value = AppFeatureType.FILES.hasPermission(context)
        if (docsVM.hasPermission.value) {
            scope.launch(Dispatchers.IO) {
                docsVM.tabsShowTags.value = DocTabsModePreference.getAsync()
                docsVM.sortBy.value = DocSortByPreference.getValueAsync()
                tagsVM.loadAsync()
                mediaFoldersVM.loadAsync(context)
                docsVM.loadAsync(context, tagsVM)
            }
        }
    }
    LaunchedEffect(sharedFlow) {
        sharedFlow.collect { event ->
            if (event is PermissionsResultEvent) {
                docsVM.hasPermission.value = AppFeatureType.FILES.hasPermission(context)
                scope.launch(Dispatchers.IO) {
                    docsVM.sortBy.value = DocSortByPreference.getValueAsync()
                    docsVM.loadAsync(context, tagsVM)
                }
            }
        }
    }
    LaunchedEffect(docsState.dragSelectState.selectMode, !context.isGestureInteractionMode()) {
        if (docsState.dragSelectState.selectMode || !context.isGestureInteractionMode()) {
            docsState.scrollBehavior.reset()
        }
    }
}
