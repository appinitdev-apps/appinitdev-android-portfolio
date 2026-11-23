
data class RoomMember(
    val userId: String,
    val name: String,
    val isFocusing: Boolean,
    val activeLabel: String,
    val currentSessionMinutes: Long
)

data class FocusRoom(
    val roomCode: String,
    val roomName: String,
    val hostUserId: String,
    val members: List<RoomMember>
)

class FocusRoomManager {

    private val _currentRoom = MutableStateFlow<FocusRoom?>(null)
    val currentRoom: StateFlow<FocusRoom?> = _currentRoom.asStateFlow()

    fun createRoom(roomName: String, hostName: String): FocusRoom {
        val code = (100000..999999).random().toString()
        val hostId = UUID.randomUUID().toString()
        val host = RoomMember(
            userId = hostId,
            name = hostName,
            isFocusing = false,
            activeLabel = "General",
            currentSessionMinutes = 0L
        )

        val room = FocusRoom(
            roomCode = code,
            roomName = roomName,
            hostUserId = hostId,
            members = listOf(host)
        )
        _currentRoom.value = room
        return room
    }

    fun joinRoom(roomCode: String, userName: String): Boolean {
        val userId = UUID.randomUUID().toString()
        val newMember = RoomMember(
            userId = userId,
            name = userName,
            isFocusing = false,
            activeLabel = "General",
            currentSessionMinutes = 0L
        )

        val active = _currentRoom.value
        if (active != null && active.roomCode == roomCode) {
            val updatedMembers = active.members + newMember
            _currentRoom.value = active.copy(members = updatedMembers)
            return true
        }

        
        _currentRoom.value = FocusRoom(
            roomCode = roomCode,
            roomName = "Focus Room #$roomCode",
            hostUserId = "remote_host",
            members = listOf(newMember)
        )
        return true
    }

    fun updateMyState(isFocusing: Boolean, activeLabel: String, minutes: Long) {
        val room = _currentRoom.value ?: return
        val updatedMembers = room.members.map { member ->
            if (member.userId == room.hostUserId) {
                member.copy(
                    isFocusing = isFocusing,
                    activeLabel = activeLabel,
                    currentSessionMinutes = minutes
                )
            } else {
                member
            }
        }
        _currentRoom.value = room.copy(members = updatedMembers)
    }

    fun serializeRoomState(): String {
        val room = _currentRoom.value ?: return ""
        val json = JSONObject().apply {
            put("roomCode", room.roomCode)
            put("roomName", room.roomName)
            put("hostUserId", room.hostUserId)
            val membersArray = JSONArray()
            for (m in room.members) {
                val memberObj = JSONObject().apply {
                    put("userId", m.userId)
                    put("name", m.name)
                    put("isFocusing", m.isFocusing)
                    put("activeLabel", m.activeLabel)
                    put("currentSessionMinutes", m.currentSessionMinutes)
                }
                membersArray.put(memberObj)
            }
            put("members", membersArray)
        }
        return json.toString()
    }

    fun leaveRoom() {
        _currentRoom.value = null
    }
}
