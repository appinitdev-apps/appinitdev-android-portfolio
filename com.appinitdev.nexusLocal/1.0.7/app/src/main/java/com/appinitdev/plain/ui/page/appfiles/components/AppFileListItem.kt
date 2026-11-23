@Composable
fun AppFileListItem(
    file: VAppFile,
    itemState: TransformItemState,
    previewerState: MediaPreviewerState,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val fileName = file.fileName
    val extension = fileName.getFilenameExtension().ifEmpty {
        MimeTypeMap.getSingleton().getExtensionFromMimeType(file.appFile.mimeType).orEmpty()
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.cardBackgroundNormal,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val isImage = fileName.isImageFast() || file.appFile.mimeType.startsWith("image/")
            val isVideo = fileName.isVideoFast() || file.appFile.mimeType.startsWith("video/")
            if (isImage || isVideo) {
                TransformImageView(
                    modifier = Modifier
                        .size(40.dp),
                    path = file.appFile.realPath,
                    fileName = fileName,
                    key = file.appFile.id,
                    itemState = itemState,
                    previewerState = previewerState,
                    widthPx = context.dp2px(40),
                    forceVideoDecoder = isVideo,
                )
            } else {
                AsyncImage(
                    model = AppHelper.getFileIconPath(extension),
                    modifier = Modifier.size(40.dp),
                    contentDescription = fileName,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                VerticalSpace(2.dp)
                Text(
                    text = file.appFile.size.formatBytes() + "  ·  " + file.appFile.createdAt.formatDateTime(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
