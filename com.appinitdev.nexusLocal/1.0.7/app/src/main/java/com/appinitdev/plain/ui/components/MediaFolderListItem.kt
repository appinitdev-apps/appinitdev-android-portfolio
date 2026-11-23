
@Composable
fun MediaFolderListItem(
    folder: DMediaBucket,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.cardBackgroundActive
                else MaterialTheme.colorScheme.cardBackgroundNormal
            )
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(Res.drawable.folder),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            HorizontalSpace(16.dp)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.listItemTitle(),
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
                )

                VerticalSpace(8.dp)

                var subtitle = pluralStringResource(
                    Res.plurals.items,
                    folder.itemCount,
                    folder.itemCount
                )
                if (folder.size > 0) {
                    subtitle += " • " + folder.size.formatBytes()
                }

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.listItemSubtitle()
                )
            }
        }
    }
} 