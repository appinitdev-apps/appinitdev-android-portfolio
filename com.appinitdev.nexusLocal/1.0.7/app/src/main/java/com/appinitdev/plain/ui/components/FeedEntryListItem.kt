
@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun FeedEntryListItem(
    feedEntriesVM: FeedEntriesViewModel,
    index: Int,
    m: DFeedEntry,
    feed: DFeed?,
    tags: List<DTag>,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onClickTag: (DTag) -> Unit
) {
    Row {
        if (feedEntriesVM.selectMode.value) {
            HorizontalSpace(dp = 16.dp)
            Checkbox(checked = feedEntriesVM.selectedIds.contains(m.id), onCheckedChange = {
                feedEntriesVM.select(m.id)
            })
        }

        Surface(
            modifier =
            PlainTheme
                .getCardModifier(selected = feedEntriesVM.selectedItem.value?.id == m.id || feedEntriesVM.selectedIds.contains(m.id))
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                )
                .weight(1f),
            color = Color.Unspecified,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text(
                            text = m.title,
                            style = MaterialTheme.typography.listItemTitle()
                        )
                    }
                    if (m.image.isNotEmpty()) {
                        HorizontalSpace(dp = 12.dp)
                        AsyncImage(
                            model = m.image,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            contentDescription = m.image,
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
                VerticalSpace(dp = 8.dp)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = arrayOf((index + 1).toString(), feed?.name ?: "", m.author, m.publishedAt.timeAgo()).filter {
                            it.isNotEmpty()
                        }.joinToString(" · "),
                        style = MaterialTheme.typography.listItemSubtitle(),
                    )
                    tags.forEach { tag ->
                        ClickableText(
                            text = AnnotatedString("#" + tag.name),
                            modifier = Modifier
                                .wrapContentHeight()
                                .align(Alignment.Bottom),
                            style = MaterialTheme.typography.listItemTag(),
                            onClick = {
                                onClickTag(tag)
                            }
                        )
                    }
                }
            }
        }
    }
}
