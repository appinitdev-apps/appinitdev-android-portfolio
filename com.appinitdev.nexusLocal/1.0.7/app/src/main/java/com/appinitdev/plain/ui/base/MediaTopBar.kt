
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun <T : IData> MediaTopBar(
    navController: NavHostController,
    mediaVM: BaseMediaViewModel<T>,
    tagsVM: TagsViewModel,
    castVM: CastViewModel,
    dragSelectState: DragSelectState,
    scrollBehavior: TopAppBarScrollBehavior,
    bucketsMap: Map<String, DMediaBucket>,
    itemsState: List<T>,
    scrollToTop: () -> Unit,
    defaultNavigationIcon: (@Composable () -> Unit)? = null,
    onSortSelected: (context: android.content.Context, sortBy: FileSortBy) -> Unit = { _, _ -> },
    onSearchAction: (context: android.content.Context, tagsViewModel: TagsViewModel) -> Unit
) {
    val context = LocalContext.current
    val isDocs = mediaVM.dataType == DataType.DOC

    val title = getMediaPageTitle(mediaVM.dataType, castVM, bucket = bucketsMap[mediaVM.bucketId.value], dragSelectState, mediaVM.tag, mediaVM.trash)
    val containerColor = if (castVM.castMode.value) MaterialTheme.colorScheme.secondaryContainer else null

    SearchableTopBar(
        navController = navController,
        viewModel = mediaVM,
        scrollBehavior = scrollBehavior,
        title = title,
        containerColor = containerColor,
        scrollToTop = scrollToTop,
        navigationIcon = {
            if (dragSelectState.selectMode) {
                NavigationCloseIcon {
                    dragSelectState.exitSelectMode()
                }
            } else if (castVM.castMode.value) {
                NavigationCloseIcon {
                    castVM.exitCastMode()
                }
            } else {
                defaultNavigationIcon?.invoke()
            }
        },
        actions = {
            if (!mediaVM.hasPermission.value) {
                return@SearchableTopBar
            }
            if (castVM.castMode.value) {
                return@SearchableTopBar
            }
            if (dragSelectState.selectMode) {
                PTopRightButton(
                    label = stringResource(if (dragSelectState.isAllSelected(itemsState)) Res.string.unselect_all else Res.string.select_all),
                    click = {
                        dragSelectState.toggleSelectAll(itemsState)
                    },
                )
                HorizontalSpace(dp = 8.dp)
            } else {
                ActionButtonSearch {
                    mediaVM.enterSearchMode()
                }
                if (isQPlus()) {
                    ActionButtonFolders {
                        mediaVM.showFoldersDialog.value = true
                    }
                }
                if (!isDocs) {
                    ActionButtonCast {
                        castVM.showCastDialog.value = true
                    }
                }

                ActionButtonTags {
                    mediaVM.showTagsDialog.value = true
                }

                PIconButton(
                    icon = Res.drawable.sort,
                    contentDescription = stringResource(Res.string.sort),
                    tint = MaterialTheme.colorScheme.onSurface,
                    click = { mediaVM.showSortAndBrowseDialog.value = true },
                )
            }
        },
        onSearchAction = {
            mediaVM.showLoading.value = true
            onSearchAction(context, tagsVM)
        }
    )

    if (mediaVM.showSortAndBrowseDialog.value) {
        SortAndBrowseDialog(
            context = context,
            mediaVM = mediaVM,
            tagsVM = tagsVM,
            sortByEntries = if (setOf(DataType.IMAGE, DataType.VIDEO).contains(mediaVM.dataType)) FileSortBy.entries else FileSortBy.entries.filter { it != FileSortBy.TAKEN_AT_DESC },
            onSortSelected = { sortBy -> onSortSelected(context, sortBy) },
            onDismiss = { mediaVM.showSortAndBrowseDialog.value = false },
        )
    }
}