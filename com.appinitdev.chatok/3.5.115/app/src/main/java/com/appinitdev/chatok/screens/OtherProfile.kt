@Composable
fun OtherProfile(
    initialUser: ChatUserData, navController: NavController, viewModel: ChatViewModel
) {
    var viewImage by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val state by viewModel.state.collectAsState()
    val user = state.User2 ?: initialUser
    val brush = Brush.linearGradient(
        listOf(
            Color(0xFF238CDD),
            Color(0xFF1952C4)
        )
    )
    BackHandler {
        if (viewImage) {
            viewImage = false
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
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
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
                    onClick = { showReportDialog = true },
                    modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.Report, contentDescription = "Report", tint = Color(0xFFFF5252))
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(brush)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = user.ppurl,
                        contentDescription = "Profile picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .clickable { viewImage = true }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.username.orEmpty(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    if (user.isVerified) {
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
                    text = user.email,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
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
                        InfoRow(icon = Icons.Default.Cake, text = user.age ?: stringResource(R.string.na))
                        InfoRow(icon = Icons.Default.Person, text = user.gender ?: stringResource(R.string.na))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (!user.bio.isNullOrEmpty()) user.bio else stringResource(R.string.sin_biografia),
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (!user.bio.isNullOrEmpty()) Color.White else Color.Gray,
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
                            if (!user.instagram.isNullOrBlank()) {
                                IconButton(onClick = { 
                                    val link = user.instagram!!
                                    val url = if (link.startsWith("http")) link else "https://instagram.com/${link.removePrefix("@")}"
                                    uriHandler.openUri(url)
                                }) {
                                    Icon(imageVector = Icons.Default.Language, contentDescription = "Instagram", tint = Color(0xFF238CDD))
                                }
                            }
                            if (!user.facebook.isNullOrBlank()) {
                                IconButton(onClick = { 
                                    val link = user.facebook!!
                                    val url = if (link.startsWith("http")) link else "https://facebook.com/$link"
                                    uriHandler.openUri(url)
                                }) {
                                    Icon(imageVector = Icons.Default.Facebook, contentDescription = "Facebook", tint = Color(0xFF238CDD))
                                }
                            }
                            if (!user.telegram.isNullOrBlank()) {
                                IconButton(onClick = { 
                                    val link = user.telegram!!
                                    val url = if (link.startsWith("http")) link else "https://t.me/${link.removePrefix("@")}"
                                    uriHandler.openUri(url)
                                }) {
                                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Telegram", tint = Color(0xFF238CDD))
                                }
                            }
                            if (!user.otherLink.isNullOrBlank()) {
                                IconButton(onClick = { 
                                    val link = user.otherLink!!
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
                AdBanner()
            }
        }
    }
    AnimatedVisibility(viewImage) {
        View(imageUrl = user.ppurl, hideDialog = { viewImage = false })
    }
    if (showReportDialog) {
        com.appinitdev.chatok.Dialogs.ReportDialog(
            reportedUsername = user.username ?: stringResource(R.string.usuario),
            onDismiss = { showReportDialog = false },
            onSubmit = { reason, details ->
                viewModel.submitReport(user.userId, user.username ?: navController.context.getString(R.string.usuario), reason, details)
                showReportDialog = false
                android.widget.Toast.makeText(navController.context, navController.context.getString(R.string.gracias_reporte), android.widget.Toast.LENGTH_LONG).show()
            }
        )
    }
}
