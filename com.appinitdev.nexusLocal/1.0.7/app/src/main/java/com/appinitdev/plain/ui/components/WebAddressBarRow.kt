
@Composable
fun WebAddressBarRow(
    url: String,
    isHostnameRow: Boolean,
    onEditClick: () -> Unit,
    onQrClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SelectionContainer {
            ClickableText(
                text = AnnotatedString(url),
                modifier = Modifier.padding(start = 16.dp),
                style =
                    TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                    ),
                onClick = {
                    val clip = ClipData.newPlainText(LocaleHelper.getString(Res.string.link), url)
                    clipboardManager.setPrimaryClip(clip)
                    DialogHelper.showTextCopiedMessage(url)
                },
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        PIconButton(
            icon = Res.drawable.pen,
            modifier = Modifier.size(32.dp),
            iconSize = 16.dp,
            contentDescription = if (isHostnameRow) "Edit hostname" else "Edit port",
            tint = MaterialTheme.colorScheme.onSurface,
            click = onEditClick,
        )
        PIconButton(
            icon = Res.drawable.qr_code,
            modifier = Modifier.size(32.dp),
            iconSize = 16.dp,
            contentDescription = "Show QR code",
            tint = MaterialTheme.colorScheme.onSurface,
            click = onQrClick,
        )
        HorizontalSpace(dp = 4.dp)
    }
}
