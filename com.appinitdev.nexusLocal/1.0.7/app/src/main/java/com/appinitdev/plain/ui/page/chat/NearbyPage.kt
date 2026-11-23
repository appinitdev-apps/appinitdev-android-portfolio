
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyPage(
    navController: NavHostController,
    pairDeviceJson: String = "",
    nearbyVM: NearbyViewModel = viewModel()
) {
    val nearbyDevices = nearbyVM.nearbyDevices
    val isDiscovering by nearbyVM.isDiscovering

    var showQrSheet by remember { mutableStateOf(false) }
    var qrData by remember { mutableStateOf<DQrPairData?>(null) }

    
    LaunchedEffect(Unit) {
        if (!isDiscovering) {
            nearbyVM.toggleDiscovering()
        }

        
        if (pairDeviceJson.isNotEmpty()) {
            try {
                val device = JsonHelper.jsonDecode<DQrPairData>(pairDeviceJson).toDNearbyDevice()
                nearbyVM.startPairingFromDevice(device)
            } catch (e: Exception) {
                
            }
        }

    }

    
    LaunchedEffect(showQrSheet) {
        if (showQrSheet && qrData == null) {
            withIO {
                qrData = nearbyVM.getQrDataAsync()
            }
        }
    }

    
    DisposableEffect(Unit) {
        onDispose {
            if (isDiscovering) {
                nearbyVM.toggleDiscovering()
            }
        }
    }


    PScaffold(
        topBar = {
            PTopAppBar(
                navController = navController,
                navigationIcon = {
                    NavigationBackIcon { navController.navigateUp() }
                },
                title = stringResource(Res.string.nearby_devices),
                actions = {
                    PIconButton(
                        icon = Res.drawable.qr_code,
                        contentDescription = stringResource(Res.string.show_qr_code),
                        tint = MaterialTheme.colorScheme.onSurface,
                    ) {
                        qrData = null
                        showQrSheet = true
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            nearbySearchingItem()
            nearbyDeviceListItems(nearbyDevices, nearbyVM)
            item {
                BottomSpace(paddingValues)
            }
        }
    }

    if (showQrSheet) {
        NearbyQrBottomSheet(
            qrData = qrData,
            onDismiss = { showQrSheet = false },
        )
    }
}

