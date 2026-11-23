
class PlayingQueue : BasePanelFragment() {

    private lateinit var binding: FragmentPlayingQueueBinding
    private lateinit var headerBinding: HeaderPlayingQueueBinding

    private var adapterPlayingQueue: AdapterPlayingQueue? = null

    private var hasScrolledToInitialPosition = false
    private var REMAINING_DURATION: Long = 0L

    private val playingQueueViewModel: PlayingQueueViewModel by viewModels({ requireActivity() })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPlayingQueueBinding.inflate(inflater, container, false)
        headerBinding = HeaderPlayingQueueBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.requireAttachedMiniPlayer()
        binding.appHeader.setContentView(headerBinding.root)
        binding.appHeader.attachTo(binding.recyclerView, AppHeader.ScrollMode.HIDE_ON_SCROLL)
        binding.recyclerView.attachSlideFastScroller()

         
        binding.recyclerView.setupGridLayoutManager(1)

        setupHeaderClicks()

        playingQueueViewModel.songs.collectWhenStarted { songs ->
            if (songs.isNotEmpty()) {
                updateQueueList(songs)
                setDurations()
                adapterPlayingQueue?.updateCurrentPosition(MediaPlaybackManager.getCurrentSongPosition())
            }
        }
    }

    override fun onDestroyView() {
        adapterPlayingQueue = null
        hasScrolledToInitialPosition = false
        super.onDestroyView()
    }

    private fun setupHeaderClicks() {
        
    }

    private fun updateQueueList(songs: List<Audio>) {
        if (adapterPlayingQueue == null) {
            adapterPlayingQueue = AdapterPlayingQueue(songs)
            adapterPlayingQueue?.setHasStableIds(true)

            adapterPlayingQueue?.setGeneralAdapterCallbacks(object : GeneralAdapterCallbacks {
                override fun onSongClicked(songs: MutableList<Audio>, position: Int, view: View) {
                    if (SelectionManager.hasSelection) {
                        SelectionManager.toggle(songs[position])
                    } else {
                        MediaPlaybackManager.updatePosition(position, forcePlay = true)
                    }
                }

                override fun onSongLongClicked(audios: MutableList<Audio>, position: Int, imageView: ImageView?) {
                    openSongsMenu(audios, position, imageView)
                }
            })

            adapterPlayingQueue?.setOnItemSwipedCallback { position ->
                MediaPlaybackManager.removeQueueItemSilently(position)
            }

            binding.recyclerView.adapter = adapterPlayingQueue
        } else {
            adapterPlayingQueue?.updateSongs(songs)
            if (binding.recyclerView.adapter == null) {
                binding.recyclerView.adapter = adapterPlayingQueue
            }
        }

        headerBinding.count.text = getString(R.string.x_songs, songs.size)
        headerBinding.hours.text = songs.sumOf { it.duration }.toDynamicTimeString()

        headerBinding.hours.setOnClickListener {
            childFragmentManager.showTotalTime(
                    totalTime = songs.sumOf { it.duration },
                    count = songs.size
            )
        }

         




        if (requireArguments().getBoolean(FIRST_LAUNCH_KEY, true)) {
            if (!hasScrolledToInitialPosition && binding.recyclerView.layoutManager is GridLayoutManager) {
                hasScrolledToInitialPosition = true
                val layoutManager = binding.recyclerView.layoutManager as GridLayoutManager
                val currentPosition = MediaPlaybackManager.getCurrentSongPosition()
                binding.recyclerView.post {
                    val firstVisible = layoutManager.findFirstVisibleItemPosition()
                    val lastVisible = layoutManager.findLastVisibleItemPosition()
                    if (currentPosition !in firstVisible..lastVisible) {
                        layoutManager.scrollToPositionWithOffset(
                                currentPosition,
                                binding.appHeader.height
                                        + resources.getDimensionPixelSize(R.dimen.padding_8)
                                        + binding.recyclerView.paddingTop)
                        binding.recyclerView.scheduleLayoutAnimation()
                    }
                }
            }

            requireArguments().putBoolean(FIRST_LAUNCH_KEY, false)
        }
    }

    override fun onSeekChanged(seek: Long) {
        super.onSeekChanged(seek)
        updateCountDown(seek)
    }

    override fun onAudio(audio: Audio) {
        super.onAudio(audio)
        setDurations()
        adapterPlayingQueue?.updateCurrentPosition(MediaPlaybackManager.getCurrentSongPosition())
    }

    private fun setDurations() {
        REMAINING_DURATION = 0L

        playingQueueViewModel.songs.value.let { songs ->
            val currentIndex = MediaPlaybackManager.getCurrentSongPosition()
            for (i in currentIndex + 1 until songs.size) {
                REMAINING_DURATION += songs[i].duration
            }
        }
    }

    private fun updateCountDown(seek: Long) {
        val remainingTimeInCurrentSong = (MediaPlaybackManager.getDuration() - seek).coerceAtLeast(0)
        val totalRemainingTime = remainingTimeInCurrentSong + REMAINING_DURATION
        headerBinding.hours.text = totalRemainingTime.toDynamicTimeString()
    }

    companion object {
        const val TAG = "PlayingQueue"

        private const val FIRST_LAUNCH_KEY = "first_launch"

        fun newInstance(): PlayingQueue {
            val args = Bundle()
            val fragment = PlayingQueue()
            fragment.arguments = args
            return fragment
        }
    }
}