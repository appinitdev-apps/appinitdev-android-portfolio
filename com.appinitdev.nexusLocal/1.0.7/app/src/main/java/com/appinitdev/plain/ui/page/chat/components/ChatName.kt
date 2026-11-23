@Composable
fun ChatName(
    m: VChat,
    isPeerChat: Boolean = false,
    isLocal: Boolean = false,
    onRetry: (() -> Unit)? = null,
    onShowDeliveryDetails: ((DMessageStatusData) -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = when (m.fromId) {
                "local" -> stringResource(Res.string.local_chat)
                "me" -> stringResource(Res.string.me)
                else -> ChatCacheManager.peerMap[m.fromId]?.name ?: stringResource(Res.string.unknown)
            },
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(end = 4.dp),
        )
        Text(
            text = m.createdAt.formatTime(),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Normal, color = MaterialTheme.colorScheme.secondaryTextColor),
        )
        if (!isLocal) {
            when {
                m.status == "pending" -> {
                    HorizontalSpace(4.dp)
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 1.5.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                m.fromId == "me" && m.statusData != null && m.statusData.total > 0 -> {
                    val statusData = m.statusData
                    val isAllFailed = statusData.allFailed
                    val isPeerFailureBadge = isPeerChat && isAllFailed
                    val badgeColor = if (isAllFailed) MaterialTheme.colorScheme.error
                                     else MaterialTheme.colorScheme.tertiary
                    HorizontalSpace(6.dp)
                    Surface(
                        color = badgeColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onShowDeliveryDetails?.invoke(statusData) },
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (isAllFailed) {
                                Icon(
                                    painter = painterResource(Res.drawable.rotate_ccw),
                                    contentDescription = null,
                                    tint = badgeColor,
                                    modifier = Modifier.size(10.dp),
                                )
                                HorizontalSpace(3.dp)
                            }
                            Text(
                                text = if (isPeerFailureBadge) stringResource(Res.string.error) else statusData.deliveryLabel(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = badgeColor,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                            )
                        }
                    }
                }
                m.fromId == "me" && m.status == "failed" -> {
                    if (onRetry != null) {
                        HorizontalSpace(4.dp)
                        PIconButton(
                            icon = Res.drawable.rotate_ccw,
                            contentDescription = stringResource(Res.string.try_again),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        ) {
                            onRetry()
                        }
                    }
                }
            }
        }
    }
}
