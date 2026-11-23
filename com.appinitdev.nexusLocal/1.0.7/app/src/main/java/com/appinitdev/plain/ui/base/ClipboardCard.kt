
@Composable
fun ClipboardCard(
    horizontal: Dp = PlainTheme.PAGE_HORIZONTAL_MARGIN,
    label: String,
    text: String,
) {
    PCard(horizontal) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val clip = ClipData.newPlainText(label, text)
                    clipboardManager.setPrimaryClip(clip)
                    DialogHelper.showTextCopiedMessage(text)
                }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
