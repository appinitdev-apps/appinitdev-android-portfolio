@Composable
fun CustomerDialog(
    customer: Customer,
    onDismiss: () -> Unit,
    onSave: (Customer) -> Unit,
) {
    var isEditing by remember { mutableStateOf(false) }
    var riskScore by remember { mutableStateOf(customer.riskScore) }
    var balanceText by remember { mutableStateOf(customer.balance.toPlainString()) }
    val balance = balanceText.toBigDecimalOrNull() ?: BigDecimal.ZERO
    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isEditing) {
                        onSave(customer.copy(riskScore = riskScore, balance = balance))
                    }
                    onDismiss()
                }
            ) {
                Text(if (isEditing) stringResource(R.string.guardar_cliente) else stringResource(R.string.cerrar))
            }
        },
        title = {
            Text(
                text = customer.fullName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    customer.email?.let { InfoField(stringResource(R.string.email), it) }
                    customer.phone?.let { InfoField(stringResource(R.string.phone), it) }
                    customer.address?.let { InfoField(label = stringResource(R.string.address), value = it) }
                    customer.city?.let { InfoField(stringResource(R.string.city), it) }
                    customer.state?.let { InfoField(stringResource(R.string.state), it) }
                    customer.zipCode?.let { InfoField(stringResource(R.string.zip_code), it) }
                    customer.country?.let { InfoField(label = stringResource(R.string.country), value = it) }
                    customer.notes?.let { InfoField(stringResource(R.string.notes), it) }
                    customer.notes?.let { InfoField(stringResource(R.string.points), customer.loyaltyPoints.toString()) }
                }
                HorizontalDivider(thickness = 1.dp, color = DividerDefaults.color)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isEditing, onCheckedChange = { isEditing = it })
                    Text(stringResource(R.string.habilitar_edici_n), style = MaterialTheme.typography.bodyLarge)
                }
                Column {
                    Text(text = stringResource(R.string.risk_score), style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.size(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (riskScore > 0) riskScore-- },
                            enabled = isEditing && riskScore > 0
                        ) { Icon(Icons.Default.Remove, contentDescription = "Disminuir") }
                        val (emoji, color) = when {
                            riskScore == 0 -> "😊" to MaterialTheme.colorScheme.primary
                            riskScore in 1..3 -> "😐" to MaterialTheme.colorScheme.secondary
                            else -> "😡" to MaterialTheme.colorScheme.error
                        }
                        Text(
                            text = "$emoji $riskScore",
                            fontWeight = FontWeight.Bold,
                            color = color,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        IconButton(
                            onClick = { if (riskScore < 10) riskScore++ },
                            enabled = isEditing && riskScore < 10
                        ) { Icon(Icons.Default.Add, contentDescription = "Aumentar") }
                    }
                }
                Column {
                    Text(stringResource(R.string.balance), style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.size(4.dp))
                    if (isEditing) {
                        OutlinedTextField(
                            value = balanceText,
                            onValueChange = { balanceText = it },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.titleSmall,
                        )
                    } else {
                        val (color, label) = when {
                            balance < BigDecimal.ZERO -> MaterialTheme.colorScheme.error to stringResource(
                                R.string.cr_dito
                            )
                            balance > BigDecimal.ZERO -> MaterialTheme.colorScheme.secondary to stringResource(
                                R.string.saldo_a_favor)
                            else -> MaterialTheme.colorScheme.primary to ""
                        }
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = color,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = balance.toPlainString(),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                if (label.isNotEmpty()) {
                                    Spacer(Modifier.width(8.dp))
                                    Text(label, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}
@Composable
private fun InfoRow(icon: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 18.sp)
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}
@Composable
private fun InfoField(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall
        )
    }
}
