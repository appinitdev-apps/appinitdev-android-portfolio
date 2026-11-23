
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ViewImageBottomSheet(
    imagesVM: ImagesViewModel,
    tagsVM: TagsViewModel,
    tagsMap: Map<String, List<DTagRelation>>,
    tagsState: List<DTag>,
    dragSelectState: DragSelectState,
) {
    val m = imagesVM.selectedItem.value ?: return
    val context = LocalContext.current
    val onDismiss = { imagesVM.selectedItem.value = null }
    var viewSize by remember { mutableStateOf(m.getRotatedSize()) }
    var showQrScanResult by remember { mutableStateOf(false) }
    var qrScanResult by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            if (m.path.endsWith(".svg", true)) {
                viewSize = SvgHelper.getSize(m.path)
            }
            try {
                val bitmap = BitmapFactory.decodeFile(m.path)
                if (bitmap != null) {
                    val result = QrCodeScanHelper.tryDecode(bitmap)
                    if (result != null) {
                        qrScanResult = result.text
                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    if (imagesVM.showRenameDialog.value) {
        FileRenameDialog(path = m.path, onDismiss = {
            imagesVM.showRenameDialog.value = false
        }, onDoneAsync = {
            imagesVM.loadAsync(context, tagsVM)
            onDismiss()
        })
    }

    PModalBottomSheet(onDismissRequest = { onDismiss() }) {
        LazyColumn {
            item { VerticalSpace(32.dp) }
            item {
                ViewImageActionButtons(
                    imagesVM = imagesVM, tagsVM = tagsVM, m = m,
                    dragSelectState = dragSelectState, qrScanResult = qrScanResult,
                    onShowQrScanResult = { showQrScanResult = true }, onDismiss = onDismiss,
                )
            }
            if (!imagesVM.trash.value) {
                item {
                    VerticalSpace(dp = 16.dp)
                    Subtitle(text = stringResource(Res.string.tags))
                    TagSelector(data = m, tagsVM = tagsVM, tagsMap = tagsMap, tagsState = tagsState,
                        onChangedAsync = { imagesVM.loadAsync(context, tagsVM) })
                }
            }
            item {
                VerticalSpace(dp = 16.dp)
                ViewImagePathCard(m = m)
            }
            item {
                VerticalSpace(dp = 16.dp)
                PCard {
                    PListItem(title = stringResource(Res.string.file_size), value = m.size.formatBytes())
                    PListItem(title = stringResource(Res.string.type), value = m.path.getMimeType())
                    PListItem(title = stringResource(Res.string.dimensions), value = "${viewSize.width}\u00d7${viewSize.height}")
                    PListItem(title = stringResource(Res.string.created_at), value = m.createdAt.formatDateTime())
                    PListItem(title = stringResource(Res.string.updated_at), value = m.updatedAt.formatDateTime())
                    ImageMetaRows(path = m.path)
                }
            }
            item { BottomSpace() }
        }
    }

    if (showQrScanResult) {
        QrScanResultBottomSheet(context, qrScanResult) { showQrScanResult = false }
    }
}
