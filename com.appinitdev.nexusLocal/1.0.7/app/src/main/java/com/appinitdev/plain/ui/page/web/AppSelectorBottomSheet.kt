@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSelectorBottomSheet(
    vm: NotificationSettingsViewModel,
    onDismiss: () -> Unit,
    onAppsSelected: (List<String>) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val allAppsState by vm.allAppsFlow.collectAsState()
    val searchQuery by vm.searchQuery
    val filteredApps = remember(allAppsState, searchQuery) {
        if (searchQuery.isBlank()) {
            allAppsState
        } else {
            allAppsState.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.id.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            vm.loadAllAppsAsync(context)
        }
    }
    PModalBottomSheet(
        onDismissRequest = {
            vm.clearSelectedApps()
            onDismiss()
        },
        sheetState = sheetState,
    ) {
        Column {
            PBottomSheetTopAppBar(
                title = stringResource(Res.string.add_app),
                actions = {
                    PTopRightButton(
                        stringResource(Res.string.add) +
                                if (vm.selectedAppIds.isNotEmpty()) " (${vm.selectedAppIds.size})" else "", click = {
                            if (vm.selectedAppIds.isNotEmpty()) {
                                onAppsSelected(vm.selectedAppIds.toList())
                                vm.clearSelectedApps()
                                onDismiss()
                            }
                        }, enabled = vm.selectedAppIds.isNotEmpty()
                    )
                }
            )
            OutlinedTextField(
                value = vm.searchQuery.value,
                onValueChange = { vm.searchQuery.value = it },
                label = { Text(stringResource(Res.string.search)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                leadingIcon = {
                    Icon(
                        painter = painterResource(Res.drawable.search),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
            if (!vm.appsLoaded.value) {
                NoDataColumn(loading = true)
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(filteredApps.filter { !vm.filterData.value.apps.contains(it.id) }, key = { it.id }) { app ->
                        AppSelectorItem(
                            app = app,
                            isSelected = vm.selectedAppIds.contains(app.id),
                            onToggleSelection = { vm.toggleAppSelection(app.id) }
                        )
                    }
                    item {
                        BottomSpace()
                    }
                }
            }
        }
    }
}
