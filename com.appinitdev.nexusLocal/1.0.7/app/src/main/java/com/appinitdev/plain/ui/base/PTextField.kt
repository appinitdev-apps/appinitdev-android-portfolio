@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PTextField(
    readOnly: Boolean,
    value: String,
    label: String = "",
    singleLine: Boolean = true,
    onValueChange: (String) -> Unit,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isPassword: Boolean = false,
    placeholder: String = "",
    errorMessage: String = "",
    requestFocus: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions(),
) {
    val clipboardManager = LocalClipboardManager.current
    val focusRequester = remember { FocusRequester() }
    var showPassword by remember { mutableStateOf(false) }
    val windowInfo = LocalWindowInfo.current
    LaunchedEffect(windowInfo) {
        snapshotFlow { windowInfo.isWindowFocused }.collect { isWindowFocused ->
            if (isWindowFocused && requestFocus) {
                focusRequester.requestFocus()
            }
        }
    }
    TextField(
        modifier =
            Modifier
                .focusRequester(focusRequester)
                .fillMaxWidth(),
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
            ),
        maxLines = if (singleLine) 1 else Int.MAX_VALUE,
        enabled = !readOnly,
        value = value,
        label =
            if (label.isEmpty()) {
                null
            } else {
                { Text(label) }
            },
        onValueChange = {
            if (!readOnly) onValueChange(it)
        },
        visualTransformation = if (isPassword && !showPassword) PasswordVisualTransformation() else visualTransformation,
        placeholder = {
            Text(
                text = placeholder,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        isError = errorMessage.isNotEmpty(),
        singleLine = singleLine,
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = {
                    if (isPassword) {
                        showPassword = !showPassword
                    } else if (!readOnly) {
                        onValueChange("")
                    }
                }) {
                    Icon(
                        painter =
                            painterResource(
                                if (isPassword) {
                                    if (showPassword) {
                                        Res.drawable.eye
                                    } else {
                                        Res.drawable.eye_off
                                    }
                                } else {
                                    Res.drawable.x
                                }
                            ),
                        contentDescription = if (isPassword) stringResource(Res.string.password) else stringResource(Res.string.clear),
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    )
                }
            } else {
                IconButton(onClick = {
                    onValueChange(clipboardManager.getText()?.text ?: "")
                }) {
                    Icon(
                        painter = painterResource(Res.drawable.content_paste),
                        contentDescription = stringResource(Res.string.paste),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
    )
}
