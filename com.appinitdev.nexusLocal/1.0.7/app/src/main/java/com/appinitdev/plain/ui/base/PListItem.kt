

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PListItem(
    modifier: Modifier = Modifier,
    enable: Boolean = true,
    title: String,
    subtitle: String = "",
    value: String? = null,
    icon: DrawableResource? = null,
    start: (@Composable () -> Unit)? = null,
    titleTrailing: (@Composable () -> Unit)? = null,
    separatedActions: Boolean = false,
    showMore: Boolean = false,
    action: (@Composable () -> Unit)? = null,
) {
    Surface(
        modifier =
            modifier
                .alpha(if (enable) 1f else 0.5f),
        color = Color.Unspecified,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 8.dp, 8.dp, 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (start != null) {
                start()
            } else if (icon != null) {
                HorizontalSpace(16.dp)
                Image(
                    modifier =
                        Modifier
                            .padding(end = 16.dp)
                            .size(24.dp),
                    painter = painterResource(icon),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                    contentDescription = title,
                )
            } else {
                HorizontalSpace(16.dp)
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp)
            ) {
                if (titleTrailing != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.listItemTitle(),
                            modifier = Modifier.weight(1f),
                        )
                        titleTrailing()
                    }
                } else {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.listItemTitle(),
                    )
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
            if (separatedActions) {
                VerticalDivider(
                    modifier =
                        Modifier
                            .height(24.dp)
                            .padding(start = 16.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(0.2f),
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
                                    style = MaterialTheme.typography.listItemValue(),
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
                    tint = MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}
