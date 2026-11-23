class ArtFlowHome : MediaFragment() {
    private lateinit var binding: FragmentHomeArtflowBinding
    private lateinit var gridLayoutManager: GridLayoutManager
    private val homeViewModel: HomeViewModel by viewModels({ requireActivity() })
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeArtflowBinding.inflate(inflater, container, false)
        return binding.root
    }
    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val spanCount = if (BarHeight.isLandscape(requireContext())) 3 else 1
        gridLayoutManager = GridLayoutManager(requireContext(), spanCount)
        binding.recyclerView.layoutManager = gridLayoutManager
        postponeEnterTransition()
        requireLightBarIcons()
        binding.recyclerView.setBackgroundColor(Color.BLACK)
        binding.recyclerView.requireAttachedMiniPlayer()
        binding.sideBar.attachToRecyclerView(binding.recyclerView)
        binding.sideBar.setCenterItemsVertically(true)
        binding.sideBar.setItemStyle(backgroundColor = ThemeManager.theme.viewGroupTheme.backgroundColor)
        binding.sideBar.setItems(listOf(
                FelicitySideBar.SidebarItem(R.drawable.ic_song),
                FelicitySideBar.SidebarItem(R.drawable.ic_people),
                FelicitySideBar.SidebarItem(R.drawable.ic_album),
                FelicitySideBar.SidebarItem(R.drawable.ic_menu),
                FelicitySideBar.SidebarItem(R.drawable.ic_search),
                FelicitySideBar.SidebarItem(R.drawable.ic_settings)
        ))
        binding.sideBar.setOnItemClickListener { id, anchorView ->
            Log.d(TAG, "Sidebar item clicked with id: $id")
            when (id) {
                R.drawable.ic_song -> {
                    openFragment(Songs.newInstance(), Songs.TAG)
                }
                R.drawable.ic_artist -> {
                    openFragment(Artists.newInstance(), Artists.TAG)
                }
                R.drawable.ic_album -> {
                    openFragment(Albums.newInstance(), Albums.TAG)
                }
                R.drawable.ic_menu -> {
                    SharedScrollViewPopup(
                            container = requireActivity().findViewById(R.id.app_container),
                            anchorView = anchorView,
                            backProgression = false,
                            menuItems = listOf(
                                    PopupMenuItem(title = R.string.album_artists, icon = R.drawable.ic_artist),
                                    PopupMenuItem(title = R.string.composers, icon = R.drawable.ic_composer),
                                    PopupMenuItem(title = R.string.genres, icon = R.drawable.ic_piano),
                                    PopupMenuItem(title = R.string.year, icon = R.drawable.ic_date_range),
                                    PopupMenuItem(title = R.string.folders, icon = R.drawable.ic_folder),
                                    PopupMenuItem(title = R.string.folders_hierarchy, icon = R.drawable.ic_tree),
                                    PopupMenuItem(title = R.string.playing_queue, icon = R.drawable.ic_queue),
                                    PopupMenuItem(title = R.string.favorites, icon = R.drawable.ic_favorite_filled),
                                    PopupMenuItem(title = R.string.playlists, icon = R.drawable.ic_playlist_play),
                                    PopupMenuItem(title = R.string.recently_added, icon = R.drawable.ic_recently_added),
                                    PopupMenuItem(title = R.string.recently_played, icon = R.drawable.ic_history),
                                    PopupMenuItem(title = R.string.most_played, icon = R.drawable.ic_equalizer),
                                    PopupMenuItem(title = R.string.always_skipped, icon = R.drawable.ic_skip_next),
                                    PopupMenuItem(title = R.string.most_skipped, icon = R.drawable.ic_skip_next),
                                    PopupMenuItem(title = R.string.bookmarks, icon = R.drawable.ic_bookmark_16dp)
                            ),
                            onMenuItemClick = { itemResId ->
                                when (itemResId) {
                                    R.string.album_artists -> openFragment(AlbumArtists.newInstance(), AlbumArtists.TAG)
                                    R.string.composers -> openFragment(Composers.newInstance(), Composers.TAG)
                                    R.string.genres -> openFragment(Genres.newInstance(), Genres.TAG)
                                    R.string.year -> openFragment(Year.newInstance(), Year.TAG)
                                    R.string.folders -> openFragment(Folders.newInstance(), Folders.TAG)
                                    R.string.folders_hierarchy -> openFragment(FoldersHierarchy.newInstance(), FoldersHierarchy.TAG)
                                    R.string.playing_queue -> openFragment(PlayingQueue.newInstance(), PlayingQueue.TAG)
                                    R.string.favorites -> openFragment(Favorites.newInstance(), Favorites.TAG)
                                    R.string.playlists -> openFragment(Playlists.newInstance(), Playlists.TAG)
                                    R.string.recently_added -> openFragment(RecentlyAdded.newInstance(), RecentlyAdded.TAG)
                                    R.string.recently_played -> openFragment(RecentlyPlayed.newInstance(), RecentlyPlayed.TAG)
                                    R.string.most_played -> openFragment(MostPlayed.newInstance(), MostPlayed.TAG)
                                    R.string.always_skipped -> openFragment(AlwaysSkipped.newInstance(), AlwaysSkipped.TAG)
                                    R.string.most_skipped -> openFragment(MostSkipped.newInstance(), MostSkipped.TAG)
                                    R.string.bookmarks -> openFragment(Bookmarks.newInstance(), Bookmarks.TAG)
                                    else -> Log.w(TAG, "Unknown popup item clicked: $itemResId")
                                }
                            },
                            onDismiss = {}
                    ).show()
                }
                R.drawable.ic_search -> {
                    openFragment(Search.newInstance(), Search.TAG)
                }
                R.drawable.ic_settings -> {
                    openFragment(Preferences.newInstance(), Preferences.TAG)
                }
                else -> {
                    Log.w(TAG, "Unknown sidebar item clicked with id: $id")
                }
            }
        }
        var adapter: AdapterArtFlowHome? = null
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(
                        homeViewModel.favorites,
                        homeViewModel.recentlyPlayed,
                        homeViewModel.mostPlayed,
                        homeViewModel.recentlyAdded,
                        homeViewModel.recommended
                ) { favorites, recentlyPlayed, mostPlayed, recentlyAdded, recommended ->
                    buildSections(favorites, recentlyPlayed, mostPlayed, recentlyAdded, recommended)
                }.collect { sections ->
                    Log.d(TAG, "Data received: ${sections.size} sections")
                    if (sections.isEmpty()) return@collect
                    if (adapter == null) {
                        adapter = AdapterArtFlowHome(sections)
                        binding.recyclerView.adapter = adapter
                        binding.recyclerView.setHasFixedSize(true)
                        adapter.setAdapterArtFlowHomeCallbacks(object : AdapterArtFlowHome.Companion.AdapterArtFlowHomeCallbacks {
                            override fun onItemClicked(imageView: ImageView, rowPosition: Int, itemPosition: Int) {
                                val audios = adapter.getSection(rowPosition)?.items?.filterIsInstance<Audio>()
                                if (audios.isNullOrEmpty()) return
                                setMediaItems(audios, itemPosition)
                            }
                            override fun onItemLongClicked(imageView: ImageView, rowPosition: Int, itemPosition: Int) {
                                binding.sideBar.hide()
                                val audios = adapter.getSection(rowPosition)?.items?.filterIsInstance<Audio>()
                                if (audios.isNullOrEmpty()) return
                                val rowHolder = binding.recyclerView
                                    .findViewHolderForAdapterPosition(rowPosition) as? AdapterArtFlowHome.Holder
                                rowHolder?.binding?.felicitySlider?.stop()
                                openSongsMenu(audios, itemPosition, imageView) {
                                    rowHolder?.binding?.felicitySlider?.start()
                                    postDelayed(250L) {
                                        binding.sideBar.show()
                                    }
                                }
                            }
                            override fun onClicked(view: View, position: Int, itemPosition: Int) {
                                Log.d(TAG, "Section container clicked at position: $position, itemPosition: $itemPosition")
                            }
                            override fun onClicked(view: View, position: Int) {
                                Log.d(TAG, "Section container clicked at position: $position")
                            }
                            override fun onPanelItemClicked(title: Int, view: View) {
                                Log.d(TAG, "Panel item clicked with title: $title")
                                when (title) {
                                    R.string.favorites -> openFragment(Favorites.newInstance(), Favorites.TAG)
                                    R.string.recently_played -> openFragment(RecentlyPlayed.newInstance(), RecentlyPlayed.TAG)
                                    R.string.most_played -> openFragment(MostPlayed.newInstance(), MostPlayed.TAG)
                                    R.string.recently_added -> openFragment(RecentlyAdded.newInstance(), RecentlyAdded.TAG)
                                    else -> Log.w(TAG, "Unknown panel item clicked with title: $title")
                                }
                            }
                        })
                        binding.recyclerView.setOnTouchListener { _, event ->
                            when (event.action) {
                                MotionEvent.ACTION_UP -> {
                                    binding.recyclerView.forEachViewHolder<AdapterArtFlowHome.Holder> {
                                        postDelayed(1_000L) {
                                            it.binding.felicitySlider.start()
                                        }
                                    }
                                }
                                MotionEvent.ACTION_DOWN -> {}
                            }
                            false
                        }
                        requireView().startTransitionOnPreDraw()
                    } else {
                        adapter.updateData(sections)
                    }
                }
            }
        }
    }
    private fun buildSections(
            favorites: List<Audio>,
            recentlyPlayed: List<Audio>,
            mostPlayed: List<Audio>,
            recentlyAdded: List<Audio>,
            recommended: List<Audio>
    ): List<ArtFlowData<Any>> {
        val sections = mutableListOf<ArtFlowData<Any>>()
        if (recommended.isNotEmpty()) sections.add(ArtFlowData(R.string.recommended, recommended))
        if (favorites.isNotEmpty()) sections.add(ArtFlowData(R.string.favorites, favorites))
        if (recentlyPlayed.isNotEmpty()) sections.add(ArtFlowData(R.string.recently_played, recentlyPlayed))
        if (mostPlayed.isNotEmpty()) sections.add(ArtFlowData(R.string.most_played, mostPlayed))
        if (recentlyAdded.isNotEmpty()) sections.add(ArtFlowData(R.string.recently_added, recentlyAdded))
        return sections
    }
    override fun onResume() {
        super.onResume()
    }
    companion object {
        fun newInstance(): ArtFlowHome {
            val args = Bundle()
            val fragment = ArtFlowHome()
            fragment.arguments = args
            return fragment
        }
        const val TAG = "ArtFlowHome"
    }
}
