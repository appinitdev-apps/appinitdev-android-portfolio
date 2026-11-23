@Composable
fun LoginRequestPage(
    clientIp: String,
    request: AuthRequest,
    onDeny: () -> Unit,
    onAllow: () -> Unit,
) {
    val isWeb = request.browserName != "PlainApp"
    val titleRes = if (isWeb) Res.string.allow_web_access else Res.string.allow_desktop_access
    val descRes = if (isWeb) Res.string.allow_web_access_desc else Res.string.allow_desktop_access_desc
    val iconRes = if (isWeb) Res.drawable.chrome else Res.drawable.laptop
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = 48.dp),
    ) {
        item {
            VerticalSpace(40.dp)
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            VerticalSpace(dp = 24.dp)
        }
        item {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            )
            VerticalSpace(dp = 8.dp)
            Text(
                text = stringResource(descRes),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            )
            VerticalSpace(dp = 40.dp)
        }
        item {
            PCard {
                PListItem(
                    title = stringResource(Res.string.os),
                    value = "${request.osName.capitalize()} ${request.osVersion}".trim(),
                )
                PListItem(
                    title = stringResource(Res.string.browser),
                    value = "${request.browserName.capitalize()} ${request.browserVersion}".trim(),
                )
                PListItem(
                    title = stringResource(Res.string.ip_address),
                    value = clientIp,
                )
            }
        }
        item {
            VerticalSpace(dp = 40.dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                PFilledButton(
                    text = stringResource(Res.string.allow),
                    buttonSize = ButtonSize.LARGE,
                    onClick = onAllow,
                )
                VerticalSpace(24.dp)
                PFilledButton(
                    text = stringResource(Res.string.deny),
                    buttonSize = ButtonSize.LARGE,
                    onClick = onDeny,
                    type = ButtonType.DANGER,
                )
            }
        }
    }
}
