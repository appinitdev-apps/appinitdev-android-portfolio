
@Composable
fun PBanner(
    modifier: Modifier = Modifier,
    title: String,
    desc: String? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    icon: DrawableResource? = null,
    action: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(16.dp, 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.let { ic ->
            Crossfade(targetState = ic, label = "") {
                Icon(
                    painter = painterResource(it), contentDescription = null,
                    modifier = Modifier.padding(end = 16.dp), tint = contentColor,
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = title, maxLines = if (desc == null) 2 else 1,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                color = contentColor, overflow = TextOverflow.Ellipsis,
            )
            desc?.let {
                Text(
                    text = it, style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.85f),
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
            action?.let {
                VerticalSpace(16.dp)
                CompositionLocalProvider(LocalContentColor provides contentColor) { it() }
            }
        }
    }
}
