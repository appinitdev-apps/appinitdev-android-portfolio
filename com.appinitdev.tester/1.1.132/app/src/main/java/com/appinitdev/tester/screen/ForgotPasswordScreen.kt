
@Composable
fun ForgotPasswordScreen(navController: NavController, viewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    
    var showSuccessMessage by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.clearState()
    }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            showSuccessMessage = true
            viewModel.clearState() 
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.recuperar_contrase_a),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = stringResource(R.string.ingresa_tu_correo_y_te_enviaremos_un_enlace_para_restablecer_tu_clave),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.email_registrado)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState is AuthUiState.Loading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { viewModel.resetPassword(email) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.enviar_correo))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = { navController.popBackStack() }) {
            Text(stringResource(R.string.cancelar_volver))
        }

        
        if (showSuccessMessage) {
            Text(
                text = stringResource(R.string.correo_enviado_revisa_tu_bandeja_de_entrada_revisa_la_seccionde_spam),
                color = Color(0xFF4CAF50), 
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        if (uiState is AuthUiState.Error) {
            Text(
                text = (uiState as AuthUiState.Error).msg ?: stringResource(R.string.error_subir_imagen_msg),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}