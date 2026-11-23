@AndroidEntryPoint
class Favorites : BasePanelFragment() {
    private lateinit var binding: FragmentFavoritesBinding
    private lateinit var headerBinding: HeaderFavoritesBinding
    private var adapterSongs: AdapterFavorites? = null
    private val favoritesViewModel: FavoritesViewModel by viewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        headerBinding = HeaderFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.requireAttachedMiniPlayer()
        binding.appHeader.setContentView(headerBinding.root)
        binding.appHeader.attachTo(binding.recyclerView, AppHeader.ScrollMode.HIDE_ON_SCROLL)
        binding.recyclerView.attachSlideFastScroller()
        binding.recyclerView.setupGridLayoutManager(FavoritesPreferences.getGridSize().spanCount)
        setupClickListeners()
        favoritesViewModel.favorites.collectListWhenStarted({ adapterSongs != null }) { songs ->
            updateFavoritesList(songs)
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
        headerBinding.menu.setOnClickListener {
            openPreferencesPanel()
        }
        headerBinding.listStyle.setOnClickListener {
            childFragmentManager.showListStyleDialog(GenericListStyleDialog.Companion.PANEL.FAVORITES)
        }
        headerBinding.sortStyle.setOnClickListener {
            childFragmentManager.showFavoritesSort()
        }
        headerBinding.search.setOnClickListener {
            openSearch()
        }
    }
    private fun updateFavoritesList(songs: List<Audio>) {
        if (adapterSongs == null) {
            adapterSongs = AdapterFavorites(songs)
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
        binding.recyclerView.requireAttachedSectionScroller(
                sections = provideScrollPositionDataBasedOnSortStyle(songs),
                header = binding.appHeader,
                view = headerBinding.scroll
        )
        headerBinding.count.text = getString(R.string.x_songs, songs.size)
        headerBinding.hours.text = songs.sumOf { it.duration }.toDynamicTimeString()
        headerBinding.sortStyle.setFavoritesSort()
        headerBinding.hours.setOnClickListener {
            childFragmentManager.showTotalTime(
                    totalTime = songs.sumOf { it.duration },
                    count = songs.size
            )
        }
    }
    private fun provideScrollPositionDataBasedOnSortStyle(songs: List<Audio>): List<SectionedFastScroller.Position> {
        return when (FavoritesPreferences.getSongSort()) {
            CommonPreferencesConstants.BY_TITLE -> {
                val firstAlphabetToIndex = linkedMapOf<String, Int>()
                songs.forEachIndexed { index, song ->
                    val firstChar = song.title?.firstOrNull()?.uppercaseChar()
                    val key = if (firstChar != null && firstChar.isLetter()) firstChar.toString() else "#"
                    if (!firstAlphabetToIndex.containsKey(key)) firstAlphabetToIndex[key] = index
                }
                firstAlphabetToIndex.map { (char, index) -> SectionedFastScroller.Position(char, index) }
            }
            CommonPreferencesConstants.BY_ARTIST -> {
                val firstAlphabetToIndex = linkedMapOf<Char, Int>()
                songs.forEachIndexed { index, song ->
                    song.artist?.firstOrNull()?.uppercaseChar()?.let { firstChar ->
                        if (firstChar.isLetter() && !firstAlphabetToIndex.containsKey(firstChar)) {
                            firstAlphabetToIndex[firstChar] = index
                        }
                    }
                }
                firstAlphabetToIndex.map { (char, index) -> SectionedFastScroller.Position(char.toString(), index) }
            }
            CommonPreferencesConstants.BY_ALBUM -> {
                val firstAlphabetToIndex = linkedMapOf<Char, Int>()
                songs.forEachIndexed { index, song ->
                    song.album?.firstOrNull()?.uppercaseChar()?.let { firstChar ->
                        if (firstChar.isLetter() && !firstAlphabetToIndex.containsKey(firstChar)) {
                            firstAlphabetToIndex[firstChar] = index
                        }
                    }
                }
                firstAlphabetToIndex.map { (char, index) -> SectionedFastScroller.Position(char.toString(), index) }
            }
            CommonPreferencesConstants.BY_YEAR -> {
                val firstAlphabetToIndex = linkedMapOf<String, Int>()
                songs.forEachIndexed { index, song ->
                    val key = song.year?.takeIf { it.all { ch -> ch.isDigit() } } ?: "#"
                    if (!firstAlphabetToIndex.containsKey(key)) firstAlphabetToIndex[key] = index
                }
                firstAlphabetToIndex.map { (year, index) -> SectionedFastScroller.Position(year, index) }
            }
            else -> {
                val firstAlphabetToIndex = linkedMapOf<Char, Int>()
                songs.forEachIndexed { index, song ->
                    song.title?.firstOrNull()?.uppercaseChar()?.let { firstChar ->
                        if (firstChar.isLetter() && !firstAlphabetToIndex.containsKey(firstChar)) {
                            firstAlphabetToIndex[firstChar] = index
                        }
                    }
                }
                firstAlphabetToIndex.map { (char, index) -> SectionedFastScroller.Position(char.toString(), index) }
            }
        }
    }
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            FavoritesPreferences.GRID_SIZE_PORTRAIT, FavoritesPreferences.GRID_SIZE_LANDSCAPE -> {
                val newMode = FavoritesPreferences.getGridSize()
                adapterSongs?.layoutMode = newMode
                applyGridSizeUpdate(binding.recyclerView, newMode.spanCount)
            }
        }
    }
    companion object {
        const val TAG = "Favorites"
        fun newInstance(): Favorites {
            val args = Bundle()
            val fragment = Favorites()
            fragment.arguments = args
            return fragment
        }
    }
}