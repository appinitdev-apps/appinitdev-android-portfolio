
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FilesPage(
    navController: NavHostController, audioPlaylistVM: AudioPlaylistViewModel,
    folderPath: String = "", filesVM: FilesViewModel = viewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val previewerState = rememberPreviewerState()
    val itemsState by filesVM.itemsFlow.collectAsState()
    val topRefreshLayoutState = rememberRefreshLayoutState {
        scope.launch { withIO { filesVM.loadAsync(context) }; setRefreshState(RefreshContentState.Finished) }
    }

    FilesPageEffects(filesVM, context, scope, folderPath, previewerState, audioPlaylistVM)
    FilesPageDialogs(filesVM, context, scope)

    val title = when {
        filesVM.selectMode.value -> stringRes(Res.string.x_selected, "count" to filesVM.selectedIds.size)

        filesVM.type == FilesType.RECENTS -> stringResource(Res.string.recents)
        ZipBrowserHelper.isZipPath(filesVM.selectedPath) -> ZipBrowserHelper.getDisplayName(filesVM.selectedPath)
        filesVM.selectedPath != filesVM.rootPath -> filesVM.selectedPath.getFilenameFromPath()
        else -> stringResource(Res.string.files)
    }
    val subtitle = if (!filesVM.selectMode.value) {
        val fc = itemsState.count { it.isDir };
        val flc = itemsState.count { !it.isDir }
        val sl = mutableListOf<String>()
        if (fc > 0) sl.add(pluralStringResource(Res.plurals.x_folders, fc, fc))
        if (flc > 0) sl.add(pluralStringResource(Res.plurals.x_files, flc, flc))
        sl.joinToString(", ")
    } else ""

    PScaffold(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
        SearchableTopBar(
            navController = navController,
            viewModel = filesVM,
            scrollBehavior = scrollBehavior,
            title = title,
            subtitle = subtitle,
            navigationIcon = { if (filesVM.selectMode.value) NavigationCloseIcon { filesVM.exitSelectMode() } else NavigationBackIcon { navController.navigateUp() } },
            actions = {
                if (!filesVM.selectMode.value) {
                    ActionButtonSearch { filesVM.enterSearchMode() }
                    ActionButtonFolderKanban { filesVM.showFolderKanbanDialog.value = true }
                    ActionButtonSort { filesVM.showSortDialog.value = true }
                    ActionButtonMoreWithMenu { dismiss ->
                        var showHiddenFiles by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            showHiddenFiles = withContext(Dispatchers.IO) {
                                ShowHiddenFilesPreference.getAsync()
                            }
                        }
                        PDropdownMenuItem(
                            text = { Text(stringResource(Res.string.show_hidden_files)) },
                            leadingIcon = {
                                Checkbox(
                                    checked = showHiddenFiles,
                                    onCheckedChange = null
                                )
                            },
                            onClick = {
                                dismiss()
                                scope.launch(Dispatchers.IO) {
                                    val nv = !showHiddenFiles
                                    ShowHiddenFilesPreference.putAsync(
                                        context,
                                        nv
                                    )
                                    showHiddenFiles = nv; filesVM.loadAsync(context)
                                }
                            })
                        if (!ZipBrowserHelper.isZipPath(filesVM.selectedPath)) {
                            PDropdownMenuItemCreateFolder {
                                dismiss()
                                filesVM.showCreateFolderDialog.value = true
                            }
                            PDropdownMenuItemCreateFile {
                                dismiss()
                                filesVM.showCreateFileDialog.value = true
                            }
                        }
                    }
                } else {
                    PTopRightButton(
                        label = stringResource(if (filesVM.isAllSelected()) Res.string.unselect_all else Res.string.select_all),
                        click = { filesVM.toggleSelectAll() }); HorizontalSpace(dp = 8.dp)
                }
            },
            onSearchAction = { q ->
                filesVM.queryText.value = q; scope.launch(Dispatchers.IO) {
                filesVM.loadAsync(
                    context
                )
            }
            })
    }, bottomBar = {
        AnimatedVisibility(
            visible = filesVM.showBottomActions() && !ZipBrowserHelper.isZipPath(filesVM.selectedPath),
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()) {
            FilesSelectModeBottomActions(
                filesVM = filesVM,
                onShowPasteBar = { filesVM.showPasteBar.value = it })
        }
        AnimatedVisibility(
            visible = filesVM.showPasteBar.value && !ZipBrowserHelper.isZipPath(filesVM.selectedPath),
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()) {
            FilePasteBar(
                filesVM = filesVM,
                coroutineScope = scope,
                onPasteComplete = { scope.launch(Dispatchers.IO) { filesVM.loadAsync(context) } })
        }
    }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (Permission.WRITE_EXTERNAL_STORAGE.can(context) && filesVM.type != FilesType.RECENTS) {
                BreadcrumbView(
                    breadcrumbs = filesVM.breadcrumbs,
                    selectedIndex = filesVM.selectedBreadcrumbIndex.value,
                    onItemClick = { filesVM.navigateToDirectory(context, it.path) })
            }
            PullToRefresh(refreshLayoutState = topRefreshLayoutState) {
                FileListContent(
                    navController = navController,
                    filesVM = filesVM,
                    files = itemsState,
                    loadFiles = { _, _ -> scope.launch(Dispatchers.IO) { filesVM.loadAsync(context) } },
                    previewerState = previewerState,
                    audioPlaylistVM = audioPlaylistVM
                )
            }
        }
    }
    MediaPreviewer(state = previewerState)
}
