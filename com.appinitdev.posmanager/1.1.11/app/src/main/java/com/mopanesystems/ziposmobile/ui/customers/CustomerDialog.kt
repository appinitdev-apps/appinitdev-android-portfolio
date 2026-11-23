
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
                // --- Datos de contacto ---
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

                // --- Habilitar edición ---
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isEditing, onCheckedChange = { isEditing = it })
                    Text(stringResource(R.string.habilitar_edici_n), style = MaterialTheme.typography.bodyLarge)
                }

                // --- Risk Score ---
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

                // --- Balance ---
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

// --- Composable auxiliar para mostrar icono + texto ---
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

//@Composable
//fun CustomerDialog(
//    customer: Customer,
//    onDismiss: () -> Unit,
//    onSave: (Customer) -> Unit,
//) {
//    var isEditing by remember { mutableStateOf(false) }
//    var riskScore by remember { mutableStateOf(customer.riskScore) }
//    var balanceText by remember { mutableStateOf(customer.balance.toPlainString()) }
//
//    val balance = balanceText.toBigDecimalOrNull() ?: BigDecimal.ZERO
//
//    AlertDialog(
//        onDismissRequest = { onDismiss() },
//        confirmButton = {
//            if (isEditing) {
//                TextButton(onClick = {
//                    onSave(customer.copy(riskScore = riskScore, balance = balance))
//                    onDismiss()
//                }) { Text("Guardar") }
//            } else {
//                TextButton(onClick = { onDismiss() }) { Text("Cerrar") }
//            }
//        },
//        title = {
//            Text(
//                text = customer.fullName,
//                style = MaterialTheme.typography.titleLarge,
//                fontWeight = FontWeight.Bold
//            )
//        },
//        text = {
//            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
//                // Datos generales
//                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
//                    customer.email?.let {
//                        Text(
//                            "📧 $it",
//                            style = MaterialTheme.typography.bodyLarge
//                        )
//                    }
//                    customer.phone?.let {
//                        Text(
//                            "📱 $it",
//                            style = MaterialTheme.typography.bodyLarge
//                        )
//                    }
//                    customer.address?.let {
//                        Text(
//                            "🏠 $it",
//                            style = MaterialTheme.typography.bodyLarge
//                        )
//                    }
//                    customer.city?.let {
//                        Text(
//                            "🌆 $it",
//                            style = MaterialTheme.typography.bodyLarge
//                        )
//                    }
//                    customer.state?.let {
//                        Text(
//                            "🌎 $it",
//                            style = MaterialTheme.typography.bodyLarge
//                        )
//                    }
//                    customer.zipCode?.let {
//                        Text(
//                            "📮 $it",
//                            style = MaterialTheme.typography.bodyLarge
//                        )
//                    }
//                    customer.country?.let {
//                        Text(
//                            "🌍 $it",
//                            style = MaterialTheme.typography.bodyLarge
//                        )
//                    }
//                    customer.notes?.let {
//                        Text("📝 $it", style = MaterialTheme.typography.bodyLarge)
//                    }
//                    customer.loyaltyPoints.let {
//                        Text("⭐ $it", style = MaterialTheme.typography.bodyLarge)
//                    }
//                }
//
//                HorizontalDivider()
//
//                // Habilitar edición
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Checkbox(checked = isEditing, onCheckedChange = { isEditing = it })
//                    Text("Habilitar edición", style = MaterialTheme.typography.bodyLarge)
//                }
//
//                // RiskScore con botones bonitos
//                Column {
//
//                    val emoji = when {
//                        riskScore == 0 -> "😊"
//                        riskScore in 1..3 ->  "😐"
//                        else -> "😡"
//                    }
//
//                    val colorContainer = when {
//                        riskScore == 0 -> MaterialTheme.colorScheme.primary
//                        else -> MaterialTheme.colorScheme.error
//                    }
//                    Text("Risk Score", style = MaterialTheme.typography.labelLarge)
//                    Spacer(Modifier.width(4.dp))
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        IconButton(
//                            onClick = { if (riskScore > 0) riskScore-- },
//                            enabled = isEditing && riskScore > 0
//                        ) {
//                            Icon(Icons.Default.Remove, contentDescription = "Disminuir")
//                        }
//
//                        Text(
//                            text = "${emoji} ${riskScore.toString()}",
//                            style = MaterialTheme.typography.titleLarge,
//                            fontWeight = FontWeight.Bold,
//                            color = colorContainer,
//                            modifier = Modifier.padding(horizontal = 12.dp)
//                        )
//
//                        IconButton(
//                            onClick = { if (riskScore < 10) riskScore++ },
//                            enabled = isEditing && riskScore < 10
//                        ) {
//                            Icon(Icons.Default.Add, contentDescription = "Aumentar")
//                        }
//                    }
//                }
//
//                // Balance con estilos
//                Column {
//                    Text("Balance", style = MaterialTheme.typography.bodyLarge)
//                    if (isEditing) {
//                        OutlinedTextField(
//                            value = balanceText,
//                            onValueChange = { balanceText = it },
//                            singleLine = true,
//                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                            modifier = Modifier.padding(top = 4.dp)
//                        )
//                    } else {
//                        val color = when {
//                            balance < BigDecimal.ZERO -> MaterialTheme.colorScheme.error
//                            balance > BigDecimal.ZERO ->MaterialTheme.colorScheme.secondary
//                            else -> MaterialTheme.colorScheme.primary
//                        }
//                        val label = when {
//                            balance < BigDecimal.ZERO -> "Crédito"
//                            balance > BigDecimal.ZERO -> "Saldo a favor"
//                            else -> ""
//                        }
//
//                        Surface(
//                            shape = MaterialTheme.shapes.medium,
//                            color = color,
//                            modifier = Modifier.padding(top = 4.dp)
//                        ) {
//                            Row(
//                                verticalAlignment = Alignment.CenterVertically,
//                                modifier = Modifier.padding(12.dp)
//                            ) {
//                                Text(
//                                    text = balance.toPlainString(),
//                                    style = MaterialTheme.typography.bodyLarge,
//                                    fontWeight = FontWeight.Bold
//                                )
//                                if (label.isNotEmpty()) {
//                                    Spacer(Modifier.width(8.dp))
//                                    Text(
//                                        text = "($label)",
//                                        style = MaterialTheme.typography.bodyLarge
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    )
//}
