
object AdapterUtils {
    fun TextView.addAudioQualityIcon(audio: Audio) {
        when (audio.audioQuality) {
            Audio.AUDIO_QUALITY_LQ -> {
                
                setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
            }
            Audio.AUDIO_QUALITY_HQ -> {
                setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_hq_12dp, 0)
            }
            Audio.AUDIO_QUALITY_LOSSLESS -> {
                setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_lossless_12dp, 0)
            }
            Audio.AUDIO_QUALITY_HI_RES -> {
                setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_hires_12dp, 0)
            }
            else -> {
                setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
            }
        }
    }

    fun TextView.addPlaylistTypeIcon(playlist: Playlist) {
        when {
            playlist.isM3UPlaylist -> {
                setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_m3u_16dp, 0)
            }
            else -> {
                setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
            }
        }
    }

     











    fun searchHighlighter(textView: TextView, searchKeyword: String) {
        if (searchKeyword.isBlank()) return
        val original = textView.text.toString()
        if (original.isEmpty()) return

        val spannable = original.toSpannable()
        val lowerOriginal = original.lowercase(Locale.getDefault())
        val lowerKeyword = searchKeyword.lowercase(Locale.getDefault())
        val accentColor = ColorStateList(arrayOf(intArrayOf()), intArrayOf(ThemeManager.accent.primaryAccentColor))

        var index = lowerOriginal.indexOf(lowerKeyword)
        while (index >= 0) {
            spannable.setSpan(
                    TextAppearanceSpan(null, Typeface.BOLD, -1, accentColor, null),
                    index, index + lowerKeyword.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            index = lowerOriginal.indexOf(lowerKeyword, index + lowerKeyword.length)
        }

        textView.text = spannable
    }

     










    fun searchHighlighter(textView: TextView, searchKeyword: String, ignoreCasing: Boolean) {
        if (searchKeyword.isBlank()) return
        val original = textView.text.toString()
        if (original.isEmpty()) return

        val searchIn = if (ignoreCasing) original.lowercase(Locale.getDefault()) else original
        val searchFor = if (ignoreCasing) searchKeyword.lowercase(Locale.getDefault()) else searchKeyword

        val spannable = original.toSpannable()
        val accentColor = ColorStateList(arrayOf(intArrayOf()), intArrayOf(ThemeManager.accent.primaryAccentColor))

        var index = searchIn.indexOf(searchFor)
        while (index >= 0) {
            spannable.setSpan(
                    TextAppearanceSpan(null, Typeface.BOLD, -1, accentColor, null),
                    index, index + searchFor.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            index = searchIn.indexOf(searchFor, index + searchFor.length)
        }

        textView.text = spannable
    }

    fun TextView.setSelectedIndicator(isSelected: Boolean) {
        if (isSelected) {
            setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_ring_12dp, 0)
        } else {
            setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
        }
    }
}