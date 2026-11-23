class ChatRepository(
    private val firestore: FirebaseFirestore,
    private val chatDao: ChatDao,
    private val scope: CoroutineScope
) {
    private var chatsListener: ListenerRegistration? = null
    private val activeMessageListeners = mutableMapOf<String, ListenerRegistration>()
    private val usersCollection = firestore.collection("users")
    fun getChatsFlow(): Flow<List<ChatData>> = chatDao.getAllChats().map { entities ->
        entities.map { it.toChatData() }
    }.flowOn(Dispatchers.IO)
    fun syncChats(userId: String) {
        chatsListener?.remove()
        chatsListener = firestore.collection("chats")
            .where(Filter.or(Filter.equalTo("user1.userId", userId), Filter.equalTo("user2.userId", userId)))
            .addSnapshotListener { snapshot, _ ->
                snapshot?.documentChanges?.forEach { change ->
                    val chat = change.document.toObject(ChatData::class.java)
                    scope.launch(Dispatchers.IO) {
                        if (change.type == DocumentChange.Type.REMOVED) chatDao.deleteChat(chat.chatId)
                        else chatDao.insertChats(listOf(chat.toEntity()))
                    }
                }
            }
    }
    suspend fun acceptChat(chatId: String) {
        firestore.collection("chats").document(chatId).update("accepted", true).await()
        acceptChatLocally(chatId)
    }
    fun acceptChatLocally(chatId: String) {
        scope.launch(Dispatchers.IO) { chatDao.updateChatAcceptedStatus(chatId, true) }
    }
    suspend fun getLocalChat(chatId: String): ChatEntity? = chatDao.getChatById(chatId)
    fun getMessagesFlow(chatId: String): Flow<List<Message>> = chatDao.getMessagesForChat(chatId).map { entities ->
        entities.map { it.toMessage() }.sortedByDescending { it.time }
    }.flowOn(Dispatchers.IO)
    fun syncMessages(chatId: String) {
        if (activeMessageListeners.containsKey(chatId)) return
        val listener = firestore.collection("chats").document(chatId)
            .collection("message").orderBy("time", Query.Direction.DESCENDING).limit(100)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.documentChanges?.forEach { change ->
                    val msg = change.document.toObject(Message::class.java)
                    scope.launch(Dispatchers.IO) {
                        if (change.type == DocumentChange.Type.REMOVED) chatDao.deleteMessageById(msg.msgId)
                        else chatDao.insertMessages(listOf(msg.toEntity(chatId)))
                    }
                }
            }
        activeMessageListeners[chatId] = listener
    }
    fun getLocalStoriesFlow(): Flow<List<Story>> = chatDao.getAllStories().map { entities ->
        entities.map { it.toStory() }
    }.flowOn(Dispatchers.IO)
    fun syncStories() {
        firestore.collection("stories").orderBy("time", Query.Direction.DESCENDING).limit(30)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    scope.launch(Dispatchers.IO) {
                        val stories = it.documents.mapNotNull { doc -> doc.toObject(Story::class.java)?.toEntity() }
                        chatDao.insertStories(stories)
                    }
                }
            }
    }
    suspend fun uploadStory(userData: UserData, url: String, storyId: String) {
        val image = Image(imgUrl = url, time = Timestamp.now())
        if (storyId.isNotBlank()) {
            firestore.collection("stories").document(storyId).update("images", FieldValue.arrayUnion(image)).await()
        } else {
            val id = firestore.collection("stories").document().id
            val story = Story(id = id, userId = userData.userId, username = userData.username, ppurl = userData.ppurl.orEmpty(), images = listOf(image), time = Timestamp.now())
            firestore.collection("stories").document(id).set(story).await()
        }
    }
    suspend fun getLocalUser(userId: String): UserData? = chatDao.getUserById(userId)?.toUserData()
    suspend fun insertUser(userData: UserData) {
        chatDao.insertUser(userData.toEntity())
    }
    fun observeUser(userId: String): Flow<UserData?> = callbackFlow {
        val listener = usersCollection.document(userId).addSnapshotListener { v, e ->
            if (e != null) { close(e); return@addSnapshotListener }
            v?.toObject(UserData::class.java)?.let { usr ->
                scope.launch(Dispatchers.IO) { chatDao.insertUser(usr.toEntity()) }
                trySend(usr)
            }
        }
        awaitClose { listener.remove() }
    }.flowOn(Dispatchers.IO)
    suspend fun updateOnlineStatus(userId: String, online: Boolean, ignorePref: Boolean = false) {
        try {
            val userDoc = usersCollection.document(userId).get().await()
            val userData = userDoc.toObject(UserData::class.java) ?: return
            val shouldBeOnline = if (!ignorePref && !userData.pref.online) false else online
            val currentOnline = userDoc.getBoolean("pref.status") ?: false
            if (currentOnline != shouldBeOnline) {
                val updates = mapOf(
                    "pref.status" to shouldBeOnline,
                    "pref.time" to Timestamp.now()
                )
                usersCollection.document(userId).update(updates).await()
                scope.launch(Dispatchers.IO) {
                    chatDao.getUserById(userId)?.let { entity ->
                        chatDao.insertUser(entity.copy(
                            isOnline = shouldBeOnline, 
                            showOnlineSetting = userData.pref.online, 
                            lastSeen = System.currentTimeMillis()
                        ))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "updateOnlineStatus error", e)
        }
    }
    suspend fun setAllUsersOffline() {
        try {
            val snapshot = usersCollection.whereEqualTo("pref.status", true).get().await()
            val batch = firestore.batch()
            snapshot.documents.forEach { doc ->
                batch.update(doc.reference, "pref.status", false)
            }
            batch.commit().await()
        } catch (e: Exception) {
            Log.e("ChatRepository", "setAllUsersOffline error", e)
        }
    }
    suspend fun updateVendedoraStatus(userId: String, isVendedora: Boolean) {
        usersCollection.document(userId).update("pref.verificada", isVendedora).await()
        scope.launch(Dispatchers.IO) {
            chatDao.getUserById(userId)?.let { entity ->
                chatDao.insertUser(entity.copy(verified = isVendedora))
            }
        }
    }
    suspend fun submitVerificationRequest(request: VerificationRequest) {
        firestore.collection("verification_requests").document(request.requestId).set(request).await()
    }
    suspend fun submitReport(report: ReportRequest) {
        firestore.collection("reports").document(report.reportId).set(report).await()
    }
    fun getPendingReports(): Flow<List<ReportRequest>> = callbackFlow {
        val listener = firestore.collection("reports")
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, _ ->
                val reports = snapshot?.toObjects(ReportRequest::class.java) ?: emptyList()
                trySend(reports)
            }
        awaitClose { listener.remove() }
    }.flowOn(Dispatchers.IO)
    suspend fun updateReportStatus(reportId: String, status: String) {
        firestore.collection("reports").document(reportId).update("status", status).await()
    }
    suspend fun deleteUserAccount(userId: String) {
        try {
            val userStories = firestore.collection("stories")
                .whereEqualTo("userId", userId)
                .get().await()
            for (doc in userStories.documents) {
                doc.reference.delete().await()
            }
            val userVerifications = firestore.collection("verification_requests")
                .whereEqualTo("userId", userId)
                .get().await()
            for (doc in userVerifications.documents) {
                doc.reference.delete().await()
            }
            firestore.collection("users").document(userId).delete().await()
            try {
                FirebaseStorage.getInstance().reference.child("photos/$userId").delete().await()
            } catch (e: Exception) {
                Log.e("ChatRepository", "No storage photos to delete or error", e)
            }
            val authUser = FirebaseAuth.getInstance().currentUser
            if (authUser != null && authUser.uid == userId) {
                try {
                    authUser.delete().await()
                } catch (e: Exception) {
                    Log.e("ChatRepository", "Error deleting Firebase Auth user account", e)
                }
            }
            chatDao.clearAllData()
            clearAllListeners()
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error in deleteUserAccount", e)
            throw e
        }
    }
    fun getPendingVerificationRequests(): Flow<List<VerificationRequest>> = callbackFlow {
        val listener = firestore.collection("verification_requests")
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, _ ->
                val requests = snapshot?.toObjects(VerificationRequest::class.java) ?: emptyList()
                trySend(requests)
            }
        awaitClose { listener.remove() }
    }.flowOn(Dispatchers.IO)
    suspend fun updateVerificationStatus(userId: String, requestId: String, approved: Boolean) {
        val status = if (approved) "approved" else "rejected"
        firestore.collection("verification_requests").document(requestId).update("status", status).await()
        if (approved) {
            firestore.collection("users").document(userId).update("pref.isVerified", true).await()
            scope.launch(Dispatchers.IO) {
                chatDao.getUserById(userId)?.let { entity ->
                    chatDao.insertUser(entity.copy(isVerifiedBadge = true))
                }
            }
        }
    }
    suspend fun addCoins(userId: String, amount: Int) {
        firestore.collection("users").document(userId).update("balance", FieldValue.increment(amount.toLong())).await()
    }
    suspend fun purchaseVip(userId: String, days: Int, cost: Int) {
        val userRef = firestore.collection("users").document(userId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentBalance = snapshot.getLong("balance") ?: 0
            if (currentBalance < cost) throw Exception("Saldo insuficiente")
            val expiration = Calendar.getInstance().apply {
                time = snapshot.getTimestamp("vipExpiration")?.toDate() ?: Date()
                if (time.before(Date())) time = Date()
                add(Calendar.DAY_OF_YEAR, days)
            }.time
            transaction.update(userRef, "balance", currentBalance - cost)
            transaction.update(userRef, "isVip", true)
            transaction.update(userRef, "vipExpiration", Timestamp(expiration))
        }.await()
    }
    suspend fun boostProfile(userId: String, cost: Int) {
        val userRef = firestore.collection("users").document(userId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentBalance = snapshot.getLong("balance") ?: 0
            if (currentBalance < cost) throw Exception("Saldo insuficiente")
            val boostUntil = Timestamp(Date(System.currentTimeMillis() + 30 * 60 * 1000)) 
            transaction.update(userRef, "balance", currentBalance - cost)
            transaction.update(userRef, "boostedUntil", boostUntil)
        }.await()
    }
    suspend fun transferCoins(fromUserId: String, toUserId: String, amount: Int) {
        val fromRef = firestore.collection("users").document(fromUserId)
        val toRef = firestore.collection("users").document(toUserId)
        firestore.runTransaction { transaction ->
            val fromSnap = transaction.get(fromRef)
            val currentBalance = fromSnap.getLong("balance") ?: 0
            if (currentBalance < amount) throw Exception("Saldo insuficiente")
            transaction.update(fromRef, "balance", currentBalance - amount)
            transaction.update(toRef, "balance", FieldValue.increment(amount.toLong()))
        }.await()
    }
    suspend fun unlockChat(userId: String, sellerId: String, cost: Int) {
        val userRef = firestore.collection("users").document(userId)
        firestore.runTransaction { transaction ->
            val userSnap = transaction.get(userRef)
            val balance = userSnap.getLong("balance") ?: 0
            if (balance < cost) throw Exception("Saldo insuficiente")
            val unlocked = userSnap.get("unlockedChats") as? List<*> ?: emptyList<String>()
            if (unlocked.contains(sellerId)) return@runTransaction
            transaction.update(userRef, "balance", balance - cost)
            transaction.update(userRef, "unlockedChats", FieldValue.arrayUnion(sellerId))
        }.await()
    }
    suspend fun unlockStory(userId: String, storyId: String, cost: Int) {
        val userRef = firestore.collection("users").document(userId)
        firestore.runTransaction { transaction ->
            val userSnap = transaction.get(userRef)
            val balance = userSnap.getLong("balance") ?: 0
            if (balance < cost) throw Exception("Saldo insuficiente")
            val unlocked = userSnap.get("unlockedStories") as? List<*> ?: emptyList<String>()
            if (unlocked.contains(storyId)) return@runTransaction
            transaction.update(userRef, "balance", balance - cost)
            transaction.update(userRef, "unlockedStories", FieldValue.arrayUnion(storyId))
        }.await()
    }
    suspend fun getRandomUser(currentUserId: String, excludedEmails: Set<String>): UserData? {
        val boostedSnapshot = usersCollection
            .whereGreaterThan("boostedUntil", Timestamp.now())
            .limit(10)
            .get().await()
        val boostedUsers = boostedSnapshot.documents
            .mapNotNull { it.toObject(UserData::class.java) }
            .filter { it.userId != currentUserId && it.email !in excludedEmails }
        if (boostedUsers.isNotEmpty()) return boostedUsers.random()
        val snapshot = usersCollection.orderBy("pref.time", Query.Direction.DESCENDING).limit(60).get().await()
        val allUsers = snapshot.documents.mapNotNull { it.toObject(UserData::class.java) }
            .filter { it.userId != currentUserId && it.email !in excludedEmails }
        if (allUsers.isEmpty()) return null
        val oneHourAgo = System.currentTimeMillis() - (3600 * 1000)
        val priorityList = allUsers.filter { it.pref.verificada || it.pref.time.toDate().time > oneHourAgo }
        return if (priorityList.isNotEmpty()) priorityList.random() else allUsers.random()
    }
    fun getMyGroupsFlow(userId: String): Flow<List<GroupChatData>> = callbackFlow {
        val listener = firestore.collection("groups").whereArrayContains("memberIds", userId)
            .addSnapshotListener { snapshot, _ -> 
                val groups = snapshot?.toObjects(GroupChatData::class.java) ?: emptyList()
                trySend(groups) 
            }
        awaitClose { listener.remove() }
    }.flowOn(Dispatchers.IO)
    fun getNotificationsFlow(userId: String): Flow<List<NotificationEntity>> = 
        chatDao.getNotifications(userId).flowOn(Dispatchers.IO)
    suspend fun clearNotifications(userId: String) {
        chatDao.clearNotifications(userId)
    }
    suspend fun insertNotification(notification: NotificationEntity) {
        chatDao.insertNotification(notification)
    }
    fun getPublicGroups(pageSize: Long, lastDoc: DocumentSnapshot?): Flow<Pair<List<GroupChatData>, DocumentSnapshot?>> = flow {
        var query = firestore.collection("groups").orderBy("createdAt", Query.Direction.DESCENDING).limit(pageSize)
        lastDoc?.let { query = query.startAfter(it) }
        val snapshot = query.get().await()
        emit(Pair(snapshot.documents.mapNotNull { it.toObject(GroupChatData::class.java) }, snapshot.documents.lastOrNull()))
    }.flowOn(Dispatchers.IO)
    fun clearAllListeners() {
        chatsListener?.remove()
        activeMessageListeners.values.forEach { it.remove() }
        activeMessageListeners.clear()
    }
    suspend fun clearLocalData() {
        chatDao.clearAllData()
    }
}
