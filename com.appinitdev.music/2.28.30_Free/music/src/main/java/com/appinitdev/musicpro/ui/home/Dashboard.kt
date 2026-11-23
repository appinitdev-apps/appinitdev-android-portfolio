
 






















@AndroidEntryPoint
class Dashboard : BaseHomeFragment() {

    private lateinit var binding: FragmentHomeDashboardBinding

    private val dashboardViewModel: DashboardViewModel by viewModels()

    private var recentlyPlayedAdapter: AdapterDashboardSongs? = null
    private var recentlyAddedAdapter: AdapterDashboardSongs? = null
    private var favoritesAdapter: AdapterDashboardSongs? = null
    private var topArtistsAdapter: AdapterDashboardArtists? = null
    private var topAlbumsAdapter: AdapterDashboardAlbums? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.label.setAppLabel()

        binding.label.setOnClickListener {
            childFragmentManager.showAppLabel()
        }

        setupPanelsGrid()
        showMiniPlayer()
        setupHeader()
        setupCarouselSpacing()
        observeData()

        binding.scrollView.requireAttachedMiniPlayer()

        binding.settings.setOnClickListener {
            openPreferencesPanel()
        }

        binding.refreshRecommended.setOnClickListener {
            dashboardViewModel.refreshRecommended()
        }
    }

    private fun setupHeader() {
        binding.search.setOnClickListener {
            openSearch()
        }

        setupServerToggle(binding.serverToggle)

        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                ServerModeService.isRunning.collect { running ->
                    if (running) binding.serverActiveChip.visible(true)
                    else binding.serverActiveChip.gone()
                }
            }
        }
    }

    private fun setupPanelsGrid() {
        val adapter = AdapterDashboardPanels(panels = dashboardViewModel.getVisiblePanelPanels())
        binding.panelsRecyclerView.layoutManager = FlexboxLayoutManager(requireContext()).apply {
            flexDirection = FlexDirection.ROW
            justifyContent = JustifyContent.FLEX_START
            alignItems = AlignItems.FLEX_START
        }
        binding.panelsRecyclerView.adapter = adapter
        adapter.setCallbacks(object : AdapterDashboardPanelsCallbacks {
            override fun onPanelClicked(panel: Panel) {
                navigateToPanel(panel)
            }
        })
    }

    private fun setupCarouselSpacing() {
        val spacing = resources.getDimensionPixelSize(R.dimen.carousel_spacing)
        binding.recentlyPlayedList.addItemDecoration(LinearHorizontalSpacingDecoration(spacing))
        binding.recentlyAddedList.addItemDecoration(LinearHorizontalSpacingDecoration(spacing))
        binding.favoritesList.addItemDecoration(LinearHorizontalSpacingDecoration(spacing))
        binding.topArtistsList.addItemDecoration(LinearHorizontalSpacingDecoration(spacing))
        binding.topAlbumsList.addItemDecoration(LinearHorizontalSpacingDecoration(spacing))
    }

     









    private fun setupRecommendedGrid(data: List<Audio>) {
        val spanConfig = dashboardViewModel.getSpanConfigForOrientation(resources.configuration.orientation)

        val layoutManager = SpannedGridLayoutManager(SpannedGridLayoutManager.Orientation.VERTICAL, spanConfig.spanCount)
        layoutManager.spanSizeLookup = SpannedGridLayoutManager.SpanSizeLookup { position ->
            if (position in spanConfig.bigCellPositions) SpanSize(2, 2) else SpanSize(1, 1)
        }

        
        binding.recommendedGrid.layoutParams.height = dashboardViewModel.recommendedHeight.takeIf { it != -1 } ?: 0

        
        val calculateAndSetHeight = {
            binding.recommendedGrid.post {
                try {
                    val totalHeight = layoutManager.getTotalHeight() +
                            binding.recommendedGrid.paddingTop +
                            binding.recommendedGrid.paddingBottom

                    if (dashboardViewModel.recommendedHeight != totalHeight) {
                        binding.recommendedGrid.layoutParams.height = totalHeight
                        binding.recommendedGrid.requestLayout()

                        
                        dashboardViewModel.recommendedHeight = totalHeight
                    }
                } catch (_: UninitializedPropertyAccessException) {
                    
                }
            }
        }

        if (binding.recommendedGrid.adapter != null) {
            binding.recommendedGrid.animate()
                .alpha(0f)
                .scaleX(0.9F)
                .scaleY(0.9F)
                .setDuration(300)
                .withEndAction {
                    applyRecommendedAdapter(data, layoutManager)

                    
                    calculateAndSetHeight()

                    binding.recommendedGrid.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(300)
                        .start()
                }.start()
        } else {
            applyRecommendedAdapter(data, layoutManager)

            
            calculateAndSetHeight()
        }

        
        
        
        binding.recommendedSection.visible(false)
    }

     







    private fun applyRecommendedAdapter(data: List<Audio>, layoutManager: SpannedGridLayoutManager) {
        binding.recommendedGrid.adapter = null
        binding.recommendedGrid.layoutManager = layoutManager
        binding.recommendedGrid.setHasFixedSize(false)
        val adapter = AdapterRecommended(data)
        binding.recommendedGrid.adapter = adapter
        binding.recommendedGrid.scheduleLayoutAnimation()

        adapter.setCallbacks(object : AdapterRecommendedCallbacks {
            override fun onItemClicked(items: List<Audio>, position: Int) {
                setMediaItems(items, position)
            }

            override fun onItemLongClicked(items: List<Audio>, position: Int, imageView: ImageView) {
                openSongsMenu(items, position, imageView)
            }
        })
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    dashboardViewModel.recommended.collect { data ->
                        if (data != null) setupRecommendedGrid(data)
                        else binding.recommendedSection.gone()
                    }
                }
                launch {
                    dashboardViewModel.recentlyPlayed.collect { songs ->
                        updateRecentlyPlayed(songs)
                    }
                }
                launch {
                    dashboardViewModel.recentlyAdded.collect { songs ->
                        updateRecentlyAdded(songs)
                    }
                }
                launch {
                    dashboardViewModel.favorites.collect { songs ->
                        updateFavorites(songs)
                    }
                }
                launch {
                    dashboardViewModel.libraryStats.collect { stats ->
                        updateLibraryStats(stats)
                    }
                }
                launch {
                    dashboardViewModel.topArtists.collect { artists ->
                        updateTopArtists(artists)
                    }
                }
                launch {
                    dashboardViewModel.topAlbums.collect { albums ->
                        updateTopAlbums(albums)
                    }
                }
            }
        }
    }

     





    private fun updateLibraryStats(stats: LibraryStats?) {
        if (stats == null) return
        val formatter = NumberFormat.getNumberInstance()
        binding.libraryStats.text = getString(
                R.string.library_stats,
                formatter.format(stats.trackCount),
                formatter.format(stats.totalHours)
        )
    }

    private fun updateRecentlyPlayed(songs: List<Audio>) {
        if (songs.isEmpty() || !UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_RECENTLY_PLAYED)) {
            binding.recentlyPlayedSection.gone()
            return
        }
        binding.recentlyPlayedSection.visible(false)
        if (recentlyPlayedAdapter == null) {
            recentlyPlayedAdapter = AdapterDashboardSongs(songs)
            recentlyPlayedAdapter!!.setCallbacks(object : AdapterDashboardSongsCallbacks {
                override fun onSongClicked(songs: MutableList<Audio>, position: Int) {
                    setMediaItems(songs, position)
                }

                override fun onSongLongClicked(songs: MutableList<Audio>, position: Int, imageView: ImageView) {
                    openSongsMenu(songs, position, imageView)
                }
            })
            binding.recentlyPlayedList.adapter = recentlyPlayedAdapter
        } else {
            recentlyPlayedAdapter!!.updateData(songs)
        }
    }

    private fun updateRecentlyAdded(songs: List<Audio>) {
        if (songs.isEmpty() || !UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_RECENTLY_ADDED)) {
            binding.recentlyAddedSection.gone()
            return
        }
        binding.recentlyAddedSection.visible(false)
        if (recentlyAddedAdapter == null) {
            recentlyAddedAdapter = AdapterDashboardSongs(songs)
            recentlyAddedAdapter!!.setCallbacks(object : AdapterDashboardSongsCallbacks {
                override fun onSongClicked(songs: MutableList<Audio>, position: Int) {
                    setMediaItems(songs, position)
                }

                override fun onSongLongClicked(songs: MutableList<Audio>, position: Int, imageView: ImageView) {
                    openSongsMenu(songs, position, imageView)
                }
            })
            binding.recentlyAddedList.adapter = recentlyAddedAdapter
        } else {
            recentlyAddedAdapter!!.updateData(songs)
        }
    }

    private fun updateFavorites(songs: List<Audio>) {
        if (songs.isEmpty() || !UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_FAVORITES)) {
            binding.favoritesSection.gone()
            return
        }
        binding.favoritesSection.visible(false)
        if (favoritesAdapter == null) {
            favoritesAdapter = AdapterDashboardSongs(songs)
            favoritesAdapter!!.setCallbacks(object : AdapterDashboardSongsCallbacks {
                override fun onSongClicked(songs: MutableList<Audio>, position: Int) {
                    setMediaItems(songs, position)
                }

                override fun onSongLongClicked(songs: MutableList<Audio>, position: Int, imageView: ImageView) {
                    openSongsMenu(songs, position, imageView)
                }
            })
            binding.favoritesList.adapter = favoritesAdapter
        } else {
            favoritesAdapter!!.updateData(songs)
        }
    }

     





    private fun updateTopArtists(artists: List<Artist>) {
        if (artists.isEmpty()) {
            binding.topArtistsSection.gone()
            return
        }
        binding.topArtistsSection.visible(false)
        if (topArtistsAdapter == null) {
            topArtistsAdapter = AdapterDashboardArtists(artists)
            topArtistsAdapter!!.setCallbacks(object : AdapterDashboardArtistsCallbacks {
                override fun onArtistClicked(artist: Artist) {
                    openFragment(ArtistPage.newInstance(artist), ArtistPage.TAG)
                }

                override fun onArtistLongClicked(artist: Artist, imageView: ImageView) {
                    openArtistMenu(artist, imageView)
                }
            })
            binding.topArtistsList.adapter = topArtistsAdapter
        } else {
            topArtistsAdapter!!.updateData(artists)
        }
    }

     





    private fun updateTopAlbums(albums: List<Album>) {
        if (albums.isEmpty()) {
            binding.topAlbumsSection.gone()
            return
        }
        binding.topAlbumsSection.visible(false)
        if (topAlbumsAdapter == null) {
            topAlbumsAdapter = AdapterDashboardAlbums(albums)
            topAlbumsAdapter!!.setCallbacks(object : AdapterDashboardAlbumsCallbacks {
                override fun onAlbumClicked(album: Album) {
                    openFragment(AlbumPage.newInstance(album), AlbumPage.TAG)
                }

                override fun onAlbumLongClicked(album: Album, imageView: ImageView) {
                    openAlbumMenu(album, imageView)
                }
            })
            binding.topAlbumsList.adapter = topAlbumsAdapter
        } else {
            topAlbumsAdapter!!.updateData(albums)
        }
    }

    override fun onDestroyView() {
        recentlyPlayedAdapter = null
        recentlyAddedAdapter = null
        favoritesAdapter = null
        topArtistsAdapter = null
        topAlbumsAdapter = null
        super.onDestroyView()
    }

    override fun onAudio(audio: Audio) {
        super.onAudio(audio)
        
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            MainPreferences.APP_LABEL -> {
                binding.label.setAppLabel()
            }
            
            
            in UserInterfacePreferences.ALL_PANEL_VISIBILITY_KEYS -> {
                setupPanelsGrid()
                
                evaluateCarouselSections()
            }
        }
    }

     




    private fun evaluateCarouselSections() {
        if (!UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_RECENTLY_PLAYED)) {
            binding.recentlyPlayedSection.gone()
        } else if ((recentlyPlayedAdapter?.itemCount ?: 0) > 0) {
            binding.recentlyPlayedSection.visible(false)
        }

        if (!UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_RECENTLY_ADDED)) {
            binding.recentlyAddedSection.gone()
        } else if ((recentlyAddedAdapter?.itemCount ?: 0) > 0) {
            binding.recentlyAddedSection.visible(false)
        }

        if (!UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_FAVORITES)) {
            binding.favoritesSection.gone()
        } else if ((favoritesAdapter?.itemCount ?: 0) > 0) {
            binding.favoritesSection.visible(false)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    companion object {
         




        fun newInstance(): Dashboard = Dashboard()

         
        const val TAG = "Dashboard"

        private const val PANEL_SPAN_COUNT = 3
    }
}