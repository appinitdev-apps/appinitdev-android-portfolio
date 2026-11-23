@Composable
fun PairingRequestPage(
    event: PairingRequestReceivedEvent,
    onDeny: () -> Unit,
    onAllow: () -> Unit,
) {
    val request = event.request
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
                    painter = painterResource(request.deviceType.getIcon()),
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            VerticalSpace(dp = 24.dp)
        }
        item {
            Text(
                text = stringResource(Res.string.pairing_request),
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            )
            VerticalSpace(dp = 16.dp)
            Text(
                text = stringResource(Res.string.pairing_request_message, request.fromName),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            )
            VerticalSpace(dp = 40.dp)
        }
        item {
            PCard {
                PListItem(title = stringResource(Res.string.device_name), value = request.fromName)
                PListItem(title = stringResource(Res.string.ip_address), value = request.fromIp)
            }
        }
        item {
            VerticalSpace(dp = 56.dp)
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
