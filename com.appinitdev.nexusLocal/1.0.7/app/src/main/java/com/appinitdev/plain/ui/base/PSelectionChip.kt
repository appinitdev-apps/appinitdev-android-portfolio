
@Composable
fun PSelectionChip(
    selected: Boolean,
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    FilterChip(
        selected, onClick,
        label = {
            Text(text = text)
        },
        modifier, enabled,
        leadingIcon = if (selected) {
            {
                Icon(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(20.dp),
                    painter = painterResource(Res.drawable.check),
                    contentDescription = stringResource(Res.string.select),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        } else null,
        colors = FilterChipDefaults.filterChipColors().copy(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = MaterialTheme.colorScheme.cardBackgroundNormal,
            labelColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = null,
    )
}
