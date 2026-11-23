
@Composable
fun HomeFeatureItemsGrid(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val featuresStr = remember {
        context.dataStore.dataFlow.map { HomeFeaturesPreference.get(it) }
    }.collectAsStateValue(initial = HomeFeaturesPreference.default)

    var enabledIds by remember(featuresStr) {
        mutableStateOf(HomeFeaturesPreference.parseList(featuresStr.ifEmpty { HomeFeaturesPreference.default }))
    }

    fun persist(newList: List<String>) {
        enabledIds = newList
        scope.launch(Dispatchers.IO) {
            HomeFeaturesPreference.putAsync(HomeFeaturesPreference.formatList(newList))
        }
    }

    val allFeatureItems = remember { FeatureItem.getList(navController) }
    val items = remember(enabledIds) {
        enabledIds
            .filter { it != AppFeatureType.CHAT.name }
            .mapNotNull { typeName -> allFeatureItems.find { it.type.name == typeName } }
    }

    val rowCount = (items.size + 2) / 2
    val gridHeight = if (rowCount > 0) (rowCount * 72 + (rowCount - 1) * 12).dp else 0.dp

    val gridState = rememberLazyGridState()
    val reorderableState = rememberReorderableLazyGridState(gridState) { from, to ->
        val fromKey = from.key as? String ?: return@rememberReorderableLazyGridState
        val toKey = to.key as? String ?: return@rememberReorderableLazyGridState
        val fromIdx = enabledIds.indexOf(fromKey)
        val toIdx = enabledIds.indexOf(toKey)
        if (fromIdx >= 0 && toIdx >= 0) {
            persist(enabledIds.toMutableList().apply { add(toIdx, removeAt(fromIdx)) })
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,
        modifier = Modifier.fillMaxWidth().height(gridHeight),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = false,
    ) {
        items(items, key = { it.type.name }) { item ->
            ReorderableItem(reorderableState, key = item.type.name, animateItemModifier = Modifier) { _ ->
                HomeFeatureGridCell(
                    iconRes = item.iconRes,
                    titleRes = item.titleRes,
                    modifier = Modifier.longPressDraggableHandle(),
                    onClick = { item.click() },
                )
            }
        }
        item {
            HomeFeatureGridCell(
                iconRes = Res.drawable.plus,
                titleRes = Res.string.more,
                onClick = { navController.navigate(Routing.CustomFeatures) },
            )
        }
    }
}

@Composable
private fun HomeFeatureGridCell(
    iconRes: DrawableResource,
    titleRes: StringResource,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier
            .height(72.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.cardBackgroundNormal,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = stringResource(titleRes),
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
