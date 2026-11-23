
@Composable
fun NoDataColumn(loading: Boolean = false, search: Boolean = false) {
    LazyColumn(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val text = if (search) {
            if (loading) Res.string.searching else Res.string.no_results_found
        } else {
            if (loading) Res.string.loading else Res.string.no_data
        }
        item {
            Text(
                text = stringResource(text),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
