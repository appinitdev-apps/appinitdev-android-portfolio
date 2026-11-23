@Composable
fun NotificationFilterModeCard(
    mode: String,
    onToggleMode: () -> Unit,
) {
    PCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(Res.string.notification_filter_mode_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            VerticalSpace(dp = 12.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val chipColors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FilterChip(
                    selected = mode == "allowlist",
                    onClick = { if (mode != "allowlist") onToggleMode() },
                    label = { Text(stringResource(Res.string.allowlist_mode)) },
                    colors = chipColors
                )
                FilterChip(
                    selected = mode == "blacklist",
                    onClick = { if (mode != "blacklist") onToggleMode() },
                    label = { Text(stringResource(Res.string.blacklist_mode)) },
                    colors = chipColors
                )
            }
        }
    }
}
