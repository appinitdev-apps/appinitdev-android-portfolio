@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ViewFeedEntryBottomSheet(
    feedEntriesVM: FeedEntriesViewModel,
    tagsVM: TagsViewModel,
    tagsMap: Map<String, List<DTagRelation>>,
    tagsState: List<DTag>,
) {
    val m = feedEntriesVM.selectedItem.value ?: return
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val onDismiss = {
        feedEntriesVM.selectedItem.value = null
    }
    PModalBottomSheet(
        onDismissRequest = {
            onDismiss()
        },
    ) {
        LazyColumn {
            item {
                VerticalSpace(32.dp)
            }
            item {
                ActionButtons {
                    if (!feedEntriesVM.showSearchBar.value) {
                        IconTextSelectButton {
                            feedEntriesVM.enterSelectMode()
                            feedEntriesVM.select(m.id)
                            onDismiss()
                        }
                    }
                    IconTextDeleteButton {
                        feedEntriesVM.delete(tagsVM, setOf(m.id))
                        onDismiss()
                    }
                }
                VerticalSpace(dp = 16.dp)
                Subtitle(text = stringResource(Res.string.tags))
                TagSelector(
                    data = m,
                    tagsVM = tagsVM,
                    tagsMap = tagsMap,
                    tagsState = tagsState,
                    onChangedAsync = {
                        feedEntriesVM.loadAsync(tagsVM)
                    }
                )
                VerticalSpace(dp = 16.dp)
                PCard {
                    PListItem(modifier = Modifier.clickable {
                        WebHelper.open(context, m.url)
                    }, title = m.url, separatedActions = true, action = {
                        CopyIconButton(text = m.url, clipLabel = stringResource(Res.string.link))
                    })
                }
                VerticalSpace(dp = 16.dp)
                PCard {
                    PListItem(title = stringResource(Res.string.published_at), value = m.publishedAt.formatDateTime())
                    PListItem(title = stringResource(Res.string.created_at), value = m.createdAt.formatDateTime())
                    PListItem(title = stringResource(Res.string.updated_at), value = m.updatedAt.formatDateTime())
                }
                BottomSpace()
            }
        }
    }
}
