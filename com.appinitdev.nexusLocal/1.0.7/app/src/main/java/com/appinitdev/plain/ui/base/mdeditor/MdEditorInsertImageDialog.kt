@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun MdEditorInsertImageDialog(
    mdEditorVM: MdEditorViewModel,
) {
    val context = LocalContext.current
    val imageUrlState = remember { mutableStateOf("") }
    var imageUrl by imageUrlState
    var description by remember { mutableStateOf("") }
    var width by remember { mutableStateOf("") }
    PickImageEffect(context = context, imageUrl = imageUrlState)
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = {
            mdEditorVM.showInsertImage = false
        },
        confirmButton = {
            Button(
                onClick = {
                    var html = "<img src=\"${imageUrl}\""
                    if (width.isNotEmpty()) {
                        html += " width=\"${width}\""
                    }
                    if (description.isNotEmpty()) {
                        html += " alt=\"${description}\""
                    }
                    mdEditorVM.textFieldState.edit {
                        add("$html />")
                    }
                    mdEditorVM.showInsertImage = false
                }
            ) {
                Text(stringResource(Res.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                mdEditorVM.showInsertImage = false
            }) {
                Text(stringResource(Res.string.cancel))
            }
        },
        title = {
            Text(
                text = stringResource(Res.string.insert_image),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = imageUrl,
                        onValueChange = {
                            imageUrl = it
                        },
                        label = {
                            Text(text = stringResource(Res.string.image_url))
                        }
                    )
                    Button(
                        onClick = {
                            sendEvent(PickFileEvent(PickFileTag.EDITOR, PickFileType.IMAGE, multiple = false))
                        }, modifier = Modifier
                            .padding(start = 8.dp)
                    ) {
                        Text(
                            stringResource(Res.string.browse),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                VerticalSpace(dp = 8.dp)
                OutlinedTextField(value = description, onValueChange = { description = it },
                    label = {
                        Text(text = stringResource(Res.string.image_description))
                    })
                VerticalSpace(dp = 8.dp)
                OutlinedTextField(value = width, onValueChange = { width = it }, label = {
                    Text(text = stringResource(Res.string.width))
                })
            }
        })
}