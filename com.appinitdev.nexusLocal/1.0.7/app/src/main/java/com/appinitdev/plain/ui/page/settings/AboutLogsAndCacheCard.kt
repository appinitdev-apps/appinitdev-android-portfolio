@Composable
fun AboutLogsAndCacheCard(
    navController: NavHostController,
    context: android.content.Context,
    scope: CoroutineScope,
    fileSize: Long,
    onFileSizeCleared: () -> Unit,
    cacheSize: Long,
    onCacheCleared: (Long) -> Unit,
    developerMode: Boolean,
    onDeveloperModeChanged: (Boolean) -> Unit,
) {
    val logsTitle = stringResource(Res.string.logs)
    PCard {
        PListItem(
            modifier = Modifier.clickable {
                navController.navigateTextFile(
                    DiskLogFormatStrategy.getLogFolder(context) + "/latest.log", logsTitle, "", TextFileType.APP_LOG
                )
            },
            title = stringResource(Res.string.logs),
            subtitle = fileSize.formatBytes(),
            separatedActions = true,
            action = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PFilledButton(text = stringResource(Res.string.share), buttonSize = ButtonSize.SMALL, onClick = {
                        AppLogHelper.export(context)
                    })
                    if (fileSize > 0L) {
                        PFilledButton(text = stringResource(Res.string.clear_logs), buttonSize = ButtonSize.SMALL, onClick = {
                            DialogHelper.confirmToAction(Res.string.confirm_to_clear_logs) {
                                val dir = File(DiskLogFormatStrategy.getLogFolder(context))
                                if (dir.exists()) dir.deleteRecursively()
                                onFileSizeCleared()
                            }
                        })
                    }
                }
            },
        )
        PListItem(
            title = stringResource(Res.string.local_cache),
            subtitle = cacheSize.formatBytes(),
            action = {
                PFilledButton(text = stringResource(Res.string.clear_cache), buttonSize = ButtonSize.SMALL, onClick = {
                    scope.launch {
                        DialogHelper.showLoading()
                        withIO {
                            AppHelper.clearCacheAsync(context)
                        }
                        coil3.SingletonImageLoader.get(context).memoryCache?.clear()
                        val newSize = AppHelper.getCacheSize(context)
                        DialogHelper.hideLoading()
                        DialogHelper.showMessage(Res.string.local_cache_cleared)
                        onCacheCleared(newSize)
                    }
                })
            },
        )
        if (developerMode) {
            PListItem(title = stringResource(Res.string.developer_mode)) {
                PSwitch(activated = developerMode) {
                    onDeveloperModeChanged(it)
                    scope.launch(Dispatchers.IO) {
                        DeveloperModePreference.putAsync(it)
                    }
                }
            }
        }
    }
}
