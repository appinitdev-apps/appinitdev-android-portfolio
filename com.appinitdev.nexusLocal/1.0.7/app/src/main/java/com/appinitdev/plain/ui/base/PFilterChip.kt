
@Composable
fun PFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    FilterChip(
        selected, onClick, label, modifier, enabled,
        colors = FilterChipDefaults.filterChipColors().copy(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor =  MaterialTheme.colorScheme.onPrimary,
            containerColor = MaterialTheme.colorScheme.cardBackgroundNormal,
            labelColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = null,
    )
}
