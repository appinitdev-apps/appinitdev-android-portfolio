abstract class BasePageFragment : MediaFragment() {
    protected var pageAdapter: PageAdapter? = null
    protected abstract val pageRecyclerView: RecyclerView
    protected abstract val pageType: PageAdapter.PageType
    protected abstract fun onMenuClicked(view: View)
    protected open fun onPageAdapterCreated() = Unit
    protected abstract fun resortPageData()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pageRecyclerView.requireAttachedMiniPlayer()
        postponeEnterTransition()
    }
    override fun onDestroyView() {
        pageAdapter = null
        super.onDestroyView()
    }
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        if (key != null && key in PagePreferences.ALL_PAGE_PREF_KEYS) {
            resortPageData()
        }
    }
    protected fun collectPageData(dataFlow: () -> StateFlow<PageData?>) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                dataFlow().collect { data ->
                    data?.let { onPageData(it) }
                }
            }
        }
    }
    private fun onPageData(data: PageData) {
        val horPad = resources.getDimensionPixelSize(R.dimen.padding_10)
        pageRecyclerView.addItemDecorationSafely(
                PageSpacingItemDecoration(horPad, AppearancePreferences.getListSpacing().toInt()))
        if (pageAdapter == null) {
            pageAdapter = PageAdapter(data, pageType)
            pageRecyclerView.adapter = pageAdapter
            setupAdapterCallbacks()
            onPageAdapterCreated()
        } else {
            pageAdapter?.updateData(data)
            if (pageRecyclerView.adapter == null) {
                pageRecyclerView.adapter = pageAdapter
            }
        }
        requireView().startTransitionOnPreDraw()
    }
    protected open fun onSortDialogRequested(view: View) {
        childFragmentManager.showPageSortDialog(resolvePageTypeKey())
    }
    private fun resolvePageTypeKey(): String {
        return when (pageType) {
            is PageAdapter.PageType.AlbumPage -> PageSort.PAGE_TYPE_ALBUM
            is PageAdapter.PageType.ArtistPage -> PageSort.PAGE_TYPE_ARTIST
            is PageAdapter.PageType.ComposerPage -> PageSort.PAGE_TYPE_COMPOSER
            is PageAdapter.PageType.GenrePage -> PageSort.PAGE_TYPE_GENRE
            is PageAdapter.PageType.FolderPage -> PageSort.PAGE_TYPE_FOLDER
            is PageAdapter.PageType.YearPage -> PageSort.PAGE_TYPE_YEAR
            is PageAdapter.PageType.PlaylistPage -> PageSort.PAGE_TYPE_PLAYLIST
        }
    }
    private fun setupAdapterCallbacks() {
        pageAdapter?.setArtistAdapterListener(object : GeneralAdapterCallbacks {
            override fun onSongClicked(songs: MutableList<Audio>, position: Int, view: View) {
                setMediaItems(songs, position)
            }
            override fun onSongLongClicked(songs: List<Audio>, position: Int, imageView: ImageView?) {
                openSongsMenu(songs, position, imageView)
            }
            override fun onPlayClicked(audios: MutableList<Audio>, position: Int) {
                setMediaItems(audios, position)
            }
            override fun onShuffleClicked(audios: MutableList<Audio>, position: Int) {
                shuffleMediaItems(audios)
            }
            override fun onShuffleLongClicked(audios: List<Audio?>?, position: Int) {
                childFragmentManager.showShuffleDialog()
            }
            override fun onArtistClicked(artists: List<Artist>, position: Int, view: View) {
                openFragment(ArtistPage.newInstance(artists[position]), ArtistPage.TAG)
            }
            override fun onAlbumArtistClicked(artists: List<Artist>, position: Int, view: View) {
                openFragment(AlbumArtistPage.newInstance(artists[position]), AlbumArtistPage.TAG)
            }
            override fun onAlbumClicked(albums: List<Album>, position: Int, view: View) {
                openFragment(AlbumPage.newInstance(albums[position]), AlbumPage.TAG)
            }
            override fun onGenreClicked(genre: Genre, view: View) {
                openFragment(GenrePage.newInstance(genre), GenrePage.TAG)
            }
            override fun onMenuClicked(view: View) {
                this@BasePageFragment.onMenuClicked(view)
            }
            override fun onSortClicked(view: View) {
                onSortDialogRequested(view)
            }
        })
    }
}