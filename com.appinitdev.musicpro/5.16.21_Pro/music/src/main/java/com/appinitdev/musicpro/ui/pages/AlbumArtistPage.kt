
 









@AndroidEntryPoint
class AlbumArtistPage : BasePageFragment() {

    private lateinit var binding: FragmentPageAlbumArtistBinding

    private val albumArtist: Artist by lazy {
        requireArguments().parcelable(BundleConstants.ALBUM_ARTIST)
            ?: throw IllegalArgumentException("Album Artist argument is required")
    }

    private val albumArtistViewerViewModel: AlbumArtistViewerViewModel by viewModels(
            ownerProducer = { this },
            extrasProducer = {
                defaultViewModelCreationExtras.withCreationCallback<AlbumArtistViewerViewModel.Factory>() {
                    it.create(albumArtist = albumArtist)
                }
            }
    )

    override val pageRecyclerView: RecyclerView
        get() = binding.recyclerView

     



    override val pageType: PageAdapter.PageType by lazy { PageAdapter.PageType.ArtistPage(albumArtist) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPageAlbumArtistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        collectPageData { albumArtistViewerViewModel.data }
    }

     



    override fun onPageAdapterCreated() {
        viewLifecycleOwner.lifecycleScope.launch {
            albumArtistViewerViewModel.artistInfo.collect { info ->
                pageAdapter?.setArtistInfo(info)
            }
        }
    }

     



    override fun resortPageData() {
        albumArtistViewerViewModel.resort()
    }

     




    override fun onMenuClicked(view: View) {
        viewLifecycleOwner.lifecycleScope.launch {
            val currentData = albumArtistViewerViewModel.data.value ?: return@launch

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
        const val TAG = "AlbumArtistPage"

         





        fun newInstance(albumArtist: Artist): AlbumArtistPage {
            val args = Bundle()
            
            
            
            args.putParcelable(BundleConstants.ALBUM_ARTIST, albumArtist.copy(songPaths = emptyList()))
            val fragment = AlbumArtistPage()
            fragment.arguments = args
            return fragment
        }
    }
}



