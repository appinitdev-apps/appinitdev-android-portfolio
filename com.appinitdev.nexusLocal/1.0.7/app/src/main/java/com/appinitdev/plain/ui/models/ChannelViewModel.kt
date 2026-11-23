
class ChannelViewModel : ViewModel() {

    private val _channels = MutableStateFlow<List<DChatChannel>>(emptyList())
    val channels: StateFlow<List<DChatChannel>> = _channels.asStateFlow()

    val showCreateChannelDialog = mutableStateOf(false)

    init {
        refresh()

        viewModelScope.launch {
            Channel.sharedFlow.collect { event ->
                if (event is ChannelUpdatedEvent) {
                    refresh()
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            val all = AppDatabase.instance.chatChannelDao().getAll()
                .sortedBy { it.name.lowercase() }
            _channels.value = all
        }
    }

    fun getChannel(id: String?): DChatChannel? =
        id?.let { _channels.value.find { ch -> ch.id == it } }

    fun createChannel(name: String, onDone: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val ok = runHandling { ChannelManager.createChannel(name) }
            if (ok != null) withContext(Dispatchers.Main) { onDone() }
        }
    }

    fun renameChannel(channelId: String, newName: String, onDone: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            // renameChannel throws if the channel is missing — surface the
            // message but still call onDone so the dialog can dismiss.
            runHandling { ChannelManager.renameChannel(channelId, newName) }
            withContext(Dispatchers.Main) { onDone() }
        }
    }

    fun removeChannel(context: Context, channelId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            runHandling { ChannelManager.deleteChannel(context, channelId) }
        }
    }
}
