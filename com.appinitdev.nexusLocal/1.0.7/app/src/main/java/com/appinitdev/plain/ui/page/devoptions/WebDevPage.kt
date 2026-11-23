
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebDevPage(
    navController: NavHostController,
) {
    WebSettingsProvider {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val adbToken = LocalAdbToken.current
        val packageId = BuildConfig.APPLICATION_ID

        PScaffold(
            topBar = {
                PTopAppBar(
                    navController = navController,
                    title = stringResource(Res.string.developer_options),
                )
            },
            content = { paddingValues ->
                LazyColumn(modifier = Modifier.padding(top = paddingValues.calculateTopPadding())) {
                    item {
                        TopSpace()
                        Subtitle(text = stringResource(Res.string.adb_automation))
                        ClipboardCard(label = stringResource(Res.string.token), text = adbToken)
                        VerticalSpace(dp = 16.dp)
                        ClipboardCard(
                            label = stringResource(Res.string.adb_cmd_start),
                            text = "adb shell am broadcast -a $packageId.action.START_HTTP_SERVER -p $packageId --es token $adbToken",
                        )
                        VerticalSpace(dp = 16.dp)
                        ClipboardCard(
                            label = stringResource(Res.string.adb_cmd_stop),
                            text = "adb shell am broadcast -a $packageId.action.STOP_HTTP_SERVER -p $packageId --es token $adbToken",
                        )
                        Tips(text = stringResource(Res.string.adb_token_desc))
                        VerticalSpace(dp = 16.dp)
                        PFilledButton(
                            text = stringResource(Res.string.reset_token),
                            type = ButtonType.DANGER,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            onClick = {
                                scope.launch(Dispatchers.IO) {
                                    AdbTokenPreference.resetAsync()
                                }
                            },
                        )
                        BottomSpace(paddingValues)
                    }
                }
            },
        )
    }
}
