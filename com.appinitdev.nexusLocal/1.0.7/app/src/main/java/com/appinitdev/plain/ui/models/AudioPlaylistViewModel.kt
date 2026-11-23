class AudioPlaylistViewModel : ViewModel() {
    val playlistItems = mutableStateOf<List<DPlaylistAudio>>(listOf())
    val selectedPath = mutableStateOf("")
    suspend fun loadAsync(context: Context) {
        selectedPath.value = AudioPlayingPreference.getValueAsync()
        playlistItems.value = AudioPlaylistPreference.getValueAsync()
    }
    fun isInPlaylist(path: String): Boolean {
        return playlistItems.value.any { it.path == path }
    }
    suspend fun addAsync(context: Context, items: List<DAudio>) {
        val audio = items.map { it.toPlaylistAudio() }
        playlistItems.value = AudioPlaylistPreference.addAsync(audio)
        if (selectedPath.value.isEmpty()) {
            setCurrentPlaying(context, audio.first().path)
        }
    }
    suspend fun clearAsync(context: Context) {
        AudioPlaylistPreference.putAsync(listOf())
        playlistItems.value = listOf()
        AudioPlayer.clear()
        setCurrentPlaying(context, "")
        sendEvent(ClearAudioPlaylistEvent())
    }
    private suspend fun setCurrentPlaying(context: Context, path: String) {
        AudioPlayingPreference.putAsync(path)
        selectedPath.value = path
    }
    suspend fun playAsync(context: Context, item: DAudio) {
        val audio = item.toPlaylistAudio()
        playlistItems.value = AudioPlaylistPreference.addAsync(listOf(audio))
        AudioPlayer.justPlay(context, audio)
        setCurrentPlaying(context, audio.path)
    }
    suspend fun removeAsync(context: Context, path: String) {
        val newList = AudioPlaylistPreference.deleteAsync(setOf(path))
        playlistItems.value = newList
        if (path == selectedPath.value) {
            if (newList.isNotEmpty()) {
                val nextItem = newList[0]
                AudioPlayingPreference.putAsync(nextItem.path)
                AudioPlayer.justPlay(context, nextItem)
            }
        }
        if (newList.isEmpty()) {
            setCurrentPlaying(context, "")
            AudioPlayer.clear()
            sendEvent(ClearAudioPlaylistEvent())
        }
    }
    suspend fun reorder(context: Context, from: Int, to: Int) {
        val newList = playlistItems.value.toMutableList()
        newList.apply {
            add(to, removeAt(from))
        }
        playlistItems.value = newList
        AudioPlaylistPreference.putAsync(newList)
    }
}