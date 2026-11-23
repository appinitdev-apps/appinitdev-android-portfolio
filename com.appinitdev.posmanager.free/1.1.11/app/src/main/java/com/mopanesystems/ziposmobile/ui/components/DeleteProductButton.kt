
@Composable
fun DeleteProductButton(
    confirmationMessage: String,
    onDelete: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    FilledTonalButton(
        onClick = { showDialog = true },
        modifier = Modifier.size(42.dp),
        shape = MaterialTheme.shapes.medium,
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Icon(
            Icons.Default.DeleteOutline,
            contentDescription = "Delete product",
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.onErrorContainer
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.confirmar_eliminaci_n)) },
            text = { Text(confirmationMessage) }, 
            confirmButton = {
                IconButton(
                    onClick = {
                        onDelete()
                        showDialog = false
                    }
                ) {
                    Icon(
                        Icons.Default.Check, 
                        contentDescription = stringResource(R.string.eliminar),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                IconButton(onClick = { showDialog = false }) {
                    Icon(
                        Icons.Default.Close, 
                        contentDescription = stringResource(R.string.cancelar)
                    )
                }
            }
        )
    }
}
