class Song(val binding: AdapterStyleListBinding) : VerticalListViewHolder(binding.root) {
    fun bind(audio: Audio, showTrackInfo: Boolean = false, totalSongs: Int = 0) {
        binding.apply {
            title.setTextOrUnknown(audio.getProperTitle())
            title.addAudioQualityIcon(audio)
            secondaryDetail.setTextOrUnknown(audio.getProperArtists())
            if (showTrackInfo && totalSongs > 0) {
                val rawPart = audio.trackNumber?.trim()?.split("/")?.firstOrNull()?.trim() ?: ""
                val trackNum = rawPart.trimStart('0').takeIf { it.isNotEmpty() } ?: rawPart.ifEmpty { "?" }
                tertiaryDetail.text = "$trackNum/$totalSongs"
            } else {
                tertiaryDetail.setTextOrUnknown(audio.album)
            }
            container.setAudioID(audio.id)
            cover.loadArtCoverWithPayload(audio)
            cover.transitionName = audio.uri
        }
    }
}
