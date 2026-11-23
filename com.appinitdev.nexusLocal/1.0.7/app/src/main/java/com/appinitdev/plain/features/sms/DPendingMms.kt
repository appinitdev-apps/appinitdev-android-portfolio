data class DPendingMms(
    val id: String,                        
    val number: String,
    val body: String,
    val attachments: List<DMessageAttachment>,
    val threadId: String,
    val launchTimeSec: Long,               
    val createdAt: Instant,
)