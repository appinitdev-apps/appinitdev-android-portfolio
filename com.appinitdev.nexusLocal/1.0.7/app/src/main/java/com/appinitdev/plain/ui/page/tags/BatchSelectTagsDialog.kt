@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BatchSelectTagsDialog(
    tagsVM: TagsViewModel,
    tagsState: List<DTag>,
    items: List<IData>,
    removeFromTags: Boolean = false,
    onDismiss: () -> Unit,
) {
    TagNameDialog(tagsVM)
    val tagIds = remember { mutableStateListOf<String>() }
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = {
            onDismiss()
        },
        title = {
            Text(text = stringResource(if (removeFromTags) Res.string.remove_from_tags else Res.string.add_to_tags),
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
                            if (tagIds.contains(tag.id)) {
                                tagIds.remove(tag.id)
                            } else {
                                tagIds.add(tag.id)
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
                enabled = tagIds.isNotEmpty(),
                onClick = {
                    if (removeFromTags) {
                        tagsVM.removeFromTags(items.map { it.id }.toSet(), tagIds.toSet())
                    } else {
                        tagsVM.addToTags(items, tagIds.toSet())
                    }
                    onDismiss()
                },
            ) {
                Text(stringResource(Res.string.save))
            }
        }, dismissButton = {
            TextButton(onClick = {
                onDismiss()
            }) {
                Text(text = stringResource(Res.string.cancel))
            }
        })
}
