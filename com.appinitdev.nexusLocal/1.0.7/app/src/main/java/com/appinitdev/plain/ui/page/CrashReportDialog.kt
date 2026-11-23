
@Composable
fun CrashReportDialog(crashReport: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var includeAppLogs by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.crash_report_title)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = stringResource(Res.string.crash_report_message),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Checkbox(
                        checked = includeAppLogs,
                        onCheckedChange = { includeAppLogs = it },
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(Res.string.crash_include_app_logs))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val appVersion = MainApp.getAppVersion()
                val deviceInfo = AppLogHelper.buildDeviceInfoText()
                val bodyText = buildString {
                    append(deviceInfo)
                    appendLine()
                    appendLine("--- Crash Report ---")
                    append(crashReport)
                    if (includeAppLogs) {
                        val logs = CrashHandler.getAppLogs(context)
                        if (logs.isNotEmpty()) {
                            appendLine()
                            appendLine()
                            appendLine("--- App Logs ---")
                            append(logs)
                        }
                    }
                }

                
                val crashFile = File(context.cacheDir, "crash_report.txt")
                try {
                    crashFile.writeText(bodyText)
                } catch (_: Exception) {}

                val attachmentUri: Uri? = try {
                    FileProvider.getUriForFile(context, Constants.AUTHORITY, crashFile)
                } catch (_: Exception) {
                    null
                }

                val intent = if (attachmentUri != null) {
                    Intent(Intent.ACTION_SEND).apply {
                        type = "message/rfc822"
                        putExtra(Intent.EXTRA_EMAIL, arrayOf(Constants.SUPPORT_EMAIL))
                        putExtra(Intent.EXTRA_SUBJECT, "Crash Report - PlainApp $appVersion")
                        putExtra(Intent.EXTRA_TEXT, bodyText)
                        putExtra(Intent.EXTRA_STREAM, attachmentUri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                } else {
                    Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:${Constants.SUPPORT_EMAIL}")
                        putExtra(Intent.EXTRA_SUBJECT, "Crash Report - PlainApp $appVersion")
                        putExtra(Intent.EXTRA_TEXT, bodyText)
                    }
                }
                context.startActivity(Intent.createChooser(intent, null))
            }) {
                Text(stringResource(Res.string.crash_share))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel))
            }
        },
    )
}

