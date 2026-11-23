@Composable
fun AddFeedDialog(feedsVM: FeedsViewModel) {
    if (feedsVM.showAddDialog.value) {
        val focusManager = LocalFocusManager.current
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = {
                feedsVM.showAddDialog.value = false
            },
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.rss),
                    contentDescription = stringResource(Res.string.subscriptions),
                )
            },
            title = {
                Text(text = stringResource(Res.string.add_subscription), maxLines = 1, overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                if (feedsVM.rssChannel.value == null) {
                    ClipboardTextField(
                        value = feedsVM.editUrl.value,
                        onValueChange = {
                            feedsVM.editUrl.value = it
                            if (feedsVM.editUrlError.value.isNotEmpty()) {
                                feedsVM.editUrlError.value = ""
                            }
                        },
                        placeholder = stringResource(Res.string.rss_url),
                        errorText = if (feedsVM.editUrl.value.isNotEmpty()) feedsVM.editUrlError.value else "",
                        focusManager = focusManager,
                        requestFocus = true,
                        onConfirm = {
                            feedsVM.fetchChannel()
                        },
                    )
                } else {
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
                        PDialogListItem(
                            title = stringResource(Res.string.auto_fetch_full_content),
                        ) {
                            PSwitch(
                                activated = feedsVM.editFetchContent.value,
                            ) {
                                feedsVM.editFetchContent.value = it
                            }
                        }
                        PDialogTips(text = stringResource(Res.string.auto_fetch_full_content_tips))
                    }
                }
            },
            confirmButton = {
                val buttonText = if (feedsVM.rssChannel.value == null) Res.string.search else Res.string.add
                Button(
                    enabled = feedsVM.editUrl.value.isNotBlank(),
                    onClick = {
                        focusManager.clearFocus()
                        if (feedsVM.rssChannel.value == null) {
                            feedsVM.fetchChannel()
                        } else {
                            feedsVM.add()
                        }
                    },
                ) {
                    Text(stringResource(buttonText))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    feedsVM.showAddDialog.value = false
                }) {
                    Text(text = stringResource(Res.string.cancel))
                }
            },
        )
    }
}