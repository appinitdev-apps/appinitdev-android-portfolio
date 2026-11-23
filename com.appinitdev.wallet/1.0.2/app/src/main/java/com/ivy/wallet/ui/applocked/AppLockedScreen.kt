@SuppressLint("ComposeModifierMissing")
@Composable
@Suppress("LongMethod", "FunctionNaming")
fun BoxWithConstraintsScope.AppLockedScreen(
    onShowOSBiometricsModal: () -> Unit,
    onContinueWithoutAuthentication: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))
        Text(
            modifier = Modifier
                .background(UI.colors.medium, UI.shapes.rFull)
                .padding(vertical = 12.dp)
                .padding(horizontal = 32.dp),
            text = stringResource(R.string.app_locked),
            style = UI.typo.b2.style(
                fontWeight = FontWeight.ExtraBold,
            )
        )
        Spacer(Modifier.weight(1f))
        Image(
            modifier = Modifier
                .size(width = 96.dp, height = 138.dp),
            painter = painterResource(id = R.drawable.ic_fingerprint),
            colorFilter = ColorFilter.tint(UI.colors.medium),
            contentScale = ContentScale.FillBounds,
            contentDescription = "unlock icon"
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = stringResource(R.string.authenticate_text),
            style = UI.typo.b2.style(
                fontWeight = FontWeight.SemiBold,
                color = Gray
            )
        )
        Spacer(Modifier.height(24.dp))
        val latestOnContinueWithoutAuthentication by rememberUpdatedState(
            newValue = onContinueWithoutAuthentication
        )
        val latestOnShowOSBiometricsModal by rememberUpdatedState(onShowOSBiometricsModal)
        val context = LocalContext.current
        IvyButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            text = stringResource(R.string.unlock),
            textStyle = UI.typo.b2.style(
                color = White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            wrapContentMode = false
        ) {
            osAuthentication(
                context = context,
                onShowOSBiometricsModal = latestOnShowOSBiometricsModal,
                onContinueWithoutAuthentication = latestOnContinueWithoutAuthentication
            )
        }
        Spacer(Modifier.height(24.dp))
        LaunchedEffect(true) {
            osAuthentication(
                context = context,
                onShowOSBiometricsModal = latestOnShowOSBiometricsModal,
                onContinueWithoutAuthentication = latestOnContinueWithoutAuthentication
            )
        }
    }
}
private fun osAuthentication(
    context: Context,
    onShowOSBiometricsModal: () -> Unit,
    onContinueWithoutAuthentication: () -> Unit
) {
    if (hasLockScreen(context)) {
        onShowOSBiometricsModal()
    } else {
        onContinueWithoutAuthentication()
    }
}
@Preview
@Composable
@Suppress("FunctionNaming", "UnusedPrivateMember")
private fun Preview_Locked() {
    IvyWalletPreview {
        AppLockedScreen(
            onContinueWithoutAuthentication = {},
            onShowOSBiometricsModal = {}
        )
    }
}
