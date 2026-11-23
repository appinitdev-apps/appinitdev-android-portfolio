
@Composable
fun UserPopup(
    user: UserData,
    onAccept: () -> Unit,
    onChatRequest: () -> Unit,
    onDismiss: () -> Unit,
    state: AppState,
    viewModel: ChatViewModel
) {
    var showChatOption by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

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
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1C1E) // Dark elegant background
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.informaci_n_del_usuario),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                listOf(Color(0xFF238CDD), Color(0xFF1952C4))
                            )
                        )
                        .padding(3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val imageUrl = if (user.ppurl?.isNotBlank() == true) user.ppurl
                    else "https://appinitdev-android.web.app/user.png"

                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        error = painterResource(R.drawable.user)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.username ?: stringResource(R.string.usuario),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    if (user.pref.isVerified) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Rounded.Verified,
                            contentDescription = "Verified",
                            tint = Color(0xFF238CDD),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.id_formato, user.userId),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Info Rows
                InfoRow(icon = Icons.Default.Cake, text = user.age ?: stringResource(R.string.na))
                InfoRow(icon = Icons.Default.Person, text = user.gender ?: stringResource(R.string.na))
                
                Spacer(modifier = Modifier.height(12.dp))
                
                user.bio?.let {
                    if(user.bio.length>3) {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color.White.copy(alpha = 0.05f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!user.instagram.isNullOrBlank()) {
                        IconButton(onClick = { 
                            val link = user.instagram
                            val url = if (link.startsWith("http")) link else "https://instagram.com/${link.removePrefix("@")}"
                            uriHandler.openUri(url)
                        }) {
                            Icon(imageVector = Icons.Default.Language, contentDescription = stringResource(R.string.instagram), tint = Color.White)
                        }
                    }
                    if (!user.facebook.isNullOrBlank()) {
                        IconButton(onClick = { 
                            val link = user.facebook
                            val url = if (link.startsWith("http")) link else "https://facebook.com/$link"
                            uriHandler.openUri(url)
                        }) {
                            Icon(imageVector = Icons.Default.Facebook, contentDescription = stringResource(R.string.facebook), tint = Color.White)
                        }
                    }
                    if (!user.telegram.isNullOrBlank()) {
                        IconButton(onClick = { 
                            val link = user.telegram
                            val url = if (link.startsWith("http")) link else "https://t.me/${link.removePrefix("@")}"
                            uriHandler.openUri(url)
                        }) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(R.string.telegram), tint = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Spacer(modifier = Modifier.height(24.dp))

                if (showChatOption) {
                    val isSeller = user.pref.verificada
                    val isVerified = user.pref.isVerified
                    val needsUnlock = isSeller && isVerified && !(state.userData?.unlockedChats?.contains(user.userId) ?: false) && state.userData?.userId != user.userId
                    val unlockCost = 20 // Platform fixed price for chat unlock

                    if (needsUnlock) {
                        Text(
                            text = stringResource(R.string.perfil_verificado_desc),
                            color = Color(0xFFFFD700),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        MainGradientButton(
                            text = stringResource(R.string.desbloquear_por, unlockCost),
                            onClick = {
                                viewModel.unlockChatWithSeller(user.userId, unlockCost) {
                                    onChatRequest()
                                    onDismiss()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Default.MonetizationOn
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.quieres_chatear_con) + " ${user.username}?",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            MainGradientButton(
                                text = stringResource(R.string.s),
                                onClick = { onChatRequest(); onDismiss() },
                                modifier = Modifier.weight(1f)
                            )
                            SecondaryButton(
                                text = stringResource(R.string.no),
                                onClick = { onDismiss() },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SecondaryButton(
                            text = stringResource(R.string.cancelarw),
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        )
                        MainGradientButton(
                            text = stringResource(R.string.aceptar),
                            onClick = {
                                onAccept()
                                showChatOption = true
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF238CDD),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
    }
}
