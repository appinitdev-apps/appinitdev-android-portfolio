
@AndroidEntryPoint
class MainActivity : BaseActivity(), MiniPlayerCallbacks {

    private lateinit var binding: ActivityMainBinding

     





    @Inject
    lateinit var lyricsManager: LyricsManager

    @Inject
    lateinit var audioRepository: AudioRepository

    private var isFirstLaunch = true

    override fun onCreate(savedInstanceState: Bundle?) {

         


        CrashReporter(applicationContext).initialize()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.miniPlayer.callbacks = object : MiniPlayer.Callbacks {
            override fun onPageSelected(position: Int, fromUser: Boolean) {
                
                
                
                
                if (fromUser) {
                    MediaPlaybackManager.updatePosition(position)
                }
            }

            override fun onLoadArt(position: Int, payload: Any?, setBitmap: (android.graphics.Bitmap?) -> Unit) {
                if (payload is Audio) {
                    loadArtIntoBitmap(payload, setBitmap)
                }
            }

            override fun onPlayPauseClick() {
                
                
                ensureServiceRunning {
                    MediaPlaybackManager.flipState()
                }
            }

            override fun onItemClick(position: Int) {
                val topFragment = supportFragmentManager.fragments.lastOrNull() as? ScopedFragment
                if (topFragment.isNotNull()) {
                    val player = openPlayerForCurrentPreference()
                    topFragment?.openFragment(player, BasePlayerFragment.TAG)
                }
            }
        }

        
        binding.miniPlayer.seekListener = { fraction ->
            val duration = MediaPlaybackManager.getDuration()
            if (duration > 0L) {
                MediaPlaybackManager.seekTo((fraction * duration).toLong())
            }
        }

        if (savedInstanceState.isNull()) {
            
            
            
            binding.miniPlayer.hide(animated = false)
            setHomePanel()

            startAudioDatabaseService()
        }

        lifecycleScope.launch {
            MediaPlaybackManager.songSeekPositionFlow.collect { position ->
                
                
                if (binding.miniPlayer.isSeeking) return@collect
                val duration = MediaPlaybackManager.getDuration()
                if (duration > 0L) {
                    
                    binding.miniPlayer.setProgress(
                            fraction = position.toFloat() / duration.toFloat(),
                            animate = false
                    )
                }
            }
        }

        lifecycleScope.launch {
            MediaPlaybackManager.songPositionFlow.collect { position ->
                val currentPagerItem = binding.miniPlayer.currentItem
                if (currentPagerItem != position) {
                    binding.miniPlayer.setCurrentItem(
                            position = position,
                            
                            smoothScroll = false)
                }
            }
        }

        lifecycleScope.launch {
            MediaPlaybackManager.songListFlow.collect { songs ->
                if (songs.isEmpty()) {
                    binding.miniPlayer.hide(animated = true)
                    binding.miniPlayer.setItems(emptyList())
                    return@collect
                }
                val items = songs.map { audio ->
                    MiniPlayerItem(
                            title = audio.getProperTitle(),
                            artist = audio.getProperArtists(),
                            payload = audio
                    )
                }
                binding.miniPlayer.setItems(items)
                val songPosition = MediaPlaybackManager.getCurrentSongPosition()
                binding.miniPlayer.setCurrentItem(
                        if (songPosition < songs.size) songPosition else 0,
                        smoothScroll = false
                )
                
                binding.miniPlayer.setProgress(0f)

                
                val fragment = supportFragmentManager.fragments.lastOrNull { it.isVisible }
                val wantsVisible = (fragment as? MiniPlayerPolicy)?.wantsMiniPlayerVisible ?: true
                if (wantsVisible) {
                    binding.miniPlayer.show(animated = true)
                }
            }
        }

        lifecycleScope.launch {
            MediaPlaybackManager.playbackStateFlow.collect { state ->
                when (state) {
                    MediaConstants.PLAYBACK_PLAYING -> binding.miniPlayer.setPlaying(true)
                    MediaConstants.PLAYBACK_PAUSED -> binding.miniPlayer.setPlaying(false)
                }
            }
        }

        
        
        lifecycleScope.launch {
            SelectionManager.selectedAudios.collect { selected ->
                updateActionBar(selected)
                binding.selectionCount.setSelectedSongsSize(selected.size)
            }
        }

        
        
        
        binding.root.viewTreeObserver.addOnPreDrawListener {
            binding.miniPlayerActionBar.translationY = binding.miniPlayer.translationY
            true
        }

        
        
        binding.miniPlayer.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            val lp = binding.miniPlayer.layoutParams as? ViewGroup.MarginLayoutParams
            val totalMiniPlayerSpace = binding.miniPlayer.height + (lp?.bottomMargin ?: 0)
            val gap = dpToPx(4f).toInt()
            val alp = binding.miniPlayerActionBar.layoutParams as? ViewGroup.MarginLayoutParams
            if (alp != null && alp.bottomMargin != totalMiniPlayerSpace + gap) {
                alp.bottomMargin = totalMiniPlayerSpace + gap
                binding.miniPlayerActionBar.requestLayout()
            }
        }

        
        
