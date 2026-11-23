@Composable
fun WebAddressBar(
    context: Context,
    mainVM: MainViewModel,
    isHttps: Boolean,
) {
    val port = if (isHttps) TempData.httpsPort.collectAsState() else TempData.httpPort.collectAsState()
    var portDialogVisible by remember { mutableStateOf(false) }
    var qrCodeDialogVisible by remember { mutableStateOf(false) }
    var mdnsEditDialogVisible by remember { mutableStateOf(false) }
    var hostname by remember { mutableStateOf(TempData.mdnsHostname) }
    var qrCodeUrl by remember { mutableStateOf("") }
    val ip4s = listOf(hostname) + mainVM.ip4s.ifEmpty { listOf("127.0.0.1") }
    val scope = rememberCoroutineScope()
    Column(
        Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.cardBackgroundNormal,
                shape = RoundedCornerShape(12.dp),
            )
            .padding(vertical = 8.dp),
    ) {
        for (ip in ip4s) {
            val url = "${if (isHttps) "https" else "http"}://$ip:${port.value}"
            Row(
                modifier = Modifier.height(40.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WebAddressBarRow(
                    url = url,
                    isHostnameRow = ip == hostname,
                    onEditClick = {
                        if (ip == hostname) {
                            mdnsEditDialogVisible = true
                        } else {
                            portDialogVisible = true
                        }
                    },
                    onQrClick = {
                        qrCodeUrl = url
                        qrCodeDialogVisible = true
                    },
                )
            }
        }
    }
    if (mdnsEditDialogVisible) {
        MdnsAndPortEditDialog(
            isHttps = isHttps,
            currentHostname = hostname,
            currentPort = port.value,
            onDismiss = { mdnsEditDialogVisible = false },
            onSave = { newHostname, newPort ->
                val hostnameChanged = newHostname != hostname
                val portChanged = newPort != port.value
                if (hostnameChanged) {
                    hostname = newHostname
                    TempData.mdnsHostname = newHostname
                    persistMdnsHostname(context, scope, newHostname)
                }
                if (portChanged) {
                    persistPort(context, scope, isHttps, newPort)
                }
                mdnsEditDialogVisible = false
                if (hostnameChanged || portChanged) {
                    showRestartAppDialog(context)
                }
            },
        )
    }
    if (portDialogVisible) {
        PortSelectionDialog(
            isHttps = isHttps,
            currentPort = port.value,
            onDismiss = { portDialogVisible = false },
            onSelect = {
                persistPort(context, scope, isHttps, it)
                portDialogVisible = false
                showRestartAppDialog(context)
            },
        )
    }
    if (qrCodeDialogVisible) {
        WebAddressBarQrDialog(
            url = qrCodeUrl,
            onClose = { qrCodeDialogVisible = false },
        )
    }
}