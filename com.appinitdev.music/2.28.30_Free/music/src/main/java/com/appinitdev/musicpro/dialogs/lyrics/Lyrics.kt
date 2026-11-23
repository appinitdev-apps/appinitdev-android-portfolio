
@AndroidEntryPoint
class Lyrics : MediaBottomDialogFragment(), AddLyrics.Companion.OnLyricsCreatedListener {

    private lateinit var binding: DialogLyricsBinding

    private val audio: Audio? by lazy {
        requireArguments().parcelable(BundleConstants.AUDIO)
    }

    private val lyricsViewModel: ModalLyricsViewModel by viewModels(
            extrasProducer = {
                defaultViewModelCreationExtras.withCreationCallback<ModalLyricsViewModel.Factory> {
                    it.create(audio = audio)
                }
            }
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogLyricsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        
        binding.lrcView.setParentDismissEnabled(true)
        binding.lrcView.setFadeLength(FelicityLrcView.DEFAULT_FADE_LENGTH.div(2))
        setAlignment()
        applyTextSize()

        binding.title.text = audio?.getProperTitle()
        binding.artists.text = audio?.getProperArtists()

        lyricsViewModel.getLrcData().observe(viewLifecycleOwner) { lrcData ->
            binding.lrcView.reset()

            if (lrcData.isEmpty) {
                binding.addLyrics.visible(animate = true)
            } else {
                binding.lrcView.setLrcData(lrcData)
                binding.addLyrics.gone(animate = true)
            }
        }

        
        
        lyricsViewModel.getLoadingStatus().observe(viewLifecycleOwner) { status ->
            val message = when (status) {
                is LyricsLoadingStatus.Searching -> getString(R.string.searching_lyrics)
                is LyricsLoadingStatus.Downloading -> getString(R.string.downloading_lyrics)
                is LyricsLoadingStatus.Idle -> getString(R.string.no_lyrics_found)
            }
            binding.lrcView.setEmptyText(message)
        }

        binding.addLyrics.setOnClickListener {
            audio?.let {
                childFragmentManager.showAddLyrics(audio!!)
                    .setOnLyricsCreatedListener(this)
            }
        }
    }

    override fun onLyricsCreated() {
        
        lyricsViewModel.reloadLrcData()
    }

    override fun onSeekChanged(seek: Long) {
        super.onSeekChanged(seek)
        
        val currentSong = MediaPlaybackManager.getCurrentSong()
        if (currentSong != null && audio != null && currentSong.id == audio!!.id) {
            binding.lrcView.updateTime(seek + lyricsViewModel.syncOffset)
        }
    }

    override fun onAudio(audio: Audio) {
        super.onAudio(audio)
        
        
        
        if (this.audio != null && audio.id == this.audio!!.id) {
            binding.lrcView.updateTime(MediaPlaybackManager.getSeekPosition() + lyricsViewModel.syncOffset)
        } else {
            binding.lrcView.updateTime(-1)
        }
    }

    private fun setAlignment(animate: Boolean = false) {
        when (LyricsPreferences.getLrcAlignment()) {
            LyricsPreferences.LEFT -> binding.lrcView.setTextAlignment(FelicityLrcView.Alignment.LEFT, animate)
            LyricsPreferences.CENTER -> binding.lrcView.setTextAlignment(FelicityLrcView.Alignment.CENTER, animate)
            LyricsPreferences.RIGHT -> binding.lrcView.setTextAlignment(FelicityLrcView.Alignment.RIGHT, animate)
        }
    }

     
    private fun applyTextSize() {
        val normal = LyricsPreferences.getLrcTextSize()
        binding.lrcView.setTextSizes(normal, normal * LRC_HIGHLIGHT_TIMES)
    }

    companion object {
        fun newInstance(audio: Audio): Lyrics {
            val args = Bundle()
            args.putParcelable(BundleConstants.AUDIO, audio)
            val fragment = Lyrics()
            fragment.arguments = args
            return fragment
        }

        private const val TAG = "Lyrics"

        private const val LRC_HIGHLIGHT_TIMES = 1.2F

        fun FragmentManager.showLyrics(audio: Audio) {
            val dialog = newInstance(audio)
            dialog.show(this, TAG)
        }
    }
}