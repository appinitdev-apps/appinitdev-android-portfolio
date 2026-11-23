
enum class WebState { OFF, ERROR, ON }

@Composable
fun HomeWeb(
    context: Context,
    navController: NavHostController,
    mainVM: MainViewModel,
    webEnabled: Boolean,
) {
    val scope = rememberCoroutineScope()
    val state = mainVM.httpServerState

    LaunchedEffect(webEnabled) {
        if (webEnabled) {
            mainVM.syncHttpServerState(context)
        }
    }

    val showSuccess = webEnabled && state == HttpServerState.ON
    val showLoading = state.isProcessing() || (webEnabled && state == HttpServerState.OFF)
    val showError = state == HttpServerState.ERROR
    val errorMessage = buildHomeWebErrorMessage(mainVM)

    val onRestartFix: () -> Unit = {
        scope.launch {
            withIO {
                if (HttpServerManager.portsInUse.contains(TempData.httpPort.value)) {
                    val nextHttp =
                        HttpServerManager.httpPorts.filter { it != TempData.httpPort.value }.random()
                    HttpPortPreference.putAsync(nextHttp)
                }
                if (HttpServerManager.portsInUse.contains(TempData.httpsPort.value)) {
                    val nextHttps =
                        HttpServerManager.httpsPorts.filter { it != TempData.httpsPort.value }.random()
                    HttpsPortPreference.putAsync(nextHttps)
                }
            }
            AppHelper.relaunch(context)
        }
    }

    val webState = when {
        showSuccess -> WebState.ON
        showError -> WebState.ERROR
        else -> WebState.OFF
    }

    AnimatedContent(
        targetState = webState,
        transitionSpec = {
            fadeIn(tween(300)) togetherWith fadeOut(tween(200)) using
                    SizeTransform(clip = false, sizeAnimationSpec = { _, _ -> tween(300) })
        },
        label = "web_state",
    ) { target ->
        HomeWebMainSection(
            context = context,
            navController = navController,
            mainVM = mainVM,
            webState = target,
            isLoading = showLoading,
            onRun = {
                if (!webEnabled && !state.isProcessing()) {
                    mainVM.enableHttpServer(context, true)
                }
            },
            errorMessage = errorMessage,
            onRestartFix = onRestartFix,
        )
    }
}

private fun buildHomeWebErrorMessage(mainVM: MainViewModel): String {
    return if (HttpServerManager.portsInUse.isNotEmpty()) {
        LocaleHelper.getStringF(
            if (HttpServerManager.portsInUse.size > 1) Res.string.http_port_conflict_errors else Res.string.http_port_conflict_error,
            "port",
            HttpServerManager.portsInUse.joinToString(", "),
        )
    } else {
        mainVM.httpServerError.ifEmpty { LocaleHelper.getString(Res.string.http_server_failed) }
    }
}
