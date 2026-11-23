
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarHome(navController: NavHostController, peerVM: PeerViewModel) {
    val context = LocalContext.current
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDiscoverableDialog by remember { mutableStateOf(false) }
    val isDiscoverable = remember {
        context.dataStore.dataFlow.map { NearbyDiscoverablePreference.get(it) }
    }.collectAsStateValue(initial = NearbyDiscoverablePreference.default)
    val deviceName by TempData.deviceName.collectAsState()

    if (showRenameDialog) {
        DeviceRenameDialog(
            name = deviceName,
            onDismiss = { showRenameDialog = false },
            onDone = { },
        )
    }

    if (showDiscoverableDialog) {
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { showDiscoverableDialog = false },
            title = { Text(stringResource(Res.string.make_discoverable), style = MaterialTheme.typography.titleLarge) },
            text = {
                PDialogListItem(title = stringResource(Res.string.make_discoverable_desc)) {
                    PSwitch(activated = isDiscoverable) {
                        peerVM.updateDiscoverable(it)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showDiscoverableDialog = false }) {
                    Text(text = stringResource(Res.string.close))
                }
            },
        )
    }

    TopAppBar(
        title = {
            Row(
                modifier = Modifier.padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = deviceName.ifEmpty { PhoneHelper.getDeviceName(context) },
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                IconButton(
                    onClick = { showRenameDialog = true },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.pen),
                        contentDescription = stringResource(Res.string.device_name),
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        actions = {
            PIconButton(
                icon = if (isDiscoverable) Res.drawable.eye else Res.drawable.eye_off,
                contentDescription = stringResource(Res.string.make_discoverable),
                tint = MaterialTheme.colorScheme.onSurface,
                click = { showDiscoverableDialog = true },
            )
            ActionButtonSettings(
                onClick = { navController.navigate(Routing.Settings) },
            )
            ActionButtonScan {
                navController.navigate(Routing.Scan)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.background,
        ),
    )
}