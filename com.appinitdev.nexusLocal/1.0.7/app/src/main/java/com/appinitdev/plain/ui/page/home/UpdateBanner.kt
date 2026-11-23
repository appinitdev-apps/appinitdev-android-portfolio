private const val GITHUB_RELEASES_URL = "https://github.com/plainhub/plain-app/releases/latest"
@Composable
fun UpdateBanner(updateVM: UpdateViewModel) {
    val newVersion = LocalNewVersion.current.toVersion()
    val skipVersion = LocalSkipVersion.current.toVersion()
    val currentVersion = Version(BuildConfig.VERSION_NAME)
    val isDownloading = updateVM.isDownloading.value
    val downloadProgress = updateVM.downloadProgress.intValue
    val downloadFailed = updateVM.downloadFailed.value
    val isDownloadComplete = updateVM.isDownloadComplete.value
    val downloadedFilePath = updateVM.downloadedFilePath.value
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val needsUpdate = newVersion.whetherNeedUpdate(currentVersion, skipVersion)
    LaunchedEffect(Unit) {
        val path = withIO { UpdateInfoPreference.getValueAsync().downloadedApkPath }
        if (path.isNotEmpty()) {
            if (File(path).exists() && newVersion.whetherNeedUpdate(currentVersion, skipVersion)
                && !updateVM.isDownloading.value && !updateVM.isDownloadComplete.value
            ) {
                updateVM.onDownloadComplete(path)
            } else {
                withIO { UpdateInfoPreference.updateAsync { it.copy(downloadedApkPath = "") } }
            }
        }
    }
    when {
        isDownloading -> {
            DownloadProgressBanner(
                newVersion = newVersion.toString(),
                progress = downloadProgress,
                onCancel = {
                    updateVM.cancelDownload()
                    sendEvent(CancelUpdateDownloadEvent())
                },
            )
            VerticalSpace(dp = 12.dp)
        }
        isDownloadComplete -> {
            DownloadCompleteBanner(
                onInstall = {
                    PackageHelper.install(context, File(downloadedFilePath))
                    exitProcess(0)
                },
                onDismiss = {
                    scope.launch { withIO { UpdateInfoPreference.updateAsync { it.copy(downloadedApkPath = "") } } }
                    updateVM.resetDownload()
                },
            )
            VerticalSpace(dp = 12.dp)
        }
        downloadFailed -> {
            DownloadFailedBanner(
                onGitHub = { WebHelper.open(context, GITHUB_RELEASES_URL) },
                onRetry = {
                    updateVM.startDownload()
                    sendEvent(DownloadUpdateEvent())
                },
                onDismiss = { updateVM.resetDownload() },
            )
            VerticalSpace(dp = 12.dp)
        }
        needsUpdate -> {
            PBanner(
                title = stringResource(Res.string.get_new_updates, newVersion.toString()),
                desc = stringResource(Res.string.get_new_updates_desc),
                icon = Res.drawable.lightbulb,
            ) { updateVM.showDialog() }
            VerticalSpace(dp = 12.dp)
        }
    }
}
@Composable
private fun DownloadProgressBanner(
    newVersion: String,
    progress: Int,
    onCancel: () -> Unit,
) {
    val backgroundColor = MaterialTheme.colorScheme.primaryContainer
    val contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.downloading_update, progress),
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor,
                )
                TextButton(onClick = onCancel) {
                    Text(
                        text = stringResource(Res.string.cancel),
                        color = contentColor,
                    )
                }
            }
            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = contentColor,
                trackColor = contentColor.copy(alpha = 0.3f),
            )
        }
    }
}
@Composable
private fun DownloadCompleteBanner(
    onInstall: () -> Unit,
    onDismiss: () -> Unit,
) {
    PBanner(
        title = stringResource(Res.string.update_downloaded),
        icon = Res.drawable.lightbulb,
        action = {
            PFilledButton(
                text = stringResource(Res.string.install_update),
                buttonSize = ButtonSize.SMALL,
                onClick = onInstall,
            )
        },
        onClick = onDismiss,
    )
}
@Composable
private fun DownloadFailedBanner(
    onGitHub: () -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    PBanner(
        title = stringResource(Res.string.download_update_failed),
        backgroundColor = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        icon = Res.drawable.lightbulb,
        action = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PFilledButton(
                    text = stringResource(Res.string.try_again),
                    buttonSize = ButtonSize.SMALL,
                    onClick = onRetry,
                )
                PFilledButton(
                    text = stringResource(Res.string.download_on_github),
                    buttonSize = ButtonSize.SMALL,
                    onClick = onGitHub,
                )
            }
        },
        onClick = onDismiss,
    )
}
