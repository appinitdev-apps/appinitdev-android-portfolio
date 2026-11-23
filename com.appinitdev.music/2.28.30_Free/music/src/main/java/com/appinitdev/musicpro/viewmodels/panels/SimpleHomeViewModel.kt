
class SimpleHomeViewModel(application: Application) : WrappedViewModel(application) {

    private val homeData: MutableLiveData<MutableList<Any>> by lazy {
        MutableLiveData<MutableList<Any>>().apply {
            setHomeData()
        }
    }

    fun getHomeData(): LiveData<MutableList<Any>> {
        return homeData
    }

     



    fun reloadHomeData() {
        setHomeData()
    }

    private fun setHomeData() {
        viewModelScope.launch(Dispatchers.IO) {
            val defaultPanels = mutableListOf<Any>()

            
            
            val libraryItems = mutableListOf<Any>()
            libraryItems.add(Panel(R.string.songs, R.drawable.ic_song))
            libraryItems.add(Panel(R.string.albums, R.drawable.ic_album))
            libraryItems.add(Panel(R.string.artists, R.drawable.ic_people))
            if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_ALBUM_ARTISTS))
                libraryItems.add(Panel(R.string.album_artists, R.drawable.ic_artist))
            if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_COMPOSERS))
                libraryItems.add(Panel(R.string.composers, R.drawable.ic_composer))
            if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_GENRES))
                libraryItems.add(Panel(R.string.genres, R.drawable.ic_piano))
            if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_YEAR))
                libraryItems.add(Panel(R.string.year, R.drawable.ic_date_range))
            if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_PLAYLISTS))
                libraryItems.add(Panel(R.string.playlists, R.drawable.ic_list))

            
            defaultPanels.add(Group(R.string.library))
            defaultPanels.addAll(libraryItems)

            
            val activityItems = mutableListOf<Any>()
            if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_PLAYING_QUEUE))
                activityItems.add(Panel(R.string.playing_queue, R.drawable.ic_queue))
            if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_RECENTLY_ADDED))
                activityItems.add(Panel(R.string.recently_added, R.drawable.ic_recently_added))
            if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_RECENTLY_PLAYED))
                activityItems.add(Panel(R.string.recently_played, R.drawable.ic_history))
            if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_MOST_PLAYED))
                activityItems.add(Panel(R.string.most_played, R.drawable.ic_equalizer))
            if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_FAVORITES)) {
                activityItems.add(
                        if (UserInterfacePreferences.isLikeIconInsteadOfThumb()) {
                            Panel(R.string.favorites, R.drawable.ic_thumb_up)
                        } else {
                            Panel(R.string.favorites, R.drawable.ic_favorite_filled)
                        }
                )
            }

            if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_BOOKMARKS)) {
                activityItems.add(Panel(R.string.bookmarks, R.drawable.ic_bookmark_16dp))
            }

            if (activityItems.isNotEmpty()) {
                defaultPanels.add(Group(R.string.activity))
                defaultPanels.addAll(activityItems)
            }

            
            val filesItems = mutableListOf<Any>()
            if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_FOLDERS))
                filesItems.add(Panel(R.string.folders, R.drawable.ic_folder))
            if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_FOLDERS_HIERARCHY))
                filesItems.add(Panel(R.string.folders_hierarchy, R.drawable.ic_tree))
            if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_ALWAYS_SKIPPED))
                filesItems.add(Panel(R.string.always_skipped, R.drawable.ic_skip_16dp))
            if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_MOST_SKIPPED))
                activityItems.add(Panel(R.string.most_skipped, R.drawable.ic_skip_16dp))

            if (filesItems.isNotEmpty()) {
                defaultPanels.add(Group(R.string.files))
                defaultPanels.addAll(filesItems)
            }

            homeData.postValue(defaultPanels.toMutableList())
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when {
            key == UserInterfacePreferences.LIKE_ICON_INSTEAD_OF_HEART -> setHomeData()
            
            
            key in UserInterfacePreferences.ALL_PANEL_VISIBILITY_KEYS -> setHomeData()
        }
    }

    companion object {
        data class Panel(val titleResId: Int, val iconResId: Int)
        data class Group(val titleResId: Int)
    }
}
