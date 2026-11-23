
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ChatViewModel, state: AppState,
    onSignOut: () -> Unit, navController: NavController
) {
    val comp by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.upload))
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    val user = state.userData
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var editProfile by remember { mutableStateOf(false) }
    var viewImage by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showVerificationDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var imgUri by remember { mutableStateOf<Uri?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()){
        imgUri = it
    }

    val brush = Brush.linearGradient(
        listOf(
            Color(0xFF238CDD),
            Color(0xFF1952C4)
        )
    )

    fun compressImage(): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        return outputStream.toByteArray()
    }

    fun uploadImage(){
        if (bitmap == null) {
            isLoading = false
            return
        }
        viewModel.uploadMedia(bytes = compressImage(), folder = "photos") { imageUrl ->
            if (imageUrl.isNotEmpty()) {
                val updatedUserData = user?.copy(ppurl = imageUrl) ?: UserData(
                    email = user?.email.toString(),
                    userId = user?.userId.toString(),
                    ppurl = imageUrl
                )
                viewModel.updateProfile(userData = updatedUserData)
            }
            isLoading = false
        }
    }

    BackHandler {
        if (editProfile || viewImage || imgUri != null) {
            editProfile = false
            viewImage = false
            imgUri = null
        } else {
            navController.popBackStack()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(R.drawable.blurry_gradient_haikei),
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
        
        
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(imageVector = Icons.Filled.ArrowBackIosNew, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { editProfile = true },
                    modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(imageVector = Icons.Filled.Edit, contentDescription = null, tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(brush)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val displayImage = if (user?.ppurl.isNullOrEmpty()) "https://appinitdev-android.web.app/Therian/user1.png" else user?.ppurl
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(displayImage)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .clickable { if (!user?.ppurl.isNullOrEmpty()) viewImage = true }
                    )
                    
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            LottieAnimation(composition = comp, iterations = LottieConstants.IterateForever, modifier = Modifier.size(80.dp))
                        }
                    }
                }
                
                IconButton(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFF238CDD), CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                ) {
                    Icon(imageVector = Icons.Filled.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = user?.username.orEmpty(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                if (user?.pref?.isVerified == true) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Rounded.Verified,
                        contentDescription = "Verified",
                        tint = Color(0xFF238CDD),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Text(
                text = user?.email.orEmpty(),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )

            if (user?.pref?.isVerified != true) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    onClick = { showVerificationDialog = true },
                    color = Color(0xFF238CDD).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF238CDD).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF238CDD), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(text = stringResource(R.string.verificar_cuenta), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    onClick = { navController.navigate("store") },
                    color = Color(0xFFFFD700).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f))
                ) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(text = stringResource(R.string.monedas_formato, user?.balance ?: 0), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                
                if (user?.isVip == true) {
                    Spacer(Modifier.width(12.dp))
                    Surface(
                        color = Color(0xFFFF71FF).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFFF71FF).copy(alpha = 0.5f))
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFF71FF), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(text = stringResource(R.string.vip), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1C1E).copy(alpha = 0.8f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    InfoRow(icon = Icons.Default.Cake, text = user?.age ?: stringResource(R.string.na))
                    InfoRow(icon = Icons.Default.Person, text = user?.gender ?: stringResource(R.string.na))
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = if (!user?.bio.isNullOrEmpty()) user?.bio!! else stringResource(R.string.sin_biografia2),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (!user?.bio.isNullOrEmpty()) Color.White else Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!user?.instagram.isNullOrBlank()) {
                            IconButton(onClick = { 
                                val link = user?.instagram!!
                                val url = if (link.startsWith("http")) link else "https://instagram.com/${link.removePrefix("@")}"
                                uriHandler.openUri(url)
                            }) {
                                Icon(imageVector = Icons.Default.Language, contentDescription = stringResource(R.string.instagram), tint = Color(0xFF238CDD))
                            }
                        }
                        if (!user?.facebook.isNullOrBlank()) {
                            IconButton(onClick = { 
                                val link = user?.facebook!!
                                val url = if (link.startsWith("http")) link else "https://facebook.com/$link"
                                uriHandler.openUri(url)
                            }) {
                                Icon(imageVector = Icons.Default.Facebook, contentDescription = stringResource(R.string.facebook), tint = Color(0xFF238CDD))
                            }
                        }
                        if (!user?.telegram.isNullOrBlank()) {
                            IconButton(onClick = { 
                                val link = user?.telegram!!
                                val url = if (link.startsWith("http")) link else "https://t.me/${link.removePrefix("@")}"
                                uriHandler.openUri(url)
                            }) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(R.string.telegram), tint = Color(0xFF238CDD))
                            }
                        }
                        if (!user?.otherLink.isNullOrBlank()) {
                            IconButton(onClick = { 
                                val link = user?.otherLink!!
                                val url = if (link.startsWith("http")) link else "https://$link"
                                uriHandler.openUri(url)
                            }) {
                                Icon(imageVector = Icons.Default.Public, contentDescription = "Website", tint = Color(0xFF238CDD))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            











            Spacer(modifier = Modifier.height(32.dp))
            
            TextButton(
                onClick = { showDeleteAccountDialog = true },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(text = stringResource(R.string.eliminar_mi_cuenta), color = Color(0xFFFF5252), style = MaterialTheme.typography.labelLarge)
            }

            AdBanner()
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    
    imgUri?.let { uri ->
        StoryPreview(
            uri = uri, 
            isSeller = user?.pref?.verificada ?: false,
            hideDialog = { imgUri = null }, 
            upload = { cropped, locked, price ->
                val finalUri = cropped ?: uri
                isLoading = true
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val src = ImageDecoder.createSource(context.contentResolver, finalUri)
                        bitmap = ImageDecoder.decodeBitmap(src) { decoder, _, _ ->
                            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                            decoder.isMutableRequired = true
                        }
                    } else {
                        bitmap = android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, finalUri)
                    }
                    uploadImage()
                } catch (e: Exception) {
                    Log.e("ProfileScreen", "Error uploading image", e)
                    isLoading = false
                }
                imgUri = null
            }
        )
    }

    AnimatedVisibility(viewImage) {
        View(imageUrl = user?.ppurl.toString(), hideDialog = { viewImage = false })
    }

    AnimatedVisibility(visible = editProfile) {
        ProfileEditDialog(userData = user ?: UserData(), onDismiss = { editProfile = false }, onSave = { n, b, a, g, i, f, t, ol ->
            val updatedUser = user?.copy(
                username = n,
                bio = b,
                age = a,
                gender = g,
                instagram = i,
                facebook = f,
                telegram = t,
                otherLink = ol
            ) ?: UserData(
                email = user?.email.toString(),
                userId = user?.userId.toString(),
                username = n,
                bio = b,
                age = a,
                gender = g,
                ppurl = user?.ppurl.toString(),
                instagram = i,
                facebook = f,
                telegram = t,
                otherLink = ol
            )
            viewModel.updateProfile(userData = updatedUser)
            editProfile = false
        })
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text(stringResource(R.string.borrar_chat)) }, 
            text = { Text(stringResource(R.string.esta_accion_es_permanente)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAccount {
                            onSignOut()
                            showDeleteAccountDialog = false
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF5252))
                ) {
                    Text(stringResource(R.string.eliminar_definitivamente))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text(stringResource(R.string.cancelarw))
                }
            },
            containerColor = Color(0xFF1A1C1E),
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }

    if (showVerificationDialog) {
        com.appinitdev.chatok.Dialogs.VerificationRequestDialog(
            state = state,
            onDismiss = { showVerificationDialog = false },
            onSubmit = { msg, uris ->
                viewModel.submitVerificationRequest(msg, uris)
                showVerificationDialog = false
                Toast.makeText(context, context.getString(R.string.reporte_enviado), Toast.LENGTH_LONG).show()
            }
        )
    }
}
