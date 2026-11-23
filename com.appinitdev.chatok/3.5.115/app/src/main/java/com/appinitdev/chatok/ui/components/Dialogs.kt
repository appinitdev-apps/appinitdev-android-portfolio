
@Composable
fun BoostDialog(balance: Int, onDismiss: () -> Unit, onBoost: (Int) -> Unit) {
    val cost = 25
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.impulsar_perfil)) },
        text = { Column {
            Text(stringResource(R.string.boost_desc))
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.costo_monedas, cost), fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.tu_saldo, balance), color = Color.Gray)
        }},
        confirmButton = {
            TextButton(
                onClick = { onBoost(cost) },
                enabled = balance >= cost
            ) { Text(stringResource(R.string.activar_boost), color = Color(0xFF238CDD)) }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) { Text(stringResource(R.string.cancelar), color = Color.Gray) }
        },
        containerColor = Color(0xFF1A1C1E),
        titleContentColor = Color.White,
        textContentColor = Color.LightGray
    )
}

@Composable
fun TransferCoinsDialog(balance: Int, onDismiss: () -> Unit, onTransfer: (Int) -> Unit) {
    var amountText by remember { mutableStateOf("") }
    val amount = amountText.toIntOrNull() ?: 0
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.transferir_monedas)) },
        text = {
            Column {
                Text(stringResource(R.string.tu_saldo, balance), color = Color.Gray)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) amountText = it },
                    label = { Text(stringResource(R.string.cantidad_monedas)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF238CDD)
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (amount > 0) onTransfer(amount) },
                enabled = amount > 0 && amount <= balance
            ) { Text(stringResource(R.string.enviar_monedas), color = Color(0xFF238CDD)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancelar), color = Color.Gray) }
        },
        containerColor = Color(0xFF1A1C1E),
        titleContentColor = Color.White,
        textContentColor = Color.LightGray
    )
}
