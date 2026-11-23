
@Keep
data class SignInResult (
    val data: UserData?,
    val errmsg: String?
)

@Keep
data class UserData (
    var userId: String = "",
    var username: String? = "",
    val bio: String? = "",
    val token: String? = "",
    val age : String? = "",
    val gender : String? = "",
    val ppurl: String? = "",
    var email: String = "",
    val instagram: String? = "",
    val facebook: String? = "",
    val telegram: String? = "",
    val otherLink: String? = "",
    val pref: Pref = Pref(),
    val blockedUsers: List<String> = emptyList(),
    val scheduledMsgs: List<ScheduledMsg> = emptyList(),
    @get:PropertyName("isAdmin") @set:PropertyName("isAdmin") @set:JvmName("setIsAdmin") var isAdmin: Boolean = false,
    @get:PropertyName("admin") @set:PropertyName("admin") var admin: Boolean = false, // Alias for Firestore
    var balance: Int = 0,
    @get:PropertyName("isVip") @set:PropertyName("isVip") @set:JvmName("setIsVip") var isVip: Boolean = false,
    @get:PropertyName("vip") @set:PropertyName("vip") var vip: Boolean = false, // Alias for Firestore
    var vipExpiration: Timestamp? = null,
    var fcmToken: String? = "",
    val unlockedChats: List<String> = emptyList(), // IDs of sellers unlocked
    val unlockedStories: List<String> = emptyList(), // Story IDs unlocked (all images)
    val reportCount: Int = 0,
    val suspendedUntil: Timestamp? = null,
    val boostedUntil: Timestamp? = null, // Added for Profile Boost
    val interests: List<String> = emptyList()
)

@Keep
data class Pref (
    var verificada: Boolean = false, // Modo Seller activado
    var isVerified: Boolean = false, // Insignia azul aprobada por admin
    val status: Boolean = false,
    val time: Timestamp = Timestamp(Date()),
    val online: Boolean = true,
    val rr: Boolean = true,
    val fontSize: Float = 16f,
    val back: Float = 1f,
    val doodles: Float = 0.1f,
    val anim: Int = 3,
    val customImg: String = ""
)

@Keep
data class ScheduledMsg (
    val username: String = "",
    val ppurl: String = "",
    val chatId: String = "",
    val content: String = "",
    val senderId: String = "",
    val time: Timestamp? = Timestamp.now()
)

@Keep
data class ChatData (
    val chatId: String = "",
    val last: Message? = null,
    val user1: ChatUserData? = null,
    val user2: ChatUserData? = null,
    val accepted: Boolean = false // Nuevo campo para separar chats de solicitudes
)

@Keep
@Immutable
data class Message (
    val msgId: String = "",
    val senderId: String = "",
    val repliedMsg: Message? = null,
    val reaction: List<Reaction> = emptyList(),
    val imgUrl: String = "",
    val fileUrl: String = "",
    val fileName: String = "",
    val fileSize: String = "",
    val vidUrl: String = "",
    val progress: String = "",
    val imgUri: String = "",
    val audioUrl: String = "", // Added for Voice Messages
    val content: String? = "",
    @ServerTimestamp
    val time: Timestamp? = null,
    val read: Boolean = false,
    val forwarded: Boolean = false,
    @get:PropertyName("isDeleted") @set:PropertyName("isDeleted") var isDeleted: Boolean = false,
    @get:PropertyName("isEphemeral") @set:PropertyName("isEphemeral") var isEphemeral: Boolean = false, // Added for View Once
    val ephemeralDuration: Long = 0, // 0 for view once, or milliseconds
    val viewedAt: Timestamp? = null,
    var localTime: Long = System.currentTimeMillis()
)

@Keep
@Immutable
data class Reaction (
    val ppurl: String = "",
    val username: String = "",
    val userId: String = "",
    val reaction: String = ""
)

@Keep
data class ChatUserData (
    val userId: String = "",
    val typing:Boolean= false,
    @get:PropertyName("isInChat") @set:PropertyName("isInChat") var isInChat: Boolean = false,
    val bio: String = "",
    val age : String? = "",
    val gender : String? = "",
    val username: String? = "",
    val ppurl: String = "",
    val email: String = "",
    val instagram: String? = "",
    val facebook: String? = "",
    val telegram: String? = "",
    val otherLink: String? = "",
    val status: Boolean = false,
    val lastSeen: Timestamp? = null,
    val unread: Int = 0,
    @get:PropertyName("isVerified") @set:PropertyName("isVerified") var isVerified: Boolean = false
)

@Keep
data class Story (
    val id: String = "",
    val userId: String = "",
    val username: String? = "",
    val images: List<Image> = emptyList(),
    val ppurl: String = "",
    @ServerTimestamp // 🔥 Esto es clave
    val time: Timestamp? = null
)

@Keep
data class Image (
    val imgUrl: String = "",
    val time: Timestamp? = Timestamp.now(),
    val viewedBy: List<StoryViewer> = emptyList(),
    val isLocked: Boolean = false,
    val price: Int = 0,
    val unlockedBy: List<String> = emptyList() // UserIds who paid
)

@Keep
data class StoryViewer (
    val ppurl: String = "",
    val username: String = "",
    val userId: String = "",
    val reaction: String = "",
    val time: Timestamp? = Timestamp.now()
)

// Agrega esto a tu archivo Datatypes.kt

@Keep
data class GroupMember(
    val userId: String = "",
    val username: String = "",
    val ppurl: String = "",
   // @ServerTimestamp val joinedAt: Timestamp? = null
)

@Keep
data class GroupChatData(
    val groupId: String = "",
    val groupName: String = "",
    val groupDescription: String = "",
    val groupImage: String = "",
    val createdBy: String = "",
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    val memberIds: List<String> = emptyList(), // <- Clave para las consultas de Firestore
    val membersInfo: List<GroupMember> = emptyList(), // <- Clave para la Interfaz Gráfica
    val lastMessage: Message? = null
)

@Keep
data class VerificationRequest(
    val requestId: String = "",
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val message: String = "",
    val imageUrls: List<String> = emptyList(), // Updated to list
    val status: String = "pending", // pending, approved, rejected
    @ServerTimestamp
    val timestamp: Timestamp? = null
)

@Keep
data class ReportRequest(
    val reportId: String = "",
    val reportedUserId: String = "",
    val reportedByUserId: String = "",
    val reportedUsername: String = "",
    val reason: String = "",
    val details: String = "",
    val status: String = "pending", // pending, reviewed, dismissed
    @ServerTimestamp
    val timestamp: Timestamp? = null
)
