class HMessageCreatedEvent(val target: ChatTarget, val items: List<DChat>) : ChannelEvent()
class HMessageUpdatedEvent(val id: String) : ChannelEvent()
class HDownloadTaskDoneEvent(val downloadTask: DownloadTask) : ChannelEvent()
class HPomodoroStartEvent(val timeLeft: Int) : ChannelEvent()
class HPomodoroPauseEvent : ChannelEvent()
class HPomodoroStopEvent : ChannelEvent()
class HStartScreenMirrorEvent(val audio: Boolean) : ChannelEvent()
class HRequestScreenMirrorAudioEvent : ChannelEvent()
class HOpenAccessibilitySettingsEvent : ChannelEvent()
class HOpenWebSettingsEvent : ChannelEvent()
class HRetryChatItemEvent(val item: DChat) : ChannelEvent()
data class HStartMmsPollingEvent(
    val pendingId: String,
    val launchTimeSec: Long,
    val attachmentPaths: List<String>,
) : ChannelEvent()
class HEnableImageSearchEvent : ChannelEvent()
class HDisableImageSearchEvent : ChannelEvent()
class HCancelImageModelDownloadEvent : ChannelEvent()
class HCancelNotificationsEvent(val ids: Set<String>) : ChannelEvent()
class HChatItemsDeletedEvent(val ids: Set<String>) : ChannelEvent()
