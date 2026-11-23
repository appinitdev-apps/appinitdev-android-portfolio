
@Composable
fun DeviceRenameDialog(name: String, onDismiss: () -> Unit, onDone: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    val newName = remember {
        mutableStateOf(name)
    }
    TextFieldDialog(
        title = stringResource(Res.string.device_name),
        value = newName.value,
        placeholder = name,
        onValueChange = {
            newName.value = it
        },
        onDismissRequest = {
            onDismiss()
        },
        confirmText = stringResource(Res.string.save),
        onConfirm = {
            scope.launch {
                withIO {
                    DeviceNamePreference.putAsync(newName.value)
                    TempData.deviceName.value = newName.value
                    sendEvent(WebSocketEvent(EventType.DEVICE_NAME_UPDATED, jsonEncode(newName.value)))
                }
                onDone(newName.value)
                onDismiss()
            }
        },
    )
}