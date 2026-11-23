
open class BaseActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener, ThemeChangedListener {

    protected var mediaController: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private lateinit var content: FrameLayout

    private var predictiveBackCallback: OnBackInvokedCallback? = null

     
    private var paletteJob: Job? = null

     
    private var lastPaletteSongId: Long = -1L

    override fun attachBaseContext(newBase: Context?) {
        com.appinitdev.musicpro.manager.SharedPreferences.init(newBase!!)
        com.appinitdev.musicpro.manager.SharedPreferences.initEncrypted(newBase)
        registerSharedPreferenceChangeListener()
        registerEncryptedSharedPreferencesListener()
        super.attachBaseContext(  ContextUtils.updateLocale(
                baseContext = newBase,
                languageCode = ConfigurationPreferences.getAppLanguage()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            presetMaterialYouDynamicColors()
        }

        AppOrientation.setOrientation(BarHeight.isLandscape(this))
        content = findViewById(android.R.id.content)
        ThemeManager.addListener(this)
        initTheme()
        ThemeUtils.makeAppFullScreen(window)
        content.setBackgroundColor(ThemeManager.theme.viewGroupTheme.backgroundColor)

        initMediaController()
        setStrictModePolicy()
        enableNotchArea()
        makeAppFullScreen()
        applyPredictiveBackGesture()
        observeSongChangesForPalette()

        lifecycleScope.launch(Dispatchers.Default) {
            if (TrialPreferences.isFirstLaunchDateSet().not()) { 
                val firstInstallTime = packageManager.getPackageInfo(packageName, 0).firstInstallTime
                TrialPreferences.setFirstLaunchDate(firstInstallTime)
                Log.d(TAG, "First install time set as trial start date: ${firstInstallTime.toDate()}")
            }
        }

         


        LocaleUtils.setAppLocale(ConfigurationCompat.getLocales(resources.configuration)[0]!!)

         


        if (ConfigurationPreferences.isKeepScreenOn()) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

     






    private fun observeSongChangesForPalette() {
        lifecycleScope.launch {
            MediaPlaybackManager.songPositionFlow.collect {
                val isAlbumArtAccent = AppearancePreferences.getAccentColorName() == AlbumArt.IDENTIFIER
                val isAlbumArtTheme = ThemeUtils.isEffectiveAlbumArtTheme(resources)
                if (isAlbumArtAccent || isAlbumArtTheme) {
                    generateAlbumArtPalette()
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun initMediaController() {
        val sessionToken =
            SessionToken(this,
                         ComponentName(this, FelicityPlayerService::class.java))

        controllerFuture =
            MediaController.Builder(this, sessionToken).buildAsync()

        val listener = Runnable {
            Log.d(TAG, "MediaController created successfully")
            mediaController = controllerFuture?.get()
            MediaPlaybackManager.setMediaController(mediaController!!)

            
            
            mediaController?.addListener(serviceDisconnectListener)

            restoreLastSongStateFromDatabase()
            generateAlbumArtPalette()
        }

        controllerFuture?.addListener(listener, MoreExecutors.directExecutor())
    }

     





    private val serviceDisconnectListener = object : androidx.media3.common.Player.Listener {
        override fun onEvents(player: androidx.media3.common.Player, events: androidx.media3.common.Player.Events) {
            if (mediaController?.isConnected == false) {
                Log.w(TAG, "MediaController disconnected — service likely died. Clearing stale reference.")
                MediaPlaybackManager.clearMediaController()
                mediaController = null
            }
        }
    }

     






    fun ensureServiceRunning(onReady: (MediaController) -> Unit) {
        val current = mediaController
        if (current != null && current.isConnected) {
            onReady(current)
            return
        }

        Log.d(TAG, "Service not connected — restarting FelicityPlayerService…")
        initMediaController()

        
        controllerFuture?.addListener(Runnable {
            val fresh = controllerFuture?.get() ?: return@Runnable
            onReady(fresh)
        }, MoreExecutors.directExecutor())
    }

    private fun restoreLastSongStateFromDatabase() {
        
        
        
        if ((mediaController?.mediaItemCount ?: 0) > 0) {
            Log.d(TAG, "MediaController already has items, skipping DB restore (likely a rotation)")
            
            val currentIndex = mediaController?.currentMediaItemIndex ?: 0
            MediaPlaybackManager.notifyCurrentPosition(currentIndex)
            onStateReady()
            return
        }

        lifecycleScope.launch(Dispatchers.Default) {
            try {
                val audioDatabase = AudioDatabase.getInstance(applicationContext)
                val playbackState = PlaybackStateManager.fetchPlaybackState(audioDatabase)
                val lastSongs = PlaybackStateManager.getAudiosFromQueueIDs(audioDatabase)?.toList()

                Log.d(TAG, "Restoring playback state: index=${playbackState?.index}, position=${playbackState?.position}, queue size=${lastSongs?.size}")

                if (!lastSongs.isNullOrEmpty() && playbackState != null) {
                    withContext(Dispatchers.Main) {
                         













                        val restoredIndex = when {
                            playbackState.currentHash == 0L -> {
                                playbackState.index.coerceIn(0, lastSongs.size - 1)
                            }
                            else -> {
                                val byHash = lastSongs.indexOfFirst { it.hash == playbackState.currentHash }
                                if (byHash >= 0) {
                                    byHash
                                } else {
                                    Log.w(TAG, "Hash lookup failed for currentHash=${playbackState.currentHash} — stale state after DB migration, resetting to position 0")
                                    0
                                }
                            }
                        }

                        
                        
                        ShufflePreferences.setShuffleEnabled(playbackState.shuffle)

                        MediaPlaybackManager.setSongs(
                                audios = lastSongs,
                                position = restoredIndex,
                                startPositionMs = playbackState.position.coerceAtLeast(0L),
                        )
                        Log.d(TAG, "Playback state restored successfully (shuffle=${playbackState.shuffle})")
                        onStateReady()
                    }
                } else {
                    
                    
                    
                    Log.d(TAG, "No valid playback state found – waiting for first songs from DB scan")
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            val dao = audioDatabase.audioDao() ?: return@launch
                            
                            val firstSongs = dao.getAllAudio().first { it.isNotEmpty() }
                            withContext(Dispatchers.Main) {
                                if (MediaPlaybackManager.getSongs().isEmpty()) {
                                    MediaPlaybackManager.setSongs(
                                            audios = firstSongs,
                                            position = 0,
                                            startPositionMs = 0L,
                                    )
                                    Log.d(TAG, "Default queue loaded on first launch: ${firstSongs.size} songs")
                                    onStateReady()
                                } else {
                                    Log.d(TAG, "Queue already populated by the time scan finished – skipping default load")
                                    onStateReady()
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error waiting for first songs from DB", e)
                        }
                    }
                }
            } catch (e: NullPointerException) {
                Log.e(TAG, "Error restoring last song state: ${e.message}")
                e.printStackTrace()
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error restoring playback state", e)
                e.printStackTrace()
            }
        }
    }

     








    protected open fun onStateReady() = Unit

    protected fun generateAlbumArtPalette() {
        val isAlbumArtAccent = AppearancePreferences.getAccentColorName() == AlbumArt.IDENTIFIER
        val isAlbumArtTheme = ThemeUtils.isEffectiveAlbumArtTheme(resources)

        if (!isAlbumArtAccent && !isAlbumArtTheme) return

        val audio = MediaPlaybackManager.getCurrentSong()

        if (audio == null) {
            Log.w(TAG, "No current song for palette generation")

             






            if (isAlbumArtTheme) {
                if (ThemeUtils.isDarkMode(resources)) {
                    ThemeManager.theme = DarkTheme()
                } else {
                    ThemeManager.theme = LightTheme()
                }
            }

            if (isAlbumArtAccent) {
                ThemeManager.accent = Felicity()
            }

            return
        }

        
        if (audio.id == lastPaletteSongId) return

        
        paletteJob?.cancel()

        paletteJob = lifecycleScope.launch(Dispatchers.IO) {
            try {
                
                val rawBitmap: Bitmap = AudioCover.load(this@BaseActivity, audio) ?: return@launch

                
                
                val thumb: Bitmap = withContext(Dispatchers.Default) {
                    val size = 128
                    if (rawBitmap.width > size || rawBitmap.height > size) {
                        rawBitmap.scale(size, size, filter = false).also {
                            if (it !== rawBitmap) rawBitmap.recycle()
                        }
                    } else {
                        rawBitmap
                    }
                }

                
                val palette: MonetPalette = withContext(Dispatchers.Default) {
                    val p = MonetPalette(thumb)
                    thumb.recycle()
                    p
                }

                withContext(Dispatchers.Main) {
                    
                    val stillAlbumArtAccent = AppearancePreferences.getAccentColorName() == AlbumArt.IDENTIFIER
                    
                    
                    val albumArtThemeActive = ThemeUtils.isEffectiveAlbumArtTheme(resources)

                    if (!stillAlbumArtAccent && !albumArtThemeActive) return@withContext

                    lastPaletteSongId = audio.id

                    
                    
                    
                    if (albumArtThemeActive) {
                        AlbumArtData.populate(palette)
                        ThemeManager.theme = if (ThemeUtils.isEffectiveAlbumArtDark(resources)) {
                            AlbumArtDark()
                        } else {
                            AlbumArtLight()
                        }
                        Log.d(TAG, "Album art theme applied for song ${audio.id}")
                    }

                    
                    
                    
                    if (stillAlbumArtAccent) {
                        val albumArtAccent = AlbumArt().apply {
                            primaryAccentColor = palette.accent1_500
                            secondaryAccentColor = palette.accent1_300
                        }
                        ThemeManager.accent = albumArtAccent
                        Log.d(TAG, "Album art accent applied for song ${audio.id}: ${albumArtAccent.hexes}")
                    }
                }
            } catch (e: FileNotFoundException) {
                Log.w(TAG, "Album art not found for song ${audio.id}", e)
            } catch (e: Exception) {
                Log.e(TAG, "Error generating album art palette for song ${audio.id}", e)
            }
        }
    }

    private fun initTheme() {
        ThemeUtils.setAppTheme(resources)
        ThemeUtils.setBarColors(resources, window)
        ThemeUtils.updateNavAndStatusColors(resources, window)

        val isAlbumArtAccent = AppearancePreferences.getAccentColorName() == AlbumArt.IDENTIFIER
        val isAlbumArtTheme = ThemeUtils.isEffectiveAlbumArtTheme(resources)

        
        
        if (!isAlbumArtAccent) {
            ThemeManager.accent = when (val accentName = AppearancePreferences.getAccentColorName()) {
                null -> Felicity().also {
                    AppearancePreferences.setAccentColorName(it.identifier)
                }
                else -> ThemeManager.getAccentByName(accentName)
            }
        }

        
        
        if (isAlbumArtAccent || isAlbumArtTheme) {
            generateAlbumArtPalette()
        }
    }

    private fun makeAppFullScreen() {
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.navigationBarDividerColor = Color.TRANSPARENT
    }

    private fun enableNotchArea() {
        window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
    }

    private fun setStrictModePolicy() {
        StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .build())
    }

    private fun applyPredictiveBackGesture() {
        if (BehaviourPreferences.isPredictiveBackEnabled()) {
            enablePredictiveBack(this)
        } else {
            disablePredictiveBack(this) {
                
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun disablePredictiveBack(activity: Activity, onBack: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && predictiveBackCallback == null) {
            predictiveBackCallback = OnBackInvokedCallback {
                onBack()
            }
            activity.onBackInvokedDispatcher.registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_OVERLAY,
                    predictiveBackCallback!!
            )
        }
    }

    private fun enablePredictiveBack(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && predictiveBackCallback != null) {
            activity.onBackInvokedDispatcher.unregisterOnBackInvokedCallback(predictiveBackCallback!!)
            predictiveBackCallback = null
        }
    }

    protected fun withSureDialog(onResult: (Boolean) -> Unit) {
        SimpleDialog.Builder(
                container = content,
                inflateBinding = DialogSureBinding::inflate)
            .onViewCreated { binding ->
                 
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

    override fun onDestroy() {
        super.onDestroy()
        unregisterSharedPreferenceChangeListener()
        unregisterEncryptedSharedPreferencesListener()
        ThemeManager.removeListener(this)

        
        
        
        if (!isChangingConfigurations) {
            MediaPlaybackManager.stopSeekPositionUpdates()
            try {
                mediaController?.let {
                    MediaPlaybackManager.clearMediaController()
                    it.release()
                }
                MediaController.releaseFuture(controllerFuture!!)
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (AppearancePreferences.getTheme() == ThemeConstants.MATERIAL_YOU_DARK ||
                    AppearancePreferences.getTheme() == ThemeConstants.MATERIAL_YOU_LIGHT) {
                recreate()
            }
        }
        initTheme()
    }

    override fun onThemeChanged(theme: Theme, animate: Boolean) {
        ThemeUtils.onThemeChanged(resources, window)
        content.setBackgroundColor(ThemeManager.theme.viewGroupTheme.backgroundColor)
        window.setBackgroundDrawable(ThemeManager.theme.viewGroupTheme.backgroundColor.toDrawable())
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            AppearancePreferences.ACCENT_COLOR -> {
                initTheme()
            }
            AppearancePreferences.THEME -> {
                
                
                lastPaletteSongId = -1L
                initTheme()
            }
            BehaviourPreferences.PREDICTIVE_BACK -> {
                applyPredictiveBackGesture()
            }
        }
    }

    companion object {
        const val TAG = "BaseActivity"
    }
}
