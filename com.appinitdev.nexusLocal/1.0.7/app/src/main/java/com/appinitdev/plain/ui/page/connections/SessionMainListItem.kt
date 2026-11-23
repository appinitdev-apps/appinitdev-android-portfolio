@Composable
internal fun SessionMainListItem(
    title: String,
    subtitle: String,
    icon: DrawableResource,
    onEditTitle: () -> Unit,
    action: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 8.dp, 8.dp, 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            modifier = Modifier
                .padding(end = 16.dp)
                .size(24.dp),
            painter = painterResource(icon),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            contentDescription = title,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.listItemTitle(),
                )
                IconButton(
                    onClick = onEditTitle,
                    modifier = Modifier.size(24.dp),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.pen),
                        contentDescription = stringResource(Res.string.rename),
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (subtitle.isNotEmpty()) {
                VerticalSpace(dp = 8.dp)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.listItemSubtitle(),
                    overflow = TextOverflow.Visible
                )
            }
        }
        Box(Modifier.padding(end = 8.dp)) {
            action()
        }
    }
}
