@Composable
fun HomeWebMainSection(
    context: Context,
    navController: NavHostController,
    mainVM: MainViewModel,
    webState: WebState,
    errorMessage: String = "",
    onRestartFix: () -> Unit = {},
    isLoading: Boolean = false,
    onRun: (() -> Unit)? = null,
) {
    val onlineCount by HttpServerManager.wsSessionCount.collectAsState()
    val collapsed = remember {
        context.dataStore.dataFlow.map { HomeSectionCollapsedPreference.get(it, AppFeatureType.WEB_PORTAL) }
    }.collectAsStateValue(initial = false)
    Column {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(PlainTheme.CARD_RADIUS),
            color = MaterialTheme.colorScheme.cardBackgroundNormal,
        ) {
            Column {
                HomeSectionClickableHeader(
                    title = when (webState) {
                        WebState.OFF -> stringResource(Res.string.web_portal_off)
                        WebState.ERROR -> stringResource(Res.string.home_web_easy_failed_title)
                        else -> stringResource(Res.string.web_portal_running)
                    },
                    onClick = { navController.navigate(Routing.WebSettings) },
                    trailingContent = {
                        if (webState != WebState.ON) {
                            PIconButton(
                                icon = Res.drawable.tune,
                                contentDescription = stringResource(Res.string.web_settings),
                                tint = MaterialTheme.colorScheme.blue,
                                click = { navController.navigate(Routing.WebSettings) })
                        } else {
                            HomeSectionCollapseButton(
                                collapsed = collapsed,
                                featureType = AppFeatureType.WEB_PORTAL,
                            )
                        }
                    },
                )
                if (webState != WebState.ON || !collapsed) {
                    Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 24.dp)) {
                        Text(
                            text = when (webState) {
                                WebState.OFF -> stringResource(Res.string.web_portal_desc_off)
                                WebState.ERROR -> errorMessage
                                else -> stringResource(Res.string.web_portal_desc_running)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        VerticalSpace(24.dp)
                        when (webState) {
                            WebState.OFF -> PFilledButton(
                                text = stringResource(Res.string.start_service),
                                onClick = onRun ?: {},
                                buttonSize = ButtonSize.LARGE,
                                isLoading = isLoading,
                            )
                            WebState.ERROR -> PFilledButton(
                                text = stringResource(Res.string.relaunch_app),
                                onClick = onRestartFix,
                                type = ButtonType.TERTIARY,
                                buttonSize = ButtonSize.LARGE,
                            )
                            else -> PFilledButton(
                                text = stringResource(Res.string.stop_service),
                                onClick = {
                                    mainVM.enableHttpServer(
                                        context,
                                        false
                                    )
                                },
                                type = ButtonType.DANGER,
                                buttonSize = ButtonSize.LARGE,
                            )
                        }
                        if (webState == WebState.ON && onlineCount > 0) {
                            VerticalSpace(16.dp)
                            OnlineSessionsIndicator(
                                count = onlineCount,
                                onClick = { navController.navigate(Routing.Connections) })
                        }
                    }
                }
            }
        }
        if (webState != WebState.OFF && (webState != WebState.ON || !collapsed)) {
            VerticalSpace(12.dp)
            HomeWebAddressSection(context, navController, mainVM, webState == WebState.ERROR)
        }
    }
}
