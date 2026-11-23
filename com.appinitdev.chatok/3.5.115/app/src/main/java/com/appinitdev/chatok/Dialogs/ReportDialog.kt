
@Composable
fun ReportDialog(
    reportedUsername: String,
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit
) {
    var reason by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val reasons = listOf("Spam", "Contenido Inapropiado", "Acoso", "Estafa", "Otro")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C1E)),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.reportar_a, reportedUsername),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(text = if (reason.isEmpty()) stringResource(R.string.seleccionar_motivo) else reason, color = Color.White)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        reasons.forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r) },
                                onClick = {
                                    reason = r
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text(stringResource(R.string.detalles_adicionales), color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF238CDD),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.cancelarw), color = Color.Gray)
                    }
                    Button(
                        onClick = { onSubmit(reason, details) },
                        enabled = reason.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                    ) {
                        Text(stringResource(R.string.enviar_reporte), color = Color.White)
                    }
                }
            }
        }
    }
}
