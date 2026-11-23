
@Composable
internal fun AppSelectorItem(
    app: com.appinitdev.nexusLocal.data.DPackage,
    isSelected: Boolean,
    onToggleSelection: () -> Unit
) {
    val appIcon = remember(app.id) {
        packageManager.getApplicationIcon(app.appInfo)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleSelection() }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = appIcon,
            contentDescription = app.name,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
        )

        HorizontalSpace(dp = 16.dp)

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = app.name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            VerticalSpace(dp = 2.dp)
            Text(
                text = app.id,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        HorizontalSpace(dp = 12.dp)

        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggleSelection() }
        )
    }
}
