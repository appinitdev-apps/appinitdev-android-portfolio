
@Composable
fun TextFieldDialog(
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    title: String = "",
    icon: ImageVector? = null,
    value: String = "",
    placeholder: String = "",
    description: String = "",
    isPassword: Boolean = false,
    errorText: String = "",
    dismissText: String = stringResource(Res.string.cancel),
    confirmText: String = stringResource(Res.string.confirm),
    validator: (String) -> Boolean = { true },
    validationErrorText: String = "",
    onValueChange: (String) -> Unit = {},
    onDismissRequest: () -> Unit = {},
    onConfirm: (String) -> Unit = {},
    keyboardOptions: KeyboardOptions =
        KeyboardOptions(
            imeAction = if (singleLine) ImeAction.Done else ImeAction.Default,
        ),
) {
    val focusManager = LocalFocusManager.current
    var currentValue by remember { mutableStateOf(value) }
    var showValidationError by remember { mutableStateOf(false) }
    val displayErrorText = if (showValidationError) validationErrorText else errorText
    
    AlertDialog(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = onDismissRequest,
        icon = {
            icon?.let {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                )
            }
        },
        title = {
            Text(
                text = title, maxLines = 1, overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                if (description.isNotEmpty()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
                ClipboardTextField(
                    modifier = modifier,
                    readOnly = readOnly,
                    value = currentValue,
                    singleLine = singleLine,
                    onValueChange = { 
                        currentValue = it
                        showValidationError = false
                        onValueChange(it)
                    },
                    placeholder = placeholder,
                    isPassword = isPassword,
                    errorText = displayErrorText,
                    keyboardOptions = keyboardOptions,
                    focusManager = focusManager,
                    requestFocus = true,
                    onConfirm = { 
                        if (validator(it)) {
                            onConfirm(it)
                        } else {
                            showValidationError = true
                        }
                    },
                )
            }
        },
        confirmButton = {
            Button(
                enabled = currentValue.isNotBlank(),
                onClick = {
                    if (validator(currentValue)) {
                        focusManager.clearFocus()
                        onConfirm(currentValue)
                    } else {
                        showValidationError = true
                    }
                },
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = dismissText)
            }
        },
    )
}
