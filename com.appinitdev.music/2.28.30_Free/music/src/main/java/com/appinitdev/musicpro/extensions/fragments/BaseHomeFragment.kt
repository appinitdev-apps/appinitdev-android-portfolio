
/**
 * Base fragment shared by all home screen variants.
 *
 * Provides a single implementation of:
 *  - Wi-Fi server toggle button setup and state synchronization.
 *  - Panel navigation routing for every panel available in the app.
 *
 * Subclasses should call [setupServerToggle] in [onViewCreated] to activate the
 * server toggle button, and [navigateToPanel] inside adapter callbacks to open
 * the correct panel fragment.
 *
 * @author Hamza417
 */
abstract class BaseHomeFragment : PanelFragment() {

    /**
     * Attaches a click listener and a lifecycle-aware state observer to [button].
     *
     * Tapping [button] starts or stops the Wi-Fi server via [ServerModeService].
     * The button's tint tracks the server's running state for the lifetime of
     * the current fragment view — no manual observation is required in subclasses.
     *
     * @param button The image button that triggers the server toggle action.
     */
    protected fun setupServerToggle(button: DynamicRippleImageButton) {
        button.setOnClickListener {
            if (ServerModeService.isRunning.value) {
                ServerModeService.stop(requireContext())
            } else {
                try {
                    ServerModeService.start(requireContext())
                } catch (e: Exception) {
                    // Starting the server can fail for various reasons (e.g. port in use, missing permissions).
                    // Just show a toast and keep the toggle off if that happens — no need to crash the app.
                    e.printStackTrace()
                    showWarning("failed to start server: ${e.message ?: "unknown error"}")
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                ServerModeService.isRunning.collect { running ->
                    applyServerToggleTint(button, running)
                }
            }
        }
    }

    protected fun toggleServer() {
        if (ServerModeService.isRunning.value) {
            ServerModeService.stop(requireContext())
        } else {
            ServerModeService.start(requireContext())
        }
    }

    /**
     * Applies the accent tint when the server is running, or clears it when stopped.
     *
     * @param button  The button whose tint to update.
     * @param running `true` if the HTTP server is currently active.
     */
    private fun applyServerToggleTint(button: DynamicRippleImageButton, running: Boolean) {
        button.imageTintList = if (running) {
            ThemeManager.accent.primaryAccentColor.toColorStateList()
        } else {
            ThemeManager.theme.iconTheme.regularIconColor.toColorStateList()
        }
    }

    /**
     * Opens the panel fragment that corresponds to [Panel.titleResId].
     * Covers every panel exposed in [SimpleHomeViewModel] and [DashboardViewModel].
     *
     * @param panel The [Panel] item the user tapped.
     */
    protected fun navigateToPanel(panel: Panel) {
        when (panel.titleResId) {
            R.string.songs -> openFragment(Songs.newInstance(), Songs.TAG)
            R.string.albums -> openFragment(Albums.newInstance(), Albums.TAG)
            R.string.artists -> openFragment(Artists.newInstance(), Artists.TAG)
            R.string.genres -> openFragment(Genres.newInstance(), Genres.TAG)
            R.string.favorites -> openFragment(Favorites.newInstance(), Favorites.TAG)
            R.string.playing_queue -> openFragment(PlayingQueue.newInstance(), PlayingQueue.TAG)
            R.string.album_artists -> openFragment(AlbumArtists.newInstance(), AlbumArtists.TAG)
            R.string.composers -> openFragment(Composers.newInstance(), Composers.TAG)
            R.string.recently_added -> openFragment(RecentlyAdded.newInstance(), RecentlyAdded.TAG)
            R.string.recently_played -> openFragment(RecentlyPlayed.newInstance(), RecentlyPlayed.TAG)
            R.string.most_played -> openFragment(MostPlayed.newInstance(), MostPlayed.TAG)
            R.string.folders -> openFragment(Folders.newInstance(), Folders.TAG)
            R.string.folders_hierarchy -> openFragment(FoldersHierarchy.newInstance(), FoldersHierarchy.TAG)
            R.string.year -> openFragment(Year.newInstance(), Year.TAG)
            R.string.playlists -> openFragment(Playlists.newInstance(), Playlists.TAG)
            R.string.preferences -> openFragment(Preferences.newInstance(), Preferences.TAG)
            R.string.always_skipped -> openFragment(AlwaysSkipped.newInstance(), AlwaysSkipped.TAG)
            R.string.most_skipped -> openFragment(MostSkipped.newInstance(), MostSkipped.TAG)
            R.string.bookmarks -> openFragment(Bookmarks.newInstance(), Bookmarks.TAG)
            R.string.search -> openSearch()
        }
    }
}

