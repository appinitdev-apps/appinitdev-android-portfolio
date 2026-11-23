@Composable
fun HomeWebAddressSection(
    context: Context,
    navController: NavHostController,
    mainVM: MainViewModel,
    isError: Boolean
) {
    val isHttps = TempData.webHttps.collectAsState()
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = if (isHttps.value) 1 else 0,
        pageCount = { 2 },
    )
    var showStayOnlineOverlay by remember { mutableStateOf(false) }
    if (showStayOnlineOverlay) {
        StayOnlineModeOverlay(onExit = { showStayOnlineOverlay = false })
    }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            val https = page == 1
            if (isHttps.value != https) {
                scope.launch { HttpsPreference.putAsync(https) }
            }
        }
    }
    Column {
        Text(
            text = stringResource(Res.string.web_address_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 32.dp),
        )
        VerticalSpace(12.dp)
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
            ) { page ->
                WebAddressBar(context = context, mainVM = mainVM, isHttps = page == 1)
            }
            VerticalSpace(8.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                repeat(2) { index ->
                    val selected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            ),
                    )
                }
            }
            VerticalSpace(12.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                if (isError) {
                    PFilledButton(
                        stringResource(Res.string.troubleshoot),
                        buttonSize = ButtonSize.SMALL,
                        onClick = {
                            WebHelper.open(
                                context,
                                "https://plainapp.app/troubleshooting"
                            )
                        },
                    )
                } else {
                    PIconTextButton(
                        icon = Res.drawable.wifi_tethering,
                        text = stringResource(Res.string.stay_online_mode),
                        click = { showStayOnlineOverlay = true },
                    )
                }
                PIconTextButton(Res.drawable.settings, stringResource(Res.string.web_settings)) {
                    navController.navigate(Routing.WebSettings)
                }
            }
        }
    }
}
