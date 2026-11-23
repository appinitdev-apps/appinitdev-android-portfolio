
@Composable
internal fun ReorderableCollectionItemScope.EnabledFeatureCard(
    feature: FeatureItem,
    index: Int,
    onDisable: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.cardBackgroundNormal),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.circleBackground)
                    .draggableHandle(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondaryTextColor,
                )
            }
            HorizontalSpace(12.dp)
            PIcon(
                icon = painterResource(feature.iconRes),
                contentDescription = stringResource(feature.titleRes),
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            HorizontalSpace(12.dp)
            Text(
                text = stringResource(feature.titleRes),
                style = MaterialTheme.typography.listItemTitle(),
                modifier = Modifier.weight(1f),
            )
            HorizontalSpace(8.dp)
            PSwitch(activated = true, onClick = { onDisable() })
        }
    }
}

@Composable
internal fun DisabledFeatureCard(
    feature: FeatureItem,
    onEnable: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.cardBackgroundNormal),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.circleBackground.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "—",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
            HorizontalSpace(12.dp)
            PIcon(
                icon = painterResource(feature.iconRes),
                contentDescription = stringResource(feature.titleRes),
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
            HorizontalSpace(12.dp)
            Text(
                text = stringResource(feature.titleRes),
                style = MaterialTheme.typography.listItemTitle(),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
            HorizontalSpace(8.dp)
            PSwitch(activated = false, onClick = { onEnable() })
        }
    }
}

