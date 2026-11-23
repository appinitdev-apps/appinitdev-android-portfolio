
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onPasswordReset: (String) -> Unit,
    navController: NavController
) {
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    val brush = Brush.linearGradient(
        listOf(
            Color(0xFF238CDD),
            Color(0xFF1952C4)
        )
    )
    
    val lottieComp by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.animation1))

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            painter = painterResource(R.drawable.login_blur),
            contentDescription = null
        )
        
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBackIosNew, contentDescription = null, tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            LottieAnimation(
                composition = lottieComp,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(150.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1C1E).copy(alpha = 0.9f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.recuperar_contrase_a),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = stringResource(id = R.string.si_el_correo_es_v_lido_recibir_s_un_enlace_de_recuperaci_n),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        textStyle = TextStyle(color = Color.White),
                        label = { Text(stringResource(R.string.correo_electr_nico), color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF238CDD)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFF238CDD),
                            unfocusedBorderColor = Color.Gray
                        )
                    )

                    if (message.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = message, 
                            color = if (message.contains("recibirás") || message.contains("enlace")) Color(0xFF4CAF50) else Color(0xFFFF5252),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    MainGradientButton(
                        text = stringResource(R.string.enviar_enlace_de_recuperaci_n),
                        onClick = {
                            if (email.isNotEmpty()) {
                                isLoading = true
                                CoroutineScope(Dispatchers.IO).launch {
                                    onPasswordReset(email)
                                    message = context.getString(R.string.si_el_correo_es_v_lido_recibir_s_un_enlace_de_recuperaci_n)
                                    isLoading = false
                                }
                            } else {
                                message = context.getString(R.string.por_favor_ingresa_un_correo_v_lido)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isLoading = isLoading
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            TextButton(onClick = { navController.popBackStack() }) {
                Text(
                    text = stringResource(R.string.volver_al_inicio_de_sesi_n),
                    color = Color(0xFF238CDD),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            AdBanner()
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
