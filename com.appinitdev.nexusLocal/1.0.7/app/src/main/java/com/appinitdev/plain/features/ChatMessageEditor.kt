
/**
 * Edits a text chat message and reconciles its link previews:
 * diffs URLs, cleans up images for removed URLs, fetches previews for added
 * URLs, persists via [ChatDbHelper], then emits one `MESSAGE_UPDATED` event.
 */
object ChatMessageEditor {

    /**
     * Returns the same [item] reference (mutated in place) so callers can
     * immediately update their UI lists. Returns true if a change was
     * persisted (and a `MESSAGE_UPDATED` event emitted), false if the text
     * and resolved previews are identical to the existing content.
     */
    suspend fun updateTextAsync(
        context: Context,
        item: DChat,
        newText: String,
    ): Boolean {
        val originalText = item.content.value as? DMessageText
        val originalPreviews = originalText?.linkPreviews ?: emptyList()
        val originalUrls = originalPreviews.map { it.url }
        val newUrls = LinkPreviewHelper.extractUrls(newText)
        val linksChanged = newUrls.toSet() != originalUrls.toSet()

        val resolvedPreviews: List<DLinkPreview> = if (!linksChanged) {
            originalPreviews
        } else {
            val removed = originalUrls - newUrls.toSet()
            originalPreviews
                .filter { it.url in newUrls }
                .also { survivors ->
                    removed.forEach { url ->
                        originalPreviews.firstOrNull { it.url == url }?.imageLocalPath?.let { path ->
                            LinkPreviewHelper.deletePreviewImage(context, path)
                        }
                    }
                }
                .toMutableList()
                .apply {
                    val added = newUrls - originalUrls.toSet()
                    if (added.isNotEmpty()) {
                        addAll(LinkPreviewHelper.fetchLinkPreviewsAsync(context, added).filter { !it.hasError })
                    }
                }
        }

        if (originalText?.text == newText && originalPreviews == resolvedPreviews) {
            return false
        }

        val content = DMessageContent(
            type = DMessageType.TEXT.value,
            value = DMessageText(text = newText, linkPreviews = resolvedPreviews),
        )
        ChatDbHelper.updateChatItemContent(item, content)

        val model = item.toModel().apply { data = getContentData() }
        sendEvent(
            WebSocketEvent(
                EventType.MESSAGE_UPDATED,
                JsonHelper.jsonEncode(listOf(model)),
            ),
        )
        return true
    }
}
