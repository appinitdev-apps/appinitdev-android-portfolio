@Composable
fun PasswordTextField(
    value: String = "", isChanged: () -> Boolean,
    onValueChange: (String) -> Unit = {}, onConfirm: (String) -> Unit = {},
) {
    Box(modifier = Modifier.padding(16.dp, 0.dp, 16.dp, 0.dp)) {
        val clipboardManager = LocalClipboardManager.current
        val focusRequester = remember { FocusRequester() }
        TextField(modifier = Modifier.focusRequester(focusRequester).fillMaxWidth(),
            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, disabledContainerColor = Color.Transparent),
            maxLines = 1, value = value, onValueChange = { onValueChange(it) },
            visualTransformation = VisualTransformation.None,
            placeholder = { Text(text = stringResource(Res.string.password), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium) },
            singleLine = true,
            trailingIcon = {
                if (value.isNotEmpty()) {
                    if (isChanged()) { Button(onClick = { onConfirm(value) }) { Text(stringResource(Res.string.save)) } }
                } else {
                    IconButton(onClick = { onValueChange(clipboardManager.getText()?.text ?: "") }) {
                        Icon(painter = painterResource(Res.drawable.content_paste), contentDescription = stringResource(Res.string.paste), tint = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done))
    }
}
