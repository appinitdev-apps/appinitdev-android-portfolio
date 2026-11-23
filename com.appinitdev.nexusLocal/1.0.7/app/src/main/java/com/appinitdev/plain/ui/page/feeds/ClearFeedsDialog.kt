
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ClearFeedsDialog(
    feedSettingsVM: FeedSettingsViewModel,
) {
    val scope = rememberCoroutineScope()
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = {
            feedSettingsVM.showClearFeedsDialog.value = false
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        DialogHelper.showLoading()
                        withIO {
                            if (feedSettingsVM.clearFeedItemsTs.longValue == 0L) {
                                feedSettingsVM.clearAllAsync()
                            } else {
                                feedSettingsVM.clearByTimeAsync(feedSettingsVM.clearFeedItemsTs.longValue)
                            }
                        }
                        DialogHelper.hideLoading()
                        feedSettingsVM.showClearFeedsDialog.value = false
                        DialogHelper.showMessage(Res.string.feed_items_cleared)
                    }
                }
            ) {
                Text(stringResource(Res.string.clear))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                feedSettingsVM.showClearFeedsDialog.value = false
            }) {
                Text(stringResource(Res.string.cancel))
            }
        },
        title = {
            Text(
                text = stringResource(Res.string.clear_feed_items),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                PDialogRadioRow(selected = feedSettingsVM.clearFeedItemsTs.longValue == 0L, onClick = {
                    feedSettingsVM.clearFeedItemsTs.longValue = 0
                }, text = stringResource(Res.string.all))
                PDialogRadioRow(selected = feedSettingsVM.clearFeedItemsTs.longValue == Constants.ONE_DAY * 7, onClick = {
                    feedSettingsVM.clearFeedItemsTs.longValue = Constants.ONE_DAY * 7
                }, text = stringResource(Res.string.older_than_7days_feed_items))
                PDialogRadioRow(selected = feedSettingsVM.clearFeedItemsTs.longValue == Constants.ONE_DAY * 30, onClick = {
                    feedSettingsVM.clearFeedItemsTs.value = Constants.ONE_DAY * 30
                }, text = stringResource(Res.string.older_than_30days_feed_items))
            }
        })
}
