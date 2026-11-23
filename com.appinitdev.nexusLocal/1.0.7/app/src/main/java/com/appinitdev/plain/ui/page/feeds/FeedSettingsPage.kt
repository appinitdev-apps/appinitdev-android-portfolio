
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedSettingsPage(
    navController: NavHostController,
    feedSettingsVM: FeedSettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        feedSettingsVM.loadSettings(context)
    }

    if (feedSettingsVM.showIntervalDialog.value) {
        val options = remember {
            setOf(900, 1800, 3600, 7200, 21600, 43200, 86400).map { FeedAutoRefreshInterval(it) }
        }

        RadioDialog(
            title = stringResource(Res.string.auto_refresh_interval),
            options = options.map {
                RadioDialogOption(
                    text = FormatHelper.formatSeconds(it.value) { res, qty, _ -> pluralStringResource(res, qty, qty) },
                    selected = it.value == feedSettingsVM.autoRefreshInterval.intValue,
                ) {
                    feedSettingsVM.setAutoRefreshInterval(context, it.value)
                }
            },
        ) {
            feedSettingsVM.showIntervalDialog.value = false
        }
    }

    if (feedSettingsVM.showClearFeedsDialog.value) {
        ClearFeedsDialog(feedSettingsVM)
    }

    PScaffold(
        topBar = {
            PTopAppBar(navController = navController, title = stringResource(Res.string.settings))
        },
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(top = paddingValues.calculateTopPadding())) {
            item {
                TopSpace()
            }
            item {
                PCard {
                    PListItem(
                        modifier = Modifier.clickable {
                            feedSettingsVM.setAutoRefresh(context, !feedSettingsVM.autoRefresh.value)
                        },
                        title = stringResource(Res.string.auto_refresh_feeds),
                    ) {
                        PSwitch(
                            activated = feedSettingsVM.autoRefresh.value,
                        ) {
                            feedSettingsVM.setAutoRefresh(context, it)
                        }
                    }

                    if (feedSettingsVM.autoRefresh.value) {
                        PListItem(
                            modifier = Modifier.clickable {
                                feedSettingsVM.showIntervalDialog.value = true
                            },
                            title = stringResource(Res.string.auto_refresh_interval),
                            value = FormatHelper.formatSeconds(feedSettingsVM.autoRefreshInterval.intValue) { res, qty, _ -> pluralStringResource(res, qty, qty) },
                            showMore = true,
                        )
                        PListItem(
                            modifier = Modifier.clickable {
                                feedSettingsVM.setAutoRefreshOnlyWifi(context, !feedSettingsVM.autoRefreshOnlyWifi.value)
                            },
                            title = stringResource(Res.string.auto_refresh_only_over_wifi),
                        ) {
                            PSwitch(
                                activated = feedSettingsVM.autoRefreshOnlyWifi.value,
                            ) {
                                feedSettingsVM.setAutoRefreshOnlyWifi(context, it)
                            }
                        }
                    }
                }
            }
            item {
                VerticalSpace(dp = 48.dp)
                PFilledButton(text = stringResource(Res.string.clear_feed_items), type = ButtonType.DANGER, onClick = {
                    feedSettingsVM.showClearFeedsDialog.value = true
                })
            }
            item {
                BottomSpace(paddingValues)
            }
        }
    }
}
