
open class MediaFragment : ScopedFragment(), MiniPlayerPolicy {

    private var shouldShowMiniPlayer = true
    private var lastSavedSeekPosition = 0L

    private var shuffleButton: HighlightTextView? = null

     



    private var pendingArtistForImagePick: Artist? = null

     




    private val artistImagePickerLauncher: ActivityResultLauncher<PickVisualMediaRequest> =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            val artist = pendingArtistForImagePick ?: return@registerForActivityResult
            pendingArtistForImagePick = null
            if (uri == null) return@registerForActivityResult
            val artistName = artist.name ?: return@registerForActivityResult

            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val bitmap = BitmapUtils.decodeBitmapFromUri(uri, requireContext())
                    if (bitmap != null) {
                        ArtistCover.saveUserPickedImage(requireContext(), artistName, bitmap)
                        Log.d("MediaFragment", "Saved user-picked image for artist: $artistName")

                        withContext(Dispatchers.Main) {
                            showWarning(getString(R.string.done))
                        }
                    }
                } catch (e: Exception) {
                    Log.w("MediaFragment", "Failed to save picked image for artist: $artistName", e)
                }
            }
        }

    private val miniPlayerCallbacks: MiniPlayerCallbacks?
        get() = requireActivity() as? MiniPlayerCallbacks

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setShuffleButtonState()

        getShuffleButton()?.setOnClickListener {
             



            onShuffleClicked()
        }

        getShuffleButton()?.setOnLongClickListener {
            childFragmentManager.showShuffleDialog()
            true
        }

        viewLifecycleOwner.lifecycleScope.launch {
            MediaPlaybackManager.songSeekPositionFlow.collect { position ->
                onSeekChanged(position)

                
                val song = MediaPlaybackManager.getCurrentSong()
                if (song != null) {
                    val threshold = maxOf(5000L, song.duration / 20) 
                    if (abs(position - lastSavedSeekPosition) > threshold) {
                        lastSavedSeekPosition = position
                        saveCurrentPlaybackState()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            
            
            
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                MediaPlaybackManager.songPositionFlow.collect { position ->
                    Log.d(TAG, "Song position: $position")
                    MediaPlaybackManager.getCurrentSong()?.let { song ->
                        onAudio(song)
                    }
                    onPositionChanged(position)
                    
                    saveCurrentPlaybackState()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            MediaPlaybackManager.songListFlow.collect { songs ->
                Log.d(TAG, "Song list updated: ${songs.size} songs")
                onSongListChanged(songs)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            MediaPlaybackManager.playbackStateFlow.collect { state ->
                onPlaybackStateChanged(state)
            }
        }
    }

    protected fun setMediaItems(songs: List<Audio>, position: Int = 0) {
        
        
        if (SelectionManager.hasSelection) {
            val audio = songs.getOrNull(position) ?: return
            SelectionManager.toggle(audio)
            return
        }

        val currentSong = MediaPlaybackManager.getCurrentSong()
        val requestedSong = songs.getOrNull(position)
        val isSameQueue = MediaPlaybackManager.isSameQueue(songs)
        val isSameSong = currentSong != null && requestedSong != null && currentSong.id == requestedSong.id

        when {
            isSameQueue && isSameSong -> {
                
                openDefaultPlayer().also {
                     




                    MediaPlaybackManager.startPlayingIfPaused()
                }
            }
            isSameQueue && !isSameSong -> {
                
                MediaPlaybackManager.updatePosition(position, forcePlay = true)
            }
            isSameSong -> {
                
                updateQueueSilently(songs, position)
            }
            else -> {
                
                MediaPlaybackManager.setSongs(songs, position, autoPlay = true)
                createSongHistoryDatabase(songs)
            }
        }

         




        showMiniPlayer()
    }

     






    protected fun setMediaItemWithSeek(audio: Audio, startPositionMs: Long) {
        MediaPlaybackManager.setSongs(listOf(audio), 0, startPositionMs = startPositionMs, autoPlay = true)
        createSongHistoryDatabase(listOf(audio))
        showMiniPlayer()
    }

     



    protected fun shuffleMediaItems(songs: List<Audio>) {
        val shuffled = smartShuffle(songs, { it.artist ?: "" }).toMutableList()
        MediaPlaybackManager.setSongs(shuffled, 0, autoPlay = true)
        createSongHistoryDatabase(shuffled)
    }

    protected fun openDefaultPlayer() {
        val player = when (UserInterfacePreferences.getPlayerInterface()) {
            UserInterfacePreferences.PLAYER_INTERFACE_FADED -> PlayerFaded.newInstance()
            else -> DefaultPlayer.newInstance()
        }
        openFragment(player, BasePlayerFragment.TAG)
    }

    private fun updateQueueSilently(songs: List<Audio>, position: Int) {
        MediaPlaybackManager.updateQueueSilently(songs, position)
        createSongHistoryDatabase(songs)
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

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val audioDatabase = AudioDatabase.getInstance(requireContext())
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

    private fun saveCurrentPlaybackState() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            PlaybackStateManager.saveCurrentPlaybackState(requireContext(), TAG)
        }
    }

    protected fun requireHiddenMiniPlayer() {
        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                super.onCreate(owner)
                shouldShowMiniPlayer = false
                hideMiniPlayer()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                
                if (requireActivity().isChangingConfigurations.not()) {
                    shouldShowMiniPlayer = true
                    showMiniPlayer()
                }
            }
        })
    }

    protected fun peekMiniPlayer() {
        
        showMiniPlayer()
        viewLifecycleOwner.lifecycleScope.launch {
            delay(2000) 

            if (wantsMiniPlayerVisible.not()) {
                hideMiniPlayer()
            }
        }
    }

    protected fun RecyclerView.requireAttachedMiniPlayer() {
        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
                miniPlayerCallbacks?.onAttachMiniPlayer(this@requireAttachedMiniPlayer)
            }

            override fun onPause(owner: LifecycleOwner) {
                super.onPause(owner)
                miniPlayerCallbacks?.onDetachMiniPlayer(this@requireAttachedMiniPlayer)
            }
        })
    }

     







    protected fun NestedScrollView.requireAttachedMiniPlayer() {
        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                super.onCreate(owner)
                miniPlayerCallbacks?.onAttachMiniPlayerScrollView(this@requireAttachedMiniPlayer)
            }

            override fun onPause(owner: LifecycleOwner) {
                super.onPause(owner)
                miniPlayerCallbacks?.onDetachMiniPlayerScrollView(this@requireAttachedMiniPlayer)
            }
        })
    }

    protected fun showMiniPlayer() {
        miniPlayerCallbacks?.onShowMiniPlayer()
    }

    protected fun hideMiniPlayer() {
        miniPlayerCallbacks?.onHideMiniPlayer()
    }

     





    protected fun requireTransparentMiniPlayer() {
        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                super.onCreate(owner)
                miniPlayerCallbacks?.onMakeTransparentMiniPlayer()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                if (requireActivity().isChangingConfigurations.not()) {
                    miniPlayerCallbacks?.onMakeOpaqueMiniPlayer()
                }
            }
        })
    }

    open fun onPlaybackStateChanged(state: Int) {
        Log.d(TAG, "Playback state changed: $state")
    }

    open fun onAudio(audio: Audio) {
        Log.d(TAG, "New song played: ${audio.title} by ${audio.artist}")
    }

    open fun onPositionChanged(position: Int) {
        Log.d(TAG, "Position changed: $position")
    }

    open fun onSeekChanged(seek: Long) {
         
    }

     





    open fun onSongListChanged(songs: List<Audio>) {
         
    }

     




    protected open fun onSongMenuBookmarksClicked(audio: Audio) {
         
    }

    protected fun openSongsMenu(
            audios: List<Audio>,
            position: Int,
            imageView: ImageView?,
            showBookmarks: Boolean = false,
            onDismiss: (() -> Unit)? = null,
            onDismissStart: (() -> Unit)? = null) {

        try {
            val audio = audios[position]

            val onViewCreated: (DialogSongMenuBinding) -> Unit = { binding ->
                miniPlayerCallbacks?.onHideMiniPlayer()
                binding.title.text = audio.getProperTitle()
                binding.title.addAudioQualityIcon(audio)
                binding.secondaryDetail.text = audio.getProperArtists()
                binding.tertiaryDetail.text = audio.getProperAlbum()

                if (imageView.isNull()) {
                    binding.cover.loadArtCoverWithPayload(audio)
                }

                val isCurrentlyPlaying = MediaPlaybackManager.getCurrentSong()?.id == audio.id

                if (isCurrentlyPlaying) {
                    binding.play.gone(animate = false)
                    binding.insertAndPlay.gone(animate = false)
                    binding.addToQueue.gone(animate = false)
                    binding.playNext.gone(animate = false)
                    
                    
                    binding.bookmarks.visible(animate = false)
                }

                if (audio.artist.isNullOrBlank()) binding.goToArtist.gone(animate = false)
                if (audio.album.isNullOrBlank()) binding.goToAlbum.gone(animate = false)
                if (audio.isFavorite) binding.addToFavorites.text = getString(R.string.remove_from_favorites)
                if (audio.isAlwaysSkip) binding.alwaysSkip.text = getString(R.string.never_skip)

                
                
                if (SelectionManager.isSelected(audio)) {
                    binding.addToSelection.text = getString(R.string.remove_from_selection)
                    binding.addToSelection.setCompoundDrawablesWithIntrinsicBounds(
                            AppCompatResources.getDrawable(requireContext(), R.drawable.ic_cross_16dp),
                            null, null, null)
                }

                
                if (UserInterfacePreferences.isLikeIconInsteadOfThumb()) {
                    if (audio.isFavorite) {
                        binding.addToFavorites.setStartDrawable(R.drawable.ic_thumb_up_16dp)
                    } else {
                        binding.addToFavorites.setStartDrawable(R.drawable.ic_thumb_up_off_16dp)
                    }
                } else {
                    if (audio.isFavorite) {
                        binding.addToFavorites.setStartDrawable(R.drawable.ic_favorite_filled_16dp)
                    } else {
                        binding.addToFavorites.setStartDrawable(R.drawable.ic_favorite_border_16dp)
                    }
                }

                
                if (showBookmarks) {
                    binding.bookmarks.visible(false)
                } else {
                    binding.bookmarks.gone(animate = false)
                }
            }

            val onDialogInflated: (DialogSongMenuBinding, () -> Unit, () -> Unit) -> Unit = { binding, dismiss, dismissImmediately ->
                binding.play.setOnClickListener {
                    val pos = audios.indexOfFirst { it.id == audio.id }.coerceAtLeast(0)
                    setMediaItems(audios, pos)
                    dismiss()
                }

                binding.addToQueue.setOnClickListener {
                    MediaPlaybackManager.addToQueue(audio)
                    dismiss()
                }

                binding.playNext.setOnClickListener {
                    MediaPlaybackManager.playNext(audio)
                    dismiss()
                }

                binding.insertAndPlay.setOnClickListener {
                    MediaPlaybackManager.insertAndPlay(audio)
                    openDefaultPlayer()
                    dismiss()
                }

                binding.addToPlaylist.setOnClickListener {
                    parentFragmentManager.showAddToPlaylistDialog(audio)
                    dismiss()
                }

                binding.goToArtist.setOnClickListener {
                    val artistName = audio.artist ?: return@setOnClickListener
                    val artist = Artist(
                            id = artistName.hashCode().toLong(),
                            name = artistName,
                            albumCount = 0,
                            trackCount = 0
                    )
                    openFragment(ArtistPage.newInstance(artist), ArtistPage.TAG)
                    dismissImmediately()
                }

                binding.goToAlbum.setOnClickListener {
                    val albumName = audio.album ?: return@setOnClickListener
                    val artistName = audio.artist ?: ""
                    val album = Album(
                            id = audio.albumId,
                            name = albumName,
                            artist = artistName,
                            artistId = artistName.hashCode().toLong()
                    )
                    openFragment(AlbumPage.newInstance(album), AlbumPage.TAG)
                    dismissImmediately()
                }

                binding.addToFavorites.setOnClickListener {
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                        val newFav = !audio.isFavorite
                        AudioDatabase.getInstance(requireContext()).audioDao()?.setFavorite(audio.id, newFav)
                        audio.isFavorite = newFav
                        if (MediaPlaybackManager.getCurrentSong()?.id == audio.id) {
                            withContext(Dispatchers.Main) { MediaPlaybackManager.notifyCurrentSongUpdated() }
                        }
                    }
                    dismiss()
                }

                binding.alwaysSkip.setOnClickListener {
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                        val newSkip = !audio.isAlwaysSkip
                        AudioDatabase.getInstance(requireContext()).audioDao()?.setAlwaysSkip(audio.id, newSkip)
                        audio.setAlwaysSkip(newSkip)
                        if (newSkip && MediaPlaybackManager.getCurrentSong()?.id == audio.id) {
                            withContext(Dispatchers.Main) { MediaPlaybackManager.next() }
                        }
                    }
                    dismiss()
                }

                binding.share.setOnClickListener {
                    
                    
                    val audioUri = audio.uri.toUri()
                    ShareCompat.IntentBuilder(requireContext())
                        .setType(audio.mimeType ?: "audio/*")
                        .setStream(audioUri)
                        .startChooser()
                    dismiss()
                }

                binding.delete.setOnClickListener {
                    dismiss()
                    showAudioDeleteConfirmation(audio) { confirmed, lyrics ->
                        if (confirmed) {
                            deleteSong(audio, lyrics)
                        } else {
                            openSongsMenu(audios, position, imageView)
                        }
                    }
                }

                binding.lyrics.setOnClickListener {
                    childFragmentManager.showLyrics(audio).also { dismiss() }
                }

                binding.info.setOnClickListener {
                    childFragmentManager.showAudioInfo(audio).also { dismiss() }
                }

                binding.playbackInfo.setOnClickListener {
                    childFragmentManager.showPlaybackInfo(audio).also { dismiss() }
                }

                binding.editMetadata.setOnClickListener {
                    openFragment(MetadataEditor.newInstance(audio), MetadataEditor.TAG)
                    dismissImmediately()
                }

                
                
                binding.addToSelection.setOnClickListener {
                    SelectionManager.toggle(audio)
                    dismiss()
                }

                if (showBookmarks) {
                    binding.bookmarks.setOnClickListener {
                        dismiss()
                        onSongMenuBookmarksClicked(audio)
                    }
                }
            }

            val onDismissStart: (() -> Unit) = {
                miniPlayerCallbacks?.onShowMiniPlayer()
                onDismissStart?.invoke()
            }

            val onDismissCallback: () -> Unit = {
                onDismiss?.invoke()
            }

            if (imageView != null) {
                SimpleSharedImageDialog.Builder(
                        container = requireContainerView(),
                        sourceImageView = imageView,
                        inflateBinding = DialogSongMenuBinding::inflate,
                        targetImageViewProvider = { it.cover })
                    .onViewCreated(onViewCreated)
                    .onDialogInflated(onDialogInflated)
                    .onDismiss(onDismissCallback)
                    .onDismissStart(onDismissStart)
                    .setWidthRatio(getDialogWidthRation())
                    .build()
                    .show()
            } else {
                SimpleDialog.Builder(
                        container = requireContainerView(),
                        inflateBinding = DialogSongMenuBinding::inflate)
                    .onViewCreated(onViewCreated)
                    .onDialogInflated(onDialogInflated)
                    .onDismiss(onDismissCallback)
                    .onDismissStart(onDismissStart)
                    .setWidthRatio(getDialogWidthRation())
                    .build()
                    .show()
            }
        } catch (e: IndexOutOfBoundsException) {
            Log.e(TAG, "Error opening song menu: position $position " +
                    "is out of bounds for list of ${audios.size} songs", e)
        }
    }

     




    protected fun toggleFavorite() {
        val audio = MediaPlaybackManager.getCurrentSong() ?: return
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val newFavorite = !audio.isFavorite
            AudioDatabase.getInstance(requireContext()).audioDao()?.setFavorite(audio.id, newFavorite)
            audio.isFavorite = newFavorite
            withContext(Dispatchers.Main) {
                MediaPlaybackManager.notifyCurrentSongUpdated()
            }
        }
    }

    protected fun showAudioDeleteConfirmation(audio: Audio, onResult: (Boolean, Boolean) -> Unit) {
        SimpleDialog.Builder(
                container = requireContainerView(),
                inflateBinding = DialogDeleteSongBinding::inflate)
            .onViewCreated { binding ->
                
                if (MediaPlaybackManager.getCurrentSong()?.id == audio.id) {
                    MediaPlaybackManager.duck()
                }

                val title = audio.getProperTitle()
                val fullText = getString(R.string.delete_audio_summary, title)

                val startIndex = fullText.indexOf(title ?: "")
                val spannable = SpannableString(fullText)

                if (startIndex >= 0) {
                    val endIndex = startIndex + title.length

                    spannable.setSpan(
                            StyleSpan(Typeface.BOLD),
                            startIndex,
                            endIndex,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    val color = ThemeManager.theme.textViewTheme.primaryTextColor
                    spannable.setSpan(
                            ForegroundColorSpan(color),
                            startIndex,
                            endIndex,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                binding.deleteSummary.text = spannable
            }.onDialogInflated { binding, dismiss, _ ->
                binding.sure.setOnClickListener {
                    onResult(true, binding.deleteLyricsCheckbox.isChecked)
                    dismiss()
                }

                binding.cancel.setOnClickListener {
                    onResult(false, false)
                    dismiss()
                }
            }
            .onDismiss {
                MediaPlaybackManager.unduck() 
            }
            .build()
            .show()
    }

     





    protected fun withSureDialog(onResult: (Boolean) -> Unit) {
        SimpleDialog.Builder(
                container = requireContainerView(),
                inflateBinding = DialogSureBinding::inflate)
            .onViewCreated { _ ->
                 
            }.onDialogInflated { binding, dismiss, _ ->
                binding.sure.setOnClickListener {
                    onResult(true)
                    dismiss()
                }

                binding.cancel.setOnClickListener {
                    onResult(false)
                    dismiss()
                }
            }
            .onDismiss {
                 
            }
            .build()
            .show()
    }

    private fun deleteSong(audio: Audio, lyrics: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                
                
                withContext(Dispatchers.Main) {
                    val queueIndex = MediaPlaybackManager.getSongs().indexOfFirst { it.id == audio.id }
                    when {
                        queueIndex != -1 -> MediaPlaybackManager.removeQueueItemSilently(queueIndex)
                        MediaPlaybackManager.getCurrentSong()?.id == audio.id -> MediaPlaybackManager.next()
                    }
                }

                
                val audioUri = audio.uri.toUri()
                val isMediaStore = audioUri.authority?.contains("media") == true

                val deleted = try {
                    if (isMediaStore) {
                        
                        requireContext().contentResolver.delete(audioUri, null, null) > 0
                    } else {
                        
                        DocumentsContract.deleteDocument(requireContext().contentResolver, audioUri)
                        true
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Delete failed for URI: $audioUri", e)
                    false
                }

                if (deleted) {
                    
                    AudioDatabase.getInstance(requireContext()).audioDao()?.delete(audio)
                    Log.d(TAG, "Song deleted successfully: ${audio.title}")

                    if (lyrics) {
                        
                        LrcRepository.deleteSidecarsStatic(requireContext(), audio.uri)
                    }
                } else {
                    Log.e(TAG, "DocumentsContract could not delete: ${audio.uri}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting song: ${e.message}", e)
            }
        }
    }

    fun openMilkdropPanel() {
        openFragment(Milkdrop.newInstance(), Milkdrop.TAG)
    }

     










    protected fun openPlaylistMenu(item: PlaylistWithSongs, imageView: ImageView?) {
        val onViewCreated: (DialogPlaylistMenuBinding) -> Unit = { binding ->
            miniPlayerCallbacks?.onHideMiniPlayer()
            binding.title.text = item.playlist.name
            binding.secondaryDetail.text = getString(R.string.x_songs, item.songs.size)
            binding.tertiaryDetail.text = item.playlist.description.orEmpty()
            if (imageView == null) {
                item.songs.firstOrNull()?.let { binding.cover.loadArtCoverWithPayload(it) }
            }
        }

        val onDialogInflated: (DialogPlaylistMenuBinding, () -> Unit, () -> Unit) -> Unit = { binding, dismiss, _ ->
            binding.play.setOnClickListener {
                if (item.songs.isNotEmpty()) setMediaItems(item.songs.toMutableList(), 0)
                dismiss()
            }

            binding.shuffle.setOnClickListener {
                if (item.songs.isNotEmpty()) shuffleMediaItems(item.songs)
                dismiss()
            }

            binding.addAllToQueue.setOnClickListener {
                item.songs.forEach { MediaPlaybackManager.addToQueue(it) }
                dismiss()
            }

            binding.share.setOnClickListener {
                dismiss()
                sharePlaylistSongs(item)
            }

            binding.edit.setOnClickListener {
                dismiss()
                showEditPlaylistDialog(item)
            }

            binding.delete.setOnClickListener {
                dismiss()
                withSureDialog { confirmed ->
                    if (confirmed) deletePlaylist(item)
                }
            }
        }

        val onDismissCallback: () -> Unit = {
            miniPlayerCallbacks?.onShowMiniPlayer()
        }

        if (imageView != null) {
            SimpleSharedImageDialog.Builder(
                    container = requireContainerView(),
                    sourceImageView = imageView,
                    inflateBinding = DialogPlaylistMenuBinding::inflate,
                    targetImageViewProvider = { it.cover })
                .onViewCreated(onViewCreated)
                .onDialogInflated(onDialogInflated)
                .onDismiss(onDismissCallback)
                .setWidthRatio(getDialogWidthRation())
                .build()
                .show()
        } else {
            SimpleDialog.Builder(
                    container = requireContainerView(),
                    inflateBinding = DialogPlaylistMenuBinding::inflate)
                .onViewCreated(onViewCreated)
                .onDialogInflated(onDialogInflated)
                .onDismiss(onDismissCallback)
                .setWidthRatio(getDialogWidthRation())
                .build()
                .show()
        }
    }

     






    private fun sharePlaylistSongs(item: PlaylistWithSongs) {
        val uris = item.songs.map { it.uri.toUri() } as ArrayList<Uri>
        if (uris.isEmpty()) return
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "audio/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.send)))
    }

     





    private fun showEditPlaylistDialog(item: PlaylistWithSongs) {
        SimpleDialog.Builder(
                container = requireContainerView(),
                inflateBinding = DialogEditPlaylistBinding::inflate)
            .onViewCreated { binding ->
                binding.playlistNameInput.setText(item.playlist.name)
                binding.playlistNameInput.requestFocus()
                val description = item.playlist.description
                if (!description.isNullOrEmpty()) {
                    binding.playlistDescriptionInput.setText(description)
                }
            }
            .onDialogInflated { binding, dismiss, _ ->
                binding.cancel.setOnClickListener { dismiss() }

                binding.save.setOnClickListener {
                    val newName = binding.playlistNameInput.text?.toString()?.trim()
                    if (newName.isNullOrEmpty()) return@setOnClickListener
                    val newDescription = binding.playlistDescriptionInput.text
                        ?.toString()?.trim()
                        .takeUnless { it.isNullOrEmpty() }
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                        val updated = item.playlist.copy(
                                name = newName,
                                description = newDescription,
                                dateModified = System.currentTimeMillis()
                        )
                        AudioDatabase.getInstance(requireContext()).playlistDao().updatePlaylist(updated)
                    }
                    dismiss()
                }
            }
            .setWidthRatio(getDialogWidthRation())
            .onDismiss {   }
            .build()
            .show()
    }

     





    private fun deletePlaylist(item: PlaylistWithSongs) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            AudioDatabase.getInstance(requireContext()).playlistDao().deletePlaylist(item.playlist)
            Log.d(TAG, "Playlist deleted: ${item.playlist.name}")
        }
    }

     













    protected fun openAlbumMenu(album: Album, imageView: ImageView?) {
        val onViewCreated: (DialogAlbumMenuBinding) -> Unit = { binding ->
            miniPlayerCallbacks?.onHideMiniPlayer()
            binding.title.text = album.name
            binding.secondaryDetail.text = album.artist
            binding.tertiaryDetail.text = getString(R.string.x_songs, album.songCount)
            if (imageView == null) {
                binding.cover.loadArtCoverWithPayload(album)
            }
        }

        val onDialogInflated: (DialogAlbumMenuBinding, () -> Unit, () -> Unit) -> Unit = { binding, dismiss, dismissImmediately ->
            binding.play.setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    val songs = fetchAudiosByPaths(album.songPaths)
                    if (songs.isNotEmpty()) setMediaItems(songs.toMutableList(), 0)
                }
                dismiss()
            }

            binding.shuffle.setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    val songs = fetchAudiosByPaths(album.songPaths)
                    if (songs.isNotEmpty()) shuffleMediaItems(songs)
                }
                dismiss()
            }

            binding.addAllToQueue.setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    val songs = fetchAudiosByPaths(album.songPaths)
                    songs.forEach { MediaPlaybackManager.addToQueue(it) }
                }
                dismiss()
            }

            binding.goToArtist.setOnClickListener {
                val artistName = album.artist ?: return@setOnClickListener
                val artist = Artist(
                        id = album.artistId,
                        name = artistName,
                        albumCount = 0,
                        trackCount = album.songCount
                )
                openFragment(ArtistPage.newInstance(artist), ArtistPage.TAG)
                dismissImmediately()
            }

            binding.share.setOnClickListener {
                dismiss()
                viewLifecycleOwner.lifecycleScope.launch {
                    val songs = fetchAudiosByPaths(album.songPaths)
                    if (songs.isNotEmpty()) shareAudioList(songs)
                }
            }
        }

        val onDismissCallback: () -> Unit = {
            miniPlayerCallbacks?.onShowMiniPlayer()
        }

        if (imageView != null) {
            SimpleSharedImageDialog.Builder(
                    container = requireContainerView(),
                    sourceImageView = imageView,
                    inflateBinding = DialogAlbumMenuBinding::inflate,
                    targetImageViewProvider = { it.cover })
                .onViewCreated(onViewCreated)
                .onDialogInflated(onDialogInflated)
                .onDismiss(onDismissCallback)
                .setWidthRatio(getDialogWidthRation())
                .build()
                .show()
        } else {
            SimpleDialog.Builder(
                    container = requireContainerView(),
                    inflateBinding = DialogAlbumMenuBinding::inflate)
                .onViewCreated(onViewCreated)
                .onDialogInflated(onDialogInflated)
                .onDismiss(onDismissCallback)
                .setWidthRatio(getDialogWidthRation())
                .build()
                .show()
        }
    }

     









    protected fun openArtistMenu(artist: Artist, imageView: ImageView?) {
        val onViewCreated: (DialogArtistMenuBinding) -> Unit = { binding ->
            miniPlayerCallbacks?.onHideMiniPlayer()
            binding.title.text = artist.name
            binding.secondaryDetail.text = getString(R.string.x_albums, artist.albumCount)
            binding.tertiaryDetail.text = getString(R.string.x_songs, artist.trackCount)
            if (imageView == null) {
                binding.cover.loadArtCoverWithPayload(artist)
            }
        }

        val onDialogInflated: (DialogArtistMenuBinding, () -> Unit, () -> Unit) -> Unit = { binding, dismiss, _ ->
            binding.play.setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    val songs = fetchAudiosByPaths(artist.songPaths)
                    if (songs.isNotEmpty()) setMediaItems(songs.toMutableList(), 0)
                }
                dismiss()
            }

            binding.shuffle.setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    val songs = fetchAudiosByPaths(artist.songPaths)
                    if (songs.isNotEmpty()) shuffleMediaItems(songs)
                }
                dismiss()
            }

            binding.addAllToQueue.setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    val songs = fetchAudiosByPaths(artist.songPaths)
                    songs.forEach { MediaPlaybackManager.addToQueue(it) }
                }
                dismiss()
            }

            binding.share.setOnClickListener {
                dismiss()
                viewLifecycleOwner.lifecycleScope.launch {
                    val songs = fetchAudiosByPaths(artist.songPaths)
                    if (songs.isNotEmpty()) shareAudioList(songs)
                }
            }

            binding.pickArtistImage.setOnClickListener {
                
                pendingArtistForImagePick = artist
                artistImagePickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
                dismiss()
            }
        }

        val onDismissCallback: () -> Unit = {
            miniPlayerCallbacks?.onShowMiniPlayer()
        }

        if (imageView != null) {
            SimpleSharedImageDialog.Builder(
                    container = requireContainerView(),
                    sourceImageView = imageView,
                    inflateBinding = DialogArtistMenuBinding::inflate,
                    targetImageViewProvider = { it.cover })
                .onViewCreated(onViewCreated)
                .onDialogInflated(onDialogInflated)
                .onDismiss(onDismissCallback)
                .setWidthRatio(getDialogWidthRation())
                .build()
                .show()
        } else {
            SimpleDialog.Builder(
                    container = requireContainerView(),
                    inflateBinding = DialogArtistMenuBinding::inflate)
                .onViewCreated(onViewCreated)
                .onDialogInflated(onDialogInflated)
                .onDismiss(onDismissCallback)
                .setWidthRatio(getDialogWidthRation())
                .build()
                .show()
        }
    }

     






    private suspend fun fetchAudiosByPaths(paths: List<String>): List<Audio> {
        return withContext(Dispatchers.IO) {
            val dao = AudioDatabase.getInstance(requireContext()).audioDao()
                ?: return@withContext emptyList<Audio>()
            paths.mapNotNull { path -> dao.getAudioByPath(path) }
        }
    }

     






    protected fun shareAudioList(audios: List<Audio>) {
        val uris = audios.map { it.uri.toUri() } as ArrayList<Uri>
        if (uris.isEmpty()) return
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "audio/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.send)))
    }

    private fun setShuffleButtonState() {
        val shuffleButton = getShuffleButton() ?: return
        shuffleButton.setUseAccentColor(ShufflePreferences.isShuffleEnabled())

        if (ShufflePreferences.isShuffleEnabled()) {

        } else {

        }
    }

    protected open fun getShuffleButton(): HighlightTextView? = null
    protected open fun onShuffleClicked() {}

    override val wantsMiniPlayerVisible: Boolean
        get() = shouldShowMiniPlayer

     








    override fun onCancelPredictiveBack() {
        super.onCancelPredictiveBack()
        if (shouldShowMiniPlayer) {
            showMiniPlayer()
        } else {
            hideMiniPlayer()
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            ShufflePreferences.SHUFFLE -> {
                setShuffleButtonState()
            }
        }
    }

     









    protected fun openBookmarksList(
            bookmarks: List<AudioBookmark>,
            onTimestampClicked: (AudioBookmark, dismiss: () -> Unit) -> Unit,
            onDelete: ((AudioBookmark) -> Unit)? = null
    ) {
        val onDialogInflated: (DialogBookmarksListBinding, () -> Unit, () -> Unit) -> Unit = { binding, dismiss, _ ->
            if (bookmarks.isEmpty()) {
                binding.bookmarksRecycler.gone()
                binding.bookmarksEmpty.visible()
            } else {
                binding.bookmarksEmpty.gone()
                binding.bookmarksRecycler.visible()

                val adapter = AdapterBookmarkTimestamps(
                        items = bookmarks.toMutableList(),
                        showDeleteButton = onDelete != null,
                        onTimestampClicked = { bookmark ->
                            onTimestampClicked(bookmark, dismiss)
                        },
                        onDeleteClicked = { bookmark, isEmpty ->
                            onDelete?.invoke(bookmark)
                            if (isEmpty) {
                                binding.bookmarksEmpty.visible()
                                binding.bookmarksRecycler.gone()
                            }
                        }
                )

                binding.bookmarksRecycler.layoutManager = LinearLayoutManager(requireContext())
                binding.bookmarksRecycler.adapter = adapter
            }
        }

        SimpleDialog.Builder(
                container = requireContainerView(),
                inflateBinding = DialogBookmarksListBinding::inflate)
            .onDialogInflated(onDialogInflated)
            .onDismiss {   }
            .setWidthRatio(getDialogWidthRation())
            .build()
            .show()
    }

    companion object {
        private const val TAG = "MediaFragment"

        const val SEEK_PER_PULSE_MS = 2500L
        private const val SEEK_INTERVAL_MS = 500L
    }
}