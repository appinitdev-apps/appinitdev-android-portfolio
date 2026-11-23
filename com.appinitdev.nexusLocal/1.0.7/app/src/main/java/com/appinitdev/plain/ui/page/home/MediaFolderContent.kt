@Composable
internal fun MediaFolderListContent(
    listState: LazyListState,
    totalBucket: DMediaBucket?,
    items: List<DMediaBucket>,
    selectedBucketId: String,
    onSelect: (String) -> Unit,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        item { TopSpace() }
        if (totalBucket != null) {
            item {
                MediaFolderListItem(
                    folder = totalBucket,
                    isSelected = selectedBucketId.isEmpty(),
                    onClick = { onSelect("") }
                )
            }
        }
        items(items = items, key = { it.id }) { folder ->
            MediaFolderListItem(
                folder = folder,
                isSelected = selectedBucketId == folder.id,
                onClick = { onSelect(folder.id) }
            )
        }
        item { BottomSpace() }
    }
}
@Composable
internal fun MediaFolderGridContent(
    gridState: LazyGridState,
    totalBucket: DMediaBucket?,
    items: List<DMediaBucket>,
    selectedBucketId: String,
    onSelect: (String) -> Unit,
) {
    LazyVerticalGrid(
        state = gridState,
        modifier = Modifier.fillMaxSize(),
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (totalBucket != null) {
            item(
                key = "",
                contentType = { "bucket" },
                span = { GridItemSpan(1) }
            ) {
                MediaFolderGridItem(
                    m = totalBucket,
                    isSelected = selectedBucketId.isEmpty(),
                    onClick = { onSelect("") }
                )
            }
        }
        items(
            items,
            key = { it.id },
            contentType = { "bucket" },
            span = { GridItemSpan(1) }
        ) { m ->
            MediaFolderGridItem(
                m = m,
                isSelected = selectedBucketId == m.id,
                onClick = { onSelect(m.id) }
            )
        }
        item(
            span = { GridItemSpan(maxLineSpan) },
            key = ""
        ) {
            BottomSpace()
        }
    }
}
