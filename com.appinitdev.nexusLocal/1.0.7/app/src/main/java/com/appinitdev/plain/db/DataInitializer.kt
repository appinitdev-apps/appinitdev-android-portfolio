class DataInitializer(val context: Context, val db: SupportSQLiteDatabase) {
    private data class TagItem(val nameKey: StringResource, val type: DataType)
    private data class MessageItem(val content: String, val fromId: String, val toId: String)
    private val now = TimeHelper.now().toString()
    private val tags =
        arrayOf(
            TagItem(Res.string.light_music, DataType.AUDIO),
            TagItem(Res.string.movie, DataType.VIDEO),
            TagItem(Res.string.family, DataType.IMAGE),
            TagItem(Res.string.important, DataType.SMS),
            TagItem(Res.string.todo, DataType.SMS),
            TagItem(Res.string.family, DataType.CONTACT),
            TagItem(Res.string.important, DataType.CONTACT),
            TagItem(Res.string.personal, DataType.NOTE),
            TagItem(Res.string.work, DataType.NOTE),
        )
    fun insertTags() {
        tags.forEach { tag ->
            db.insert(
                "tags",
                SQLiteDatabase.CONFLICT_NONE,
                ContentValues().apply {
                    put("id", StringHelper.shortUUID())
                    put("name", LocaleHelper.getString(tag.nameKey))
                    put("type", tag.type.value)
                    put("count", 0)
                    put("created_at", now)
                    put("updated_at", now)
                },
            )
        }
    }
    fun insertNotes() {
        setOf(Res.string.note_sample1).forEach {
            val sample = LocaleHelper.getString(it)
            db.insert(
                "notes",
                SQLiteDatabase.CONFLICT_NONE,
                ContentValues().apply {
                    put("id", StringHelper.shortUUID())
                    put("title", sample.cut(100).replace("\n", ""))
                    put("content", sample)
                    put("created_at", now)
                    put("updated_at", now)
                },
            )
        }
    }
    fun insertWelcome() {
        setOf<MessageItem>(
            MessageItem("""{"type":"text","value":{"text":"${LocaleHelper.getString(Res.string.welcome_text)}"}}""", "local", "me"),
        ).forEach {
            db.insert(
                "chats",
                SQLiteDatabase.CONFLICT_NONE,
                ContentValues().apply {
                    put("id", StringHelper.shortUUID())
                    put("from_id", it.fromId)
                    put("to_id", it.toId)
                    put("channel_id", "") 
                    put("status", "sent") 
                    put("content", it.content)
                    put("created_at", now)
                    put("updated_at", now)
                },
            )
        }
    }
}
