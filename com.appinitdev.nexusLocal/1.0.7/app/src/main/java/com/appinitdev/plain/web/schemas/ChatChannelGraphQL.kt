
fun SchemaBuilder.addChatChannelSchema() {
    query("chatChannels") {
        resolver { ->
            AppDatabase.instance.chatChannelDao().getAll()
                .sortedBy { it.name.lowercase() }
                .map { it.toModel() }
        }
    }
    mutation("createChatChannel") {
        resolver { name: String ->
            ChannelManager.createChannel(name).toModel()
        }
    }
    mutation("updateChatChannel") {
        resolver { id: ID, name: String ->
            ChannelManager.renameChannel(id.value, name).toModel()
        }
    }
    mutation("deleteChatChannel") {
        resolver { id: ID ->
            ChannelManager.deleteChannel(MainApp.instance, id.value)
            true
        }
    }
    mutation("leaveChatChannel") {
        resolver { id: ID ->
            ChannelManager.leaveChannel(id.value)
            true
        }
    }
    mutation("addChatChannelMember") {
        resolver { id: ID, peerId: String ->
            ChannelManager.addMember(id.value, peerId).toModel()
        }
    }
    mutation("removeChatChannelMember") {
        resolver { id: ID, peerId: String ->
            ChannelManager.removeMember(id.value, peerId).toModel()
        }
    }
    mutation("acceptChatChannelInvite") {
        resolver { id: ID ->
            ChannelManager.acceptInvite(id.value)
            true
        }
    }
    mutation("declineChatChannelInvite") {
        resolver { id: ID ->
            ChannelManager.declineInvite(MainApp.instance, id.value)
            true
        }
    }
    type<ChatChannel> {}
    type<ChatChannelMember> {}
}
