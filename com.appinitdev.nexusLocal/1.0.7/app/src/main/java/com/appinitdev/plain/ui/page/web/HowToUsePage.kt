
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HowToUsePage(navController: NavHostController, webVM: WebConsoleViewModel = viewModel()) {

    WebSettingsProvider {
        val webEnabled = LocalWeb.current
        val context = LocalContext.current
        PScaffold(
            topBar = {
                PTopAppBar(navController = navController, title = stringResource(Res.string.how_to_use))
            },
            content = { paddingValues ->
                LazyColumn(modifier = Modifier.padding(top = paddingValues.calculateTopPadding())) {
                    item {
                        TopSpace()
                        Subtitle(text = stringResource(Res.string.instruction_for_use))
                        PCard {
                            Text(
                                stringResource(Res.string.web_how_to),
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                        VerticalSpace(dp = 16.dp)
                    }
                    item {
                        Subtitle(text = stringResource(Res.string.recommendation))
                        PCard {
                            Text(
                                stringResource(Res.string.usb_connect_recommendation),
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                        VerticalSpace(dp = 16.dp)
                    }
                    item {
                        if (webEnabled) {
                            VerticalSpace(dp = 16.dp)
                            PFilledButton(text = stringResource(Res.string.http_server_diagnostics), modifier = Modifier.padding(horizontal = 16.dp), onClick = {
                                webVM.dig(context)
                            })
                        }
                        BottomSpace(paddingValues)
                    }
                }
            },
        )
    }
}
