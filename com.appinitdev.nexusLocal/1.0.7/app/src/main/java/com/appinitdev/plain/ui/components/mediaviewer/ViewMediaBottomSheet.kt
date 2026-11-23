@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ViewMediaBottomSheet(
    m: PreviewItem,
    tagsVM: TagsViewModel? = null,
    tagsMap: Map<String, List<DTagRelation>>? = null,
    tagsState: List<DTag> = emptyList(),
    onDismiss: () -> Unit = {},
    onRenamedAsync: suspend () -> Unit = {},
    deleteAction: () -> Unit = {},
    onTagsChangedAsync: suspend () -> Unit = {},
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    var showQrScanResult by remember { mutableStateOf(false) }
    var qrScanResult by remember { mutableStateOf("") }
    val context = LocalContext.current
    if (showRenameDialog) {
        FileRenameDialog(path = m.path, onDismiss = { showRenameDialog = false }, onDoneAsync = {
            m.path = m.path.substring(0, m.path.lastIndexOf("/") + 1) + it
            onRenamedAsync()
            onDismiss()
        })
    }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        if (m.data is DImage) {
            scope.launch(Dispatchers.IO) {
                try {
                    val bitmap = BitmapFactory.decodeFile(m.path)
                    if (bitmap != null) {
                        val result = QrCodeScanHelper.tryDecode(bitmap)
                        if (result != null) { qrScanResult = result.text }
                    }
                } catch (e: Exception) { }
            }
        }
    }
    PModalBottomSheet(onDismissRequest = { onDismiss() }) {
        LazyColumn {
            item { VerticalSpace(32.dp) }
            if (m.data is DImage || m.data is DVideo) {
                item {
                    ViewMediaActionButtons(m = m, qrScanResult = qrScanResult,
                        onShowQrScanResult = { showQrScanResult = true },
                        onShowRenameDialog = { showRenameDialog = true },
                        deleteAction = deleteAction, onDismiss = onDismiss)
                }
                item {
                    VerticalSpace(dp = 16.dp)
                    Subtitle(text = stringResource(Res.string.tags))
                    TagSelector(data = m.data, tagsVM = tagsVM!!, tagsMap = tagsMap!!,
                        tagsState = tagsState, onChangedAsync = { onTagsChangedAsync() })
                    VerticalSpace(dp = 16.dp)
                }
            }
            item { ViewMediaPathCard(m = m) }
            item {
                VerticalSpace(dp = 16.dp)
                PCard {
                    PListItem(title = stringResource(Res.string.file_size), value = m.size.formatBytes())
                    val mimeType = m.getMimeType()
                    PListItem(title = stringResource(Res.string.type), value = mimeType)
                    val intrinsicSize = m.intrinsicSize
                    if (intrinsicSize.width > 0 && intrinsicSize.height > 0) {
                        PListItem(title = stringResource(Res.string.dimensions), value = "${intrinsicSize.width}\u00d7${intrinsicSize.height}")
                    }
                    if (m.data is DImage) {
                        PListItem(title = stringResource(Res.string.created_at), value = m.data.createdAt.formatDateTime())
                        PListItem(title = stringResource(Res.string.updated_at), value = m.data.updatedAt.formatDateTime())
                        ImageMetaRows(path = m.path)
                    } else if (m.data is DVideo) {
                        PListItem(title = stringResource(Res.string.created_at), value = m.data.createdAt.formatDateTime())
                        PListItem(title = stringResource(Res.string.updated_at), value = m.data.updatedAt.formatDateTime())
                        VideoMetaRows(path = m.path)
                    } else if (m.path.isUrl()) {
                    } else if (mimeType.startsWith("image/")) {
                        ImageMetaRows(path = m.path)
                    } else if (mimeType.startsWith("video/")) {
                        VideoMetaRows(path = m.path)
                    }
                }
            }
            item { BottomSpace() }
        }
    }
    if (showQrScanResult) {
        QrScanResultBottomSheet(context, qrScanResult) { showQrScanResult = false }
    }
}
