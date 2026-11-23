
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotificationSettingsPage(
    navController: NavHostController,
    vm: NotificationSettingsViewModel = viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val selectedAppsState by vm.selectedAppsFlow.collectAsState()

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) { vm.loadDataAsync(context) }
    }

    PScaffold(
        topBar = {
            PTopAppBar(
                navController = navController,
                title = stringResource(Res.string.notification_filter_settings),
                actions = {
                    if (selectedAppsState.isNotEmpty()) {
                        ActionButtonMoreWithMenu { dismiss ->
                            PDropdownMenuItem(
                                leadingIcon = {
                                    Icon(painter = painterResource(Res.drawable.delete_forever),
                                        tint = MaterialTheme.colorScheme.red,
                                        contentDescription = stringResource(Res.string.clear_all))
                                },
                                onClick = { dismiss(); scope.launch(Dispatchers.IO) { vm.clearAllAsync(context) } },
                                text = { Text(text = stringResource(Res.string.clear_all)) }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(top = paddingValues.calculateTopPadding())) {
            item {
                TopSpace()
                Subtitle(text = stringResource(Res.string.filter_mode))
                NotificationFilterModeCard(
                    mode = vm.filterData.value.mode,
                    onToggleMode = { scope.launch(Dispatchers.IO) { vm.toggleModeAsync(context) } }
                )
                VerticalSpace(dp = 16.dp)
            }

            item {
                Subtitle(text = stringResource(
                    if (vm.filterData.value.mode == "allowlist") Res.string.allowed_apps else Res.string.blocked_apps
                ))
            }

            if (!vm.isLoading.value) {
                items(selectedAppsState, key = { it.id }) { app ->
                    NotificationAppListItem(app = app, onRemove = {
                        scope.launch(Dispatchers.IO) { vm.removeAppAsync(context, app.id) }
                    })
                }
                item {
                    Button(
                        onClick = { vm.showAppSelectorDialog() },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    ) {
                        Icon(painter = painterResource(Res.drawable.plus), contentDescription = null, modifier = Modifier.size(18.dp))
                        HorizontalSpace(dp = 8.dp)
                        Text(stringResource(Res.string.add_app))
                    }
                    VerticalSpace(dp = 16.dp)
                }
            }

            item { BottomSpace(paddingValues) }
        }
    }

    if (vm.showAppSelector.value) {
        AppSelectorBottomSheet(
            vm = vm,
            onDismiss = { vm.showAppSelector.value = false },
            onAppsSelected = { packageNames ->
                scope.launch(Dispatchers.IO) { vm.addAppsAsync(context, packageNames) }
                vm.showAppSelector.value = false
            }
        )
    }
}
