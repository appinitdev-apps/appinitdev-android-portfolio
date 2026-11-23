
@Composable
fun ChannelInvitePage(
    event: ChannelInviteReceivedEvent,
    onDecline: () -> Unit,
    onAccept: () -> Unit,
) {
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
                    painter = painterResource(Res.drawable.hash),
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            VerticalSpace(dp = 24.dp)
        }
        item {
            Text(
                text = stringResource(Res.string.channel_invite),
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            )
            VerticalSpace(dp = 8.dp)
            Text(
                text = stringResource(Res.string.channel_invite_message, event.ownerPeerName, event.channelName),
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
                PListItem(title = stringResource(Res.string.channel_name), value = event.channelName)
                PListItem(title = stringResource(Res.string.inviter), value = event.ownerPeerName)
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
                    text = stringResource(Res.string.accept),
                    buttonSize = ButtonSize.LARGE,
                    onClick = onAccept,
                )
                VerticalSpace(24.dp)
                PFilledButton(
                    text = stringResource(Res.string.decline),
                    buttonSize = ButtonSize.LARGE,
                    onClick = onDecline,
                    type = ButtonType.DANGER,
                )
            }
        }
    }
}
