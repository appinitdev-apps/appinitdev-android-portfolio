
@Composable
fun EditProfileScreen(navController: NavController, viewModel: AuthViewModel) {
    val profile by viewModel.userProfile.collectAsState()

    var devName by remember(profile) { mutableStateOf(profile?.devName ?: "") }
    var bio by remember(profile) { mutableStateOf(profile?.bio ?: "") }
    var contacto by remember(profile) { mutableStateOf(profile?.contacto ?: "") }
    var web by remember(profile) { mutableStateOf(profile?.web ?: "") }

    
    val isEmailValid = contacto.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(contacto).matches()
    val isFormValid = devName.isNotBlank() && isEmailValid

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.tu_perfil_de_desarrollador),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            
            OutlinedTextField(
                value = devName,
                onValueChange = { devName = it },
                label = { Text("${stringResource(R.string.nombre_de_desarrollador)} *") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                isError = devName.isBlank(),
                supportingText = { if (devName.isBlank()) Text(stringResource(R.string.nombre_requerido)) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            
            OutlinedTextField(
                value = contacto,
                onValueChange = { contacto = it },
                label = { Text("${stringResource(R.string.correo_para_realizar_test)} *") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                isError = contacto.isNotEmpty() && !isEmailValid,
                supportingText = {
                    if (contacto.isEmpty()) {
                        Text(stringResource(R.string.correo_requerido))
                    } else if (!isEmailValid) {
                        Text(stringResource(R.string.formato_de_correo_inv_alido))
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text(stringResource(R.string.biograf_a)) },
                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                minLines = 3,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            
            OutlinedTextField(
                value = web,
                onValueChange = { web = it },
                label = { Text(stringResource(R.string.p_gina_web_o_enlace)) },
                leadingIcon = { Icon(Icons.Default.Language, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done
                )
            )

            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        val currentProfile = profile ?: com.appinitdev.tester.model.UserProfile(
                            uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                        )

                        val updated = currentProfile.copy(
                            devName = devName.trim(),
                            bio = bio.trim(),
                            contacto = contacto.trim(),
                            web = web.trim(),
                            perfilCompletado = true
                        )

                        viewModel.saveProfile(updated) {
                            navController.navigate("home") {
                                popUpTo("edit_profile") { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = isFormValid 
                ) {
                    Text(
                        text = stringResource(R.string.guardar_perfil),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}