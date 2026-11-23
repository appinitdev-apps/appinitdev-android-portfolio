@androidx.media3.common.util.UnstableApi
class ChatViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    @androidx.media3.common.util.UnstableApi
    private val chatDao = BaseApplication.database.chatDao()
    private val repository = ChatRepository(firestore, chatDao, viewModelScope)
    private val _state = MutableStateFlow(AppState())
    val state = _state.asStateFlow()
    var chats by mutableStateOf<List<ChatData>>(emptyList())
    var solicitudesRecibidas by mutableStateOf<List<ChatData>>(emptyList())
    var solicitudesEnviadas by mutableStateOf<List<ChatData>>(emptyList())
    var messages by mutableStateOf<List<Message>>(emptyList())
        private set
    var stories by mutableStateOf<List<Story>>(emptyList())
    var publicGroups by mutableStateOf<List<GroupChatData>>(emptyList())
    var myGroups by mutableStateOf<List<GroupChatData>>(emptyList())
    var groupMessages by mutableStateOf<List<Message>>(emptyList())
    var list by mutableStateOf<List<UserData>>(emptyList())
    var notifications by mutableStateOf<List<NotificationEntity>>(emptyList())
    var verificationRequests by mutableStateOf<List<VerificationRequest>>(emptyList())
    var reportRequests by mutableStateOf<List<ReportRequest>>(emptyList())
    var tp by mutableStateOf(ChatData())
    var forwardMsgs = mutableStateListOf<Message>()
    var reply by mutableStateOf("")
    var replyMessage by mutableStateOf(Message())
    var imgUri by mutableStateOf<Uri?>(null)
    var vidUri by mutableStateOf<Uri?>(null)
    var showDialog by mutableStateOf(false)
    var msg by mutableStateOf(Message())
    var searchText by mutableStateOf("")
    var fileUri by mutableStateOf<Uri?>(null)
    var searchText1 by mutableStateOf("")
    var showSearch by mutableStateOf(false)
    var selectionMode by mutableStateOf(false)
    var clearChatDialog by mutableStateOf(false)
    var editMsgId by mutableStateOf("")
    var editMsgContent by mutableStateOf("")
    var isModalBottomSheetVisible by mutableStateOf(false)
    var datePicker by mutableStateOf(false)
    var timePicker by mutableStateOf(false)
    var bitmap by mutableStateOf<Bitmap?>(null)
    var selectedEmoji by mutableStateOf("")
    var showOptions by mutableStateOf(false)
    var boostDialog by mutableStateOf(false)
    var showTransferDialog by mutableStateOf(false)
    var showTransferAnimation by mutableStateOf(false)
    fun addReaction(chatId: String, msgId: String, reaction: com.appinitdev.chatok.Firebase.Reaction) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val msgRef = firestore.collection("chats").document(chatId).collection("message").document(msgId)
                val msgDoc = msgRef.get().await()
                val message = msgDoc.toObject(Message::class.java) ?: return@launch
                val updatedReactions = message.reaction.toMutableList()
                val existingIndex = updatedReactions.indexOfFirst { it.userId == reaction.userId }
                if (existingIndex != -1) {
                    updatedReactions[existingIndex] = reaction
                } else {
                    updatedReactions.add(reaction)
                }
                msgRef.update("reaction", updatedReactions).await()
            } catch (e: Exception) { Log.e("ChatViewModel", "Add reaction error", e) }
        }
    }
    fun Reaction(emoji: String, chatId: String, msgId: String) {
        val myUser = state.value.userData ?: return
        addReaction(chatId, msgId, com.appinitdev.chatok.Firebase.Reaction(
            ppurl = myUser.ppurl ?: "",
            username = myUser.username ?: "",
            userId = myUser.userId,
            reaction = emoji
        ))
    }
    fun isBLockedByMe(userId: String): Boolean {
        return state.value.userData?.blockedUsers?.contains(userId) ?: false
    }
    fun isBlocked(userId: String, callback: (Boolean) -> Unit) {
        callback(false)
    }
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    private val _isSearchingRandom = MutableStateFlow(false)
    val isSearchingRandom = _isSearchingRandom.asStateFlow()
    private var chatsJob: Job? = null
    private var messagesJob: Job? = null
    private var storiesJob: Job? = null
    private var userJob: Job? = null
    var tpListener: ListenerRegistration? = null
    var userDataListener: ListenerRegistration? = null
    val brush = Brush.linearGradient(listOf(Color(0xFF238CDD), Color(0xFF1952C4)))
    init {
        observeChats()
        observeStories()
    }
    private fun observeChats() {
        chatsJob?.cancel()
        chatsJob = repository.getChatsFlow()
            .combine(state.map { it.userData?.userId }.distinctUntilChanged()) { allChats, myId ->
                val currentMyId = myId ?: ""
                chats = allChats.filter { it.accepted }
                val pendientes = allChats.filter { !it.accepted }
                solicitudesRecibidas = pendientes.filter { it.user1?.userId != currentMyId }
                solicitudesEnviadas = pendientes.filter { it.user1?.userId == currentMyId }
            }
            .launchIn(viewModelScope)
    }
    private var notificationsJob: Job? = null
    fun observeNotifications() {
        val uid = state.value.userData?.userId ?: return
        notificationsJob?.cancel()
        notificationsJob = repository.getNotificationsFlow(uid)
            .onEach { notifications = it }
            .launchIn(viewModelScope)
    }
    fun clearNotifications() {
        val uid = state.value.userData?.userId ?: return
        viewModelScope.launch { repository.clearNotifications(uid) }
    }
    fun submitVerificationRequest(message: String, uris: List<Uri>) {
        val myUser = state.value.userData ?: return
        val cost = 50
        if (myUser.balance < cost) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val imageUrls = mutableListOf<String>()
                uris.forEach { uri ->
                    val url = suspendCoroutine<String> { continuation ->
                        uploadMedia(uri = uri, folder = "verifications") { uploadedUrl ->
                            continuation.resume(uploadedUrl)
                        }
                    }
                    if (url.isNotEmpty()) {
                        imageUrls.add(url)
                    }
                }
                if (imageUrls.isNotEmpty()) {
                    val requestId = firestore.collection("verification_requests").document().id
                    val request = VerificationRequest(
                        requestId = requestId,
                        userId = myUser.userId,
                        username = myUser.username ?: "",
                        email = myUser.email,
                        message = message,
                        imageUrls = imageUrls,
                        status = "pending"
                    )
                    viewModelScope.launch { 
                        repository.submitVerificationRequest(request)
                        firestore.collection("users").document(myUser.userId).update("balance", FieldValue.increment(-cost.toLong()))
                    }
                }
            } catch (e: Exception) { Log.e("ChatViewModel", "submitVerification error", e) }
        }
    }
    fun observeVerificationRequests() {
        repository.getPendingVerificationRequests()
            .onEach { verificationRequests = it }
            .launchIn(viewModelScope)
    }
    fun updateVerificationStatus(userId: String, requestId: String, approved: Boolean) {
        viewModelScope.launch {
            try { repository.updateVerificationStatus(userId, requestId, approved) }
            catch (e: Exception) { Log.e("ChatViewModel", "updateVerification error", e) }
        }
    }
    fun submitReport(reportedUserId: String, reportedUsername: String, reason: String, details: String) {
        val myId = state.value.userData?.userId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val reportId = firestore.collection("reports").document().id
                val report = ReportRequest(
                    reportId = reportId,
                    reportedUserId = reportedUserId,
                    reportedByUserId = myId,
                    reportedUsername = reportedUsername,
                    reason = reason,
                    details = details,
                    status = "pending"
                )
                repository.submitReport(report)
                val userRef = firestore.collection("users").document(reportedUserId)
                firestore.runTransaction { transaction ->
                    val snapshot = transaction.get(userRef)
                    val currentReports = snapshot.getLong("reportCount")?.toInt() ?: 0
                    val nextReports = currentReports + 1
                    transaction.update(userRef, "reportCount", nextReports)
                    if (nextReports >= 3) {
                        val suspendedUntil = Timestamp(Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                        transaction.update(userRef, "suspendedUntil", suspendedUntil)
                    }
                }.await()
            } catch (e: Exception) { Log.e("ChatViewModel", "submitReport error", e) }
        }
    }
    fun observeReports() {
        repository.getPendingReports()
            .onEach { reportRequests = it }
            .launchIn(viewModelScope)
    }
    fun dismissReport(reportId: String) {
        viewModelScope.launch {
            try { repository.updateReportStatus(reportId, "dismissed") }
            catch (e: Exception) { Log.e("ChatViewModel", "dismissReport error", e) }
        }
    }
    fun deleteAccount(onSuccess: () -> Unit) {
        val uid = state.value.userData?.userId ?: return
        viewModelScope.launch {
            try {
                repository.deleteUserAccount(uid)
                repository.clearLocalData()
                onSuccess()
            } catch (e: Exception) { Log.e("ChatViewModel", "deleteAccount error", e) }
        }
    }
    fun addCoins(amount: Int) {
        val uid = state.value.userData?.userId ?: return
        viewModelScope.launch {
            try { repository.addCoins(uid, amount) }
            catch (e: Exception) { Log.e("ChatViewModel", "addCoins error", e) }
        }
    }
    fun buyVip(days: Int, cost: Int) {
        val uid = state.value.userData?.userId ?: return
        viewModelScope.launch {
            try { repository.purchaseVip(uid, days, cost) }
            catch (e: Exception) { Log.e("ChatViewModel", "buyVip error", e) }
        }
    }
    fun unlockChatWithSeller(sellerId: String, cost: Int, onSuccess: () -> Unit) {
        val uid = state.value.userData?.userId ?: return
        viewModelScope.launch {
            try { 
                repository.unlockChat(uid, sellerId, cost)
                onSuccess()
            } catch (e: Exception) { 
                Log.e("ChatViewModel", "unlockChat error", e)
            }
        }
    }
    fun unlockStory(storyId: String, cost: Int) {
        val uid = state.value.userData?.userId ?: return
        viewModelScope.launch {
            try { repository.unlockStory(uid, storyId, cost) }
            catch (e: Exception) { Log.e("ChatViewModel", "unlockStory error", e) }
        }
    }
    fun boostProfile(cost: Int) {
        val uid = state.value.userData?.userId ?: return
        viewModelScope.launch {
            try { repository.boostProfile(uid, cost) }
            catch (e: Exception) { Log.e("ChatViewModel", "boostProfile error", e) }
        }
    }
    fun transferCoins(toUserId: String, amount: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val fromUid = state.value.userData?.userId ?: return
        viewModelScope.launch {
            try {
                repository.transferCoins(fromUid, toUserId, amount)
                showTransferAnimation = true
                delay(3000) 
                showTransferAnimation = false
                withContext(Dispatchers.Main) { onSuccess() }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError(e.message ?: "Error desconocido") }
            }
        }
    }
    private fun observeStories() {
        storiesJob?.cancel()
        repository.syncStories()
        storiesJob = repository.getLocalStoriesFlow()
            .combine(state.map { it.userData?.userId }.distinctUntilChanged()) { allStories, myId ->
                val friendsIds = chats.flatMap { listOfNotNull(it.user1?.userId, it.user2?.userId) }.toSet()
                allStories.filter { story ->
                    story.userId == myId || story.userId in friendsIds
                }
            }
            .onEach { stories = it }
            .launchIn(viewModelScope)
    }
    fun showChats(userId: String) = repository.syncChats(userId)
    fun acceptChat(chatId: String) {
        viewModelScope.launch {
            try {
                repository.acceptChat(chatId)
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error accepting chat", e)
            }
        }
    }
    fun popMessage(chatId: String) {
        if (chatId.isEmpty()) return
        _isLoading.value = true
        messagesJob?.cancel()
        messages = emptyList()
        repository.syncMessages(chatId)
        messagesJob = repository.getMessagesFlow(chatId).onEach { 
            val oldMessagesSize = messages.size
            messages = it 
            _isLoading.value = false
            if (it.isNotEmpty() && oldMessagesSize > 0 && it.size > oldMessagesSize) {
                val lastMsg = it.first() 
                if (lastMsg.senderId != state.value.userData?.userId && 
                    lastMsg.content?.startsWith("🎁") == true) {
                    showTransferAnimation = true
                    viewModelScope.launch {
                        delay(3000)
                        showTransferAnimation = false
                    }
                }
            }
        }.launchIn(viewModelScope)
    }
    fun getUserData(userId: String) {
        if (userId.isBlank() || userId == "null") return
        userJob?.cancel()
        userJob = viewModelScope.launch {
            repository.getLocalUser(userId)?.let { _state.update { s -> s.copy(userData = it) } }
            repository.observeUser(userId).collect { usr -> usr?.let { _state.update { s -> s.copy(userData = it) } } }
        }
    }
    fun uploadMedia(uri: Uri = Uri.EMPTY, bytes: ByteArray = ByteArray(0), folder: String = "photos", 
                    msgId: String = "", chatId: String = "", callback: (String) -> Unit) {
        val uid = state.value.userData?.userId ?: "anonymous"
        val fileName = "${System.currentTimeMillis()}.jpg"
        val ref = storage.reference.child("$folder/$uid/$fileName")
        val finalBytes = if (uri != Uri.EMPTY && folder == "photos") {
            try {
                com.appinitdev.chatok.utils.MediaUtils.compressImage(BaseApplication.instance.applicationContext, uri)
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Compression failed, using original", e)
                null
            }
        } else null
        val uploadTask = if (finalBytes != null) {
            val metadata = com.google.firebase.storage.StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build()
            ref.putBytes(finalBytes, metadata)
        } else if (uri != Uri.EMPTY) {
            ref.putFile(uri)
        } else {
            val metadata = com.google.firebase.storage.StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build()
            ref.putBytes(bytes, metadata)
        }
        uploadTask.addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener { callback(it.toString()) }
        }.addOnFailureListener { e -> 
            Log.e("ChatViewModel", "Upload failed: ${e.message}")
            callback("") 
        }
        .addOnProgressListener { task ->
            if (msgId.isNotEmpty() && chatId.isNotEmpty()) {
                val progress = (100.0 * task.bytesTransferred / task.totalByteCount).toInt()
                firestore.collection("chats").document(chatId).collection("message").document(msgId).update("progress", "$progress%")
            }
        }
    }
    fun sendReply(
        chatId: String, 
        msg: String, 
        imgUrl: String = "", 
        vidUrl: String = "", 
        fileUrl: String = "",
        fileName: String = "",
        fileSize: String = "",
        imgUri: String = "", 
        audioUrl: String = "",
        isEphemeral: Boolean = false,
        ephemeralDuration: Long = 0,
        replyMsg: Message = Message(), 
        senderId: String = "",
        forwarded: Boolean = false
    ): String {
        val msgId = firestore.collection("chats").document(chatId).collection("message").document().id
        val finalSenderId = if (senderId.isNotEmpty()) senderId else state.value.userData?.userId.orEmpty()
        val message = Message(
            msgId = msgId, 
            senderId = finalSenderId, 
            content = msg, 
            imgUrl = imgUrl, 
            vidUrl = vidUrl, 
            fileUrl = fileUrl,
            fileName = fileName,
            fileSize = fileSize,
            audioUrl = audioUrl,
            imgUri = imgUri,
            isEphemeral = isEphemeral,
            ephemeralDuration = ephemeralDuration,
            repliedMsg = if(replyMsg.msgId.isEmpty()) null else replyMsg, 
            time = Timestamp.now(),
            forwarded = forwarded
        )
        viewModelScope.launch(Dispatchers.IO) {
            try {
                chatDao.insertMessages(listOf(message.toEntity(chatId)))
                repository.getLocalChat(chatId)?.let { chatEntity ->
                    chatDao.insertChats(listOf(chatEntity.copy(
                        lastMessageId = msgId,
                        lastMessageContent = msg,
                        lastMessageTime = message.time?.toDate()?.time ?: System.currentTimeMillis()
                    )))
                }
                firestore.collection("chats").document(chatId).collection("message").document(msgId).set(message).await()
                val chatDoc = firestore.collection("chats").document(chatId).get().await()
                val isAccepted = chatDoc.getBoolean("accepted") ?: false
                val user1Id = chatDoc.getString("user1.userId")
                val isUser1 = user1Id == finalSenderId
                val otherUserInChat = if (isUser1) chatDoc.getBoolean("user2.isInChat") ?: false else chatDoc.getBoolean("user1.isInChat") ?: false
                val otherUserId = if (isUser1) chatDoc.getString("user2.userId") else chatDoc.getString("user1.userId")
                val unreadField = if (isUser1) "user2.unread" else "user1.unread"
                val updates = mutableMapOf<String, Any>("last" to message)
                val user2Id = chatDoc.getString("user2.userId")
                if (!isAccepted && finalSenderId == user2Id) {
                    updates["accepted"] = true
                    repository.acceptChatLocally(chatId)
                }
                if (!otherUserInChat) {
                    updates[unreadField] = FieldValue.increment(1)
                    if (otherUserId != null) {
                        val notificationContent = msg.ifEmpty { 
                            if(imgUrl.isNotEmpty()) "📷 Foto" 
                            else if(vidUrl.isNotEmpty()) "🎥 Video" 
                            else "Archivo" 
                        }
                        sendNotification(notificationContent, otherUserId)
                    }
                }
                firestore.collection("chats").document(chatId).update(updates).await()
            } catch (e: Exception) { Log.e("ChatViewModel", "Send error", e) }
        }
        return msgId
    }
    fun storyReaction(id: String, index: Int, reaction: String, storyUserId: String) {
        val myUser = state.value.userData ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val storyRef = firestore.collection("stories").document(id)
                val storyDoc = storyRef.get().await()
                val story = storyDoc.toObject(Story::class.java) ?: return@launch
                val updatedImages = story.images.toMutableList()
                val image = updatedImages[index]
                val updatedViewers = image.viewedBy.toMutableList()
                val viewerIndex = updatedViewers.indexOfFirst { it.userId == myUser.userId }
                val newViewer = StoryViewer(
                    ppurl = myUser.ppurl ?: "",
                    username = myUser.username ?: "",
                    userId = myUser.userId,
                    reaction = reaction,
                    time = Timestamp.now()
                )
                if (viewerIndex != -1) {
                    updatedViewers[viewerIndex] = newViewer
                } else {
                    updatedViewers.add(newViewer)
                }
                updatedImages[index] = image.copy(viewedBy = updatedViewers)
                storyRef.update("images", updatedImages).await()
                sendNotification("${myUser.username} reaccionó a tu historia: $reaction", storyUserId)
            } catch (e: Exception) {
                Log.e("ChatViewModel", "storyReaction error", e)
            }
        }
    }
    fun viewStory(storyId: String, imageIndex: Int) {
        val myUser = state.value.userData ?: return
        if (myUser.userId.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val storyRef = firestore.collection("stories").document(storyId)
                val storyDoc = storyRef.get().await()
                val story = storyDoc.toObject(Story::class.java) ?: return@launch
                if (story.userId == myUser.userId) return@launch 
                val updatedImages = story.images.toMutableList()
                val image = updatedImages[imageIndex]
                if (image.viewedBy.any { it.userId == myUser.userId }) return@launch 
                val newViewer = StoryViewer(
                    ppurl = myUser.ppurl ?: "",
                    username = myUser.username ?: "",
                    userId = myUser.userId,
                    time = Timestamp.now()
                )
                val updatedViewers = image.viewedBy + newViewer
                updatedImages[imageIndex] = image.copy(viewedBy = updatedViewers)
                storyRef.update("images", updatedImages).await()
            } catch (e: Exception) {
                Log.e("ChatViewModel", "viewStory error", e)
            }
        }
    }
    fun findChatId(otherUserId: String, callback: (String) -> Unit) {
        val myId = state.value.userData?.userId ?: return
        firestore.collection("chats")
            .whereIn("user1.userId", listOf(myId, otherUserId))
            .get()
            .addOnSuccessListener { querySnapshot ->
                val doc = querySnapshot.documents.find { doc ->
                    val u1 = doc.getString("user1.userId")
                    val u2 = doc.getString("user2.userId")
                    (u1 == myId && u2 == otherUserId) || (u1 == otherUserId && u2 == myId)
                }
                callback(doc?.id ?: "")
            }
            .addOnFailureListener { callback("") }
    }
    fun findChatIdPro(otherUserId: String, callback: (String) -> Unit) {
        findChatId(otherUserId, callback)
    }
    fun createChat(otherUser: UserData) {
        val myUser = state.value.userData ?: return
        val chatId = if (myUser.userId < otherUser.userId) "${myUser.userId}_${otherUser.userId}" else "${otherUser.userId}_${myUser.userId}"
        val chatData = ChatData(
            chatId = chatId,
            user1 = ChatUserData(
                userId = myUser.userId,
                username = myUser.username,
                ppurl = myUser.ppurl ?: "",
                email = myUser.email
            ),
            user2 = ChatUserData(
                userId = otherUser.userId,
                username = otherUser.username,
                ppurl = otherUser.ppurl ?: "",
                email = otherUser.email
            ),
            accepted = false 
        )
        firestore.collection("chats").document(chatId).set(chatData)
    }
    fun createChatAndReply(otherUser: UserData, replyMessage: Message, replyContent: String) {
        val myUser = state.value.userData ?: return
        val chatId = if (myUser.userId < otherUser.userId) "${myUser.userId}_${otherUser.userId}" else "${otherUser.userId}_${myUser.userId}"
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val chatDoc = firestore.collection("chats").document(chatId).get().await()
                if (!chatDoc.exists()) {
                    val chatData = ChatData(
                        chatId = chatId,
                        user1 = ChatUserData(
                            userId = myUser.userId,
                            username = myUser.username,
                            ppurl = myUser.ppurl ?: "",
                            email = myUser.email
                        ),
                        user2 = ChatUserData(
                            userId = otherUser.userId,
                            username = otherUser.username,
                            ppurl = otherUser.ppurl ?: "",
                            email = otherUser.email
                        ),
                        accepted = false
                    )
                    firestore.collection("chats").document(chatId).set(chatData).await()
                }
                sendReply(chatId, replyContent, replyMsg = replyMessage)
            } catch (e: Exception) {
                Log.e("ChatViewModel", "createChatAndReply error", e)
            }
        }
    }
    fun sendNotification(message: String, userId: String) {
        val notification = mapOf(
            "to" to userId,
            "message" to message,
            "from" to (state.value.userData?.username ?: "Alguien"),
            "time" to Timestamp.now()
        )
        firestore.collection("notifications").add(notification)
    }
    fun deleteMsg(msgIds: List<String>, chatId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            msgIds.forEach { msgId ->
                val msgRef = firestore.collection("chats").document(chatId).collection("message").document(msgId)
                msgRef.update("isDeleted", true, "content", "Mensaje borrado").await()
                val chatDoc = firestore.collection("chats").document(chatId).get().await()
                val lastMsg = chatDoc.get("last", Message::class.java)
                if (lastMsg?.msgId == msgId) {
                    val updatedLast = lastMsg.copy(isDeleted = true, content = "Mensaje borrado")
                    firestore.collection("chats").document(chatId).update("last", updatedLast)
                }
                try {
                    val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "ChatOk")
                    directory.listFiles()?.forEach { file ->
                        if (file.name.startsWith(msgId)) {
                            file.delete()
                        }
                    }
                } catch (e: Exception) { Log.e("ChatViewModel", "Error deleting local file", e) }
            }
        }
    }
    fun downloadMedia(context: Context, url: String, fileName: String) {
        if (url.isEmpty() || url.startsWith("uploading")) return
        val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "ChatOk")
        if (!directory.exists()) directory.mkdirs()
        val file = File(directory, fileName)
        if (file.exists()) return 
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(fileName)
            .setDescription(context.getString(R.string.descargando_archivo))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "ChatOk/$fileName")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }
    fun toggleVendedora(isVendedora: Boolean) {
        val uid = state.value.userData?.userId ?: return
        viewModelScope.launch {
            try { repository.updateVendedoraStatus(uid, isVendedora) } 
            catch (e: Exception) { Log.e("ChatViewModel", "Vendedora error", e) }
        }
    }
    fun addChatWithRandomUser(callback: (UserData?) -> Unit) {
        val currentUser = state.value.userData ?: return
        val existingEmails = (chats + solicitudesRecibidas + solicitudesEnviadas).flatMap { listOfNotNull(it.user1?.email, it.user2?.email) }.toSet()
        viewModelScope.launch {
            _isSearchingRandom.value = true
            try {
                val user = repository.getRandomUser(currentUser.userId, existingEmails)
                callback(user)
            } catch (e: Exception) { callback(null) }
            finally { _isSearchingRandom.value = false }
        }
    }
    fun loadMyGroups() {
        val uid = state.value.userData?.userId ?: return
        repository.getMyGroupsFlow(uid).onEach { myGroups = it }.launchIn(viewModelScope)
    }
    fun updateStatus(online: Boolean, userId: String? = null, v: Boolean = false) {
        val uid = userId ?: state.value.userData?.userId ?: return
        viewModelScope.launch { 
            repository.updateOnlineStatus(uid, online, ignorePref = v)
        }
    }
    fun actualizarEstadoYStatusUsuario(online: Boolean, userId: String) {
        updateStatus(online, userId)
    }
    fun actualizarTodosUsuariosOffline() {
        viewModelScope.launch { repository.setAllUsersOffline() }
    }
    fun ejecutarUnaVezPorDia(context: Context, action: () -> Unit) {
        val prefs = context.getSharedPreferences("daily_tasks", Context.MODE_PRIVATE)
        val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val ultimaFecha = prefs.getString("last_run_date", null)
        if (ultimaFecha != hoy) {
            action()
            prefs.edit().putString("last_run_date", hoy).apply()
        }
    }
    fun readAllMessagesInChat(chatId: String) {
        if (chatId.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val myId = state.value.userData?.userId ?: return@launch
                val messagesCollection = firestore.collection("chats").document(chatId).collection("message")
                val unread = messagesCollection
                    .whereEqualTo("read", false)
                    .get().await()
                    .documents
                    .filter { it.getString("senderId") != myId }
                if (unread.isNotEmpty()) {
                    val batch = firestore.batch()
                    unread.forEach { batch.update(it.reference, "read", true) }
                    batch.commit().await()
                }
                val chatDoc = firestore.collection("chats").document(chatId).get().await()
                val user1Id = chatDoc.getString("user1.userId")
                val fieldToReset = if (user1Id == myId) "user1.unread" else "user2.unread"
                firestore.collection("chats").document(chatId).update(fieldToReset, 0).await()
            } catch (e: Exception) {
                Log.e("ChatViewModel", "readAllMessages error", e)
            }
        }
    }
    fun setchatUser(usr: ChatUserData, id: String) {
        _state.update { it.copy(User2 = usr, chatId = id) }
        val myId = state.value.userData?.userId ?: return
        userJob?.cancel()
        userJob = viewModelScope.launch {
            repository.observeUser(usr.userId).collect { updatedUser ->
                if (updatedUser != null) {
                    val updatedChatUser = usr.copy(
                        status = updatedUser.pref.status,
                        ppurl = updatedUser.ppurl ?: usr.ppurl,
                        username = updatedUser.username ?: usr.username,
                        isVerified = updatedUser.pref.isVerified
                    )
                    _state.update { it.copy(User2 = updatedChatUser) }
                }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val chatDoc = firestore.collection("chats").document(id).get().await()
                if (chatDoc.exists()) {
                    val user1Id = chatDoc.getString("user1.userId")
                    val field = if (user1Id == myId) "user1.isInChat" else "user2.isInChat"
                    firestore.collection("chats").document(id).update(field, true)
                }
            } catch (e: Exception) { Log.e("ChatViewModel", "setchatUser error", e) }
        }
    }
    fun dltchatUser() {
        val chatId = state.value.chatId
        val myId = state.value.userData?.userId
        _state.update { it.copy(chatId = "") }
        userJob?.cancel() 
        if (chatId.isNotEmpty() && myId != null) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val chatDoc = firestore.collection("chats").document(chatId).get().await()
                    if (chatDoc.exists()) {
                        val user1Id = chatDoc.getString("user1.userId")
                        val field = if (user1Id == myId) "user1.isInChat" else "user2.isInChat"
                        firestore.collection("chats").document(chatId).update(field, false, if (user1Id == myId) "user1.typing" else "user2.typing", false)
                    }
                } catch (e: Exception) { Log.e("ChatViewModel", "dltchatUser error", e) }
            }
        }
    }
    fun getTp(chatId: String) {
        tpListener?.remove()
        tpListener = firestore.collection("chats").document(chatId).addSnapshotListener { snp, _ ->
            snp?.toObject(ChatData::class.java)?.let { tp = it }
        }
    }
    fun depopTp() {
        tpListener?.remove()
    }
    fun popStory() {
        observeStories()
    }
    fun getFCMToken(userId: String) {
        com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            firestore.collection("users").document(userId).update("fcmToken", token)
        }
    }
    fun resetState() {
        _state.update { AppState() }
    }
    fun onSignInResult(result: SignInResult) {
        _state.update { it.copy(isSignedIn = result.data != null, signInError = result.errmsg) }
        if (result.data != null) {
            if (_state.value.userData == null || _state.value.userData?.username.isNullOrEmpty()) {
                _state.update { it.copy(userData = result.data) }
            }
        }
    }
    fun addUserDataToFirestore(userData: UserData, age: String = "", gender: String = "") {
        val updatedUser = userData.copy(
            age = if(age.isNotEmpty()) age else userData.age, 
            gender = if(gender.isNotEmpty()) gender else userData.gender
        )
        _state.update { it.copy(userData = updatedUser, isSignedIn = true) }
        viewModelScope.launch {
            try { 
                firestore.collection("users").document(updatedUser.userId).set(updatedUser).await() 
                repository.insertUser(updatedUser)
            }
            catch (e: Exception) { Log.e("ChatViewModel", "Error adding user data", e) }
        }
    }
    fun dePopMsg() {
        messagesJob?.cancel()
        messages = emptyList()
    }
    fun getBlockedUsers(userIds: List<String>) {
    }
    fun unblockUser(userId: String) {
    }
    fun blockUser(userId: String) {
    }
    fun loadMoreMessages(chatId: String) {
    }
    fun clearChat(chatId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val msgs = firestore.collection("chats").document(chatId).collection("message").get().await()
                val batch = firestore.batch()
                msgs.forEach { batch.delete(it.reference) }
                batch.commit().await()
                chatDao.deleteMessagesForChat(chatId)
            } catch (e: Exception) { Log.e("ChatViewModel", "Clear chat error", e) }
        }
    }
    fun addUrl(chatId: String, msgId: String, imgUrl: String = "", vidUrl: String = "", fileUrl: String = "") {
        val updates = mutableMapOf<String, Any>()
        if (imgUrl.isNotEmpty()) updates["imgUrl"] = imgUrl
        if (vidUrl.isNotEmpty()) updates["vidUrl"] = vidUrl
        if (fileUrl.isNotEmpty()) updates["fileUrl"] = fileUrl
        firestore.collection("chats").document(chatId).collection("message").document(msgId).update(updates)
    }
    fun uploadVideo(vidUri: Uri, chatId: String, msgId: String, callback: (String) -> Unit) {
        uploadMedia(uri = vidUri, folder = "videos", chatId = chatId, msgId = msgId, callback = callback)
    }
    fun uploadFile(fileUri: Uri, chatId: String, msgId: String, callback: (String) -> Unit) {
        uploadMedia(uri = fileUri, folder = "files", chatId = chatId, msgId = msgId, callback = callback)
    }
    fun removeReaction(chatId: String, msgId: String) {
    }
    fun addScheduledMsg(msg: ScheduledMsg) {
    }
    fun cancelScheduledMsg(chatId: String, content: String, senderId: String = "") {
    }
    fun deleteChat(chatIds: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            chatIds.forEach { chatId ->
                firestore.collection("chats").document(chatId).delete()
                chatDao.deleteChat(chatId)
            }
        }
    }
    fun deleteStory(story: Story, index: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val updatedImages = story.images.toMutableList()
                if (index in updatedImages.indices) {
                    updatedImages.removeAt(index)
                    if (updatedImages.isEmpty()) {
                        firestore.collection("stories").document(story.id).delete().await()
                    } else {
                        firestore.collection("stories").document(story.id).update("images", updatedImages).await()
                    }
                }
            } catch (e: Exception) { Log.e("ChatViewModel", "Delete story error", e) }
        }
    }
    fun hideDialog() {
        _state.update { it.copy(showDialog = false) }
    }
    fun showDialog() {
        _state.update { it.copy(showDialog = true) }
    }
    fun addChat(email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val snapshot = firestore.collection("users").whereEqualTo("email", email).get().await()
                if (!snapshot.isEmpty) {
                    val otherUser = snapshot.documents[0].toObject(UserData::class.java)
                    if (otherUser != null) {
                        createChat(otherUser)
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error adding chat by email", e)
            }
        }
    }
    fun setSrEmail(email: String) {
        _state.update { it.copy(srEmail = email) }
    }
    fun uploadStory(imgUrl: String, storyId: String = "", isLocked: Boolean = false, price: Int = 0) {
        val myUser = state.value.userData ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newImage = Image(imgUrl = imgUrl, time = Timestamp.now(), isLocked = isLocked, price = price)
                if (storyId.isNotEmpty()) {
                    val storyRef = firestore.collection("stories").document(storyId)
                    val storyDoc = storyRef.get().await()
                    if (storyDoc.exists()) {
                        val story = storyDoc.toObject(Story::class.java)!!
                        val updatedImages = story.images + newImage
                        storyRef.update("images", updatedImages).await()
                        return@launch
                    }
                }
                val newId = firestore.collection("stories").document().id
                val newStory = Story(
                    id = newId,
                    userId = myUser.userId,
                    username = myUser.username,
                    ppurl = myUser.ppurl ?: "",
                    images = listOf(newImage),
                    time = Timestamp.now()
                )
                firestore.collection("stories").document(newId).set(newStory).await()
            } catch (e: Exception) { Log.e("ChatViewModel", "Upload story error", e) }
        }
    }
    fun markUserAsRejected(email: String) {
    }
    fun updatePref(userData: UserData, pref: Pref) {
        firestore.collection("users").document(userData.userId).update("pref", pref)
    }
    fun forwardMsg(chatIds: List<String>) {
        chatIds.forEach { chatId ->
            forwardMsgs.forEach { msg ->
                sendReply(chatId, msg = msg.content ?: "", imgUrl = msg.imgUrl, vidUrl = msg.vidUrl, fileUrl = msg.fileUrl, forwarded = true)
            }
        }
        forwardMsgs.clear()
    }
    fun updateProfile(userData: UserData) {
        firestore.collection("users").document(userData.userId).set(userData)
    }
    fun removeL() {
        viewModelScope.launch {
            try {
                repository.clearLocalData()
                chats = emptyList()
                solicitudesRecibidas = emptyList()
                solicitudesEnviadas = emptyList()
                messages = emptyList()
                stories = emptyList()
                notifications = emptyList()
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error clearing local data", e)
            }
        }
    }
    fun removeFCMToken(userId: String) {
        firestore.collection("users").document(userId).update("fcmToken", FieldValue.delete())
    }
    fun updateNetworkStatus(isOffline: Boolean) {
        _state.update { it.copy(isOffline = isOffline) }
    }
    fun editMessage(chatId: String, msgId: String, newContent: String) {
        firestore.collection("chats").document(chatId).collection("message").document(msgId).update("content", newContent)
    }
    fun typing(isTyping: Boolean, chatId: String, userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val chatDoc = firestore.collection("chats").document(chatId).get().await()
                if (chatDoc.exists()) {
                    val user1Id = chatDoc.getString("user1.userId")
                    val field = if (user1Id == userId) "user1.typing" else "user2.typing"
                    firestore.collection("chats").document(chatId).update(field, isTyping)
                }
            } catch (e: Exception) { Log.e("ChatViewModel", "typing error", e) }
        }
    }
    fun uploadVoiceNote(file: File, chatId: String, callback: (String) -> Unit) {
        val dir = SimpleDateFormat("yyyy_MMMM", Locale("es", "ES")).format(Date())
        val fileName = "voice_${System.currentTimeMillis()}.m4a"
        val ref = storage.reference.child("voice_notes/$dir/$fileName")
        ref.putFile(Uri.fromFile(file)).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener { callback(it.toString()) }
        }
    }
    fun markMessageAsViewed(chatId: String, msgId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                firestore.collection("chats").document(chatId).collection("message").document(msgId)
                    .update("viewedAt", Timestamp.now())
                val entity = chatDao.getMessageById(msgId)
                if (entity != null) {
                    chatDao.insertMessages(listOf(entity.copy(viewedAt = System.currentTimeMillis())))
                }
            } catch (e: Exception) { Log.e("ChatViewModel", "markViewed error", e) }
        }
    }
    fun getAllMedia(onResult: (List<String>) -> Unit, onError: (Exception) -> Unit) {
        val uid = state.value.userData?.userId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val storage = FirebaseStorage.getInstance()
                val photosRef = storage.reference.child("photos/$uid")
                val videosRef = storage.reference.child("videos/$uid")
                val photosResult = try { photosRef.listAll().await() } catch(e: Exception) { null }
                val videosResult = try { videosRef.listAll().await() } catch(e: Exception) { null }
                val photoUrls = photosResult?.items?.map { it.downloadUrl.await().toString() } ?: emptyList()
                val videoUrls = videosResult?.items?.map { it.downloadUrl.await().toString() } ?: emptyList()
                val allUrls = photoUrls + videoUrls
                withContext(Dispatchers.Main) { onResult(allUrls) }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "getAllMedia error", e)
                withContext(Dispatchers.Main) { onError(e) }
            }
        }
    }
    fun deleteMedia(url: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val storage = FirebaseStorage.getInstance()
                val storageRef = storage.getReferenceFromUrl(url)
                storageRef.delete().await()
                withContext(Dispatchers.Main) { onSuccess() }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "deleteMedia error", e)
                withContext(Dispatchers.Main) { onError(e) }
            }
        }
    }
    override fun onCleared() {
        super.onCleared()
        repository.clearAllListeners()
        chatsJob?.cancel()
        messagesJob?.cancel()
        storiesJob?.cancel()
        userJob?.cancel()
        tpListener?.remove()
        userDataListener?.remove()
    }
    companion object {
        @Volatile private var instance: ChatViewModel? = null
        fun getInstance(): ChatViewModel = instance ?: synchronized(this) {
            instance ?: ChatViewModel().also { instance = it }
        }
    }
}
