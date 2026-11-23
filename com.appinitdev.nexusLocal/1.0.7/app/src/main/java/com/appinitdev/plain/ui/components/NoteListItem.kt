
@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun NoteListItem(
    notesVM: NotesViewModel,
    m: DNote,
    tags: List<DTag>,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onClickTag: (DTag) -> Unit
) {
    Row {
        if (notesVM.selectMode.value) {
            HorizontalSpace(dp = 16.dp)
            Checkbox(checked = notesVM.selectedIds.contains(m.id), onCheckedChange = {
                notesVM.select(m.id)
            })
        }

        Surface(
            modifier =
            PlainTheme
                .getCardModifier(selected = notesVM.selectedItem.value?.id == m.id || notesVM.selectedIds.contains(m.id))
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
                    .padding(16.dp)
            ) {
                Text(
                    text = m.title.getSummary(),
                    style = MaterialTheme.typography.listItemDescription(),
                )
                VerticalSpace(dp = 8.dp)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = m.updatedAt.timeAgo(),
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
