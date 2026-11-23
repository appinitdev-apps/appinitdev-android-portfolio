
 


















class TiledHome : BaseHomeFragment() {

    private lateinit var binding: FragmentHomeSpannedBinding
    private val homeViewModel: HomeViewModel by viewModels({ requireActivity() })

     
    private var tilesAdapter: AdapterSpannedHomeTiles? = null
    private var tileAnimators: MutableMap<Int, Pair<SpringAnimation, SpringAnimation>> = mutableMapOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeSpannedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireLightBarIcons()
        binding.recyclerView.setBackgroundColor(Color.BLACK)
        binding.recyclerView.requireAttachedMiniPlayer()

        setupRecyclerView()
        observeRecommended()
    }

     








    private fun setupRecyclerView() {
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val spanCount = if (isLandscape) SPAN_COUNT_LANDSCAPE else SPAN_COUNT_PORTRAIT
        val bigTileSpan = 2
        val smallTileSpan = 1

        val layoutManager = GridLayoutManager(requireContext(), spanCount)

        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                
                return if (position in AdapterSpannedHomeTiles.BIG_TILE_POSITIONS) bigTileSpan else smallTileSpan
            }
        }

        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.setHasFixedSize(false)
        binding.recyclerView.itemAnimator = null
    }

     




    private fun observeRecommended() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeViewModel.recommended.collect { songs ->
                    
                    if (songs.isEmpty() || tilesAdapter != null) return@collect
                    buildAndAttachAdapter(songs)
                }
            }
        }
    }

     





    private fun buildAndAttachAdapter(songs: List<Audio>) {
        val tiles = AdapterSpannedHomeTiles.buildTiles(songs, buildPanelTiles())
        tilesAdapter = AdapterSpannedHomeTiles(tiles)

        tilesAdapter!!.setCallbacks(object : AdapterSpannedHomeTiles.SpannedHomeTileCallbacks {
            override fun onSongTileClicked(songs: List<Audio>, position: Int) {
                setMediaItems(songs, position)
            }

            override fun onSongTileLongClicked(audio: Audio, imageView: ImageView) {
                val allSongs = tiles
                    .filterIsInstance<SpannedTile.SongTile>()
                    .map { it.audio }
                    .toMutableList()
                val index = allSongs.indexOfFirst { it.id == audio.id }.coerceAtLeast(0)

                
                
                
                animateTilesTo(randomScale = true, imageView)

                openSongsMenu(
                        audios = allSongs,
                        position = index,
                        imageView = imageView,
                        onDismissStart = {
                            
                            
                            
                            animateTilesTo(randomScale = false, imageView = imageView)
                        }
                )
            }

            override fun onPanelTileClicked(panelTile: SpannedTile.PanelTile) {
                when (panelTile.titleRes) {
                    R.string.server -> {
                        toggleServer()
                    }
                    else -> {
                        navigateToPanel(panelTile.getPanel())
                    }
                }
            }
        })

        binding.recyclerView.adapter = tilesAdapter
        binding.recyclerView.scheduleLayoutAnimation()
    }

     








    private fun buildPanelTiles(): List<SpannedTile.PanelTile> {
        val tiles = mutableListOf<SpannedTile.PanelTile>()

        
        tiles.add(SpannedTile.PanelTile(R.string.songs, R.drawable.ic_song))
        tiles.add(SpannedTile.PanelTile(R.string.albums, R.drawable.ic_album))
        tiles.add(SpannedTile.PanelTile(R.string.artists, R.drawable.ic_people))

        tiles.add(SpannedTile.PanelTile(R.string.server, R.drawable.ic_wifi))
        tiles.add(SpannedTile.PanelTile(R.string.search, R.drawable.ic_search))

        if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_ALBUM_ARTISTS))
            tiles.add(SpannedTile.PanelTile(R.string.album_artists, R.drawable.ic_artist))

        if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_COMPOSERS))
            tiles.add(SpannedTile.PanelTile(R.string.composers, R.drawable.ic_composer))

        if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_GENRES))
            tiles.add(SpannedTile.PanelTile(R.string.genres, R.drawable.ic_piano))

        if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_YEAR))
            tiles.add(SpannedTile.PanelTile(R.string.year, R.drawable.ic_date_range))

        if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_PLAYLISTS))
            tiles.add(SpannedTile.PanelTile(R.string.playlists, R.drawable.ic_list))

        if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_PLAYING_QUEUE))
            tiles.add(SpannedTile.PanelTile(R.string.playing_queue, R.drawable.ic_queue))

        if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_RECENTLY_ADDED))
            tiles.add(SpannedTile.PanelTile(R.string.recently_added, R.drawable.ic_recently_added))

        if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_RECENTLY_PLAYED))
            tiles.add(SpannedTile.PanelTile(R.string.recently_played, R.drawable.ic_history))

        if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_MOST_PLAYED))
            tiles.add(SpannedTile.PanelTile(R.string.most_played, R.drawable.ic_equalizer))

        if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_FAVORITES)) {
            val iconRes = if (UserInterfacePreferences.isLikeIconInsteadOfThumb())
                R.drawable.ic_thumb_up else R.drawable.ic_favorite_filled
            tiles.add(SpannedTile.PanelTile(R.string.favorites, iconRes))
        }

        if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_FOLDERS))
            tiles.add(SpannedTile.PanelTile(R.string.folders, R.drawable.ic_folder))

        if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_FOLDERS_HIERARCHY))
            tiles.add(SpannedTile.PanelTile(R.string.folders_hierarchy, R.drawable.ic_tree))

        if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_ALWAYS_SKIPPED))
            tiles.add(SpannedTile.PanelTile(R.string.always_skipped, R.drawable.ic_skip))

        if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_MOST_SKIPPED))
            tiles.add(SpannedTile.PanelTile(R.string.most_skipped, R.drawable.ic_skip))

        if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_BOOKMARKS)) {
            tiles.add(SpannedTile.PanelTile(R.string.bookmarks, R.drawable.ic_bookmark_16dp))
        }

        tiles.add(SpannedTile.PanelTile(R.string.preferences, R.drawable.ic_settings))

        return tiles
    }

    private fun animateTilesTo(randomScale: Boolean, imageView: ImageView) {
        tileAnimators.values.forEach { (xAnim, yAnim) ->
            xAnim.cancel()
            yAnim.cancel()
        }

        binding.recyclerView.forEachViewHolderIndexed<AdapterSpannedHomeTiles.SongHolder> { holder, _ ->
            if (holder.binding.art != imageView) {
                val targetScale = if (randomScale) Random.nextDouble(0.6, 0.9).toFloat() else 1f
                holder.itemView.applySpringScale(targetScale)
            }
        }

        binding.recyclerView.forEachViewHolderIndexed<AdapterSpannedHomeTiles.PanelHolder> { holder, _ ->
            val targetScale = if (randomScale) Random.nextDouble(0.6, 0.9).toFloat() else 1f
            holder.itemView.applySpringScale(targetScale)
        }
    }

    
    private fun View.applySpringScale(targetScale: Float) {
        
        val xAnimator = SpringAnimation(this, DynamicAnimation.SCALE_X, targetScale).apply {
            spring.stiffness = TILE_STIFFNESS
            spring.dampingRatio = TILE_DAMPING_RATIO
            start()
        }

        
        val yAnimator = SpringAnimation(this, DynamicAnimation.SCALE_Y, targetScale).apply {
            spring.stiffness = TILE_STIFFNESS
            spring.dampingRatio = TILE_DAMPING_RATIO
            start()
        }

        tileAnimators[this.id] = Pair(xAnimator, yAnimator)
    }

    override fun onDestroyView() {
        tilesAdapter = null
        tileAnimators.values.forEach { (xAnim, yAnim) ->
            xAnim.cancel()
            yAnim.cancel()
        }
        tileAnimators.clear()
        super.onDestroyView()
    }

    companion object {
         




        fun newInstance(): TiledHome {
            val args = Bundle()
            val fragment = TiledHome()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "TiledHome"

         
        private const val SPAN_COUNT_PORTRAIT = 3

         



        private const val SPAN_COUNT_LANDSCAPE = 6

        private const val TILE_STIFFNESS = 150F
        private const val TILE_DAMPING_RATIO = 1F
    }
}