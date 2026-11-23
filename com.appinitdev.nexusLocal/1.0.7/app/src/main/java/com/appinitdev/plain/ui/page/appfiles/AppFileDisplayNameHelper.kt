
object AppFileDisplayNameHelper {
    fun buildNameMap(chats: List<DChat>): Map<String, String> {
        val map = mutableMapOf<String, String>()
        chats
            .sortedByDescending { it.createdAt }
            .forEach { chat ->
                when (val value = chat.content.value) {
                    is DMessageFiles -> bindItems(value.items, map)
                    is DMessageImages -> bindItems(value.items, map)
                }
            }
        return map
    }

    fun resolveDisplayName(file: DAppFile, nameMap: Map<String, String>): String {
        val fromChat = nameMap[file.id].orEmpty().trim()
        if (fromChat.isNotEmpty()) return fromChat
        val ext = MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(file.mimeType)
            .orEmpty()
            .trim()
        return if (ext.isNotEmpty()) "file.$ext" else "file"
    }

    suspend fun resolveDisplayNameByPath(path: String, title: String): String {
        if (title.isNotEmpty()) return title
        
        val fileId = File(path).nameWithoutExtension
        val appFile = AppDatabase.instance.appFileDao().getById(fileId)
            ?: return fileId
        val nameMap = buildNameMap(AppDatabase.instance.chatDao().getAll())
        return resolveDisplayName(appFile, nameMap)
    }

    private fun bindItems(items: List<DMessageFile>, map: MutableMap<String, String>) {
        items.forEach { item ->
            if (item.isFidFile() && item.fileName.isNotEmpty()) {
                
                val hash = item.localFileId().substringBefore(".")
                map.putIfAbsent(hash, item.fileName)
            }
        }
    }
}