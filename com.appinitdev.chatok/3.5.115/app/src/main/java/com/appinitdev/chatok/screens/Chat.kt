

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Chat(navController: NavController,
         viewModel: ChatViewModel,
         userData: ChatUserData,
         chatId: String,
         messages: List<Message>,
         state: AppState,
         onBack:() -> Unit,
         schedule:(LocalDateTime, String, String, String) -> Unit,
         context: Context = LocalContext.current
) {
    var showReportDialog by remember { mutableStateOf(false) }
    val file = File(context.filesDir, "temp.png")
    val uri = FileProvider.getUriForFile(
        context,
        "com.appinitdev.chatok.fileProvider",
        file
    )
    val isLoading by viewModel.isLoading.collectAsState()
    
    val lottieSearchComp by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.animation1))

    val tState = rememberTimePickerState(is24Hour = false)
    val dState = rememberDatePickerState(yearRange = (2024 .. 2026))
    val cal = Calendar.getInstance()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
        viewModel.imgUri = it
    }
    val vLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
        viewModel.vidUri = it
    }
    val fLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
        viewModel.fileUri = it
    }
    val cLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture()) {
        viewModel.imgUri=null
        if(it)
            viewModel.imgUri = uri
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){
        if(!it) {
            Toast.makeText(context, context.getString(R.string.permiso_denegado), Toast.LENGTH_SHORT).show()
        }
        else{
            cLauncher.launch(uri)
        }
    }
    val tp = viewModel.tp
    val selectedItem = remember {
        mutableStateListOf<String>()
    }
    var indx by remember { mutableStateOf(-1) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true,)
    val msgs = if (viewModel.searchText1.isNotEmpty()) {
            messages.filter { it.content.toString().contains(viewModel.searchText1, ignoreCase = true) }
        } else {
            emptyList()
        }
    var srchIndx by remember { mutableStateOf(-1) }
    val fileName = viewModel.fileUri?.let { DocumentFile.fromSingleUri(context, it)?.name } ?: ""
    val size = viewModel.fileUri?.let { String.format(Locale.getDefault(), "%.2f", DocumentFile.fromSingleUri(context, it)?.length()?.toFloat()?.div(1024f * 1024f) ?: 0f) } ?: ""
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = viewModel.reply){
        if(viewModel.reply.isNotEmpty()) {
            viewModel.typing(true, chatId = chatId, userId = state.userData?.userId.toString())
        } else {
            viewModel.typing(false, chatId = chatId, userId = state.userData?.userId.toString())
        }
    }

    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    LaunchedEffect(key1 = messages) {
        if(chatId!=""){
            viewModel.readAllMessagesInChat(chatId)
        }
    }
    
    LaunchedEffect(key1 = chatId) {
        if (chatId.isNotEmpty()) {
            viewModel.popMessage(chatId)
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && lastVisibleIndex >= viewModel.messages.size - 5) {
                    viewModel.loadMoreMessages(chatId)
                }
            }
    }

    BackHandler {
        if (viewModel.searchText1.isNotBlank() || 
            srchIndx != -1 || 
            viewModel.showSearch || 
            viewModel.selectionMode || 
            viewModel.showDialog || 
            viewModel.clearChatDialog || 
            viewModel.editMsgId.isNotBlank() || 
            viewModel.isModalBottomSheetVisible || 
            viewModel.replyMessage.msgId.isNotEmpty() || 
            viewModel.forwardMsgs.isNotEmpty() || 
            viewModel.msg.msgId.isNotEmpty() ||
            viewModel.imgUri != null ||
            viewModel.vidUri != null ||
            viewModel.showOptions ||
            viewModel.datePicker ||
            viewModel.timePicker ||
            showReportDialog
        ) {
            srchIndx = -1
            viewModel.apply {
                showDialog = false
                clearChatDialog = false
                selectionMode = false
                editMsgId = ""
                isModalBottomSheetVisible = false
                replyMessage = Message()
                forwardMsgs.clear()
                reply = ""
                showSearch = false
                searchText1 = ""
                msg = Message()
                imgUri = null
                vidUri = null
                showOptions = false
                datePicker = false
                timePicker = false
            }
            showReportDialog = false
            selectedItem.clear()
            expanded = false
        } else {
            onBack()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(
                color = Color(0xFF1A1C1E).copy(alpha = 0.9f),
                shadowElevation = 8.dp
            ) {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    title = {
                        if (!viewModel.selectionMode && !viewModel.showSearch) {
                            Row(
                                modifier = Modifier
                                    .clickable { navController.navigate("otherprofile") }
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = userData.ppurl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                                )
                                Column(modifier = Modifier.padding(start = 12.dp)) {
                                    val currentUser2 = state.User2 ?: userData
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = currentUser2.username.orEmpty(),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        if (currentUser2.isVerified) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Rounded.Verified,
                                                contentDescription = "Verified",
                                                tint = Color(0xFF238CDD),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(Modifier.width(6.dp))
                                        Icon(
                                            imageVector = if (state.isOffline) Icons.Rounded.WifiOff else Icons.Rounded.Wifi,
                                            contentDescription = null,
                                            tint = if (state.isOffline) Color(0xFFFF5252).copy(alpha = 0.7f) else Color(0xFF4CAF50).copy(alpha = 0.7f),
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                    val otherUserTp = if (tp.user1?.userId == currentUser2.userId) tp.user1 else tp.user2
                                    val isTyping = otherUserTp?.typing ?: false
                                    if (isTyping) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = stringResource(R.string.escribiendo),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF238CDD)
                                            )
                                            TypingIndicator(Color(0xFF238CDD))
                                        }
                                    } else {
                                        val isOnline = currentUser2.status
                                        
                                        val statusText = if (isOnline) {
                                            stringResource(R.string.en_linea)
                                        } else {
                                            currentUser2.lastSeen?.let { lastSeenTime ->
                                                val lastSeenDate = lastSeenTime.toDate()
                                                val formattedTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(lastSeenDate)
                                                val isToday = android.text.format.DateUtils.isToday(lastSeenDate.time)
                                                
                                                if (isToday) {
                                                    "últ. vez hoy $formattedTime"
                                                } else {
                                                    val formattedDate = SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(lastSeenDate)
                                                    "últ. vez $formattedDate"
                                                }
                                            } ?: stringResource(R.string.desconectado)
                                        }
                                        Text(
                                            text = statusText,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isOnline) Color(0xFF4CAF50) else Color.Gray
                                        )
                                    }
                                }
                            }
                        } else if (viewModel.selectionMode) {
                            Text(text = "${selectedItem.size} " + stringResource(R.string.seleccionados), color = Color.White)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (viewModel.selectionMode) {
                                viewModel.selectionMode = false
                                selectedItem.clear()
                                viewModel.forwardMsgs.clear()
                            } else if (viewModel.showSearch) {
                                viewModel.showSearch = false
                                viewModel.searchText1 = ""
                                srchIndx = -1
                            } else if (viewModel.msg.msgId.isNotEmpty()) {
                                viewModel.msg = Message()
                            } else {
                                onBack()
                            }
                        }) {
                            Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        if (!viewModel.selectionMode && !viewModel.showSearch) {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Filled.MoreVert, contentDescription = null, tint = Color.White)
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(Color(0xFF1A1C1E))
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.perfil), color = Color.White) },
                                    onClick = { navController.navigate("otherprofile"); expanded = false }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.buscar), color = Color.White) },
                                    onClick = { viewModel.showSearch = true; expanded = false }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (viewModel.isBLockedByMe(userData.userId)) stringResource(R.string.desbloquear) else stringResource(R.string.bloquear), color = Color.White) },
                                    onClick = {
                                        if (viewModel.isBLockedByMe(userData.userId)) viewModel.unblockUser(userData.userId)
                                        else viewModel.blockUser(userData.userId)
                                        expanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.vaciar_chat), color = Color.White) },
                                    onClick = { viewModel.clearChatDialog = true; expanded = false }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.transferir_monedas), color = Color(0xFFFFD700)) },
                                    onClick = { viewModel.showTransferDialog = true; expanded = false }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.galeria_media), color = Color.White) },
                                    onClick = { navController.navigate("gallery"); expanded = false }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.reportar_usuario), color = Color(0xFFFF5252)) },
                                    onClick = { showReportDialog = true; expanded = false }
                                )
                            }
                        } else if (viewModel.selectionMode) {
                            IconButton(onClick = {
                                navController.navigate("forward")
                                viewModel.selectionMode = false
                            }) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White)
                            }
                            IconButton(onClick = { viewModel.showDialog = true }) {
                                Icon(Icons.Filled.Delete, contentDescription = null, tint = Color.White)
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            
            if (state.userData?.pref?.customImg.isNullOrBlank()) {
                Image(
                    painter = painterResource(R.drawable.blurry_gradient_haikei),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().alpha(state.userData?.pref?.back ?: 1f),
                    contentScale = ContentScale.Crop
                )
                Image(
                    painter = painterResource(R.drawable.social_media_doodle_seamless_pattern_vector_27700734),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().alpha(state.userData?.pref?.doodles ?: 0.1f),
                    contentScale = ContentScale.Crop
                )
            } else {
                AsyncImage(
                    model = state.userData?.pref?.customImg,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().alpha(state.userData?.pref?.back ?: 1f),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                
                val orderedMessages = remember(messages) {
                    messages.sortedWith(
                        compareBy<Message> { it.time == null }
                            .thenByDescending { it.time?.toDate()?.time ?: it.localTime }
                    )
                }

                if (!isLoading && orderedMessages.isEmpty() && state.chatId == chatId && state.chatId.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(R.string.rompe_el_hielo), color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                            Spacer(Modifier.height(24.dp))
                            listOf(
                                stringResource(R.string.hobby_favorito),
                                stringResource(R.string.musica_favorita),
                                stringResource(R.string.viaje_favorito)
                            ).forEach { question ->
                                IceBreakerButton(question) { viewModel.reply = question }
                            }
                        }
                    }
                }

                if (isLoading) {
                    SkeletonMessageList()
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f).fillMaxSize(),
                        reverseLayout = true,
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        itemsIndexed(orderedMessages, key = { _, msg -> msg.msgId }) { index, message ->
                            val prevMessage = orderedMessages.getOrNull(index - 1)
                            val nextMessage = orderedMessages.getOrNull(index + 1)
                            MessageItem(
                                search = viewModel.searchText1,
                                viewModel = viewModel,
                                searchIndx = srchIndx,
                                message = message,
                                state = state,
                                reaction = { viewModel.Reaction(it, chatId, message.msgId) },
                                viewImage = { viewModel.msg = it },
                                selectionMode = {
                                    viewModel.selectionMode = true
                                    selectedItem.add(it)
                                    viewModel.forwardMsgs.add(message)
                                },
                                mode = viewModel.selectionMode,
                                Selected = { id ->
                                    if (selectedItem.contains(id)) {
                                        selectedItem.remove(id)
                                        viewModel.forwardMsgs.removeIf { it.msgId == id }
                                        if (selectedItem.isEmpty()) viewModel.selectionMode = false
                                    } else {
                                        selectedItem.add(id)
                                        viewModel.forwardMsgs.add(message)
                                    }
                                },
                                isSelected = selectedItem.contains(message.msgId),
                                prevId = prevMessage?.senderId.orEmpty(),
                                nextId = nextMessage?.senderId.orEmpty(),
                                editMessage = { id, content ->
                                    viewModel.editMsgId = id
                                    viewModel.editMsgContent = content
                                    viewModel.reply = content
                                    focusRequester.requestFocus()
                                    keyboardController?.show()
                                },
                                reactionPicker = {
                                    viewModel.selectedEmoji = message.msgId
                                    viewModel.isModalBottomSheetVisible = true
                                },
                                removeReaction = { viewModel.removeReaction(chatId, message.msgId) },
                                replyMessage = { msg ->
                                    viewModel.replyMessage = msg
                                    focusRequester.requestFocus()
                                    keyboardController?.show()
                                },
                                onReplyClick = { id ->
                                    coroutineScope.launch {
                                        indx = orderedMessages.indexOfFirst { it.msgId == id }
                                        if (indx != -1) listState.animateScrollToItem(indx)
                                    }
                                },
                                cindex = indx,
                                index = index,
                                playVideo = { viewModel.msg = it }
                            )
                        }
                    }
                }

                if (state.userData?.isVip != true) {
                    AdBanner(modifier = Modifier.padding(vertical = 2.dp), isVip = false)
                }

                
                val isInitiator = tp.user1?.userId == state.userData?.userId
                val isRecipient = tp.user2?.userId == state.userData?.userId

                if (tp.accepted || isInitiator) {
                    ChatInputBar(
                        viewModel = viewModel,
                        chatId = chatId,
                        state = state,
                        focusRequester = focusRequester,
                        launcher = launcher,
                        vLauncher = vLauncher,
                        fLauncher = fLauncher,
                        cLauncher = cLauncher,
                        uri = uri,
                        context = context,
                        permissionLauncher = permissionLauncher,
                        fileName = fileName,
                        fileSize = size
                    )
                } else if (isRecipient) {
                    
                    AcceptChatPanel(
                        onAccept = { viewModel.acceptChat(chatId) },
                        onReject = { viewModel.deleteChat(listOf(chatId)); onBack() }
                    )
                }
            }
        }

        
        ChatOverlays(viewModel, chatId, userData, dState, tState, cal, schedule, state, context, selectedItem)

        if (viewModel.msg != Message()) {
            if (viewModel.msg.isEphemeral && viewModel.msg.senderId != state.userData?.userId && viewModel.msg.viewedAt != null) {
                
                viewModel.msg = Message()
            } else if (viewModel.msg.imgUrl.isNotEmpty()) {
                ImageViewer(
                    userData = userData,
                    hideDialog = { 
                        if (viewModel.msg.isEphemeral && viewModel.msg.senderId != state.userData?.userId) {
                            
                            viewModel.markMessageAsViewed(chatId, viewModel.msg.msgId)
                        }
                        viewModel.msg = Message() 
                    },
                    message = viewModel.msg
                )
            } else if (viewModel.msg.audioUrl.isNotEmpty()) {
                
                viewModel.msg = Message()
            } else if (viewModel.msg.vidUrl.isNotEmpty()) {
                
                viewModel.msg = Message()
            } else if (viewModel.msg.vidUrl.isNotEmpty()) {
                VideoPlayer(
                    userData = userData,
                    hideDialog = { viewModel.msg = Message() },
                    message = viewModel.msg
                )
            }
        }

        if (viewModel.imgUri != null) {
            ImagePreview(
                uri = viewModel.imgUri,
                hideDialog = { viewModel.imgUri = null },
                send = { uri, msg, duration ->
                    val finalUri = uri ?: viewModel.imgUri ?: Uri.EMPTY
                    val isEphemeral = duration != 0L
                    val id = viewModel.sendReply(
                        chatId = chatId, 
                        msg = msg, 
                        imgUrl = "uploadingImage", 
                        imgUri = finalUri.toString(),
                        isEphemeral = isEphemeral,
                        ephemeralDuration = if (duration == -1L) 0L else duration
                    )
                    viewModel.uploadMedia(uri = finalUri, folder = "photos", chatId = chatId, msgId = id) { url ->
                        viewModel.addUrl(chatId, id, imgUrl = url)
                    }
                    viewModel.imgUri = null
                }
            )
        }

        if (viewModel.vidUri != null) {
            VideoPreview(
                uri = viewModel.vidUri,
                hideDialog = { viewModel.vidUri = null },
                send = { msg ->
                    val id = viewModel.sendReply(
                        chatId = chatId, 
                        msg = msg, 
                        vidUrl = "uploadingVideo",
                        imgUri = viewModel.vidUri?.toString().orEmpty()
                    )
                    viewModel.uploadVideo(viewModel.vidUri!!, chatId, id) { url ->
                        viewModel.addUrl(chatId, id, vidUrl = url)
                    }
                    viewModel.vidUri = null
                }
            )
        }

        if (showReportDialog) {
            ReportDialog(
                reportedUsername = userData.username ?: stringResource(R.string.usuario),
                onDismiss = { showReportDialog = false },
                onSubmit = { reason, details ->
                    viewModel.submitReport(userData.userId, userData.username ?: context.getString(R.string.usuario), reason, details)
                    showReportDialog = false
                    Toast.makeText(context, context.getString(R.string.reporte_enviado), Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInputBar(
    viewModel: ChatViewModel,
    chatId: String,
    state: AppState,
    focusRequester: FocusRequester,
    launcher: androidx.activity.result.ActivityResultLauncher<String>,
    vLauncher: androidx.activity.result.ActivityResultLauncher<String>,
    fLauncher: androidx.activity.result.ActivityResultLauncher<String>,
    cLauncher: androidx.activity.result.ActivityResultLauncher<Uri>,
    uri: Uri,
    context: Context,
    permissionLauncher: androidx.activity.result.ActivityResultLauncher<String>,
    fileName: String,
    fileSize: String
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val voiceRecorder = remember { com.appinitdev.chatok.utils.VoiceRecorder(context) }
    var isRecording by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1C1E))
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .imePadding()
            .navigationBarsPadding()
    ) {
        
        AnimatedVisibility(viewModel.replyMessage.msgId.isNotEmpty()) {
            ReplyPreview(viewModel.replyMessage, state) {
                viewModel.replyMessage = Message()
            }
        }

        AnimatedVisibility(viewModel.editMsgId.isNotEmpty()) {
            EditPreview(viewModel.editMsgContent) {
                viewModel.editMsgId = ""
                viewModel.reply = ""
            }
        }

         





        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom 
        ) {
            
            if (isRecording) {
                SmallGradientIconButton(
                    icon = Icons.Rounded.Stop,
                    onClick = {
                        isRecording = false
                        val audioFile = voiceRecorder.stopRecording()
                        audioFile?.let { file ->
                            viewModel.uploadVoiceNote(file, chatId) { url ->
                                viewModel.sendReply(chatId, msg = "🎤 Mensaje de voz", audioUrl = url)
                            }
                        }
                    },
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            } else {
                SmallGradientIconButton(
                    icon = Icons.Rounded.CameraAlt,
                    onClick = {
                        val permissionCheckResult = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                        if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) cLauncher.launch(uri)
                        else permissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 2.dp),
                shape = RoundedCornerShape(28.dp),
                color = Color.White.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    
                    IconButton(onClick = { viewModel.isModalBottomSheetVisible = true }) {
                        Icon(
                            imageVector = Icons.Rounded.EmojiEmotions,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    if (isRecording) {
                        Text(
                            text = stringResource(R.string.grabando),
                            color = Color.Red,
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        TextField(
                            value = viewModel.reply,
                            onValueChange = { viewModel.reply = it },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester),
                            placeholder = { Text(stringResource(R.string.escribe_mensaje), color = Color.Gray, fontSize = 15.sp) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFF238CDD)
                            ),
                            maxLines = 5,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp)
                        )
                    }
                    
                    
                    if (!isRecording) {
                        Box {
                            IconButton(onClick = { viewModel.showOptions = !viewModel.showOptions }) {
                                Icon(
                                    imageVector = Icons.Rounded.AttachFile,
                                    contentDescription = null,
                                    tint = if (viewModel.showOptions) Color(0xFF238CDD) else Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            if (viewModel.showOptions) {
                                AttachmentMenu(viewModel, launcher, vLauncher, fLauncher)
                            }
                        }
                    }
                }
            }

            
            Spacer(modifier = Modifier.width(10.dp))
            val isTyping = (viewModel.reply.trim().isNotEmpty() || viewModel.fileUri != null) && !isRecording

            AnimatedContent(
                targetState = isTyping,
                transitionSpec = {
                    (scaleIn() + fadeIn()).togetherWith(scaleOut() + fadeOut())
                },
                label = "SendMicAnimation"
            ) { typing ->
                if (typing) {
                    SmallGradientIconButton(
                        icon = Icons.AutoMirrored.Rounded.Send,
                        onClick = {
                             





if (viewModel.editMsgId.isEmpty()) {
                                viewModel.sendReply(chatId, viewModel.reply.trim(), replyMsg = viewModel.replyMessage)
                                viewModel.reply = ""
                                viewModel.replyMessage = Message()
                            } else {
                                viewModel.editMessage(chatId, viewModel.editMsgId, viewModel.reply.trim())
                                viewModel.reply = ""
                                viewModel.editMsgId = ""
                                keyboardController?.hide()
                            }
                        },
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                } else if (!isRecording) {
                    SmallGradientIconButton(
                        icon = Icons.Rounded.Mic,
                        onClick = { 
                            val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                isRecording = true
                                voiceRecorder.startRecording()
                            } else {
                                
                            }
                        },
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.size(40.dp))
                }
            }
        }
    }
}

@Composable
fun IceBreakerButton(question: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color.White.copy(alpha = 0.08f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(0.85f)
    ) {
        Text(
            text = question,
            color = Color.White,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ReplyPreview(message: Message, state: AppState, onCancel: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.width(4.dp).height(40.dp).background(Color(0xFF238CDD), RoundedCornerShape(2.dp)))
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.respondiendo_a), style = MaterialTheme.typography.labelSmall, color = Color(0xFF238CDD))
                Text(
                    text = message.content.orEmpty().ifEmpty { stringResource(R.string.media) },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun EditPreview(content: String, onCancel: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF238CDD), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = content, style = MaterialTheme.typography.bodySmall, color = Color.LightGray, modifier = Modifier.weight(1f), maxLines = 1)
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun FilePreview(name: String, size: String, onCancel: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Filled.InsertDriveFile, contentDescription = null, tint = Color(0xFF238CDD))
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.bodySmall, color = Color.White, maxLines = 1)
                Text(stringResource(R.string.file_size_mb, size), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun AttachmentMenu(
    viewModel: ChatViewModel,
    launcher: androidx.activity.result.ActivityResultLauncher<String>,
    vLauncher: androidx.activity.result.ActivityResultLauncher<String>,
    fLauncher: androidx.activity.result.ActivityResultLauncher<String>
) {
    DropdownMenu(
        expanded = viewModel.showOptions,
        onDismissRequest = { viewModel.showOptions = false },
        modifier = Modifier.background(Color(0xFF1A1C1E))
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.imagen), color = Color.White) },
            leadingIcon = { Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFF238CDD)) },
            onClick = { launcher.launch("image/*"); viewModel.showOptions = false }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.video), color = Color.White) },
            leadingIcon = { Icon(Icons.Default.Videocam, contentDescription = null, tint = Color(0xFF238CDD)) },
            onClick = { vLauncher.launch("video/*"); viewModel.showOptions = false }
        )
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatOverlays(
    viewModel: ChatViewModel,
    chatId: String,
    userData: ChatUserData,
    dState: DatePickerState,
    tState: TimePickerState,
    cal: Calendar,
    schedule: (LocalDateTime, String, String, String) -> Unit,
    state: AppState,
    context: Context,
    selectedItem: MutableList<String>
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (viewModel.isModalBottomSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.isModalBottomSheetVisible = false },
            sheetState = sheetState,
            containerColor = Color(0xFF1A1C1E),
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
        ) {
            CustomEmojiPicker(
                onEmojiSelected = { emoji ->
                    viewModel.isModalBottomSheetVisible = false
                    viewModel.sendReply(chatId, msg = emoji)
                },
                onGifSelected = { gifUrl ->
                    viewModel.isModalBottomSheetVisible = false
                    viewModel.sendReply(chatId, msg = "", imgUrl = gifUrl)
                }
            )
        }
    }

    if (viewModel.showDialog) {
        MsgDeleteDialog(0, hideDialog = { viewModel.showDialog = false }, deleteMsg = { 
            viewModel.deleteMsg(selectedItem.toList(), chatId)
            viewModel.showDialog = false
            viewModel.selectionMode = false
            selectedItem.clear()
        })
    }

    if (viewModel.datePicker) {
        DatePickerDialog(
            onDismissRequest = { viewModel.datePicker = false },
            confirmButton = { TextButton(onClick = { viewModel.datePicker = false; viewModel.timePicker = true }) { Text(stringResource(R.string.siguiente)) } }
        ) { DatePicker(state = dState) }
    }

    if (viewModel.timePicker) {
        TimePickerDialog(
            onCancel = { viewModel.timePicker = false },
            onConfirm = {
                val date = LocalDateTime.ofInstant(Instant.ofEpochMilli(dState.selectedDateMillis!!), ZoneId.systemDefault())
                cal.set(date.year, date.monthValue - 1, date.dayOfMonth, tState.hour, tState.minute, 0)
                schedule(cal.time.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), viewModel.reply, chatId, state.userData?.userId.orEmpty())
                viewModel.timePicker = false
                viewModel.reply = ""
            }
        ) { TimePicker(state = tState) }
    }
    
    if (viewModel.clearChatDialog) {
        ClearChatDialog(hideDialog = { viewModel.clearChatDialog = false }, clearChat = { viewModel.clearChat(chatId); viewModel.clearChatDialog = false })
    }

    if (viewModel.boostDialog) {
        BoostDialog(
            balance = state.userData?.balance ?: 0,
            onDismiss = { viewModel.boostDialog = false },
            onBoost = { cost ->
                viewModel.boostProfile(cost)
                viewModel.boostDialog = false
                Toast.makeText(context, context.getString(R.string.perfil_impulsado_exito), Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (viewModel.showTransferDialog) {
        TransferCoinsDialog(
            balance = state.userData?.balance ?: 0,
            onDismiss = { viewModel.showTransferDialog = false },
            onTransfer = { amount ->
                viewModel.transferCoins(
                    toUserId = userData.userId,
                    amount = amount,
                    onSuccess = {
                        viewModel.showTransferDialog = false
                        Toast.makeText(context, context.getString(R.string.transferencia_exitosa), Toast.LENGTH_SHORT).show()
                        
                        viewModel.sendReply(chatId, context.getString(R.string.regalo_monedas, amount))
                    },
                    onError = { error ->
                        Toast.makeText(context, context.getString(R.string.error_transferencia, error), Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }

    if (viewModel.showTransferAnimation) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.animation3))
        Dialog(onDismissRequest = { viewModel.showTransferAnimation = false }) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(300.dp)) {
                LottieAnimation(
                    composition = composition,
                    iterations = 1,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    if (viewModel.msg.isEphemeral && viewModel.msg.senderId != state.userData?.userId && viewModel.msg.viewedAt == null && viewModel.msg.msgId.isNotEmpty()) {
        
        var remainingTime by remember { mutableLongStateOf(viewModel.msg.ephemeralDuration) }
        val isTimer = viewModel.msg.ephemeralDuration > 0
        var hasStarted by remember { mutableStateOf(false) }

        LaunchedEffect(hasStarted) {
            if (hasStarted && isTimer) {
                while (remainingTime > 0) {
                    delay(1000)
                    remainingTime -= 1000
                }
                viewModel.markMessageAsViewed(chatId, viewModel.msg.msgId)
                viewModel.msg = Message()
            }
        }

        Dialog(onDismissRequest = { 
            if (!hasStarted) viewModel.msg = Message() 
        }) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.9f))) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (!hasStarted) {
                        Icon(Icons.Rounded.Visibility, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = if (isTimer) stringResource(R.string.foto_temporal, viewModel.msg.ephemeralDuration / 1000) else stringResource(R.string.foto_un_uso),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(stringResource(R.string.foto_desaparecera), color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(32.dp))
                        MainGradientButton(
                            text = stringResource(R.string.ver_ahora),
                            onClick = { 
                                hasStarted = true
                                if (!isTimer) {
                                    
                                }
                            }
                        )
                        Spacer(Modifier.height(16.dp))
                        TextButton(onClick = { viewModel.msg = Message() }) {
                            Text(stringResource(R.string.cancelarw), color = Color.White)
                        }
                    } else {
                        
                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            AsyncImage(
                                model = viewModel.msg.imgUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                            
                            if (isTimer) {
                                Box(
                                    modifier = Modifier
                                        .padding(24.dp)
                                        .align(Alignment.TopEnd)
                                        .size(48.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(stringResource(R.string.seconds_format, remainingTime / 1000), color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        
                        MainGradientButton(
                            text = stringResource(R.string.cerrar),
                            onClick = { 
                                viewModel.markMessageAsViewed(chatId, viewModel.msg.msgId)
                                viewModel.msg = Message() 
                            },
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AcceptChatPanel(onAccept: () -> Unit, onReject: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.aceptar_solicitud_titulo), color = Color.White, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.aceptar_solicitud_desc), color = Color.Gray, textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SecondaryButton(text = stringResource(R.string.rechazar), onClick = onReject, modifier = Modifier.weight(1f), contentColor = Color(0xFFFF5252))
                MainGradientButton(text = stringResource(R.string.aceptar), onClick = onAccept, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun PendingRequestPanel() {
    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(stringResource(R.string.solicitud_pendiente), color = Color.Gray, textAlign = TextAlign.Center)
    }
}
