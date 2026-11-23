
@Composable
fun NavigationBackIcon(onClick: () -> Unit = {}) {
    PIconButton(
        icon = Res.drawable.arrow_left,
        contentDescription = stringResource(Res.string.back),
        tint = MaterialTheme.colorScheme.onSurface,
    ) {
        onClick()
    }
}

@Composable
fun NavigationCloseIcon(onClick: () -> Unit = {}) {
    PIconButton(
        icon = Res.drawable.x,
        contentDescription = stringResource(Res.string.close),
        tint = MaterialTheme.colorScheme.onSurface,
    ) {
        onClick()
    }
}
