
@AndroidEntryPoint
class AlbumPage : BasePageFragment() {

    private lateinit var binding: FragmentPageArtistBinding

    private val album: Album by lazy {
        requireArguments().parcelable(BundleConstants.ALBUM)
            ?: throw IllegalArgumentException("Album is required")
    }

    private val albumViewerViewModel: AlbumViewerViewModel by viewModels(
            ownerProducer = { this },
            extrasProducer = {
                defaultViewModelCreationExtras.withCreationCallback<AlbumViewerViewModel.Factory> {
                    it.create(album = album)
                }
            }
    )

    override val pageRecyclerView: RecyclerView
        get() = binding.recyclerView

    override val pageType: PageAdapter.PageType by lazy { PageAdapter.PageType.AlbumPage(album) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPageArtistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        collectPageData { albumViewerViewModel.data }
    }

     



    override fun onPageAdapterCreated() {
        viewLifecycleOwner.lifecycleScope.launch {
            albumViewerViewModel.albumInfo.collect { info ->
                pageAdapter?.setAlbumInfo(info)
            }
        }
    }

    override fun resortPageData() {
        albumViewerViewModel.resort()
    }

    override fun onMenuClicked(view: View) {
        viewLifecycleOwner.lifecycleScope.launch {
            val currentData = albumViewerViewModel.data.value ?: return@launch

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
        const val TAG = "AlbumPage"

        fun newInstance(album: Album): AlbumPage {
            val args = Bundle()
            
            
            
            args.putParcelable(BundleConstants.ALBUM, album.copy(songPaths = emptyList()))
            val fragment = AlbumPage()
            fragment.arguments = args
            return fragment
        }
    }
}

