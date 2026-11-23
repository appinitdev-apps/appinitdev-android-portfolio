
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ColorPickerDialog(
    title: String,
    initValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val colorPickerController = rememberColorPickerController()
    var customColorValue by remember { mutableStateOf(initValue) }
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(customColorValue)
                }
            ) {
                Text(stringResource(Res.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(stringResource(Res.string.cancel))
            }
        },
        title = {
            Text(text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            )
            {
                HsvColorPicker(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(300.dp),
                    controller = colorPickerController,
                    initialColor = customColorValue.safeHexToColor(),
                    onColorChanged = { colorEnvelope: ColorEnvelope ->
                        customColorValue = colorEnvelope.hexCode
                    }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(15.dp),
                ) {
                    ClipboardTextField(
                        value = customColorValue,
                        modifier = Modifier.weight(0.6f),
                        placeholder = stringResource(Res.string.primary_color_hint),
                        onValueChange = {
                            customColorValue = it
                        },
                        onConfirm = {
                            onConfirm(it)
                        }
                    )
                    Card(
                        modifier = Modifier
                            .height(50.dp)
                            .width(50.dp),
                        shape = RoundedCornerShape(CornerSize(5.dp)),
                        colors = CardColors(
                            containerColor = customColorValue.safeHexToColor(),
                            contentColor = contentColorFor(backgroundColor = customColorValue.safeHexToColor()),
                            disabledContainerColor = Color.LightGray,
                            disabledContentColor = Color.LightGray
                        )
                    ) {}
                }
            }
        })
}