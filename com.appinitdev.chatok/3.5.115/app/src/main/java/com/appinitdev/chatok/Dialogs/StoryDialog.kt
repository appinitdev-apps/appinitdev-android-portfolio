@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StoryDialog(appState: AppState,
                viewModel: ChatViewModel,
                story: Story,
                hideDialog:()->Unit,
                deleteStory:(Int)->Unit
){
    var reply by rememberSaveable {
        mutableStateOf("")
    }
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val formatter = remember {
        SimpleDateFormat(("hh:mm a"), Locale.getDefault())
    }
    var dialog by remember {
        mutableStateOf(false)
    }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pagerState = rememberPagerState(
        pageCount = { story.images.size }, initialPage = 0
    )
    val brush = Brush.linearGradient(listOf(
        Color(0xFF238CDD),
        Color(0xFF1952C4)
    ))
    var onClick by remember {
        mutableStateOf(false)
    }
    var isModalBottomSheetVisible by remember {
        mutableStateOf(false)
    }
    Dialog(onDismissRequest = hideDialog,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Column(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f))) {
        }
        if (isModalBottomSheetVisible) {
            ModalBottomSheet(
                sheetState = sheetState,
                shape = RectangleShape,
                tonalElevation = 0.dp,
                onDismissRequest = {
                    isModalBottomSheetVisible = false
                },
                dragHandle = null,
                windowInsets = WindowInsets(0),
            ) {
                CustomEmojiPicker(
                    onEmojiSelected = { emoji ->
                        isModalBottomSheetVisible = false
                        viewModel.storyReaction(story.id, pagerState.currentPage, reaction = emoji, story.userId)
                    }
                )
            }
        }
        if(showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                },
                sheetState = sheetState
            ) {
                LazyColumn(modifier = Modifier
                    .padding(start = 16.dp, bottom = 16.dp)) {
                    item{
                        Text(modifier = Modifier
                            .padding(bottom = 16.dp),
                            text = stringResource(id = R.string.viewers),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(story.images[pagerState.currentPage].viewedBy) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(0.95f)
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = it.ppurl,
                                contentDescription = "Profile picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .size(40.dp)
                            )
                            Column(modifier = Modifier.padding(start = 16.dp)) {
                                Text(
                                    text = it.username,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = formatter.format(it.time?.toDate()!!),
                                    color = Color.LightGray,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Light)
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = it.reaction,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
            HorizontalPager(state = pagerState) { index ->
                val image = story.images[index]
                val isOwnStory = story.userId == appState.userData?.userId
                val isStoryUnlocked = appState.userData?.unlockedStories?.contains(story.id) ?: false
                val isUnlocked = index == 0 || isOwnStory || isStoryUnlocked
                val unlockCost = 15 
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = image.imgUrl,
                        contentDescription = "Story image",
                        modifier = Modifier
                            .clickable(interactionSource = interactionSource, indication = null) {}
                            .fillMaxSize()
                            .then(if (!isUnlocked) Modifier.blur(25.dp) else Modifier)
                    )
                    if (!isUnlocked) {
                        Column(
                            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(16.dp))
                            Text(stringResource(R.string.contenido_vip), color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text(stringResource(R.string.para_ver_todas_fotos), color = Color.LightGray, style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(24.dp))
                            Button(
                                onClick = { viewModel.unlockStory(story.id, unlockCost) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color.Black)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.desbloquear_todas_por, unlockCost), color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            AnimatedVisibility(visible = !isPressed, enter = fadeIn(), exit = fadeOut()) {
                Column {
                    Row(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { hideDialog() }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBackIosNew,
                                contentDescription = null
                            )
                        }
                        AsyncImage(
                            model = story.ppurl,
                            contentDescription = "Profile picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .clip(CircleShape)
                                .size(40.dp)
                        )
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            Text(
                                text = if (story.userId == appState.userData?.userId) story.username.toString() + " (" + stringResource(R.string.yo) + ")" else story.username.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = formatter.format(story.images[pagerState.currentPage].time?.toDate()!!),
                                color = Color.LightGray,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Light)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        if (story.userId == appState.userData?.userId) {
                            IconButton(onClick = { dialog = true }) {
                                Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
                            }
                        }else{
                            IconButton(onClick = { onClick = true }) {
                                Icon(imageVector = Icons.Filled.AddReaction, contentDescription = null)
                            }
                            Column{
                                MaterialTheme(shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(18.dp)), colorScheme = MaterialTheme.colorScheme.copy(background = Color(
                                    0xFF294F86
                                )
                                ) ) {
                                    DropdownMenu( offset = DpOffset(50.dp, 30.dp),
                                        expanded = onClick,
                                        onDismissRequest = { onClick = false },
                                        modifier = Modifier.background(MaterialTheme.colorScheme.background)
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(horizontal = 10.dp)) {
                                            listOf("😂", "👍", "❤️", "😭", "😮").forEach {
                                                Text(
                                                    text = it,
                                                    style = MaterialTheme.typography.headlineMedium,
                                                    modifier = Modifier.clickable { viewModel.storyReaction(id = story.id, index = pagerState.currentPage, reaction = it, story.userId)
                                                        onClick = false
                                                    }
                                                )
                                            }
                                            Icon(
                                                modifier = Modifier
                                                    .size(35.dp)
                                                    .clickable {
                                                        isModalBottomSheetVisible=true
                                                        onClick = false
                                                    },
                                                imageVector = Icons.Filled.AddCircle,
                                                contentDescription = null)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                           repeat(story.images.size) {
                               val color = if(it == pagerState.currentPage) Color(0xFF0F70AF) else Color.LightGray
                               Box(modifier = Modifier
                                   .padding(3.dp)
                                   .height(5.dp)
                                   .weight(1f)
                                   .background(color, CircleShape)) {
                               }
                           }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (story.userId != appState.userData?.userId){
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                                .background(MaterialTheme.colorScheme.onPrimary, CircleShape)) {
                            TextField(modifier = Modifier.weight(1f), placeholder = { Text(text = stringResource(R.string.responde_historia)) },
                                value = reply,
                                onValueChange = { reply=it },
                                colors = TextFieldDefaults.colors(
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                            )
                            AnimatedVisibility(visible = reply.isNotBlank()) {
                                IconButton(modifier = Modifier
                                    .padding(end = 4.dp)
                                    .background(brush, CircleShape) ,
                                    onClick = {
                                        Log.d("STORY_REPLY", "Iniciando respuesta para el usuario: ${story.userId}")
                                        viewModel.findChatIdPro(story.userId) { chatId ->
                                            Log.d("STORY_REPLY", "Resultado de findChatId: $chatId")
                                            if (chatId != "null" && chatId.isNotEmpty()) {
                                                Log.d("STORY_REPLY", "Chat existente encontrado. Enviando respuesta normal...")
                                                viewModel.sendReply(
                                                    chatId = chatId,
                                                    replyMsg = Message().copy(
                                                        imgUrl = story.images[pagerState.currentPage].imgUrl,
                                                        content = context.getString(R.string.respondio_a_la_historia)
                                                    ),
                                                    msg = reply
                                                )
                                            } else {
                                                Log.d("STORY_REPLY", "No se encontró chat previo. Creando nuevo chat con ${story.username}...")
                                                val storyAuthor = UserData(
                                                    userId = story.userId,
                                                    username = story.username,
                                                    ppurl = story.ppurl
                                                )
                                                val currentUserId = appState.userData?.userId ?: ""
                                                val replyMessage = Message().copy(
                                                    senderId = currentUserId,
                                                    imgUrl = story.images[pagerState.currentPage].imgUrl,
                                                    content = context.getString(R.string.respondio_a_la_historia), 
                                                )
                                                viewModel.createChat(storyAuthor)
                                                viewModel.createChatAndReply(storyAuthor, replyMessage, reply)
                                                Log.d("STORY_REPLY", "Llamada a createChat enviada. Re-buscando nuevo ID...")
                                                viewModel.findChatId(story.userId) { newChatId ->
                                                    Log.d("STORY_REPLY", "Segundo intento findChatId: $newChatId")
                                                    if (newChatId != "null" && newChatId.isNotEmpty()) {
                                                        Log.d("STORY_REPLY", "Nuevo chat creado con éxito. Enviando mensaje...")
                                                        viewModel.sendReply(
                                                            chatId = newChatId,
                                                            replyMsg = Message().copy(
                                                                imgUrl = story.images[pagerState.currentPage].imgUrl,
                                                                content = context.getString(R.string.respondio_a_la_historia)
                                                            ),
                                                            msg = reply
                                                        )
                                                    } else {
                                                        Log.e("STORY_REPLY", "ERROR: No se pudo obtener el ID del nuevo chat. Revisa las reglas de Firestore.")
                                                    }
                                                }
                                            }
                                            viewModel.sendNotification(context.getString(R.string.respondio_a_la_historia), story.userId)
                                            reply = ""
                                            Toast.makeText(context, context.getString(R.string.mensaje_enviado_a, story.username ?: ""), Toast.LENGTH_SHORT).show()
                                        }
                                    }) {
                                    Icon(imageVector = Icons.Filled.Send, contentDescription = null)
                                }
                            }
                        }
                    }
                    if (story.userId == appState.userData?.userId) {
                        Row(
                            modifier = Modifier
                                .padding(bottom = 20.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                Text(text = story.images[pagerState.currentPage].viewedBy.size.toString())
                                IconButton(onClick = { showBottomSheet = true }) {
                                    Icon(imageVector = Icons.Filled.RemoveRedEye, contentDescription = null)
                                }
                            }
                        }
                    }
                }
            }
    }
    AnimatedVisibility(visible = dialog) {
        DeleteStoryDialog(hideDialog = { dialog = false }, deleteStory = { deleteStory(pagerState.currentPage)
            dialog = false
        hideDialog()})
    }
    LaunchedEffect(pagerState.currentPage) {
        if(story.images[pagerState.currentPage].viewedBy.all { it.userId != appState.userData?.userId } && story.userId != appState.userData?.userId){
            viewModel.viewStory(story.id, pagerState.currentPage)
        }
    }
}
