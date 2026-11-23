@Composable
fun EditFeedDialog(feedsVM: FeedsViewModel) {
    if (feedsVM.showEditDialog.value) {
        val focusManager = LocalFocusManager.current
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = {
                feedsVM.showEditDialog.value = false
            },
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.square_pen),
                    contentDescription = stringResource(Res.string.edit),
                )
            },
            title = {
                Text(
                    text = stringResource(Res.string.edit), maxLines = 1, overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = feedsVM.editName.value,
                        onValueChange = {
                            feedsVM.editName.value = it
                        },
                        singleLine = true,
                        label = {
                            Text(text = stringResource(Res.string.name))
                        }
                    )
                    VerticalSpace(dp = 8.dp)
                    OutlinedTextField(
                        value = feedsVM.editUrl.value,
                        onValueChange = {
                            feedsVM.editUrl.value = it
                            if (feedsVM.editUrlError.value.isNotEmpty()) {
                                feedsVM.editUrlError.value = ""
                            }
                        },
                        singleLine = true,
                        label = {
                            Text(text = stringResource(Res.string.url))
                        }
                    )
                    if (feedsVM.editUrlError.value.isNotEmpty()) {
                        SelectionContainer {
                            Text(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                text = feedsVM.editUrlError.value,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = feedsVM.editUrl.value.isNotBlank() && feedsVM.editName.value.isNotBlank(),
                    onClick = {
                        focusManager.clearFocus()
                        feedsVM.edit()
                    },
                ) {
                    Text(stringResource(Res.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    feedsVM.showEditDialog.value = false
                }) {
                    Text(text = stringResource(Res.string.cancel))
                }
            },
        )
    }
}