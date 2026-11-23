
@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val chatId: String,
    val user1Id: String,
    val user1Name: String?,
    val user1Ppurl: String?,
    val user1Email: String?,
    val user1Status: Boolean = false,
    val user1IsVerified: Boolean = false,
    val user2Id: String,
    val user2Name: String?,
    val user2Ppurl: String?,
    val user2Email: String?,
    val user2Status: Boolean = false,
    val user2IsVerified: Boolean = false,
    val lastMessageId: String?,
    val lastMessageContent: String?,
    val lastMessageTime: Long?,
    val user1Unread: Int = 0,
    val user2Unread: Int = 0,
    val accepted: Boolean = false
)

@Entity(
    tableName = "messages",
    indices = [Index(value = ["chatId"]), Index(value = ["time"])]
)
data class MessageEntity(
    @PrimaryKey val msgId: String,
    val chatId: String,
    val senderId: String,
    val content: String?,
    val time: Long?,
    val read: Boolean,
    val imgUrl: String = "",
    val vidUrl: String = "",
    val fileUrl: String = "",
    val fileName: String = "",
    val fileSize: String = "",
    val audioUrl: String = "",
    val repliedMsgId: String? = null,
    val reactionsJson: String? = null,
    val forwarded: Boolean = false,
    val isDeleted: Boolean = false,
    val isEphemeral: Boolean = false,
    val ephemeralDuration: Long = 0,
    val viewedAt: Long? = null
) {
    fun copy(viewedAt: Long?): MessageEntity {
        return MessageEntity(
            msgId = msgId,
            chatId = chatId,
            senderId = senderId,
            content = content,
            time = time,
            read = read,
            imgUrl = imgUrl,
            vidUrl = vidUrl,
            fileUrl = fileUrl,
            fileName = fileName,
            fileSize = fileSize,
            audioUrl = audioUrl,
            repliedMsgId = repliedMsgId,
            reactionsJson = reactionsJson,
            forwarded = forwarded,
            isDeleted = isDeleted,
            isEphemeral = isEphemeral,
            ephemeralDuration = ephemeralDuration,
            viewedAt = viewedAt
        )
    }
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val username: String?,
    val ppurl: String?,
    val email: String,
    val bio: String?,
    val age: String?,
    val gender: String?,
    val verified: Boolean = false,
    val isVerifiedBadge: Boolean = false,
    val lastSeen: Long? = null,
    val isOnline: Boolean = false, 
    val showOnlineSetting: Boolean = true, 
    val isAdmin: Boolean = false,
    val balance: Int = 0,
    val isVip: Boolean = false,
    val vipExpiration: Long? = null,
    val reportCount: Int = 0,
    val suspendedUntil: Long? = null,
    val boostedUntil: Long? = null,
    val unlockedChatsJson: String = "[]",
    val unlockedStoriesJson: String = "[]"
)

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val username: String?,
    val ppurl: String,
    val time: Long?,
    val imagesJson: String
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val from: String,
    val message: String,
    val time: Long,
    val userId: String 
)

fun Story.toEntity(): StoryEntity {
    val gson = Gson()
    return StoryEntity(
        id = id,
        userId = userId,
        username = username,
        ppurl = ppurl,
        time = time?.toDate()?.time,
        imagesJson = gson.toJson(images)
    )
}

fun StoryEntity.toStory(): Story {
    val gson = Gson()
    val type = object : TypeToken<List<com.appinitdev.chatok.Firebase.Image>>() {}.type
    val images: List<com.appinitdev.chatok.Firebase.Image> = gson.fromJson(imagesJson, type)
    return Story(
        id = id,
        userId = userId,
        username = username,
        ppurl = ppurl,
        time = time?.let { Timestamp(Date(it)) },
        images = images
    )
}

fun UserData.toEntity(): UserEntity {
    val gson = Gson()
    return UserEntity(
        userId = userId,
        username = username,
        ppurl = ppurl,
        email = email,
        bio = bio,
        age = age,
        gender = gender,
        verified = pref.verificada,
        isVerifiedBadge = pref.isVerified,
        lastSeen = pref.time.toDate().time,
        isOnline = pref.status,
        showOnlineSetting = pref.online,
        isAdmin = isAdmin,
        balance = balance,
        isVip = isVip,
        vipExpiration = vipExpiration?.toDate()?.time,
        reportCount = reportCount,
        suspendedUntil = suspendedUntil?.toDate()?.time,
        boostedUntil = boostedUntil?.toDate()?.time,
        unlockedChatsJson = gson.toJson(unlockedChats),
        unlockedStoriesJson = gson.toJson(unlockedStories)
    )
}

