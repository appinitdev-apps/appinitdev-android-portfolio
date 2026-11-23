@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SelectTagsDialog(
    tagsVM: TagsViewModel,
    tagsState: List<DTag>,
    tagsMap: Map<String, List<DTagRelation>>,
    data: IData,
    onDismiss: () -> Unit,
) {
    val tagIds = remember {
        mutableStateListOf<String>()
    }
    val scope = rememberCoroutineScope()
    TagNameDialog(tagsVM)
    LaunchedEffect(Unit) {
        tagIds.addAll(tagsMap[data.id]?.map { it.tagId } ?: emptyList())
    }
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = {
            onDismiss()
        },
        title = {
            Text(text = stringResource(Res.string.select_tags),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            FlowRow(
                modifier =
                Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                tagsState.forEach { tag ->
                    PSelectionChip(
                        selected = tagIds.contains(tag.id),
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                tagsVM.toggleTagAsync(data, tag.id)
                                if (tagIds.contains(tag.id)) {
                                    tagIds.remove(tag.id)
                                } else {
                                    tagIds.add(tag.id)
                                }
                            }
                        },
                        text = tag.name
                    )
                }
                NewTagButton(click = {
                    tagsVM.showAddDialog()
                })
            }
        }, confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                },
            ) {
                Text(stringResource(Res.string.close))
            }
        }
    )
}
