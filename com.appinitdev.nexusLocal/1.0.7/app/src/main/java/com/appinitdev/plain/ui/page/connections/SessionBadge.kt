@Composable
fun SessionBadge(m: VSession, isOnline: Boolean) {
    val label = if (m.isCustom) stringResource(Res.string.custom_token) else if (isOnline) stringResource(Res.string.online) else stringResource(Res.string.offline)
    val textColor = if (!m.isCustom && isOnline) Color.White else MaterialTheme.colorScheme.onSurface
    val bgColor = if (!m.isCustom && isOnline) MaterialTheme.colorScheme.greenDot else MaterialTheme.colorScheme.surfaceVariant
    Surface(shape = RoundedCornerShape(6.dp), color = bgColor) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}