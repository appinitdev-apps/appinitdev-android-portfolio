@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeFeaturesSelectionPage(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val allFeatureItems = remember { FeatureItem.getList(navController) }
    val featuresStr = remember {
        context.dataStore.dataFlow.map { HomeFeaturesPreference.get(it) }
    }.collectAsStateValue(initial = HomeFeaturesPreference.default)
    var enabledIds by remember(featuresStr) {
        mutableStateOf(
            HomeFeaturesPreference.parseList(featuresStr.ifEmpty { HomeFeaturesPreference.default })
        )
    }
    fun persist(newList: List<String>) {
        enabledIds = newList
        scope.launch(Dispatchers.IO) {
            HomeFeaturesPreference.putAsync(HomeFeaturesPreference.formatList(newList))
        }
    }
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val fromKey = from.key as? String ?: return@rememberReorderableLazyListState
        val toKey = to.key as? String ?: return@rememberReorderableLazyListState
        val fromIdx = enabledIds.indexOf(fromKey)
        val toIdx = enabledIds.indexOf(toKey)
        if (fromIdx >= 0 && toIdx >= 0) {
            persist(enabledIds.toMutableList().apply { add(toIdx, removeAt(fromIdx)) })
        }
    }
    val disabledItems = remember(enabledIds) {
        allFeatureItems.filter { it.type.name !in enabledIds }
    }
    var animateItems by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(300); animateItems = true }
    PScaffold(
        topBar = {
            PTopAppBar(navController = navController, title = stringResource(Res.string.customize_home_features))
        },
        content = { paddingValues ->
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxWidth().padding(top = paddingValues.calculateTopPadding()),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                item(key = "") {
                    Text(
                        text = stringResource(Res.string.drag_number_to_reorder_list),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondaryTextColor,
                        modifier = Modifier.fillMaxWidth()
                            .padding(start = 24.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                    )
                }
                itemsIndexed(enabledIds, key = { _, id -> id }) { index, featureId ->
                    val feature = allFeatureItems.find { it.type.name == featureId }
                        ?: return@itemsIndexed
                    ReorderableItem(
                        reorderableLazyListState, key = featureId,
                        animateItemModifier = if (animateItems) Modifier.animateItem() else Modifier,
                    ) {
                        EnabledFeatureCard(
                            feature = feature,
                            index = index,
                            onDisable = { persist(enabledIds.filter { it != featureId }) },
                        )
                    }
                }
                items(disabledItems, key = { "dis_${it.type.name}" }) { feature ->
                    DisabledFeatureCard(
                        feature = feature,
                        onEnable = { persist(enabledIds + feature.type.name) },
                    )
                }
                item {
                    BottomSpace()
                }
            }
        },
    )
}
