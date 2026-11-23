@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ViewFeedBottomSheet(
    feedsVM: FeedsViewModel,
) {
    val m = feedsVM.selectedItem.value ?: return
    LaunchedEffect(Unit) {
        feedsVM.editFetchContent.value = m.fetchContent
    }
    val context = LocalContext.current
    val onDismiss = {
        feedsVM.selectedItem.value = null
    }
    PModalBottomSheet(
        onDismissRequest = {
            onDismiss()
        },
    ) {
        VerticalSpace(32.dp)
        ActionButtons {
            IconTextSelectButton {
                feedsVM.enterSelectMode()
                feedsVM.select(m.id)
                onDismiss()
            }
            IconTextEditButton {
                feedsVM.showEditDialog(m)
                onDismiss()
            }
            IconTextDeleteButton {
                feedsVM.delete(setOf(m.id))
                onDismiss()
            }
        }
        VerticalSpace(dp = 24.dp)
        Subtitle(text = m.name)
        PCard {
            PListItem(modifier = Modifier.clickable {
                WebHelper.open(context, m.url)
            }, title = m.url, separatedActions = true, action = {
                CopyIconButton(text = m.url, clipLabel = stringResource(Res.string.link))
            })
        }
        VerticalSpace(dp = 16.dp)
        PCard {
            PListItem(modifier = Modifier.clickable {
                feedsVM.editFetchContent.value = !feedsVM.editFetchContent.value
                m.fetchContent = feedsVM.editFetchContent.value
                feedsVM.updateFetchContent(m.id, feedsVM.editFetchContent.value)
            }, title = stringResource(Res.string.auto_fetch_full_content), action = {
                PSwitch(
                    activated = feedsVM.editFetchContent.value,
                ) {
                    feedsVM.editFetchContent.value = it
                    m.fetchContent = it
                    feedsVM.updateFetchContent(m.id, it)
                }
            })
        }
        Tips(text = stringResource(Res.string.auto_fetch_full_content_tips))
        VerticalSpace(dp = 16.dp)
        PCard {
            PListItem(title = stringResource(Res.string.created_at), value = m.createdAt.formatDateTime())
            PListItem(title = stringResource(Res.string.updated_at), value = m.updatedAt.formatDateTime())
        }
        BottomSpace()
    }
}
