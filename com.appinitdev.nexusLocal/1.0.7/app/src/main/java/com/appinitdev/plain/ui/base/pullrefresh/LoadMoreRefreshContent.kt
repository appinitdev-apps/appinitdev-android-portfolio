
@Composable
fun LoadMoreRefreshContent(isLoadFinish: Boolean = false) {
    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 32.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        if (!isLoadFinish) {
            Text(
                text = stringResource(Res.string.loading),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.secondaryTextColor,
            )
        }
    }
}
