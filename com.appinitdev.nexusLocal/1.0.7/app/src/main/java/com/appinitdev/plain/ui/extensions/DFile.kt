fun DFile.toPreviewItem(): PreviewItem {
    return PreviewItem(path, path, size, path, this)
}