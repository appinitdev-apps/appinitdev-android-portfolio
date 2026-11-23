
@Composable
fun NotificationAppListItem(
    app: DPackage,
    onRemove: () -> Unit,
) {
    Row(
        modifier = PlainTheme.getCardModifier()
            .padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val appIcon = remember(app.id) {
            packageManager.getApplicationIcon(app.appInfo)
        }
        AsyncImage(
            model = appIcon,
            contentDescription = app.name,
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp))
        )
        HorizontalSpace(dp = 12.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = app.name, style = MaterialTheme.typography.listItemTitle())
            VerticalSpace(dp = 4.dp)
            Text(text = app.id, style = MaterialTheme.typography.listItemSubtitle())
        }
        TextButton(onClick = onRemove) {
            Text(stringResource(Res.string.remove), color = MaterialTheme.colorScheme.red)
        }
    }
    VerticalSpace(dp = 8.dp)
}
