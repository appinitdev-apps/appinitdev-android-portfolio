
 






@AndroidEntryPoint
class MostPlayed : BasePanelFragment() {

    private lateinit var binding: FragmentMostPlayedBinding
    private lateinit var headerBinding: HeaderMostPlayedBinding

    private var adapterSongs: AdapterMostPlayed? = null

    private val mostPlayedViewModel: MostPlayedViewModel by viewModels({ requireActivity() })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMostPlayedBinding.inflate(inflater, container, false)
        headerBinding = HeaderMostPlayedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.requireAttachedMiniPlayer()
        binding.recyclerView.attachSlideFastScroller()
        binding.appHeader.setContentView(headerBinding.root)
        binding.appHeader.attachTo(binding.recyclerView, AppHeader.ScrollMode.HIDE_ON_SCROLL)

        binding.recyclerView.setupGridLayoutManager(MostPlayedPreferences.getGridSize().spanCount)

        setupClickListeners()

        mostPlayedViewModel.songs.collectListWhenStarted({ adapterSongs != null }) { songs ->
            updateSongsList(songs)
        }
    }

    override fun onDestroyView() {
        adapterSongs = null
        super.onDestroyView()
    }

    override fun getShuffleButton(): HighlightTextView {
        return headerBinding.shuffle
    }

    private fun setupClickListeners() {
        headerBinding.search.setOnClickListener {
            openSearch()
        }

        headerBinding.menu.setOnClickListener {
            openPreferencesPanel()
        }

        headerBinding.listStyle.setOnClickListener {
            childFragmentManager.showListStyleDialog(GenericListStyleDialog.Companion.PANEL.MOST_PLAYED)
        }
    }

    private fun updateSongsList(songs: List<AudioWithStat>) {
        if (adapterSongs == null) {
            adapterSongs = AdapterMostPlayed(initial = songs)
            adapterSongs?.setHasStableIds(true)
            adapterSongs?.setGeneralAdapterCallbacks(object : GeneralAdapterCallbacks {
                override fun onSongClicked(songs: MutableList<Audio>, position: Int, view: View) {
                    setMediaItems(songs, position)
                }

                override fun onSongLongClicked(audios: MutableList<Audio>, position: Int, imageView: ImageView?) {
                    openSongsMenu(audios, position, imageView)
                }
            })
            binding.recyclerView.adapter = adapterSongs
        } else {
            adapterSongs?.updateSongs(songs)
            if (binding.recyclerView.adapter == null) {
                binding.recyclerView.adapter = adapterSongs
            }
        }

        headerBinding.count.text = getString(R.string.x_songs, songs.size)
        headerBinding.hours.text = songs.sumOf { it.audio.duration }.toDynamicTimeString()

        headerBinding.hours.setOnClickListener {
            childFragmentManager.showTotalTime(
                    totalTime = songs.sumOf { it.audio.duration },
                    count = songs.size
            )
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            MostPlayedPreferences.GRID_SIZE_PORTRAIT, MostPlayedPreferences.GRID_SIZE_LANDSCAPE -> {
                val newMode = MostPlayedPreferences.getGridSize()
                adapterSongs?.layoutMode = newMode
                applyGridSizeUpdate(binding.recyclerView, newMode.spanCount)
            }
        }
    }

    companion object {
        fun newInstance(): MostPlayed {
            val args = Bundle()
            val fragment = MostPlayed()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "MostPlayed"
    }
}
