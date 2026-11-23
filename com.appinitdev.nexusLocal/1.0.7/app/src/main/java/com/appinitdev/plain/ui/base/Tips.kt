@Composable
fun Tips(
    text: String,
    modifier: Modifier = Modifier.padding(start = 32.dp, end = 24.dp, top = 8.dp),
) {
    SelectionContainer {
        Text(
            modifier = modifier
                .fillMaxWidth(),
            text = text,
            style = MaterialTheme.typography.tipsText(),
        )
    }
}
@Composable
fun PDialogTips(
    text: String,
) {
    SelectionContainer {
        Text(
            modifier = Modifier
                .fillMaxWidth(),
            text = text,
            style = MaterialTheme.typography.tipsText(),
        )
    }
}
