@Composable
fun ChatInput(
    value: String,
    hint: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onSend: () -> Unit = {},
    onValueChange: (String) -> Unit = {},
) {
    var hasFocus by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = { onValueChange(it) },
            modifier =
            Modifier
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(8.dp),
                )
                .onFocusChanged { focusState -> hasFocus = focusState.hasFocus }
                .fillMaxWidth()
                .heightIn(max = 200.dp),
            keyboardOptions = keyboardOptions,
            shape = RoundedCornerShape(8.dp),
            placeholder = {
                Text(
                    hint,
                    color = MaterialTheme.colorScheme.secondaryTextColor,
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
        )
        if (hasFocus) {
            Row(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                PIconButton(
                    icon = Res.drawable.image,
                    contentDescription = stringResource(Res.string.images),
                    tint = MaterialTheme.colorScheme.primary,
                ) {
                    sendEvent(PickFileEvent(PickFileTag.SEND_MESSAGE, PickFileType.IMAGE_VIDEO, multiple = true))
                }
                PIconButton(
                    icon = Res.drawable.folder,
                    contentDescription = stringResource(Res.string.files),
                    tint = MaterialTheme.colorScheme.primary,
                ) {
                    sendEvent(PickFileEvent(PickFileTag.SEND_MESSAGE, PickFileType.FILE, multiple = true))
                }
                Spacer(modifier = Modifier.weight(1f))
                PIconButton(
                    icon = Res.drawable.send,
                    contentDescription = stringResource(Res.string.send_message),
                    tint = MaterialTheme.colorScheme.primary,
                ) {
                    onSend()
                }
            }
        } else {
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}
