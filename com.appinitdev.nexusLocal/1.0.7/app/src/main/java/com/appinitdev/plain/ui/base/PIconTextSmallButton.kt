@Composable
fun PIconTextSmallButton(
    icon: DrawableResource,
    text: String,
    click: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { click() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().defaultMinSize(minWidth = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            PIcon(
                icon = painterResource(icon),
                contentDescription = text,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp),
            )
        }
    }
}
