@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditDialog(
    userData: UserData,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(userData.username ?: "") }
    var bio by remember { mutableStateOf(userData.bio ?: "") }
    var age by remember { mutableStateOf(userData.age ?: "") }
    var gender by remember { mutableStateOf(userData.gender ?: "") }
    var instagram by remember { mutableStateOf(userData.instagram ?: "") }
    var facebook by remember { mutableStateOf(userData.facebook ?: "") }
    var telegram by remember { mutableStateOf(userData.telegram ?: "") }
    var otherLink by remember { mutableStateOf(userData.otherLink ?: "") }
    var genderExpanded by remember { mutableStateOf(false) }
    val genderOptions = listOf(
        stringResource(R.string.sin_especificar),
        stringResource(R.string.masculino),
        stringResource(R.string.femenino),
        stringResource(R.string.no_binario),
        stringResource(R.string.g_nero_fluido),
        stringResource(R.string.ag_nero),
        stringResource(R.string.big_nero),
        stringResource(R.string.demiboy),
        stringResource(R.string.demigirl),
        stringResource(R.string.intersexual),
        stringResource(R.string.transg_nero),
        stringResource(R.string.transexual),
        stringResource(R.string.dos_esp_ritus),
        stringResource(R.string.otro)
    )
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C1E)),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(28.dp)
                    .fillMaxSize()
            ) {
                Text(
                    text = stringResource(id = R.string.editar_perfil),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text(text = stringResource(id = R.string.nombre), color = Color.Gray) },
                            textStyle = TextStyle(color = Color.White),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF238CDD),
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            label = { Text(text = stringResource(id = R.string.biograf_a), color = Color.Gray) },
                            textStyle = TextStyle(color = Color.White),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF238CDD),
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = age,
                            onValueChange = { age = it },
                            label = { Text(text = stringResource(id = R.string.edad), color = Color.Gray) },
                            textStyle = TextStyle(color = Color.White),
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF238CDD),
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                    }
                    item {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = gender,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(text = stringResource(id = R.string.g_nero), color = Color.Gray) },
                                textStyle = TextStyle(color = Color.White),
                                trailingIcon = {
                                    IconButton(onClick = { genderExpanded = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().clickable { genderExpanded = true },
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF238CDD),
                                    unfocusedBorderColor = Color.Gray
                                )
                            )
                            DropdownMenu(
                                expanded = genderExpanded,
                                onDismissRequest = { genderExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.8f).background(Color(0xFF2A2D31))
                            ) {
                                genderOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option, color = Color.White) },
                                        onClick = {
                                            gender = option
                                            genderExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Text(
                            text = stringResource(R.string.redes_sociales),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = instagram,
                            onValueChange = { instagram = it },
                            label = { Text(text = stringResource(R.string.instagram), color = Color.Gray) },
                            textStyle = TextStyle(color = Color.White),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF238CDD),
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = facebook,
                            onValueChange = { facebook = it },
                            label = { Text(text = stringResource(R.string.facebook), color = Color.Gray) },
                            textStyle = TextStyle(color = Color.White),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF238CDD),
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = telegram,
                            onValueChange = { telegram = it },
                            label = { Text(text = stringResource(R.string.telegram), color = Color.Gray) },
                            textStyle = TextStyle(color = Color.White),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF238CDD),
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = otherLink,
                            onValueChange = { otherLink = it },
                            label = { Text(text = stringResource(R.string.link_externo), color = Color.Gray) },
                            textStyle = TextStyle(color = Color.White),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF238CDD),
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.cancel), 
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = { onSave(name, bio, age, gender, instagram, facebook, telegram, otherLink) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF238CDD))
                    ) {
                        Text(
                            text = stringResource(id = R.string.guardar),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
