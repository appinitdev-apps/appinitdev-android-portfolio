
@Composable
fun TimePickerDialog(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    toggle: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min)
                .background(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface
                )
        ) {
            Column(
                modifier =Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(id = R.string.select_time), modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                    style = typography.labelLarge
                )
                content()
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth()
                ) {
                    toggle()
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onCancel) {
                        Text(text = stringResource(id = R.string.cancel),style = typography.bodyLarge)
                    }
                    TextButton(onClick = onConfirm) {
                        Text(text = stringResource(id = R.string.confirm),style = typography.bodyLarge)
                    }
                }
            }

        }
    }
}