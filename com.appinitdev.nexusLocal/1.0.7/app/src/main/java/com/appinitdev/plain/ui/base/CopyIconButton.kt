@Composable
fun CopyIconButton(
    text: String,
    clipLabel: String,
    modifier: Modifier = Modifier,
    icon: DrawableResource = Res.drawable.copy,
    contentDescription: String = stringResource(Res.string.copy_text),
    copiedMessage: String = text,
    onCopied: (() -> Unit)? = null,
) {
    PIconButton(
        icon = icon,
        modifier = modifier,
        contentDescription = contentDescription,
        click = {
            val clip = ClipData.newPlainText(clipLabel, text)
            clipboardManager.setPrimaryClip(clip)
            DialogHelper.showTextCopiedMessage(copiedMessage)
            onCopied?.invoke()
        },
    )
}
