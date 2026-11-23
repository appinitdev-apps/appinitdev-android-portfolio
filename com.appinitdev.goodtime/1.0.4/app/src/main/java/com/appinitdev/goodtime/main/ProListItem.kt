@Composable
fun ProListItem(
    subtitle: String? = null,
    centered: Boolean = false,
    onClick: () -> Unit,
) {
    IconTextButton(
        title = stringResource(R.string.unlock_premium),
        subtitle = subtitle,
        icon = {
            Icon(
                imageVector = EvaIcons.Outline.Unlock,
                contentDescription = stringResource(R.string.unlock_premium),
            )
        },
        isFilled = true,
        centered = centered,
    ) { onClick() }
}
