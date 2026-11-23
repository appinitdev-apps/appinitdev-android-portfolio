@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagSelector(
    data: IData,
    tagsVM: TagsViewModel,
    tagsMap: Map<String, List<DTagRelation>>,
    tagsState: List<DTag>,
    onChangedAsync: suspend () -> Unit
) {
    val tagIds = remember {
        mutableStateListOf<String>()
    }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        tagIds.addAll(tagsMap[data.id]?.map { it.tagId } ?: emptyList())
    }
    TagNameDialog(tagsVM, onChangedAsync)
    FlowRow(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
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
                        onChangedAsync()
                    }
                },
                text = tag.name
            )
        }
        NewTagButton(click = {
            tagsVM.showAddDialog()
        })
    }
}