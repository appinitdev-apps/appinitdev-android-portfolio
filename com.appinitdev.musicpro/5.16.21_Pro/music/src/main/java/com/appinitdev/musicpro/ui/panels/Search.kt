class Search : PanelFragment() {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var headerBinding: HeaderSearchBinding
    private var adapterSearch: AdapterSearch? = null
    private var gridLayoutManager: GridLayoutManager? = null
    private val searchViewModel: SearchViewModel by viewModels({ requireActivity() })
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        headerBinding = HeaderSearchBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.requireAttachedMiniPlayer()
        binding.appHeader.setContentView(headerBinding.root)
        binding.appHeader.attachTo(binding.recyclerView, AppHeader.ScrollMode.HIDE_ON_SCROLL)
        binding.recyclerView.attachSlideFastScroller()
        gridLayoutManager = GridLayoutManager(
                requireContext(), SearchPreferences.getGridSize().spanCount
        )
        binding.recyclerView.layoutManager = gridLayoutManager
        setupAdapters()
        setupClickListeners()
        viewLifecycleOwner.lifecycleScope.launch {
            searchViewModel.searchQuery.collect { query ->
                if (headerBinding.editText.text.toString() != query) {
                    headerBinding.editText.setText(query)
                    headerBinding.editText.setSelection(query.length)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                searchViewModel.searchResults.collect { results ->
                    updateSearchResults(results)
                }
            }
        }
    }
    override fun onDestroyView() {
        adapterSearch = null
        gridLayoutManager = null
        super.onDestroyView()
    }
    private fun setupAdapters() {
        adapterSearch = AdapterSearch().also { adapter ->
            adapter.setGeneralAdapterCallbacks(object : GeneralAdapterCallbacks {
                override fun onSongClicked(songs: MutableList<Audio>, position: Int, view: View) {
                    setMediaItems(songs, position)
                }
                override fun onSongLongClicked(
                        audios: MutableList<Audio>,
                        position: Int,
                        imageView: ImageView?) {
                    openSongsMenu(audios, position, imageView)
                }
                override fun onAlbumClicked(albums: MutableList<Album>, position: Int, view: View) {
                    openFragment(AlbumPage.newInstance(albums[position]), AlbumPage.TAG)
                }
                override fun onArtistClicked(
                        artist: MutableList<Artist>,
                        position: Int,
                        view: View) {
                    openFragment(ArtistPage.newInstance(artist[position]), ArtistPage.TAG)
                }
                override fun onGenreClicked(genre: Genre, view: View) {
                    openFragment(GenrePage.newInstance(genre), GenrePage.TAG)
                }
            })
        }
        binding.recyclerView.adapter = adapterSearch
        gridLayoutManager?.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val spanCount = gridLayoutManager?.spanCount ?: 1
                val adapter = adapterSearch ?: return 1
                if (position < 0 || position >= adapter.itemCount) return 1
                return when (adapter.getItemViewType(position)) {
                    AdapterSearch.VIEW_TYPE_SONG_LIST,
                    AdapterSearch.VIEW_TYPE_SONG_GRID,
                    AdapterSearch.VIEW_TYPE_SONG_LABEL -> 1
                    else -> spanCount
                }
            }
        }
    }
    private fun setupClickListeners() {
        headerBinding.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) =
                Unit
            override fun afterTextChanged(s: Editable?) {
                searchViewModel.setSearchQuery(s?.toString() ?: "")
            }
        })
        headerBinding.filter.setOnClickListener {
            childFragmentManager.showSearchFilter()
        }
        headerBinding.menu.setOnClickListener {
            openPreferencesPanel()
        }
        headerBinding.listStyle.setOnClickListener {
            childFragmentManager.showListStyleDialog(GenericListStyleDialog.Companion.PANEL.SEARCH)
        }
        headerBinding.sortStyle.setOnClickListener {
            childFragmentManager.showSearchSort()
        }
        headerBinding.scroll.setOnClickListener {
            binding.recyclerView.smoothScrollToPosition(0)
        }
    }
    override fun getShuffleButton(): HighlightTextView {
        return headerBinding.shuffle
    }
    private fun updateSearchResults(results: SearchResults) {
        val songsHeader = getString(R.string.songs)
        val albumsHeader = getString(R.string.albums)
        val artistsHeader = getString(R.string.artists)
        val genresHeader = getString(R.string.genres)
        adapterSearch?.submitResults(
                results = results,
                songsHeader = songsHeader,
                albumsHeader = albumsHeader,
                artistsHeader = artistsHeader,
                genresHeader = genresHeader
        )
        headerBinding.sortStyle.setSearchSort()
        if (results.songs.isNotEmpty()) {
            headerBinding.chipSongs.visible()
            headerBinding.hours.visible()
            headerBinding.shuffle.visible()
            headerBinding.chipSongs.text = getString(R.string.x_songs, results.songs.size)
            headerBinding.hours.text = results.songs.sumOf { it.duration }.toDynamicTimeString()
            headerBinding.chipSongs.setOnClickListener {
                scrollToSection(songsHeader)
            }
        } else {
            headerBinding.chipSongs.gone()
            headerBinding.hours.gone()
            headerBinding.shuffle.gone()
        }
        if (results.albums.isNotEmpty()) {
            headerBinding.chipAlbums.visible()
            headerBinding.chipAlbums.text = getString(R.string.x_albums, results.albums.size)
            headerBinding.chipAlbums.setOnClickListener {
                scrollToSection(albumsHeader)
            }
        } else {
            headerBinding.chipAlbums.gone()
        }
        if (results.artists.isNotEmpty()) {
            headerBinding.chipArtists.visible()
            headerBinding.chipArtists.text = getString(R.string.x_artists, results.artists.size)
            headerBinding.chipArtists.setOnClickListener {
                scrollToSection(artistsHeader)
            }
        } else {
            headerBinding.chipArtists.gone()
        }
        if (results.genres.isNotEmpty()) {
            headerBinding.chipGenres.visible()
            headerBinding.chipGenres.text = getString(R.string.x_genres, results.genres.size)
            headerBinding.chipGenres.setOnClickListener {
                scrollToSection(genresHeader)
            }
        } else {
            headerBinding.chipGenres.gone()
        }
        headerBinding.hours.setOnClickListener {
            childFragmentManager.showTotalTime(
                    totalTime = results.songs.sumOf { it.duration },
                    count = results.songs.size
            )
        }
    }
    private fun scrollToSection(headerTitle: String) {
        val position = adapterSearch?.getSectionPosition(headerTitle) ?: return
        if (position >= 0) {
            (binding.recyclerView.layoutManager as GridLayoutManager)
                .scrollToPositionWithOffset(
                        position,
                        binding.appHeader.height + resources.getDimensionPixelSize(R.dimen.padding_8))
        }
    }
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            SearchPreferences.SONG_SORT -> {
                headerBinding.sortStyle.setSearchSort()
            }
            SearchPreferences.GRID_SIZE_PORTRAIT, SearchPreferences.GRID_SIZE_LANDSCAPE -> {
                val newMode = SearchPreferences.getGridSize()
                gridLayoutManager?.spanCount = newMode.spanCount
                adapterSearch?.layoutMode = newMode
                binding.recyclerView.beginDelayedTransition()
                adapterSearch?.notifyItemRangeChanged(0, binding.recyclerView.adapter?.itemCount ?: 0)
            }
        }
    }
    companion object {
        const val TAG = "Search"
        fun newInstance(): Search {
            val args = Bundle()
            val fragment = Search()
            fragment.arguments = args
            return fragment
        }
    }
}