
@Composable
fun HomeSectionCollapseButton(
    collapsed: Boolean,
    featureType: AppFeatureType,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    PIconButton(
        icon = if (collapsed) Res.drawable.chevron_down else Res.drawable.chevron_up,
        contentDescription = if (collapsed) stringResource(Res.string.expand_section) else stringResource(Res.string.collapse_section),
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        click = {
            scope.launch {
                HomeSectionCollapsedPreference.putAsync(featureType, !collapsed)
            }
        },
    )
}
