
class DlnaCastRulesViewModel : ViewModel() {
    val allowedFlow = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val deniedFlow = MutableStateFlow<List<Pair<String, String>>>(emptyList())

    fun load(context: android.content.Context) {
        viewModelScope.launch(Dispatchers.IO) {
            allowedFlow.value = DlnaAllowedSendersPreference.getAsync().map { decodeSenderEntry(it) }
            deniedFlow.value = DlnaDeniedSendersPreference.getAsync().map { decodeSenderEntry(it) }
        }
    }

    fun removeAllowed(context: android.content.Context, ip: String) {
        viewModelScope.launch(Dispatchers.IO) {
            DlnaAllowedSendersPreference.removeAsync(ip)
            allowedFlow.value = allowedFlow.value.filter { it.first != ip }
        }
    }

    fun removeDenied(context: android.content.Context, ip: String) {
        viewModelScope.launch(Dispatchers.IO) {
            DlnaDeniedSendersPreference.removeAsync(ip)
            deniedFlow.value = deniedFlow.value.filter { it.first != ip }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DlnaCastHistoryPage(
    navController: NavHostController,
    vm: DlnaCastRulesViewModel = viewModel(),
) {
    val context = LocalContext.current
    val allowed by vm.allowedFlow.collectAsState()
    val denied by vm.deniedFlow.collectAsState()

    LaunchedEffect(Unit) { vm.load(context) }

    PScaffold(
        topBar = {
            PTopAppBar(navController = navController, title = stringResource(Res.string.dlna_cast_history))
        },
    ) { paddingValues ->
        if (allowed.isEmpty() && denied.isEmpty()) {
            NoDataColumn()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(top = paddingValues.calculateTopPadding()),
            ) {
                item { TopSpace() }
                if (allowed.isNotEmpty()) {
                    item { Subtitle(text = stringResource(Res.string.dlna_cast_accepted)) }
                    items(allowed, key = { it.first }) { (ip, name) ->
                        PCard {
                            PListItem(
                                title = if (name.isNotEmpty() && name != ip) "$name ($ip)" else ip,
                                action = {
                                    IconButton(onClick = { vm.removeAllowed(context, ip) }) {
                                        Icon(
                                            painter = painterResource(Res.drawable.trash_2),
                                            contentDescription = stringResource(Res.string.delete),
                                            tint = MaterialTheme.colorScheme.error,
                                        )
                                    }
                                },
                            )
                        }
                        VerticalSpace(8.dp)
                    }
                }
                if (denied.isNotEmpty()) {
                    item { VerticalSpace(8.dp) }
                    item { Subtitle(text = stringResource(Res.string.dlna_cast_rejected)) }
                    items(denied, key = { it.first }) { (ip, name) ->
                        PCard {
                            PListItem(
                                title = if (name.isNotEmpty() && name != ip) "$name ($ip)" else ip,
                                action = {
                                    IconButton(onClick = { vm.removeDenied(context, ip) }) {
                                        Icon(
                                            painter = painterResource(Res.drawable.trash_2),
                                            contentDescription = stringResource(Res.string.delete),
                                            tint = MaterialTheme.colorScheme.error,
                                        )
                                    }
                                },
                            )
                        }
                        VerticalSpace(8.dp)
                    }
                }
                item { BottomSpace() }
            }
        }
    }
}