fun UserEntity.toUserData(): UserData {
    val gson = Gson()
    val type = object : TypeToken<List<String>>() {}.type
    val unlockedChats: List<String> = gson.fromJson(unlockedChatsJson, type) ?: emptyList()
    val unlockedStories: List<String> = gson.fromJson(unlockedStoriesJson, type) ?: emptyList()
    
    return UserData(
        userId = userId,
        username = username,
        ppurl = ppurl,
        email = email,
        bio = bio,
        age = age,
        gender = gender,
        pref = Pref(
            verificada = verified,
            isVerified = isVerifiedBadge,
            time = lastSeen?.let { Timestamp(Date(it)) } ?: Timestamp.now(),
            online = showOnlineSetting,
            status = isOnline
        ),
        isAdmin = isAdmin,
        balance = balance,
        isVip = isVip,
        vipExpiration = vipExpiration?.let { Timestamp(Date(it)) },
        reportCount = reportCount,
        suspendedUntil = suspendedUntil?.let { Timestamp(Date(it)) },
        boostedUntil = boostedUntil?.let { Timestamp(Date(it)) },
        unlockedChats = unlockedChats,
        unlockedStories = unlockedStories
    )
}

fun ChatData.toEntity(): ChatEntity {
    return ChatEntity(
        chatId = chatId,
        user1Id = user1?.userId ?: "",
        user1Name = user1?.username,
        user1Ppurl = user1?.ppurl,
        user1Email = user1?.email,
        user1Status = user1?.status ?: false,
        user1IsVerified = user1?.isVerified ?: false,
        user2Id = user2?.userId ?: "",
        user2Name = user2?.username,
        user2Ppurl = user2?.ppurl,
        user2Email = user2?.email,
        user2Status = user2?.status ?: false,
        user2IsVerified = user2?.isVerified ?: false,
        lastMessageId = last?.msgId,
        lastMessageContent = last?.content,
        lastMessageTime = last?.time?.toDate()?.time,
        user1Unread = user1?.unread ?: 0,
        user2Unread = user2?.unread ?: 0,
        accepted = accepted
    )
}

fun Message.toEntity(chatId: String): MessageEntity {
    val gson = Gson()
    return MessageEntity(
        msgId = msgId,
        chatId = chatId,
        senderId = senderId,
        content = content,
        time = time?.toDate()?.time,
        read = read,
        imgUrl = imgUrl,
        vidUrl = vidUrl,
        fileUrl = fileUrl,
        fileName = fileName,
        fileSize = fileSize,
        audioUrl = audioUrl,
        repliedMsgId = repliedMsg?.msgId,
        reactionsJson = if (reaction.isNotEmpty()) gson.toJson(reaction) else null,
        forwarded = forwarded,
        isDeleted = isDeleted,
        isEphemeral = isEphemeral,
        ephemeralDuration = ephemeralDuration,
        viewedAt = viewedAt?.toDate()?.time
    )
}

fun ChatEntity.toChatData(): ChatData {
    return ChatData(
        chatId = chatId,
        last = Message(
            msgId = lastMessageId ?: "",
            content = lastMessageContent,
            time = lastMessageTime?.let { Timestamp(Date(it)) }
        ),
        user1 = ChatUserData(
            userId = user1Id,
            username = user1Name,
            ppurl = user1Ppurl ?: "",
            email = user1Email ?: "",
            status = user1Status,
            lastSeen = lastMessageTime?.let { Timestamp(Date(it)) },
            unread = user1Unread,
            isVerified = user1IsVerified
        ),
        user2 = ChatUserData(
            userId = user2Id,
            username = user2Name,
            ppurl = user2Ppurl ?: "",
            email = user2Email ?: "",
            status = user2Status,
            lastSeen = lastMessageTime?.let { Timestamp(Date(it)) },
            unread = user2Unread,
            isVerified = user2IsVerified
        ),
        accepted = accepted
    )
}

fun MessageEntity.toMessage(repliedMsg: Message? = null): Message {
    val gson = Gson()
    val reactionList: List<Reaction> = if (!reactionsJson.isNullOrEmpty()) {
        val type = object : TypeToken<List<Reaction>>() {}.type
        gson.fromJson(reactionsJson, type)
    } else {
        emptyList()
    }
    
    return Message(
        msgId = msgId,
        senderId = senderId,
        content = content,
        time = time?.let { Timestamp(Date(it)) },
        read = read,
        imgUrl = imgUrl,
        vidUrl = vidUrl,
        fileUrl = fileUrl,
        fileName = fileName,
        fileSize = fileSize,
        audioUrl = audioUrl,
        repliedMsg = repliedMsg,
        reaction = reactionList,
        forwarded = forwarded,
        isDeleted = isDeleted,
        isEphemeral = isEphemeral,
        ephemeralDuration = ephemeralDuration,
        viewedAt = viewedAt?.let { Timestamp(Date(it)) }
    )
}
