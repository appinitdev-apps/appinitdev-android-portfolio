
@Composable
private fun ActionButton(
    icon: DrawableResource,
    text: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = text,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = tint
        )
    }
}

@Composable
fun ScanHistoryItem(
    context: Context,
    text: String,
    onDelete: () -> Unit,
) {
    PCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            val newText = text.linkify()
            SelectionContainer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                ClickableText(
                    text = newText,
                    style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    onClick = { position -> newText.urlAt(context, position) },
                )
            }

            VerticalSpace(dp = 8.dp)

            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                ActionButton(
                    icon = Res.drawable.copy,
                    text = stringResource(Res.string.copy),
                    tint = MaterialTheme.colorScheme.onSurface,
                    onClick = {
                        val clip = ClipData.newPlainText(LocaleHelper.getString(Res.string.scan_result), text)
                        clipboardManager.setPrimaryClip(clip)
                        DialogHelper.showTextCopiedMessage(text)
                    },
                    modifier = Modifier.weight(1f)
                )
                ActionButton(
                    icon = Res.drawable.delete_forever,
                    text = stringResource(Res.string.delete),
                    tint = MaterialTheme.colorScheme.red,
                    onClick = onDelete,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}