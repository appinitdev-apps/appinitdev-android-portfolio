@file:OptIn(ExperimentalToolkitApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    state: AppState,
    onSignUpClick: (email: String, PW: String, user: String, age: String, gender: String, profileUri: String, isVendedora: Boolean, instagram: String, facebook: String, telegram: String) -> Unit,
    onSignInClick: (email: String, PW: String) -> Unit,
    navController: NavController,
) {
    var selectedImageUri by remember { mutableStateOf("https://appinitdev-android.web.app/Therian/user1.png") }
    val defaultAvatars = listOf(
        "https://appinitdev-android.web.app/Therian/user1.png",
        "https://appinitdev-android.web.app/Therian/user2.png",
        "https://appinitdev-android.web.app/Therian/user3.png",
        "https://appinitdev-android.web.app/Therian/user4.png",
        "https://appinitdev-android.web.app/Therian/user5.png",
        "https://appinitdev-android.web.app/Therian/user6.png",
        "https://appinitdev-android.web.app/Therian/user7.png",
        "https://appinitdev-android.web.app/Therian/user8.png",
        "https://appinitdev-android.web.app/Therian/user9.png",
    )
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordVisibleOne by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var genderExpanded by remember { mutableStateOf(false) }
    var ageError by remember { mutableStateOf("") }
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
    val context = LocalContext.current
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isSignIn by remember { mutableStateOf(true) }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isVendedora by remember { mutableStateOf(false) }
    var instagram by remember { mutableStateOf("") }
    var facebook by remember { mutableStateOf("") }
    var telegram by remember { mutableStateOf("") }
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
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(60.dp))
            LottieAnimation(
                composition = lottieComp,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(120.dp)
            )
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = Color.White
            )
            Text(
                modifier = Modifier.padding(horizontal = 32.dp),
                text = stringResource(R.string.sum_rgete_y_con_ctate_con_amigos_comparte_historias_y_descubre_nuevas_perspectivas),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center, 
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(32.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1C1E).copy(alpha = 0.85f)
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
                        text = if (isSignIn) stringResource(R.string.iniciar_sesi_n) else stringResource(R.string.registrarse),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(24.dp))
                    OutlinedTextField(
                        value = email,
                        textStyle = TextStyle(color = Color.White),
                        onValueChange = { email = it },
                        label = { Text(stringResource(R.string.correo_electr_nico), color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF238CDD)) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = errorMessage.isNotEmpty(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF238CDD),
                            unfocusedBorderColor = Color.Gray,
                            cursorColor = Color(0xFF238CDD)
                        ),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None)
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = password,
                        textStyle = TextStyle(color = Color.White),
                        onValueChange = { password = it },
                        label = { Text(stringResource(R.string.password), color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF238CDD)) },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (passwordVisibleOne) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisibleOne = !passwordVisibleOne }) {
                                Icon(
                                    imageVector = if (passwordVisibleOne) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFF238CDD),
                            unfocusedBorderColor = Color.Gray,
                            cursorColor = Color(0xFF238CDD)
                        ),
                        isError = errorMessage.isNotEmpty()
                    )
                    if (!isSignIn) {
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = confirmPassword,
                            textStyle = TextStyle(color = Color.White),
                            onValueChange = { confirmPassword = it },
                            label = { Text(stringResource(R.string.confirmar_contrase_a), color = Color.Gray) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF238CDD)) },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = null,
                                        tint = Color.Gray
                                    )
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Color(0xFF238CDD),
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = username,
                            textStyle = TextStyle(color = Color.White),
                            onValueChange = { username = it },
                            label = { Text(stringResource(R.string.nombre_de_usuario), color = Color.Gray) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF238CDD)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Color(0xFF238CDD),
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                        Spacer(Modifier.height(20.dp))
                        Text(
                            text = stringResource(R.string.selecciona_tu_avatar),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            defaultAvatars.forEach { url ->
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .border(
                                            width = if (selectedImageUri == url) 3.dp else 1.dp,
                                            color = if (selectedImageUri == url) Color(0xFF238CDD) else Color.Gray,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedImageUri = url }
                                ) {
                                    AsyncImage(
                                        model = url,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = age,
                            textStyle = TextStyle(color = Color.White),
                            onValueChange = {
                                val newAge = it.filter { char -> char.isDigit() }
                                age = newAge
                                if (newAge.isNotEmpty() && newAge.toIntOrNull() != null) {
                                    ageError = if (newAge.toInt() >= 18) "" else context.getString(R.string.debes_ser_mayor_de_18_a_os)
                                } else {
                                    ageError = ""
                                }
                            },
                            label = { Text(stringResource(R.string.edad_18), color = Color.Gray) },
                            leadingIcon = { Icon(Icons.Default.Cake, contentDescription = null, tint = Color(0xFF238CDD)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Color(0xFF238CDD),
                                unfocusedBorderColor = Color.Gray
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        if (ageError.isNotEmpty()) {
                            Text(text = ageError, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(Modifier.height(12.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = gender,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.g_nero), color = Color.Gray) },
                                leadingIcon = { Icon(Icons.Default.Transgender, contentDescription = null, tint = Color(0xFF238CDD)) },
                                trailingIcon = {
                                    IconButton(onClick = { genderExpanded = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().clickable { genderExpanded = true },
                                shape = RoundedCornerShape(16.dp),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = Color(0xFF238CDD),
                                    unfocusedBorderColor = Color.Gray
                                )
                            )
                            DropdownMenu(
                                expanded = genderExpanded,
                                onDismissRequest = { genderExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.7f).background(Color(0xFF2A2D31))
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
                        Spacer(Modifier.height(16.dp))
                        Text(stringResource(R.string.redes_sociales_opcional), color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                        OutlinedTextField(
                            value = instagram,
                            textStyle = TextStyle(color = Color.White),
                            onValueChange = { instagram = it },
                            label = { Text(stringResource(R.string.instagram), color = Color.Gray) },
                            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = Color(0xFF238CDD)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF238CDD), unfocusedBorderColor = Color.Gray)
                        )
                        OutlinedTextField(
                            value = facebook,
                            textStyle = TextStyle(color = Color.White),
                            onValueChange = { facebook = it },
                            label = { Text(stringResource(R.string.facebook), color = Color.Gray) },
                            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = Color(0xFF238CDD)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF238CDD), unfocusedBorderColor = Color.Gray)
                        )
                        OutlinedTextField(
                            value = telegram,
                            textStyle = TextStyle(color = Color.White),
                            onValueChange = { telegram = it },
                            label = { Text(stringResource(R.string.telegram), color = Color.Gray) },
                            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = Color(0xFF238CDD)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF238CDD), unfocusedBorderColor = Color.Gray)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(id = R.string.eres_vendedora),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = stringResource(R.string.solo_para_usuarias_vendedoras),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                            Switch(
                                checked = isVendedora,
                                onCheckedChange = { isVendedora = it }
                            )
                        }
                    }
                    if (errorMessage.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text(text = errorMessage, color = Color(0xFFFF5252), style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(Modifier.height(32.dp))
                    MainGradientButton(
                        text = if (isSignIn) stringResource(R.string.iniciar_sesi_nw) else stringResource(R.string.registrarsew),
                        onClick = {
                            if (email.isNotEmpty() && password.isNotEmpty()) {
                                if (!isLoading) {
                                    isLoading = true
                                    if (isSignIn) {
                                        onSignInClick(email.lowercase(), password)
                                    } else {
                                        if (password == confirmPassword) {
                                            if (username.isNotEmpty()) {
                                                if (age.isNotEmpty() && (age.toIntOrNull() ?: 0) >= 18) {
                                                    if (gender.isNotEmpty()) {
                                                        onSignUpClick(email.lowercase(), password, username, age, gender, selectedImageUri, isVendedora, instagram, facebook, telegram)
                                                    } else errorMessage = context.getString(R.string.agrege_tu_genero)
                                                } else errorMessage = context.getString(R.string.debes_ser_mayor_de_18_a_oss)
                                            } else errorMessage = context.getString(R.string.agrege_su_nombre)
                                        } else errorMessage = context.getString(R.string.las_contrase_as_no_coinciden)
                                    }
                                    CoroutineScope(Dispatchers.Main).launch {
                                        delay(2000)
                                        isLoading = false
                                    }
                                }
                            } else {
                                errorMessage = context.getString(R.string.por_favor_ingrese_el_correo_electr_nico_y_la_contrase_a)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isLoading = isLoading
                    )
                    Spacer(Modifier.height(16.dp))
                    TextButton(
                        onClick = {
                            isSignIn = !isSignIn
                            errorMessage = ""
                        }
                    ) {
                        Text(
                            text = if (isSignIn) stringResource(R.string.no_tienes_una_cuenta_reg_strate) else stringResource(R.string.ya_tienes_una_cuenta_inicia_sesi_n),
                            color = Color(0xFF238CDD),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (isSignIn) {
                        TextButton(onClick = { navController.navigate("forgot_password") }) {
                            Text(
                                text = stringResource(R.string.olvidaste_tu_contrase_a),
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
