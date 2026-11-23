@Composable
internal fun SessionListItem(
    m: VSession,
    onDelete: (String) -> Unit,
    onRename: (String, String) -> Unit,
) {
    val isOnline = HttpServerManager.wsSessions.any { it.clientId == m.clientId }
    val osDisplay = (m.osName.capitalize() + " " + m.osVersion).trim()
    val browserDisplay = (m.browserName.capitalize() + " " + m.browserVersion).trim()
    var showFullTime by remember { mutableStateOf(false) }
    var showTokenTipsDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var inputName by remember(m.clientId, m.name) { mutableStateOf(m.name) }
    val lastActiveText = if (showFullTime) m.lastActiveAt?.formatDateTime() else m.lastActiveAt?.timeAgo()
    val displayName = m.name.ifEmpty { stringResource(Res.string.unknown) }
    val title = if (m.isCustom) displayName else osDisplay
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text(stringResource(Res.string.rename)) },
            text = {
                OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    label = { Text(stringResource(Res.string.name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onRename(m.clientId, inputName.trim())
                    showRenameDialog = false
                }) {
                    Text(stringResource(Res.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            },
        )
    }
    if (showTokenTipsDialog) {
        ApiTokenTipsDialog(m = m, onDismiss = { showTokenTipsDialog = false })
    }
    PCard {
        Column {
            SessionMainListItem(
                title = m.name.ifEmpty { osDisplay },
                subtitle = if (m.name.isEmpty()) "" else osDisplay,
                icon = if (m.isCustom) Res.drawable.lock else Res.drawable.laptop,
                onEditTitle = {
                    inputName = m.name
                    showRenameDialog = true
                },
                action = { SessionBadge(m = m, isOnline = isOnline) },
            )
            PListItem(
                title = stringResource(Res.string.client_id) + " - " + stringResource(Res.string.token),
                subtitle = m.clientId + "-" + m.token, action = {
                    Column(horizontalAlignment = Alignment.End) {
                        if (m.isCustom) {
                            PFilledButton(stringResource(Res.string.how_to_use), buttonSize = ButtonSize.SMALL, onClick = {
                                showTokenTipsDialog = true
                            })
                        }
                        CopyIconButton(text = m.clientId + "-" + m.token, clipLabel = stringResource(Res.string.token))
                    }
                })
            if (m.clientIP.isNotEmpty()) {
                PListItem(title = stringResource(Res.string.ip_address), value = m.clientIP)
            }
            if (browserDisplay.isNotEmpty()) {
                PListItem(title = stringResource(Res.string.browser), value = browserDisplay)
            }
            PListItem(title = stringResource(Res.string.created_at), value = m.createdAt.formatDateTime())
            Text(
                text = stringResource(if (m.isCustom) Res.string.revoke_api_token else Res.string.revoke_session),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.red,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { DialogHelper.confirmToDelete { onDelete(m.clientId) } }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            )
        }
    }
    if (lastActiveText != null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(Res.string.last_active, lastActiveText),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showFullTime = !showFullTime }
                    .padding(horizontal = 16.dp, vertical = 6.dp),
            )
        }
    }
    VerticalSpace(dp = 16.dp)
}
@Composable
private fun ApiTokenTipsDialog(m: VSession, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val hostname = remember { TempData.mdnsHostname }
    val httpsPort = remember { TempData.httpsPort }
    val curlReal = remember(m, hostname, httpsPort) {
        """curl -X POST "https://$hostname:$httpsPort/graphql" -H "c-id: ${m.clientId}" -H "Authorization: Bearer ${m.token}" -H "Content-Type: application/json" --data '{"query":"{ app { version } }"}'"""
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(Res.string.api_tokens) + " · " + stringResource(Res.string.how_to_use))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = stringResource(Res.string.auth_dev_token_tips),
                    style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
                )
                ClipboardCard(
                    horizontal = 0.dp,
                    label = "CURL",
                    text = curlReal,
                )
                PFilledButton(text = stringResource(Res.string.docs), onClick = { WebHelper.open(context, "https://plainapp.app/api-docs") })
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text(stringResource(Res.string.close)) }
        },
    )
}
