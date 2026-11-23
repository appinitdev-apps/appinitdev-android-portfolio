@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateDialog(updateVM: UpdateViewModel) {
    val context = LocalContext.current
    val newVersion = LocalNewVersion.current.toVersion()
    val newVersionPublishDate = LocalNewVersionPublishDate.current
    val newVersionLog = LocalNewVersionLog.current
    val newVersionSize = LocalNewVersionSize.current
    val scope = rememberCoroutineScope()
    if (updateVM.updateDialogVisible.value) {
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.heightIn(max = 400.dp),
            onDismissRequest = { updateVM.updateDialogVisible.value = false },
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.rocket),
                    contentDescription = stringResource(Res.string.change_log),
                )
            },
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(text = "Release $newVersion")
                    VerticalSpace(dp = 16.dp)
                    Text(
                        text = "${Instant.parse(newVersionPublishDate).formatDateTime()} ${newVersionSize.formatBytes()}",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    VerticalSpace(dp = 16.dp)
                }
            },
            text = {
                SelectionContainer {
                    Text(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        text = newVersionLog,
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        updateVM.hideDialog()
                        updateVM.startDownload()
                        sendEvent(DownloadUpdateEvent())
                    }
                ) {
                    Text(text = stringResource(Res.string.update))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            withIO { UpdateInfoPreference.updateAsync { it.copy(skipVersion = newVersion.toString()) } }
                            updateVM.hideDialog()
                        }
                    }
                ) {
                    Text(text = stringResource(Res.string.skip_this_version))
                }
            },
        )
    }
}
