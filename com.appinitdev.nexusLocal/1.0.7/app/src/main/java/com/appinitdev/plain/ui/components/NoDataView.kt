
@Composable
fun NoDataView(
    modifier: Modifier = Modifier,
    message: String = stringResource(Res.string.no_data),
    icon: DrawableResource = Res.drawable.files,
    showRefreshButton: Boolean = false,
    onRefresh: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier
                .size(112.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary.copy(alpha = 0.85f))
        )

        VerticalSpace(20.dp)

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
            textAlign = TextAlign.Center
        )
        
        if (showRefreshButton) {
            VerticalSpace(32.dp)

            FilledTonalButton(
                onClick = onRefresh
            ) {
                Text(text = stringResource(Res.string.refresh))
            }
        }
    }
} 