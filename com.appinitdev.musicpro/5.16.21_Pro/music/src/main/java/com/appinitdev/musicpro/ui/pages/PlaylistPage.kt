@AndroidEntryPoint
class PlaylistPage : BasePageFragment() {
    private lateinit var binding: FragmentPageArtistBinding
    private val playlist: Playlist by lazy {
        requireArguments().parcelable(BundleConstants.PLAYLIST)
            ?: throw IllegalArgumentException("Playlist is required")
    }
    private val playlistViewerViewModel: PlaylistViewerViewModel by viewModels(
            ownerProducer = { this },
            extrasProducer = {
                defaultViewModelCreationExtras.withCreationCallback<PlaylistViewerViewModel.Factory> {
                    it.create(playlist = playlist)
                }
            }
    )
    override val pageRecyclerView: RecyclerView
        get() = binding.recyclerView
    override val pageType: PageAdapter.PageType by lazy { PageAdapter.PageType.PlaylistPage(playlist) }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPageArtistBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        collectPageData { playlistViewerViewModel.data }
        viewLifecycleOwner.lifecycleScope.launch {
            playlistViewerViewModel.currentPlaylist.collect { playlist ->
                pageAdapter?.updatePlaylist(playlist)
            }
        }
    }
    override fun resortPageData() {
        playlistViewerViewModel.resort()
    }
    override fun onSortDialogRequested(view: View) {
        childFragmentManager.showPlaylistSongsSort(playlistViewerViewModel.currentPlaylist.value)
    }
    override fun onMenuClicked(view: View) {
        viewLifecycleOwner.lifecycleScope.launch {
            val currentData = playlistViewerViewModel.data.value ?: return@launch
            SharedScrollViewPopup(
                    container = requireContainerView(),
                    anchorView = view,
                    menuItems = listOf(
                            PopupMenuItem(title = R.string.play, icon = R.drawable.ic_play),
                            PopupMenuItem(title = R.string.shuffle, icon = R.drawable.ic_shuffle),
                            PopupMenuItem(title = R.string.add_to_queue, icon = R.drawable.ic_add_to_queue),
                            PopupMenuItem(title = R.string.add_to_playlist, icon = R.drawable.ic_add_to_playlist),
                            PopupMenuItem(title = R.string.send, icon = R.drawable.ic_send)
                    ),
                    onMenuItemClick = {
                        when (it) {
                            R.string.play -> setMediaItems(currentData.songs.toMutableList(), 0)
                            R.string.shuffle -> shuffleMediaItems(currentData.songs)
                            R.string.add_to_queue -> currentData.songs.forEach { song -> MediaPlaybackManager.addToQueue(song) }
                            R.string.add_to_playlist -> parentFragmentManager.showAddMultipleToPlaylistDialog(currentData.songs)
                            R.string.send -> shareAudioList(currentData.songs)
                        }
                    },
                    onDismiss = {}
            ).show()
        }
    }
    companion object {
        const val TAG = "PlaylistPage"
        fun newInstance(playlist: Playlist): PlaylistPage {
            val args = Bundle()
            args.putParcelable(BundleConstants.PLAYLIST, playlist)
            val fragment = PlaylistPage()
            fragment.arguments = args
            return fragment
        }
    }
}
