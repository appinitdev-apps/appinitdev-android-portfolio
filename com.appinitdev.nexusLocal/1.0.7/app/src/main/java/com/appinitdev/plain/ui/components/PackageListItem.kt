@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PackageListItem(
    item: VPackage,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = { },
    onLongClick: (() -> Unit)? = null,
) {
    Surface(
        modifier = modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        color = Color.Unspecified,
    ) {
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val icon = remember(item.id) {
                try {
                    packageManager.getApplicationIcon(item.id)
                } catch (e: Exception) {
                    null
                }
            }
            AsyncImage(
                modifier =
                Modifier
                    .padding(end = 16.dp)
                    .size(48.dp),
                model = icon,
                contentDescription = item.name,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = item.name + " (${item.version})",
                    style = MaterialTheme.typography.listItemTitle(),
                )
                VerticalSpace(dp = 8.dp)
                Text(
                    text = item.id,
                    style = MaterialTheme.typography.listItemDescription(),
                )
                VerticalSpace(dp = 8.dp)
                Text(
                    text = stringResource(if (item.type == "system") Res.string.app_type_system else Res.string.app_type_user) + " " + item.size.formatBytes(),
                    style = MaterialTheme.typography.listItemDescription(),
                )
                VerticalSpace(dp = 8.dp)
                Text(
                    text = stringResource(Res.string.updated_at) + "  " + item.updatedAt.formatDateTime(),
                    style = MaterialTheme.typography.listItemSubtitle()
                )
            }
        }
    }
}
