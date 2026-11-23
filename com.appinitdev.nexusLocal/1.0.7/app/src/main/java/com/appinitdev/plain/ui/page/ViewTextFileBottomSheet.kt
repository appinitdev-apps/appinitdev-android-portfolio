
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ViewTextFileBottomSheet(
    textFileVM: TextFileViewModel,
    path: String,
    m: DFile?,
    onDeleted: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val onDismiss = {
        textFileVM.showMoreActions.value = false
    }

    PModalBottomSheet(
        onDismissRequest = {
            onDismiss()
        },
    ) {
        VerticalSpace(32.dp)
        ActionButtons {
            IconTextShareButton {
                ShareHelper.sharePaths(context, setOf(path))
                onDismiss()
            }
            IconTextToTopButton {
                textFileVM.gotoTop()
                onDismiss()
            }
            IconTextToBottomButton {
                textFileVM.gotoEnd()
                onDismiss()
            }
            IconTextDeleteButton {
                DialogHelper.confirmToDelete {
                    scope.launch(Dispatchers.IO) {
                        val paths = mutableListOf(path)
                        paths.forEach {
                            File(it).deleteRecursively()
                        }
                        MainApp.instance.scanFileByConnection(paths.toTypedArray())
                        onDismiss()
                        onDeleted()
                    }
                }
            }
        }
        VerticalSpace(dp = 24.dp)
        if (m != null) {
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
            VerticalSpace(dp = 16.dp)
            PCard {
                PListItem(title = stringResource(Res.string.wrap_content), action = {
                    PSwitch(
                        activated = textFileVM.wrapContent.value,
                    ) {
                        textFileVM.toggleWrapContent(context)
                    }
                })
            }
        }
        BottomSpace()
    }
}


