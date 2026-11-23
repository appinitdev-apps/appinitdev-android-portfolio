@Composable
fun NetworkErrorDialog(showDialog: Boolean) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {   },
            title = null,
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_network_error),
                        contentDescription = "Error de red",
                        modifier = Modifier.size(100.dp)
                    )
                }
            },
            confirmButton = {},  
            dismissButton = {},
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        )
    }
}
