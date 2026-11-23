object ChannelSystemMessages {
    const val TYPE_INVITE = "channel_invite"
    const val TYPE_INVITE_ACCEPT = "channel_invite_accept"
    const val TYPE_INVITE_DECLINE = "channel_invite_decline"
    const val TYPE_UPDATE = "channel_update"
    const val TYPE_KICK = "channel_kick"
    const val TYPE_LEAVE = "channel_leave"
    @Serializable
    data class ChannelInvite(
        val channelId: String,
        val channelName: String,
        val key: String,
        val owner: String,
        val members: List<ChannelMember>,
        val memberPeers: List<MemberPeerInfo> = emptyList(),
        val version: Long,
    )
    @Serializable
    data class MemberPeerInfo(
        val id: String,
        val name: String = "",
        val publicKey: String = "",
        val deviceType: String = "",
        val ip: String = "",
        val port: Int = 0,
    )
    @Serializable
    data class ChannelInviteAccept(
        val channelId: String,
        val publicKey: String = "",
        val name: String = "",
        val deviceType: String = "",
    )
    @Serializable
    data class ChannelInviteDecline(
        val channelId: String,
    )
    @Serializable
    data class ChannelUpdate(
        val channelId: String,
        val channelName: String,
        val members: List<ChannelMember>,
        val memberPeers: List<MemberPeerInfo> = emptyList(),
        val version: Long,
    )
    @Serializable
    data class ChannelKick(
        val channelId: String,
    )
    @Serializable
    data class ChannelLeave(
        val channelId: String,
    )
}
