@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ViewDocBottomSheet(
    docsVM: DocsViewModel,
    tagsVM: TagsViewModel,
    tagsMapState: Map<String, List<DTagRelation>>,
    tagsState: List<DTag>,
    dragSelectState: DragSelectState,
) {
    val context = LocalContext.current
    val m = docsVM.selectedItem.value ?: return
    val onDismiss = {
        docsVM.selectedItem.value = null
    }
    if (docsVM.showRenameDialog.value) {
        FileRenameDialog(path = m.path, onDismiss = {
            docsVM.showRenameDialog.value = false
        }, onDoneAsync = {
            m.path = it
            m.title = it.getFilenameFromPath()
        })
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
                    if (!docsVM.showSearchBar.value) {
                        IconTextSelectButton {
                            dragSelectState.enterSelectMode()
                            dragSelectState.select(m.id)
                            onDismiss()
                        }
                    }
                    if (!docsVM.trash.value) {
                        IconTextShareButton {
                            ShareHelper.sharePaths(context, setOf(m.path))
                            onDismiss()
                        }
                        IconTextOpenWithButton {
                            ShareHelper.openPathWith(context, m.path)
                        }
                        IconTextRenameButton {
                            docsVM.showRenameDialog.value = true
                        }
                    }
                    if (AppFeatureType.MEDIA_TRASH.has()) {
                        if (docsVM.trash.value) {
                            IconTextRestoreButton {
                                if (isRPlus()) {
                                    docsVM.restore(context, tagsVM, setOf(m.id))
                                    onDismiss()
                                }
                            }
                            IconTextDeleteButton {
                                DialogHelper.confirmToDelete {
                                    docsVM.delete(context, tagsVM, setOf(m.id))
                                    onDismiss()
                                }
                            }
                        } else {
                            IconTextTrashButton {
                                if (isRPlus()) {
                                    docsVM.trash(context, tagsVM, setOf(m.id))
                                    onDismiss()
                                }
                            }
                        }
                    } else {
                        IconTextDeleteButton {
                            DialogHelper.confirmToDelete {
                                docsVM.delete(context, tagsVM, setOf(m.id))
                                onDismiss()
                            }
                        }
                    }
                }
                VerticalSpace(dp = 24.dp)
                if (!docsVM.trash.value) {
                    Subtitle(text = stringResource(Res.string.tags))
                    TagSelector(
                        data = m,
                        tagsVM = tagsVM,
                        tagsMap = tagsMapState,
                        tagsState = tagsState,
                        onChangedAsync = { docsVM.loadAsync(context, tagsVM) }
                    )
                    VerticalSpace(dp = 16.dp)
                }
                PCard {
                    PListItem(title = m.path, action = {
                        CopyIconButton(text = m.path, clipLabel = stringResource(Res.string.file_path))
                    })
                }
                VerticalSpace(dp = 16.dp)
                PCard {
                    PListItem(title = stringResource(Res.string.file_size), value = m.size.formatBytes())
                    PListItem(title = stringResource(Res.string.type), value = m.path.getMimeType())
                    if (m.createdAt != null) {
                        PListItem(title = stringResource(Res.string.created_at), value = m.createdAt.formatDateTime())
                    }
                    PListItem(title = stringResource(Res.string.updated_at), value = m.updatedAt.formatDateTime())
                }
            }
            item {
                BottomSpace()
            }
        }
    }
}
