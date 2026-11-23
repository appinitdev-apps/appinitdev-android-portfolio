@Composable
fun ChatScreen(
    navController: NavController,
    viewModel: ChatViewModel,
    state: AppState,
    showSingleChat: (ChatUserData, String) -> Unit,
) {
    SolicitarPermisoNotificaciones()
    val isLoading by viewModel.isLoading.collectAsState()
    var showUserDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<UserData?>(null) }
    val notLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(key1 = true) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    val myStoryVm = viewModel.stories.filter { it.userId == state.userData?.userId }
    val myStory = if (myStoryVm.isEmpty()) listOf(Story()) else myStoryVm
    val stories = viewModel.stories.filter { it.userId != state.userData?.userId }
    val isSearchingRandom by viewModel.isSearchingRandom.collectAsState()
    var showLoading by remember { mutableStateOf(false) }
    val chats = viewModel.chats
    val solicitudesRecibidas = viewModel.solicitudesRecibidas
    val solicitudesEnviadas = viewModel.solicitudesEnviadas
    var searchText by rememberSaveable { mutableStateOf("") }
    val filteredChats = if (searchText.isBlank()) chats else {
        chats.filter {
            val otherUser = if (it.user1?.userId == state.userData?.userId) it.user2 else it.user1
            otherUser?.username.orEmpty().contains(searchText, ignoreCase = true)
        }
    }
    val filteredSolicitudesRecibidas = if (searchText.isBlank()) solicitudesRecibidas else {
        solicitudesRecibidas.filter {
            val otherUser = if (it.user1?.userId == state.userData?.userId) it.user2 else it.user1
            otherUser?.username.orEmpty().contains(searchText, ignoreCase = true)
        }
    }
    val filteredSolicitudesEnviadas = if (searchText.isBlank()) solicitudesEnviadas else {
        solicitudesEnviadas.filter {
            val otherUser = if (it.user1?.userId == state.userData?.userId) it.user2 else it.user1
            otherUser?.username.orEmpty().contains(searchText, ignoreCase = true)
        }
    }
    var selectionMode by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    val selectedItem = remember { mutableStateListOf<String>() }
    var curStory by remember { mutableStateOf(Story()) }
    var showStory by remember { mutableStateOf(false) }
    var imgUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
        imgUri = it
    }
    val brush = Brush.linearGradient(listOf(Color(0xFF238CDD), Color(0xFF1952C4)))
    val storyBrush = Brush.sweepGradient(listOf(Color(0xFF78BEDA), Color(0xFF2458DB), Color(0xFFFF71FF), Color(0xFF8B2ECF), Color(0xFF3F3FD3), Color(0xFF78BEDA)))
    var showSearch by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedScreen by remember { mutableStateOf("chats") }
    val comp by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.upload))
    val searchComp by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.animation1))
    LaunchedEffect(state.userData?.userId) {
        if (state.userData?.userId != null) {
            viewModel.observeNotifications()
        }
    }
    val shouldHandleBack = drawerState.isOpen || showDialog || showStory || showSearch || selectionMode || imgUri != null
    val context = LocalContext.current
    BackHandler(enabled = true) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else if (showDialog || showStory || showSearch || selectionMode || imgUri != null) {
            showDialog = false
            showStory = false
            showSearch = false
            searchText = ""
            selectedItem.clear()
            selectionMode = false
            imgUri = null
        } else {
            (context as? android.app.Activity)?.finish()
        }
    }
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF1A1C1E),
                drawerShape = RoundedCornerShape(topEnd = 32.dp, bottomEnd = 32.dp),
                modifier = Modifier.fillMaxHeight().width(300.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate("profile")
                                scope.launch { drawerState.close() }
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = state.userData?.ppurl,
                            contentDescription = null,
                            modifier = Modifier.size(90.dp).clip(CircleShape).border(2.dp, brush, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = state.userData?.username ?: stringResource(R.string.usuario),
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = state.userData?.email ?: "email",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(Modifier.height(32.dp))
                    val chatsCount = viewModel.chats.size
                    val recibidasCount = viewModel.solicitudesRecibidas.size
                    val enviadasCount = viewModel.solicitudesEnviadas.size
                    val notificationsCount = viewModel.notifications.size
                    DrawerItem(
                        icon = Icons.Rounded.Chat, 
                        label = stringResource(R.string.chats), 
                        badgeCount = chatsCount,
                        isSelected = selectedScreen == "chats"
                    ) { 
                        selectedScreen = "chats"
                        searchText = ""
                        scope.launch { drawerState.close() }
                    }
                    Text(
                        text = stringResource(R.string.solicitudes),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                    )
                    DrawerItem(
                        icon = Icons.AutoMirrored.Rounded.CallReceived, 
                        label = stringResource(R.string.recibidas), 
                        badgeCount = recibidasCount,
                        isSelected = selectedScreen == "solicitudes_recibidas"
                    ) { 
                        selectedScreen = "solicitudes_recibidas"
                        searchText = ""
                        scope.launch { drawerState.close() }
                    }
                    DrawerItem(
                        icon = Icons.AutoMirrored.Rounded.CallMade,
                        label = stringResource(R.string.enviadas), 
                        badgeCount = enviadasCount,
                        isSelected = selectedScreen == "solicitudes_enviadas"
                    ) { 
                        selectedScreen = "solicitudes_enviadas"
                        searchText = ""
                        scope.launch { drawerState.close() }
                    }
                    Spacer(Modifier.height(16.dp))
                    DrawerItem(
                        icon = Icons.Rounded.Notifications, 
                        label = stringResource(R.string.notificaciones), 
                        badgeCount = notificationsCount,
                        isSelected = selectedScreen == "notifications"
                    ) { 
                        selectedScreen = "notifications"
                        searchText = ""
                        scope.launch { drawerState.close() }
                    }
                    DrawerItem(
                        icon = Icons.Rounded.Settings, 
                        label = stringResource(R.string.configuracion), 
                        badgeCount = 0,
                        isSelected = false
                    ) { 
                        navController.navigate("settings")
                        scope.launch { drawerState.close() }
                    }
                }
            }
        }
    ) {
        if (viewModel.boostDialog) {
            BoostDialog(
                balance = state.userData?.balance ?: 0,
                onDismiss = { viewModel.boostDialog = false },
                onBoost = { cost ->
                    viewModel.boostProfile(cost)
                    viewModel.boostDialog = false
                    Toast.makeText(context, context.getString(R.string.perfil_impulsado), Toast.LENGTH_SHORT).show()
                }
            )
        }
        Scaffold(
            floatingActionButton = {
                if (selectedScreen == "chats" || selectedScreen == "solicitudes_recibidas" || selectedScreen == "solicitudes_enviadas") {
                    MainFAB(
                        icon = Icons.Rounded.AddComment,
                        onClick = { viewModel.showDialog() }
                    )
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.blck_blurry),
                    contentDescription = "",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))
                Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    Header(
                        state = state,
                        viewModel = viewModel,
                        selectionMode = selectionMode,
                        selectedCount = selectedItem.size,
                        showSearch = showSearch,
                        searchText = searchText,
                        onSearchToggle = { showSearch = it },
                        onSearchTextChange = { searchText = it },
                        onClearSelection = {
                            selectionMode = false
                            selectedItem.clear()
                        },
                        onDeleteSelection = {
                            showDialog = true
                        },
                        onMenuToggle = { expanded = it },
                        expanded = expanded,
                        navController = navController,
                        onOpenDrawer = { scope.launch { drawerState.open() } }
                    )
                    when (selectedScreen) {
                        "chats" -> {
                            if (isLoading) {
                                SkeletonChatList()
                            } else {
                                ChatListContent(
                                    showSearch = showSearch,
                                    isUploading = isUploading,
                                    myStory = myStory,
                                    stories = stories,
                                    state = state,
                                    onStoryClick = { story -> curStory = story; showStory = true },
                                    onAddStory = { launcher.launch("image/*") },
                                    viewModel = viewModel,
                                    uploadComp = comp,
                                    storyBrush = storyBrush,
                                    showLoading = { showLoading = true },
                                    filteredChats = filteredChats,
                                    selectedItem = selectedItem,
                                    selectionMode = selectionMode,
                                    onChatLongClick = { selectionMode = true; selectedItem.add(it) },
                                    onChatClick = { chatUser, chatId ->
                                        if (selectionMode) {
                                            if (selectedItem.contains(chatId)) {
                                                selectedItem.remove(chatId)
                                                if (selectedItem.isEmpty()) selectionMode = false
                                            } else {
                                                selectedItem.add(chatId)
                                            }
                                        } else {
                                            showSingleChat(chatUser, chatId)
                                        }
                                    }
                                )
                            }
                        }
                        "solicitudes_recibidas" -> {
                            SolicitudesContent(
                                title = stringResource(R.string.recibidas),
                                currentList = filteredSolicitudesRecibidas,
                                emptyMessage = stringResource(R.string.no_recibidas),
                                state = state,
                                showSingleChat = showSingleChat
                            )
                        }
                        "solicitudes_enviadas" -> {
                            SolicitudesContent(
                                title = stringResource(R.string.enviadas),
                                currentList = filteredSolicitudesEnviadas,
                                emptyMessage = stringResource(R.string.no_enviadas),
                                state = state,
                                showSingleChat = showSingleChat
                            )
                        }
                        "notifications" -> {
                            NotificationsContent(
                                viewModel = viewModel
                            )
                        }
                    }
                    if (state.userData?.isVip != true) {
                        AdBanner(modifier = Modifier.padding(vertical = 4.dp), isVip = false)
                    }
                }
            }
            if (showDialog) {
                DeleteDialog(
                    hideDialog = {
                        showDialog = false
                        selectedItem.clear()
                        selectionMode = false
                    },
                    deleteChat = {
                        viewModel.deleteChat(selectedItem.toList())
                        showDialog = false
                        selectedItem.clear()
                        selectionMode = false
                    }
                )
            }
            if (showStory) {
                StoryDialog(
                    state,
                    viewModel,
                    story = curStory,
                    hideDialog = { showStory = false },
                    deleteStory = { viewModel.deleteStory(curStory, it) }
                )
            }
            if (state.showDialog) {
                CustomDialogBox(
                    state = state,
                    hideDialog = { viewModel.hideDialog() },
                    addChat = {
                        viewModel.addChat(state.srEmail)
                        viewModel.hideDialog()
                        viewModel.setSrEmail("")
                    },
                    setEmail = { viewModel.setSrEmail(it) }
                )
            }
            if (showLoading || isSearchingRandom) {
                Dialog(
                    onDismissRequest = {},
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .padding(16.dp),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C1E)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            LottieAnimation(
                                composition = searchComp,
                                iterations = LottieConstants.IterateForever,
                                modifier = Modifier.size(140.dp)
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = stringResource(id = R.string.buscando_alguien_especial),
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                LaunchedEffect(key1 = showLoading) {
                    if (showLoading) {
                        delay(2000)
                        showLoading = false
                        viewModel.addChatWithRandomUser { user ->
                            selectedUser = user
                            showUserDialog = user != null
                        }
                    }
                }
            }
            if (showUserDialog && selectedUser != null) {
                UserPopup(
                    user = selectedUser ?: UserData(),
                    onAccept = {
                        selectedUser?.let { viewModel.createChat(it) }
                        showUserDialog = false
                    },
                    onChatRequest = {},
                    onDismiss = {
                        selectedUser?.let { 
                            try { viewModel.markUserAsRejected(it.email) } catch (e: Exception) {}
                        }
                        showUserDialog = false
                        selectedUser = null
                    },
                    state = state,
                    viewModel = viewModel
                )
            }
        imgUri?.let { uri ->
            StoryPreview(
                uri = uri, 
                isSeller = state.userData?.pref?.verificada ?: false,
                hideDialog = { imgUri = null }, 
                upload = { cUri, locked, price ->
                    isUploading = true
                    viewModel.uploadMedia(uri = cUri ?: uri, folder = "photos") { url ->
                        val myUser = state.userData ?: return@uploadMedia
                        viewModel.uploadStory(url, myStory[0].id, locked, price)
                        isUploading = false
                    }
                    imgUri = null
                }
            )
        }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Header(
    state: AppState,
    viewModel: ChatViewModel,
    selectionMode: Boolean,
    selectedCount: Int,
    showSearch: Boolean,
    searchText: String,
    onSearchToggle: (Boolean) -> Unit,
    onSearchTextChange: (String) -> Unit,
    onClearSelection: () -> Unit,
    onDeleteSelection: () -> Unit,
    onMenuToggle: (Boolean) -> Unit,
    expanded: Boolean,
    navController: NavController,
    onOpenDrawer: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
        androidx.compose.animation.AnimatedContent(
            targetState = selectionMode to showSearch,
            transitionSpec = {
                slideInVertically { it } + fadeIn() togetherWith slideOutVertically { -it } + fadeOut()
            }
        ) { (isSelection, isSearch) ->
            when {
                isSelection -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onClearSelection) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                        }
                        Text(text = selectedCount.toString(), style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = onDeleteSelection) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                        }
                    }
                }
                isSearch -> {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = onSearchTextChange,
                        modifier = Modifier.fillMaxWidth(0.9f),
                        placeholder = { Text(stringResource(R.string.search1), color = Color.Gray) },
                        shape = CircleShape,
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { onSearchToggle(false) }) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray)
                            }
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            containerColor = Color.White.copy(alpha = 0.1f),
                            focusedBorderColor = Color(0xFF238CDD),
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
                else -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onOpenDrawer) {
                            Icon(Icons.Rounded.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = stringResource(R.string.hola), style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    imageVector = if (state.isOffline) Icons.Rounded.WifiOff else Icons.Rounded.Wifi,
                                    contentDescription = null,
                                    tint = if (state.isOffline) Color(0xFFFF5252) else Color(0xFF4CAF50),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(text = state.userData?.username ?: stringResource(R.string.usuario), style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.ExtraBold)
                        }
                        IconButton(onClick = { onSearchToggle(true) }, modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = Color.White)
                        }
                        Spacer(Modifier.width(8.dp))
                        Box {
                            IconButton(onClick = { onMenuToggle(true) }, modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)) {
                                Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.White)
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { onMenuToggle(false) },
                                modifier = Modifier.background(Color(0xFF1A1C1E))
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.perfil1), color = Color.White) },
                                    onClick = { navController.navigate("profile"); onMenuToggle(false) }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.galeria_media), color = Color.White) },
                                    onClick = { navController.navigate("gallery"); onMenuToggle(false) }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.impulsar_perfil_emoji), color = Color(0xFF238CDD)) },
                                    onClick = { viewModel.boostDialog = true; onMenuToggle(false) }
                                )
                                if (DEBUG) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.galeria), color = Color.White) },
                                        onClick = { navController.navigate("gallery"); onMenuToggle(false) }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.configuraciones), color = Color.White) },
                                    onClick = { navController.navigate("settings"); onMenuToggle(false) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun DrawerItem(
    icon: ImageVector, 
    label: String, 
    badgeCount: Int, 
    isSelected: Boolean, 
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color.White.copy(alpha = 0.1f) else Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon, 
                contentDescription = null, 
                tint = if (isSelected) Color(0xFF238CDD) else Color.White
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = label, 
                color = if (isSelected) Color(0xFF238CDD) else Color.White, 
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            if (badgeCount > 0) {
                Surface(
                    color = if (isSelected) Color(0xFF238CDD) else Color(0xFF2A2D31),
                    shape = CircleShape
                ) {
                    Text(
                        text = badgeCount.toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
@Composable
fun ChatListContent(
    showSearch: Boolean,
    isUploading: Boolean,
    myStory: List<Story>,
    stories: List<Story>,
    state: AppState,
    onStoryClick: (Story) -> Unit,
    onAddStory: () -> Unit,
    viewModel: ChatViewModel,
    uploadComp: LottieComposition?,
    storyBrush: Brush,
    showLoading: () -> Unit,
    filteredChats: List<ChatData>,
    selectedItem: List<String>,
    selectionMode: Boolean,
    onChatLongClick: (String) -> Unit,
    onChatClick: (ChatUserData, String) -> Unit
) {
    Column {
        AnimatedVisibility(visible = !showSearch) {
            StoriesRow(
                isUploading = isUploading,
                myStory = myStory,
                stories = stories,
                state = state,
                onStoryClick = onStoryClick,
                onAddStory = onAddStory,
                viewModel = viewModel,
                uploadComp = uploadComp,
                storyBrush = storyBrush
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            MainGradientButton(
                text = stringResource(R.string.usuario_random),
                onClick = showLoading,
                icon = Icons.Rounded.Autorenew
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C1E).copy(alpha = 0.8f))
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Text(
                        text = stringResource(R.string.chats),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(24.dp)
                    )
                }
                if (filteredChats.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.no_chats), color = Color.Gray)
                        }
                    }
                }
                items(filteredChats, key = { it.chatId }) { chat: ChatData ->
                    val chatUser: ChatUserData = if (chat.user1?.userId != state.userData?.userId) {
                        chat.user1 ?: ChatUserData(username = "Unknown")
                    } else {
                        chat.user2 ?: ChatUserData(username = "Unknown")
                    }
                    ChatItem(
                        state = state,
                        userData = chatUser,
                        chat = chat,
                        isSelected = selectedItem.contains(chat.chatId),
                        mode = selectionMode,
                        onLongClick = { onChatLongClick(chat.chatId) },
                        onClick = { onChatClick(chatUser, chat.chatId) }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}
@Composable
fun SolicitudesContent(
    title: String,
    currentList: List<ChatData>,
    emptyMessage: String,
    state: AppState,
    showSingleChat: (ChatUserData, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C1E).copy(alpha = 0.8f))
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(24.dp)
                )
            }
            if (currentList.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Text(emptyMessage, color = Color.Gray, textAlign = TextAlign.Center)
                    }
                }
            }
            items(currentList, key = { it.chatId }) { chat: ChatData ->
                val chatUser: ChatUserData = if (chat.user1?.userId != state.userData?.userId) {
                    chat.user1 ?: ChatUserData(username = "Unknown")
                } else {
                    chat.user2 ?: ChatUserData(username = "Unknown")
                }
                ChatItem(
                    state = state,
                    userData = chatUser,
                    chat = chat,
                    isSelected = false,
                    mode = false,
                    onLongClick = { },
                    onClick = { showSingleChat(chatUser, chat.chatId) }
                )
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
@Composable
fun NotificationsContent(viewModel: ChatViewModel) {
    val notifications = viewModel.notifications
    val formatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C1E).copy(alpha = 0.8f))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.notificaciones),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                if (notifications.isNotEmpty()) {
                    TextButton(onClick = { viewModel.clearNotifications() }) {
                        Text(stringResource(R.string.limpiar), color = Color(0xFF238CDD))
                    }
                }
            }
            if (notifications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_notificaciones), color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(notifications) { notification: NotificationEntity ->
                        NotificationItem(notification, formatter)
                    }
                }
            }
        }
    }
}
@Composable
fun NotificationItem(notification: NotificationEntity, formatter: SimpleDateFormat) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(48.dp).background(Color.White.copy(alpha = 0.05f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Notifications, contentDescription = null, tint = Color(0xFF238CDD))
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = notification.from, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Text(text = notification.message, style = MaterialTheme.typography.bodyMedium, color = Color.Gray, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(8.dp))
        Text(text = formatter.format(Date(notification.time)), style = MaterialTheme.typography.labelSmall, color = Color.DarkGray)
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatItem(
    state: AppState,
    userData: ChatUserData,
    chat: ChatData,
    isSelected: Boolean,
    mode: Boolean,
    onLongClick: () -> Unit,
    onClick: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val isUnread = (if (chat.user1?.userId == state.userData?.userId) chat.user1?.unread ?: 0 else chat.user2?.unread ?: 0) > 0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) Color.White.copy(alpha = 0.1f) else Color.Transparent)
            .combinedClickable(onLongClick = onLongClick, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            AsyncImage(
                model = userData.ppurl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(60.dp).clip(CircleShape).border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
            )
            if (userData.status) {
                Box(modifier = Modifier.size(14.dp).background(Color(0xFF4CAF50), CircleShape).border(2.dp, Color(0xFF1A1C1E), CircleShape))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = userData.username.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (userData.isVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Rounded.Verified,
                        contentDescription = "Verified",
                        tint = Color(0xFF238CDD),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = chat.last?.time?.let { formatter.format(it.toDate()) }.orEmpty(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isUnread) Color(0xFF238CDD) else Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (userData.typing) {
                    Text(text = stringResource(R.string.escribiendo2), color = Color(0xFF238CDD), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                } else {
                    if (chat.last?.senderId == state.userData?.userId) {
                        Icon(
                            painter = painterResource(id = R.drawable.check_mark),
                            contentDescription = null,
                            tint = if (chat.last?.read == true) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(14.dp).padding(end = 4.dp)
                        )
                    }
                    val lastText = when {
                        chat.last?.isDeleted == true -> stringResource(R.string.mensaje_borrado)
                        chat.last?.imgUrl?.isNotEmpty() == true -> "📷 ${stringResource(R.string.foto_preview)}"
                        chat.last?.vidUrl?.isNotEmpty() == true -> "🎥 ${stringResource(R.string.video_preview)}"
                        chat.last?.fileUrl?.isNotEmpty() == true -> "📄 ${stringResource(R.string.archivo_preview)}"
                        chat.last?.content?.isNotEmpty() == true -> chat.last.content
                        else -> "👋 ${stringResource(R.string.hola)}" 
                    }
                    Text(
                        text = lastText,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (isUnread) {
                    val count = if (chat.user1?.userId == state.userData?.userId) chat.user1?.unread ?: 0 else chat.user2?.unread ?: 0
                    Box(modifier = Modifier.size(20.dp).background(Color(0xFF238CDD), CircleShape), contentAlignment = Alignment.Center) {
                        Text(text = count.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
@Composable
fun StoriesRow(
    isUploading: Boolean,
    myStory: List<Story>,
    stories: List<Story>,
    state: AppState,
    onStoryClick: (Story) -> Unit,
    onAddStory: () -> Unit,
    viewModel: ChatViewModel,
    uploadComp: LottieComposition?,
    storyBrush: Brush
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            if (isUploading) {
                Box(modifier = Modifier.size(70.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                    LottieAnimation(composition = uploadComp, iterations = LottieConstants.IterateForever, modifier = Modifier.size(40.dp))
                }
            } else {
                val hasMyStory = myStory[0].userId.isNotEmpty()
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .then(
                                if (hasMyStory) Modifier.background(storyBrush) 
                                else Modifier.background(Color.White.copy(alpha = 0.1f))
                            )
                            .clickable { if (hasMyStory) onStoryClick(myStory[0]) else onAddStory() }
                            .padding(if (hasMyStory) 3.dp else 0.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (hasMyStory) {
                            AsyncImage(
                                model = myStory[0].ppurl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                        if (hasMyStory) {
                            Box(
                                modifier = Modifier.align(Alignment.BottomEnd).size(22.dp).background(Color(0xFF238CDD), CircleShape).border(2.dp, Color.Black, CircleShape).clickable { onAddStory() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                    Text(text = if (hasMyStory) stringResource(R.string.mis_historias) else stringResource(R.string.añadir), style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
        items(stories) { story ->
            val isViewed = story.images.all { it.viewedBy.any { viewer -> viewer.userId == state.userData?.userId } }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .then(
                            if (isViewed) Modifier.background(Color.Gray) 
                            else Modifier.background(storyBrush)
                        )
                        .clickable { 
                            onStoryClick(story)
                            if (!story.images[0].viewedBy.any { it.userId == state.userData?.userId }) {
                                viewModel.viewStory(story.id, 0)
                            }
                        }
                        .padding(3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = story.ppurl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                Text(text = story.username ?: "", style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.padding(top = 4.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
