
@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageItem(search: String,
                viewModel: ChatViewModel, // Añadido
                searchIndx: Int,
                message: Message,
                state: AppState,
                reaction:(String)->Unit,
                viewImage:(Message)->Unit,
                selectionMode:(String)->Unit,
                mode: Boolean,
                Selected:(String)->Unit,
                isSelected: Boolean,
                prevId: String,
                nextId: String,
                editMessage: (String, String) -> Unit,
                reactionPicker: () -> Unit,
                removeReaction: () -> Unit,
                replyMessage: (Message) -> Unit,
                onReplyClick:(String)->Unit,
                cindex: Int,
                index: Int,
                playVideo:(Message)->Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val privacyPrefs = remember { context.getSharedPreferences("privacy_prefs", Context.MODE_PRIVATE) }
    val isAutoTranslateEnabled = remember { privacyPrefs.getBoolean("auto_translate", false) }
    
    var translatedText by remember { mutableStateOf<String?>(null) }
    var isTranslating by remember { mutableStateOf(false) }
    var linkMetadata by remember { mutableStateOf<LinkMetadata?>(null) }

    // Auto Translate logic
    LaunchedEffect(message.content, isAutoTranslateEnabled) {
        if (isAutoTranslateEnabled && !message.isDeleted && message.content != null && message.senderId != state.userData?.userId) {
            isTranslating = true
            translatedText = TranslationManager.autoTranslate(message.content!!)
            isTranslating = false
        }
    }

    // Link Preview logic
    LaunchedEffect(message.content) {
        if (!message.isDeleted && message.content != null) {
            val url = LinkPreviewUtils.containsUrl(message.content)
            if (url != null) {
                linkMetadata = LinkPreviewUtils.extractMetadata(url)
            }
        }
    }

    val centerX = LocalConfiguration.current.screenWidthDp.dp / 2
    val centerY = 64.dp / 2
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val indication = rememberRipple(
        bounded = true,
        color = Color(0xFFFFFFFF)
    )
    val density = LocalDensity.current
    LaunchedEffect(key1 = cindex) {
        scope.launch(Dispatchers.IO) {
            if (cindex == index) {
                with(density) {
                    delay(100)
                    interactionSource.emit(PressInteraction.Press(Offset(centerX.toPx(), centerY.toPx())))
                    delay(400)
                    interactionSource.emit(PressInteraction.Release(PressInteraction.Press(Offset(centerX.toPx(), centerY.toPx()))))
                }
            }
        }
    }
    var released by remember {
        mutableStateOf(false)
    }
    var offset by remember { mutableStateOf(0.dp) }
    
    // Haptic when drag reaches reply threshold
    LaunchedEffect(offset) {
        if (offset > 70.dp && !released) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val aoffset by animateDpAsState(targetValue = if (released) 0.dp else offset)
    val size by animateDpAsState(targetValue = if (offset > 70.dp) 30.dp else 20.dp)
    val brush = Brush.linearGradient(
        listOf(
            Color(0xFF238CDD),
            Color(0xFF1952C4)
        )
    )
    val brush2 = Brush.linearGradient(
        listOf(
            Color(0xFF2A4783),
            Color(0xFF2F6086)
        )
    )
    val isCurrentUser = state.userData?.userId == message.senderId
    val shape =  if (isCurrentUser) {
        if (prevId == message.senderId && nextId == message.senderId) {
            RoundedCornerShape(16.dp, 3.dp, 3.dp, 16.dp)
        } else if (prevId == message.senderId) {
            RoundedCornerShape(16.dp, 16.dp, 3.dp, 16.dp)
        } else if (nextId == message.senderId) {
            RoundedCornerShape(16.dp, 3.dp, 16.dp, 16.dp)
        } else {
            RoundedCornerShape(16.dp, 16.dp, 16.dp, 16.dp)
        }
    } else {
        if (prevId == message.senderId && nextId == message.senderId) {
            RoundedCornerShape(3.dp, 16.dp, 16.dp, 3.dp)
        } else if (prevId == message.senderId) {
            RoundedCornerShape(16.dp, 16.dp, 16.dp, 3.dp)
        } else if (nextId == message.senderId) {
            RoundedCornerShape(3.dp, 16.dp, 16.dp, 16.dp)
        } else {
            RoundedCornerShape(16.dp, 16.dp, 16.dp, 16.dp)
        }
    }
    val color = if (isCurrentUser) brush else  brush2
    val alignment = if (isCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    val formatter = remember {
        SimpleDateFormat(("hh:mm a"), Locale.getDefault())
    }
    var onClick by remember {
        mutableStateOf(false)
    }
    
    // Floating reactions bar state
    var showFloatingReactions by remember { mutableStateOf(false) }

    var showReactions by remember {
        mutableStateOf(false)
    }
    val clkcolor = if(isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else if(searchIndx == index) Color.White.copy(alpha = 0.4f) else Color.Transparent
    Box(modifier = Modifier
        .indication(interactionSource, indication)
        .pointerInput(message) {
            scope.launch(Dispatchers.IO) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        released = false
                    },
                    onDragEnd = {
                        if (offset > 70.dp && !message.isDeleted) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            replyMessage(message)
                        }
                        released = true
                        offset = 0.dp
                    }
                ) { _, dragAmount ->
                    if (offset >= 0.dp && !message.isDeleted)
                        offset += (dragAmount * 0.35f).toDp()
                }
            }
        }
        .background(clkcolor)
        .combinedClickable(enabled = !message.isDeleted,
            onLongClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                if (!mode && !message.isDeleted) {
                    showFloatingReactions = true
                }
            }, onClick = { 
                if (mode) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    Selected(message.msgId)
                } else if (!message.isDeleted) {
                    if (showFloatingReactions) showFloatingReactions = false
                    else onClick = !onClick 
                }
            })
        .fillMaxWidth()
        .padding(
            top = 3.dp,
            bottom = 0.dp,
            start = 10.dp,
            end = 10.dp
        ),
        contentAlignment = alignment
    ) {
        if (showFloatingReactions) {
            Popup(
                alignment = if (isCurrentUser) Alignment.TopEnd else Alignment.TopStart,
                offset = IntOffset(0, -100),
                onDismissRequest = { showFloatingReactions = false },
                properties = PopupProperties(focusable = true)
            ) {
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .shadow(8.dp, RoundedCornerShape(30.dp)),
                    color = Color(0xFF1A1C1E),
                    shape = RoundedCornerShape(30.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("😂", "👍", "❤️", "😭", "😮", "🔥").forEach { emoji ->
                            Text(
                                text = emoji,
                                fontSize = 24.sp,
                                modifier = Modifier
                                    .clickable {
                                        reaction(emoji)
                                        showFloatingReactions = false
                                    }
                                    .animateContentSize()
                            )
                        }
                        IconButton(
                            onClick = {
                                reactionPicker()
                                showFloatingReactions = false
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = null, tint = Color.Gray)
                        }
                        
                        VerticalDivider(modifier = Modifier.height(20.dp), color = Color.White.copy(alpha = 0.1f))
                        
                        IconButton(
                            onClick = { 
                                selectionMode(message.msgId)
                                showFloatingReactions = false
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            }
        }

        Icon(modifier = Modifier
            .size(size)
            .align(Alignment.CenterStart)
            .offset(aoffset - 50.dp),
            tint = if(offset>70.dp) Color.White else Color.Gray,
            imageVector = Icons.AutoMirrored.Rounded.Reply, contentDescription = null)
        Column(
            verticalArrangement = Arrangement.Bottom) {
            Column(
                modifier = Modifier
                    .offset(aoffset)
                    .shadow(2.dp, shape)
                    .widthIn(max = 270.dp)
                    .fillMaxHeight()
                    .background(color, shape), horizontalAlignment = Alignment.End
            ) {
                if(message.forwarded && !message.isDeleted) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier
                            .padding(horizontal = 10.dp)) {
                        Icon(modifier = Modifier.size(15.dp),
                            painter = painterResource(id = R.drawable.pngwing_com),
                            contentDescription = null,
                            tint = Color.LightGray
                        )
                        Text(
                            text = stringResource(id = R.string.forwarded),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = state.userData?.pref?.fontSize.toString()
                                    .toFloat().sp.times(0.8)
                            ),
                            color = Color.LightGray,
                        )
                    }
                }
                if(!message.isDeleted && message.repliedMsg != null && (message.repliedMsg.imgUrl.isNotEmpty() || (message.repliedMsg.content?.isNotEmpty() ?: false) || message.repliedMsg.vidUrl.isNotEmpty() || message.repliedMsg.fileUrl.isNotEmpty())) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(top = 4.dp, start = 4.dp, end = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onReplyClick(message.repliedMsg.msgId) }
                            .background(Color.Black.copy(alpha = 0.4f)),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        if (message.repliedMsg.imgUrl.isNotEmpty()) {
                            AsyncImage(
                                model = message.repliedMsg.imgUrl,
                                contentDescription = "Profile picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(60.dp)
                                    .aspectRatio(1f / 1f)
                                    .padding(5.dp)
                                    .clip(
                                        RoundedCornerShape(7.dp)
                                    )
                            )
                        }
                        if(message.repliedMsg.vidUrl.isNotEmpty()) {
                            Image(painter = painterResource(id = R.drawable.play),
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(40.dp),
                                contentDescription = null
                            )
                        }
                        if(message.repliedMsg.fileUrl.isNotEmpty()) {
                            Icon(imageVector = Icons.Rounded.InsertDriveFile,
                                modifier = Modifier
                                    .padding(top = 5.dp, start = 5.dp, bottom = 5.dp)
                                    .size(45.dp),
                                contentDescription = null
                            )
                        }
                        Column {
                            Text(
                                text = if(message.repliedMsg.senderId==state.userData?.userId) state.userData.username.toString() else state.User2?.username.toString(),
                                modifier = Modifier
                                    .padding(horizontal = 6.dp)
                                    .padding(top = 4.dp),
                                fontSize = state.userData?.pref?.fontSize.toString().toFloat().sp.times(0.85),
                                color = Color(0xFF3CAAFF),
                                fontWeight = FontWeight.Black
                            )
                            if (message.repliedMsg.content?.isNotEmpty() == true) {
                                Text(
                                    text = message.repliedMsg.content.toString(),
                                    modifier = Modifier
                                        .padding(horizontal = 6.dp)
                                        .padding(bottom = 4.dp),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = state.userData?.pref?.fontSize.toString()
                                            .toFloat().sp.times(0.8)
                                    ),
                                    color = Color.White,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            else if(message.repliedMsg.fileUrl.isNotEmpty()) {
                                Text(
                                    text = message.repliedMsg.fileName,
                                    modifier = Modifier
                                        .padding(horizontal = 6.dp)
                                        .padding(bottom = 4.dp),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = state.userData?.pref?.fontSize.toString()
                                            .toFloat().sp.times(0.8)
                                    ),
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                if(!message.isDeleted && message.fileUrl!="" && message.fileUrl!="uploadingFile"){
                    Row(modifier = Modifier
                        .padding(4.dp, 4.dp, 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val uri = Uri.parse(message.fileUrl)
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        }
                        .background(Color.Black.copy(alpha = 0.4f))
                        .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(modifier = Modifier
                            .padding(5.dp)
                            .size(45.dp),
                            imageVector = Icons.Rounded.InsertDriveFile, contentDescription = null)
                        Column {
                            Text(modifier = Modifier.width(190.dp),
                                text = message.fileName,
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = state.userData?.pref?.fontSize.toString().toFloat().sp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = Color.White)
                            Text(text = message.fileSize)
                        }
                    }
                }
                if(!message.isDeleted && message.fileUrl=="uploadingFile" && isCurrentUser){
                    val comp by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.upload))
                    Row(modifier = Modifier
                        .padding(4.dp, 4.dp, 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                        .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column (modifier = Modifier
                            .padding(5.dp)
                            .size(45.dp)){
                            LottieAnimation(
                                composition = comp,
                                iterations = LottieConstants.IterateForever
                            )
                        }
                        Column {
                            Text(modifier = Modifier.width(190.dp),
                                text = message.fileName,
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = state.userData?.pref?.fontSize.toString().toFloat().sp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = Color.White)
                            Text(modifier = Modifier
                                .background(Color.Gray.copy(alpha = 0.6f), CircleShape)
                                .padding(vertical = 4.dp, horizontal = 6.dp),
                                text = message.progress,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
                if(!message.isDeleted && message.fileUrl=="uploadingFile" && !isCurrentUser){
                    Row(modifier = Modifier
                        .padding(4.dp, 4.dp, 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                        .fillMaxWidth()
                        .height(50.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                            Text(text = stringResource(id = R.string.enviando_archivo),
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = state.userData?.pref?.fontSize.toString().toFloat().sp),
                                color = Color.White)
                    }
                }
                if(!message.isDeleted && message.audioUrl.isNotEmpty()){
                    Row(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.4f))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { 
                            val uri = Uri.parse(message.audioUrl)
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        }) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color.White)
                        }
                        Text(text = stringResource(R.string.nota_de_voz), color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                if(!message.isDeleted && message.vidUrl!= "" && message.vidUrl!="uploadingVideo") {
                    Box(contentAlignment = Alignment.Center) {
                        AsyncImage (
                            model = ImageRequest.Builder(context)
                                .data(message.vidUrl)
                                .videoFrameMillis(0)
                                .build(),
                            contentDescription = "First frame of video",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .aspectRatio(16f / 9f)
                                .padding(4.dp, 4.dp, 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    playVideo(message)
                                }
                        )
                        Image(
                            painter = painterResource(id = R.drawable.play),
                            contentDescription = null,
                            modifier = Modifier.clickable { playVideo(message) }
                        )
                    }
                }
                if(!message.isDeleted && message.vidUrl=="uploadingVideo" &&  isCurrentUser){
                    val comp by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.upload))
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(4.dp)) {
                        if (message.imgUri.isNotEmpty()) {
                            AsyncImage(
                                model = message.imgUri, // Using local thumbnail if available
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .aspectRatio(16f / 9f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .blur(10.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .aspectRatio(16f / 9f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.DarkGray)
                            )
                        }
                        Column(
                            modifier = Modifier
                                .matchParentSize()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.4f)),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LottieAnimation(
                                modifier = Modifier.size(70.dp),
                                composition = comp,
                                iterations = LottieConstants.IterateForever
                            )
                            Text(
                                text = if (message.progress.isNotEmpty()) message.progress else "0%",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    .padding(vertical = 4.dp, horizontal = 10.dp)
                            )
                        }
                    }
                }
                if(!message.isDeleted && message.vidUrl=="uploadingVideo" && !isCurrentUser){
                    Box(contentAlignment = Alignment.Center) {
                        Column(
                            modifier = Modifier
                                .aspectRatio(16f / 9f)
                                .padding(4.dp, 4.dp, 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .blur(5.dp)
                                .background(Color.DarkGray.copy(alpha = 0.3f))
                                .size(60.dp)
                        ) {

                        }
                        Text(text = stringResource(id = R.string.enviando_video),
                            modifier = Modifier.padding(top = if(message.forwarded && message.imgUrl=="") 0.dp else 5.dp, start = 10.dp, end = 10.dp),
                            style = MaterialTheme.typography.titleMedium.copy(fontSize =
                            state.userData?.pref?.fontSize.toString().toFloat().sp
                            ),
                            color = Color.White
                        )
                    }
                }
                if(!message.isDeleted && message.imgUrl!="" && message.imgUrl!="uploadingImage"){
                    val isViewed = message.isEphemeral && message.senderId != state.userData?.userId && message.viewedAt != null
                    val isLocked = message.isEphemeral && message.senderId != state.userData?.userId && message.viewedAt == null
                    val senderIsVerified = if (isCurrentUser) state.userData?.pref?.isVerified == true else state.User2?.isVerified == true
                    val senderUsername = if (isCurrentUser) state.userData?.username else state.User2?.username
                    
                    val imageModel = remember(message.imgUrl, message.imgUri, message.msgId) {
                        val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "ChatOk")
                        val localJpg = File(directory, "IMG_${message.msgId}.jpg")
                        val localGif = File(directory, "IMG_${message.msgId}.gif")
                        when {
                            localJpg.exists() -> localJpg
                            localGif.exists() -> localGif
                            message.imgUri.isNotEmpty() && (message.imgUri.startsWith("file:") || message.imgUri.startsWith("content:")) -> {
                                try {
                                    val uri = Uri.parse(message.imgUri)
                                    if (uri.scheme == "file") {
                                        val file = File(uri.path ?: "")
                                        if (file.exists()) uri else message.imgUrl
                                    } else uri
                                } catch (e: Exception) { message.imgUrl }
                            }
                            else -> message.imgUrl
                        }
                    }

                    if (isViewed) {
                        // Show "Opened" status for viewed ephemeral messages
                        Row(
                            modifier = Modifier
                                .padding(8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.3f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.Visibility, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.foto_abierta), color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                        }
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            AsyncImage(
                                model = imageModel,
                                contentDescription = "Profile picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .clickable { if (!isLocked) viewImage(message) }
                                    .aspectRatio(16f / 9f)
                                    .padding(4.dp, 4.dp, 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .then(if (isLocked) Modifier.blur(20.dp) else Modifier)
                            )
                            
                            if (senderIsVerified) {
                                Text(
                                    text = stringResource(R.string.watermark_format, senderUsername ?: ""),
                                    color = Color.White.copy(alpha = 0.5f),
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                                )
                            }
                            
                            if (isLocked) {
                                Icon(Icons.Rounded.Visibility, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                            }
                        }
                    }
                }
                if(!message.isDeleted && message.imgUrl=="uploadingImage" && isCurrentUser){
                    val comp by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.upload))
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(4.dp)) {
                        if (message.imgUri.isNotEmpty()) {
                            AsyncImage(
                                model = message.imgUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .aspectRatio(16f / 9f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .blur(10.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .aspectRatio(16f / 9f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.DarkGray)
                            )
                        }
                        Column(
                            modifier = Modifier
                                .matchParentSize()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.4f)),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LottieAnimation(
                                modifier = Modifier.size(70.dp),
                                composition = comp,
                                iterations = LottieConstants.IterateForever
                            )
                            Text(
                                text = if (message.progress.isNotEmpty()) message.progress else "0%",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    .padding(vertical = 4.dp, horizontal = 10.dp)
                            )
                        }
                    }
                }
                if(!message.isDeleted && message.imgUrl=="uploadingImage" && !isCurrentUser){
                    Box(contentAlignment = Alignment.Center) {
                        Column(
                            modifier = Modifier
                                .aspectRatio(16f / 9f)
                                .padding(4.dp, 4.dp, 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .blur(5.dp)
                                .background(Color.DarkGray.copy(alpha = 0.3f))
                                .size(60.dp)
                        ) {

                        }
                        Text(text = stringResource(id = R.string.enviando_imagen),
                            modifier = Modifier.padding(top = if(message.forwarded && message.imgUrl=="") 0.dp else 5.dp, start = 10.dp, end = 10.dp),
                            style = MaterialTheme.typography.titleMedium.copy(fontSize =
                                state.userData?.pref?.fontSize.toString().toFloat().sp
                            ),
                            color = Color.White
                        )
                    }
                }
                if(message.isDeleted) {
                    Text(text = stringResource(R.string.mensaje_borrado),
                        modifier = Modifier.padding(top = 5.dp, start = 10.dp, end = 10.dp),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = state.userData?.pref?.fontSize.toString().toFloat().sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        ),
                        color = Color.LightGray.copy(alpha = 0.7f)
                    )
                } else if(message.content!="") {
                    if(search.isNotEmpty()) {
                    Text(text = buildAnnotatedString {
                                var start = 0
                                while (message.content.toString().indexOf(
                                        search,
                                        start,
                                        ignoreCase = true
                                    ) != -1 && search.isNotBlank()
                                ) {
                                    val firstIndex = message.content.toString().indexOf(search, start, true)
                                    val end = firstIndex + search.length
                                    append(message.content.toString().substring(start, firstIndex))
                                    withStyle(
                                        style = SpanStyle(
                                            color = Color.DarkGray,
                                            fontWeight = FontWeight.SemiBold,
                                            background = Color.White
                                        )
                                    ) {
                                        append(
                                            message.content.toString().substring(firstIndex, end)
                                        )
                                    }
                                    start = end
                                }
                                append(
                                    message.content.toString()
                                        .substring(start, message.content.toString().length)
                                )
                                toAnnotatedString()
                            },
                        modifier = Modifier.padding(top = if(message.forwarded && message.imgUrl=="" ) 0.dp else 5.dp, start = 10.dp, end = 10.dp),
                        style = MaterialTheme.typography.titleMedium.copy(fontSize =
                        if(Regex("[\\p{So}\\p{Sk}]").matches(message.content.toString())){
                            state.userData?.pref?.fontSize.toString().toFloat().sp.times(3)
                        }
                        else
                            state.userData?.pref?.fontSize.toString().toFloat().sp
                        ),
                        color = Color.White
                    )}
                    else{
                        Text(text = translatedText ?: message.content.toString(),
                            modifier = Modifier.padding(top = if(message.forwarded && message.imgUrl=="" ) 0.dp else 5.dp, start = 10.dp, end = 10.dp),
                            style = MaterialTheme.typography.titleMedium.copy(fontSize =
                            if(Regex("[\\p{So}\\p{Sk}]").matches(message.content.toString())){
                                state.userData?.pref?.fontSize.toString().toFloat().sp.times(3)
                            }
                            else
                                state.userData?.pref?.fontSize.toString().toFloat().sp
                            ),
                            color = Color.White
                        )
                    }
                }
                
                if (linkMetadata != null && !message.isDeleted) {
                    Column(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.3f))
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(linkMetadata!!.url))
                                context.startActivity(intent)
                            }
                    ) {
                        if (linkMetadata!!.imageUrl != null) {
                            AsyncImage(
                                model = linkMetadata!!.imageUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = linkMetadata!!.title ?: "",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Bold
                            )
                            if (linkMetadata!!.description != null) {
                                Text(
                                    text = linkMetadata!!.description!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.LightGray,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

    if (translatedText != null) {
        Text(
            text = stringResource(R.string.traducido),
            style = MaterialTheme.typography.labelSmall,
            color = Color.Cyan.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 10.dp)
        )
    }

    if (message.content?.startsWith("🎁") == true) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.animation3))
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier
                .padding(8.dp)
                .size(100.dp)
        )
    }

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier.padding(end = 8.dp, bottom = if(message.reaction.isNotEmpty()) 10.dp else 5.dp, start = 8.dp, top = 2.dp)) {
        val time = message.time?.toDate()//adrian if de mas
        Text(
            text = if (time != null) {
                formatter.format(time)
            } else {
                ""  // o algún texto alternativo como "Sin hora"
            },
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
        )
        if(isCurrentUser) {
            val checkColor by animateColorAsState(
                targetValue = if(message.read) Color(0xFF13C70D) else Color.White,
                animationSpec = tween(durationMillis = 500)
            )
            Icon(modifier = Modifier.size(10.dp),
                painter = painterResource(id = R.drawable.check_mark),
                contentDescription = null,
                tint = checkColor
            )
        }
    }
            }
            if(message.reaction.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .offset(aoffset)
                        .clickable {
                            showReactions = true
                        }
                        .graphicsLayer(
                            translationY = (-20).dp.value,
                            clip = false
                        )
                        .background(color, CircleShape)
                        .border(
                            BorderStroke(1.5.dp, MaterialTheme.colorScheme.background),
                            shape = CircleShape
                        )
                        .padding(4.dp)
                ) {
                    items(message.reaction){
                        Text(text = it.reaction,  modifier = Modifier.padding(horizontal = 2.dp))
                    }
                }
            }
        }
        Column{
            MaterialTheme(shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(18.dp)), colorScheme = MaterialTheme.colorScheme.copy(background = Color(
                0xFF274F6F)
            )) {
                DropdownMenu( offset = DpOffset(50.dp, 30.dp),
                    expanded = showReactions,
                    onDismissRequest = { showReactions = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.background)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .width(200.dp)) {
                        message.reaction.forEach {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = it.ppurl,
                                    contentDescription = "Profile picture",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .clip(CircleShape)
                                        .size(30.dp)

                                )
                                Text(
                                    text = it.username,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(modifier = Modifier.clickable {
                                    if ( it.userId==state.userData?.userId ) removeReaction(); showReactions=false
                                },
                                    text = it.reaction,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        }
                    }
                }
            }
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
                                modifier = Modifier.clickable { reaction(it)
                                    onClick = false
                                }
                            )
                        }
                        Icon(
                            modifier = Modifier
                                .size(35.dp)
                                .clickable {
                                    reactionPicker()
                                    onClick = false
                                },
                            imageVector = Icons.Filled.AddCircle,
                            contentDescription = null)
                    }
                    DropdownMenuItem(
                        text = { Text(text = stringResource(id = R.string.copiar), style = MaterialTheme.typography.bodyLarge) },
                        onClick = { val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clipData: ClipData = ClipData.newPlainText("text", message.content)
                            clipboardManager.setPrimaryClip(clipData)
                            Toast.makeText(context, context.getString(R.string.mensaje_copiado), Toast.LENGTH_SHORT).show()
                            onClick = false
                        }
                    )
                    if (!message.isDeleted && !message.isEphemeral && (message.imgUrl.isNotEmpty() || message.vidUrl.isNotEmpty() || message.fileUrl.isNotEmpty()) && 
                        !message.imgUrl.startsWith("uploading") && !message.vidUrl.startsWith("uploading") && !message.fileUrl.startsWith("uploading")) {
                        DropdownMenuItem(
                            text = { Text(text = stringResource(R.string.guardar), style = MaterialTheme.typography.bodyLarge) },
                            onClick = {
                                when {
                                    message.imgUrl.isNotEmpty() -> {
                                        val ext = if (message.imgUrl.contains(".gif", ignoreCase = true)) "gif" else "jpg"
                                        viewModel.downloadMedia(context, message.imgUrl, "IMG_${message.msgId}.$ext")
                                    }
                                    message.vidUrl.isNotEmpty() -> viewModel.downloadMedia(context, message.vidUrl, "VID_${message.msgId}.mp4")
                                    message.fileUrl.isNotEmpty() -> viewModel.downloadMedia(context, message.fileUrl, message.fileName)
                                }
                                onClick = false
                            }
                        )
                    }
                    if (message.content.toString().isNotEmpty() && !message.isDeleted) {
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = stringResource(R.string.traducir), style = MaterialTheme.typography.bodyLarge)
                                    if (isTranslating) {
                                        Spacer(Modifier.width(8.dp))
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                                    }
                                }
                            },
                            onClick = {
                                scope.launch {
                                    isTranslating = true
                                    translatedText = TranslationManager.autoTranslate(message.content.toString())
                                    isTranslating = false
                                    onClick = false
                                }
                            }
                        )
                    }
                    if(isCurrentUser){
                        DropdownMenuItem(
                            text = { Text(text = stringResource(R.string.editar_mensaje_titulo), style = MaterialTheme.typography.bodyLarge) },
                            onClick = {
                                editMessage(message.msgId, message.content.toString())
                                onClick = false
                            }
                        )
                    }
                }
            }
        }
    }
}