        binding.actionDelete.setOnClickListener {
            deleteSelectedSongs()
        }

        binding.actionShare.setOnClickListener {
            shareSelectedAudios(SelectionManager.selectedAudios.value)
        }

        binding.actionMore.setOnClickListener {
            showSelectionMenu()
        }

        binding.selectionCount.setOnClickListener {
            
            
            val topFragment = supportFragmentManager.fragments.lastOrNull() as? ScopedFragment
            topFragment?.openFragment(Selections.newInstance(), Selections.TAG)
        }
    }

    private fun startAudioDatabaseService() {
        
        if (applicationContext.isReadMediaAudioPermissionGranted()) {
            AudioDatabaseService.startScan(applicationContext)
        }

        isFirstLaunch = false
    }

    private fun runDatabaseScanner() {
        if (isFirstLaunch.not()) {
            if (LibraryPreferences.isScannerOnResumeEnabled()) {
                if (applicationContext.isReadMediaAudioPermissionGranted()) {
                    AudioDatabaseService.refreshScan(applicationContext)
                }
            }
        }
    }

    private fun setHomePanel() {
        val allPermissionsGranted = isPostNotificationsPermissionGranted() &&
                isReadMediaAudioPermissionGranted()

        when {
            !allPermissionsGranted -> {
                
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, Setup.newInstance(), Setup.TAG)
                    .commit()
            }
            TrialPreferences.isTrialExpired() -> {
                
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, TrialExpired.newInstance(), TrialExpired.TAG)
                    .commit()
            }
            else -> {
                
                showHome()
            }
        }
    }

     





    private fun openPlayerForCurrentPreference(): BasePlayerFragment {
        return when (UserInterfacePreferences.getPlayerInterface()) {
            UserInterfacePreferences.PLAYER_INTERFACE_FADED -> PlayerFaded.newInstance()
            else -> DefaultPlayer.newInstance()
        }
    }

    fun showHome() {
        when (UserInterfacePreferences.getHomeInterface()) {
            UserInterfacePreferences.HOME_INTERFACE_DASHBOARD -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, Dashboard.newInstance(), Dashboard.TAG)
                    .commit()
            }
            UserInterfacePreferences.HOME_INTERFACE_TILED -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, TiledHome.newInstance(), TiledHome.TAG)
                    .commit()
            }
            UserInterfacePreferences.HOME_INTERFACE_ARTFLOW -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ArtFlowHome.newInstance(), ArtFlowHome.TAG)
                    .commit()
            }
            UserInterfacePreferences.HOME_INTERFACE_SIMPLE -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, SimpleHome.newInstance(), SimpleHome.TAG)
                    .commit()
            }
        }
    }

     






    private fun updateActionBar(selected: List<Audio>) {
        val shouldShow = selected.isNotEmpty()
        if (shouldShow && !binding.miniPlayerActionBar.isVisible) {
            binding.miniPlayerActionBar.visibility = View.VISIBLE
            binding.miniPlayerActionBar.animate().alpha(1f).setDuration(200).start()
        } else if (!shouldShow && binding.miniPlayerActionBar.isVisible) {
            binding.miniPlayerActionBar.animate().alpha(0f).setDuration(160).withEndAction {
                binding.miniPlayerActionBar.visibility = View.GONE
            }.start()
        }
    }

     






    private fun deleteSelectedSongs() {
        val songsToDelete = SelectionManager.selectedAudios.value.toList()
        if (songsToDelete.isEmpty()) return

        SimpleDialog.Builder(
                container = binding.appContainer,
                inflateBinding = DialogDeleteSongBinding::inflate)
            .onViewCreated { dialogBinding ->
                
                
                val countLabel = getString(R.string.x_songs, songsToDelete.size)
                dialogBinding.deleteSummary.text = getString(R.string.delete_audio_summary, countLabel)
            }
            .onDialogInflated { dialogBinding, dismiss, _ ->
                dialogBinding.sure.setOnClickListener {
                    dismiss()
                    performBulkDelete(songsToDelete, deleteLyrics = dialogBinding.deleteLyricsCheckbox.isChecked)
                }
                dialogBinding.cancel.setOnClickListener { dismiss() }
            }
            .onDismiss {   }
            .build()
            .show()
    }

     











    private fun performBulkDelete(songs: List<Audio>, deleteLyrics: Boolean) {
        lifecycleScope.launch {
            
            
            songs.forEach { audio ->
                val queueIndex = MediaPlaybackManager.getSongs().indexOfFirst { it.id == audio.id }
                when {
                    queueIndex != -1 -> MediaPlaybackManager.removeQueueItemSilently(queueIndex)
                    MediaPlaybackManager.getCurrentSong()?.id == audio.id -> MediaPlaybackManager.next()
                }
            }

            
            withContext(Dispatchers.IO) {
                val db = AudioDatabase.getInstance(applicationContext)
                songs.forEach { audio ->
                    runCatching {
                        val audioUri = audio.uri.toUri()
                        val isMediaStore = audioUri.authority?.contains("media") == true

                        val deleted = if (isMediaStore) {
                            contentResolver.delete(audioUri, null, null) > 0
                        } else {
                            DocumentsContract.deleteDocument(contentResolver, audioUri)
                            true
                        }

                        if (deleted) {
                            db.audioDao()?.delete(audio)
                            if (deleteLyrics) {
                                LrcRepository.deleteSidecarsStatic(applicationContext, audio.uri)
                            }
                        }
                    }.onFailure { e ->
                        Log.e("MainActivity", "Failed to delete ${audio.title}: ${e.message}", e)
                    }
                }
            }

            
            SelectionManager.clear()
        }
    }

     



    private fun showSelectionMenu() {
        val selected = SelectionManager.selectedAudios.value
        SimpleDialog.Builder(
                container = binding.appContainer,
                inflateBinding = DialogSelectionMenuBinding::inflate)
            .onViewCreated { binding ->
                binding.selectionCount.setSelectedSongsSize(selected.size)
            }
            .onDialogInflated { dialogBinding, dismiss, _ ->
                dialogBinding.play.setOnClickListener {
                    MediaPlaybackManager.setSongs(selected, autoPlay = true)
                    createSongHistoryDatabase(selected)
                    dismiss()
                }
                dialogBinding.deleteAll.setOnClickListener {
                    dismiss()
                    
                    deleteSelectedSongs()
                }
                dialogBinding.shareAll.setOnClickListener {
                    shareSelectedAudios(selected)
                    dismiss()
                }
                
                dialogBinding.addAllToQueue.setOnClickListener {
                    selected.forEach { MediaPlaybackManager.addToQueue(it) }
                    SelectionManager.clear()
                    dismiss()
                }
                
                dialogBinding.addToPlaylist.setOnClickListener {
                    dismiss()
                    supportFragmentManager.showAddMultipleToPlaylistDialog(selected)
                }
                dialogBinding.clearSelection.setOnClickListener {
                    SelectionManager.clear()
                    dismiss()
                }
                
                dialogBinding.alwaysSkipAll.setOnClickListener {
                    lifecycleScope.launch {
                        audioRepository.setAlwaysSkipBatch(selected, skip = true)
                    }
                    SelectionManager.clear()
                    dismiss()
                }
                
                dialogBinding.neverSkipAll.setOnClickListener {
                    lifecycleScope.launch {
                        audioRepository.setAlwaysSkipBatch(selected, skip = false)
                    }
                    SelectionManager.clear()
                    dismiss()
                }
            }
            .onDismiss {   }
            .build()
            .show()
    }

     






    private fun shareSelectedAudios(songs: List<Audio>) {
        val uris = songs.map { it.uri.toUri() } as ArrayList<Uri>
        if (uris.isEmpty()) return

        val intent = ShareCompat.IntentBuilder(this)
            .setType("audio/*")
            .apply { uris.forEach { addStream(it) } }
            .createChooserIntent()
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(intent)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                openVolumeKnobDialog()
                true
            }

            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                openVolumeKnobDialog()
                true
            }

            else -> {
                super.onKeyDown(keyCode, event)
            }
        }
    }

    private fun openVolumeKnobDialog() {
        showVolumeKnob().setVolumeListener(object : VolumeKnob.Companion.VolumeListener {
            override fun onEqualizerClicked() {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, Equalizer.newInstance(), Equalizer.TAG)
                    .addToBackStack(Equalizer.TAG)
                    .commit()
            }
        })
    }

    override fun onHideMiniPlayer() {
        binding.miniPlayer.hide(animated = true)
    }

     






    override fun onStateReady() {
        if (MediaPlaybackManager.getSongs().isEmpty()) return
        val fragment = supportFragmentManager.fragments.lastOrNull { it.isVisible }
        val wantsVisible = (fragment as? MiniPlayerPolicy)?.wantsMiniPlayerVisible ?: true
        if (wantsVisible) {
            binding.miniPlayer.show(animated = true)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            TrialPreferences.HAS_LICENSE_KEY -> {

            }
        }
    }

    override fun onShowMiniPlayer() {
        if (MediaPlaybackManager.getSongs().isEmpty()) return
        if (supportFragmentManager.fragments.last() is MediaFragment) {
            val currentFragment = supportFragmentManager
                .fragments
                .lastOrNull { it.isVisible }

            val visible = (currentFragment as? MiniPlayerPolicy)?.wantsMiniPlayerVisible ?: true

            if (visible) {
                binding.miniPlayer.show(animated = true)
            }
        } else {
            binding.miniPlayer.show(animated = true)
        }
    }

    override fun onAttachMiniPlayer(recyclerView: RecyclerView?) {
        recyclerView?.let {
            binding.miniPlayer.attachToRecyclerView(it)
        }
    }

    override fun onDetachMiniPlayer(recyclerView: RecyclerView?) {
        Log.d("MainActivity", "Detaching mini player from RecyclerView")
        recyclerView?.let {
            binding.miniPlayer.detachFromRecyclerView(it)
        }
    }

    override fun onAttachMiniPlayerScrollView(scrollView: NestedScrollView?) {
        scrollView?.let {
            binding.miniPlayer.attachToScrollView(it)
        }
    }

    override fun onDetachMiniPlayerScrollView(scrollView: NestedScrollView?) {
        scrollView?.let {
            binding.miniPlayer.detachFromScrollView(it)
        }
    }

    override fun onMakeTransparentMiniPlayer() {
        binding.miniPlayer.makeTransparent(animated = true)
    }

    override fun onMakeOpaqueMiniPlayer() {
        binding.miniPlayer.makeOpaque(animated = true)
    }

    override fun onStart() {
        super.onStart()
        
        
        
        
        
    }

    override fun onResume() {
        super.onResume()
        
        
        
        lyricsManager.refreshIfNoLyrics()
        runDatabaseScanner()
    }

    override fun onStop() {
        super.onStop()
        savePlaybackState()
    }

    private fun savePlaybackState() {
        lifecycleScope.launch(Dispatchers.IO) {
            PlaybackStateManager.saveCurrentPlaybackState(applicationContext, "MainActivity")
        }
    }

    private fun TextView.setSelectedSongsSize(size: Int) {
        text = resources.getQuantityString(R.plurals.songs_selected, size, size)
    }

    private fun createSongHistoryDatabase(songs: List<Audio>) {
        val seek = MediaPlaybackManager.getSeekPosition()
        val shuffleEnabled = ShufflePreferences.isShuffleEnabled()

        
        
        val queueToSave = MediaPlaybackManager.getOriginalQueue().takeIf { it.isNotEmpty() } ?: songs
        val currentSong = MediaPlaybackManager.getCurrentSong()
        val idx = if (currentSong != null) {
            queueToSave.indexOfFirst { it.id == currentSong.id }.coerceAtLeast(0)
        } else {
            MediaPlaybackManager.getCurrentSongPosition()
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val audioDatabase = AudioDatabase.getInstance(baseContext)
            PlaybackStateManager.savePlaybackState(
                    db = audioDatabase,
                    queueHash = queueToSave.map { it.hash },
                    index = idx,
                    position = seek,
                    shuffle = shuffleEnabled,
                    repeat = 0
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleSearchIntent(intent)
    }

    private fun handleSearchIntent(intent: Intent) {
        if (intent.action == MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            Log.d("MainActivity", "Search query: $query")
        } else {
            Log.d("MainActivity", "Received non-search intent: ${intent.action}")
        }
    }
}