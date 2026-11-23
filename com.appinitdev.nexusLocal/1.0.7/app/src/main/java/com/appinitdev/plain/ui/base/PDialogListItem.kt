@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PDialogListItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    value: String? = null,
    icon: DrawableResource? = null,
    separatedActions: Boolean = false,
    showMore: Boolean = false,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier =
        modifier
            .fillMaxWidth()
            .padding(0.dp, 8.dp, 8.dp, 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Image(
                modifier =
                    Modifier
                        .padding(end = 16.dp)
                        .size(24.dp),
                painter = painterResource(icon),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                contentDescription = title,
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
            )
            subtitle?.let {
                VerticalSpace(dp = 8.dp)
                SelectionContainer {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    )
                }
            }
        }
        if (separatedActions) {
            VerticalDivider(
                modifier =
                Modifier
                    .height(32.dp)
                    .padding(start = 16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (value != null || action != null) {
            Box(Modifier.padding(start = 16.dp)) {
                action?.invoke()
                value?.let {
                    Box(Modifier.padding(end = if (showMore) 0.dp else 8.dp, top = 8.dp, bottom = 8.dp)) {
                        SelectionContainer {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                            )
                        }
                    }
                }
            }
        }
        if (showMore) {
            Icon(
                painter = painterResource(Res.drawable.chevron_right),
                modifier =
                Modifier
                    .size(24.dp),
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
