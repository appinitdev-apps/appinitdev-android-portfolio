@Dao
interface ChatDao {
    @Query("SELECT * FROM chats ORDER BY lastMessageTime DESC")
    fun getAllChats(): Flow<List<ChatEntity>>
    @Query("SELECT * FROM chats WHERE chatId = :chatId")
    suspend fun getChatById(chatId: String): ChatEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChats(chats: List<ChatEntity>)
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY time ASC")
    fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)
    @Query("SELECT * FROM messages WHERE msgId = :msgId")
    suspend fun getMessageById(msgId: String): MessageEntity?
    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun deleteMessagesForChat(chatId: String)
    @Query("DELETE FROM chats WHERE chatId = :chatId")
    suspend fun deleteChat(chatId: String)
    @Query("UPDATE chats SET accepted = :accepted WHERE chatId = :chatId")
    suspend fun updateChatAcceptedStatus(chatId: String, accepted: Boolean)
    @Query("DELETE FROM messages WHERE msgId = :msgId")
    suspend fun deleteMessageById(msgId: String)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    @Query("SELECT * FROM users WHERE userId = :userId")
    suspend fun getUserById(userId: String): UserEntity?
    @Query("SELECT * FROM stories ORDER BY time DESC")
    fun getAllStories(): Flow<List<StoryEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStories(stories: List<StoryEntity>)
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY time DESC")
    fun getNotifications(userId: String): Flow<List<NotificationEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)
    @Query("DELETE FROM notifications WHERE userId = :userId")
    suspend fun clearNotifications(userId: String)
    @Query("DELETE FROM chats")
    suspend fun clearAllChats()
    @Query("DELETE FROM messages")
    suspend fun clearAllMessages()
    @Query("DELETE FROM users")
    suspend fun clearAllUsers()
    @Query("DELETE FROM stories")
    suspend fun clearAllStories()
    @Transaction
    suspend fun clearAllData() {
        clearAllChats()
        clearAllMessages()
        clearAllUsers()
        clearAllStories()
    }
    @Transaction
    suspend fun updateChatAndMessages(chat: ChatEntity, messages: List<MessageEntity>) {
        insertChats(listOf(chat))
        insertMessages(messages)
    }
}
