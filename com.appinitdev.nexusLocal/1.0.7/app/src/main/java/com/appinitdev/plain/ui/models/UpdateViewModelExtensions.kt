fun UpdateViewModel.consumeUpdateDownloadEvent(event: ChannelEvent): Boolean {
    return when (event) {
        is UpdateDownloadProgressEvent -> {
            onDownloadProgress(event.progress)
            true
        }
        is UpdateDownloadCompleteEvent -> {
            onDownloadComplete(event.filePath)
            true
        }
        is UpdateDownloadFailedEvent -> {
            onDownloadFailed()
            true
        }
        else -> false
    }
}