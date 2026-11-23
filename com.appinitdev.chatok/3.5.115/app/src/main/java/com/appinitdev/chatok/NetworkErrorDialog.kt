
@Composable
fun NetworkErrorDialog(showDialog: Boolean) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { /* No se cierra con toque fuera ni back */ },
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
            confirmButton = {},  // Sin botones
            dismissButton = {},
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        )
    }
}



/*@Composable
fun NetworkErrorDialog(showDialog: Boolean) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { /* No se cierra con toque fuera ni back */ },
            title = null,
            text = {
                Image(
                    painter = painterResource(id = R.drawable.ic_network_error),
                    contentDescription = "Error de red",
                    modifier = Modifier.size(150.dp)
                )
            },
            confirmButton = {},  // Sin botones
            dismissButton = {},
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        )
    }
}*/
