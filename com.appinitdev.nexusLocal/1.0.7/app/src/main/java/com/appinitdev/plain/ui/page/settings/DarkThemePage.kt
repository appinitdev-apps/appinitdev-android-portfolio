@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DarkThemePage(navController: NavHostController) {
    val context = LocalContext.current
    val darkTheme = LocalDarkTheme.current
    val amoledDarkTheme = LocalAmoledDarkTheme.current
    val scope = rememberCoroutineScope()
    PScaffold(
        topBar = {
            PTopAppBar(
                navController = navController,
                title = stringResource(Res.string.dark_theme),
            )
        },
        content = { paddingValues ->
            LazyColumn(modifier = Modifier.padding(top = paddingValues.calculateTopPadding())) {
                item {
                    TopSpace()
                }
                item {
                    PCard {
                        DarkTheme.entries.map {
                            PListItem(
                                modifier = Modifier.clickable {
                                    scope.launch {
                                        withIO {
                                            DarkThemePreference.putAsync(it)
                                        }
                                    }
                                },
                                title = it.getText(),
                            ) {
                                RadioButton(selected = it.value == darkTheme, onClick = {
                                    scope.launch {
                                        withIO {
                                            DarkThemePreference.putAsync(it)
                                        }
                                    }
                                })
                            }
                        }
                    }
                }
                item {
                    VerticalSpace(dp = 16.dp)
                    Subtitle(
                        text = stringResource(Res.string.other),
                    )
                    PCard {
                        PListItem(
                            modifier = Modifier.clickable {
                                scope.launch(Dispatchers.IO) {
                                    AmoledDarkThemePreference.putAsync(!amoledDarkTheme)
                                }
                            },
                            title = stringResource(Res.string.amoled_dark_theme),
                        ) {
                            PSwitch(activated = amoledDarkTheme) {
                                scope.launch(Dispatchers.IO) {
                                    AmoledDarkThemePreference.putAsync(!amoledDarkTheme)
                                }
                            }
                        }
                    }
                    BottomSpace(paddingValues)
                }
            }
        },
    )
}
