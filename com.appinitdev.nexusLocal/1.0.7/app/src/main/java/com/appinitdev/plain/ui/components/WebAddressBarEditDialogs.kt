@Composable
fun MdnsAndPortEditDialog(
    isHttps: Boolean,
    currentHostname: String,
    currentPort: Int,
    onDismiss: () -> Unit,
    onSave: (hostname: String, port: Int) -> Unit,
) {
    val ports = if (isHttps) HttpServerManager.httpsPorts else HttpServerManager.httpPorts
    var editHostname by remember { mutableStateOf(currentHostname) }
    var selectedPort by remember { mutableStateOf(currentPort) }
    var hostnameError by remember { mutableStateOf(false) }
    AlertDialog(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(Res.string.edit)) },
        text = {
            Column {
                Text(
                    text = stringResource(Res.string.mdns_hostname),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                PTextField(
                    readOnly = false,
                    value = editHostname,
                    singleLine = true,
                    onValueChange = {
                        editHostname = it
                        hostnameError = false
                    },
                    placeholder = currentHostname,
                    errorMessage = if (hostnameError) stringResource(Res.string.mdns_hostname_invalid) else "",
                )
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = stringResource(Res.string.change_port),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyColumn(modifier = Modifier.height(200.dp)) {
                    items(ports.toList()) { p ->
                        PDialogRadioRow(
                            selected = p == selectedPort,
                            onClick = { selectedPort = p },
                            text = p.toString(),
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val valid = editHostname.isNotBlank() && editHostname.endsWith(".local")
                    if (!valid) {
                        hostnameError = true
                        return@Button
                    }
                    onSave(editHostname, selectedPort)
                },
            ) { Text(stringResource(Res.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(Res.string.cancel))
            }
        },
    )
}
@Composable
fun PortSelectionDialog(
    isHttps: Boolean,
    currentPort: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit,
) {
    val ports = if (isHttps) HttpServerManager.httpsPorts else HttpServerManager.httpPorts
    RadioDialog(
        title = stringResource(Res.string.change_port),
        options =
            ports.map {
                RadioDialogOption(
                    text = it.toString(),
                    selected = it == currentPort,
                ) { onSelect(it) }
            },
    ) { onDismiss() }
}
