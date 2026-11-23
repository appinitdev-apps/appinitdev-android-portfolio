@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguagePage(navController: NavHostController) {
    val context = LocalContext.current
    val language = LocalLocale.current
    val scope = rememberCoroutineScope()
    val list = mutableListOf<Locale?>()
    list.add(null)
    list.addAll(Language.locales)
    PScaffold(
        topBar = {
            PTopAppBar(navController = navController, title = stringResource(Res.string.language))
        },
        content = { paddingValues ->
            LazyColumn(modifier = Modifier.padding(top = paddingValues.calculateTopPadding())) {
                item {
                    TopSpace()
                }
                itemsIndexed(list) { index, item ->
                    PListItem(
                        modifier = PlainTheme
                            .getCardModifier(index = if (index > 0) index - 1 else 0, size = if (index > 0) list.size - 1 else 1)
                            .clickable {
                                scope.launch(Dispatchers.IO) {
                                    LanguagePreference.putAsync(item)
                                }
                            },
                        title = item?.getElegantDisplayName() ?: stringResource(Res.string.use_device_language),
                    ) {
                        RadioButton(selected = (item == null && language == null) || (item?.language == language?.language && item?.country == language?.country), onClick = {
                            scope.launch(Dispatchers.IO) {
                                LanguagePreference.putAsync(item)
                            }
                        })
                    }
                    if (index == 0) {
                        VerticalSpace(16.dp)
                    }
                }
                item {
                    BottomSpace(paddingValues)
                }
            }
        },
    )
}
