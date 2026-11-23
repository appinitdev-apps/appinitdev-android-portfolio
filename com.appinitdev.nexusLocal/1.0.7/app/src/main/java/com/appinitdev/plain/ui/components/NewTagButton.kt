@Composable
fun NewTagButton(click: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(top = 8.dp)
            .size(32.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.cardBackgroundNormal)
            .clickable { click() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            painter = painterResource(Res.drawable.plus),
            contentDescription = stringResource(Res.string.add_tag),
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}