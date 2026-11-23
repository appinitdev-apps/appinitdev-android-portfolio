@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DocItem(
    navController: NavHostController,
    docsVM: DocsViewModel,
    dragSelectState: DragSelectState,
    m: DDoc,
    tags: List<DTag>,
    onTagClick: (DTag) -> Unit,
) {
    Row {
        if (dragSelectState.selectMode) {
            HorizontalSpace(dp = 16.dp)
            Checkbox(checked = dragSelectState.isSelected(m.id), onCheckedChange = {
                dragSelectState.select(m.id)
            })
        }
        Surface(
            modifier =
            PlainTheme
                .getCardModifier(selected = docsVM.selectedItem.value?.id == m.id || dragSelectState.isSelected(m.id))
                .combinedClickable(
                    onClick = {
                        if (dragSelectState.selectMode) {
                            dragSelectState.select(m.id)
                        } else {
                            if (m.path.isTextFile()) {
                                navController.navigateTextFile(m.path, mediaId = m.id)
                            } else if (m.path.isPdfFile()) {
                                navController.navigatePdf(File(m.path).toUri())
                            } else {
                                navController.navigateOtherFile(m.path)
                            }
                        }
                    },
                    onLongClick = {
                        if (dragSelectState.selectMode) {
                            return@combinedClickable
                        }
                        docsVM.selectedItem.value = m
                    },
                )
                .weight(1f),
            color = Color.Unspecified,
        ) {
            Row(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 8.dp, 8.dp, 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AsyncImage(
                    model =  AppHelper.getFileIconPath(m.title.getFilenameExtension()),
                    contentDescription = m.title,
                    modifier = Modifier
                        .size(24.dp),
                )
                HorizontalSpace(dp = 16.dp)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = m.title,
                        style = MaterialTheme.typography.listItemTitle(),
                    )
                    VerticalSpace(dp = 8.dp)
                    Text(
                        text = m.size.formatBytes() + ", " + m.updatedAt.formatDateTime(),
                        style = MaterialTheme.typography.listItemSubtitle(),
                    )
                    if (tags.isNotEmpty()) {
                        VerticalSpace(dp = 8.dp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            tags.forEach { tag ->
                                ClickableText(
                                    text = AnnotatedString("#" + tag.name),
                                    modifier = Modifier.padding(end = 8.dp),
                                    style = MaterialTheme.typography.listItemTag(),
                                    onClick = {
                                        if (!dragSelectState.selectMode) {
                                            onTagClick(tag)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}