@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDialogBox(
    state: AppState,
    hideDialog: () -> Unit,
    addChat: () -> Unit,
    setEmail: (String) -> Unit
) {
    Dialog(
        onDismissRequest = hideDialog,
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
                    text = stringResource(R.string.ingrese_su_id_de_correo_electr_nico),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = state.srEmail,
                    onValueChange = { setEmail(it.lowercase()) },
                    label = { Text(stringResource(R.string.ingrese_el_correo_electr_nico), color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF238CDD)) },
                    textStyle = TextStyle(color = Color.White),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF238CDD),
                        unfocusedBorderColor = Color.Gray
                    )
                )
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SecondaryButton(
                        text = stringResource(R.string.cancelar),
                        onClick = hideDialog,
                        modifier = Modifier.weight(1f)
                    )
                    MainGradientButton(
                        text = stringResource(R.string.agregar),
                        onClick = addChat,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
