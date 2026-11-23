
 











@AndroidEntryPoint
class ArtistPage : BasePageFragment() {

    private lateinit var binding: FragmentPageArtistBinding

    private val artist: Artist by lazy {
        requireArguments().parcelable(BundleConstants.ARTIST)
            ?: throw IllegalArgumentException("Artist is required")
    }

    private val artistViewerViewModel: ArtistViewerViewModel by viewModels(
            ownerProducer = { this },
            extrasProducer = {
                defaultViewModelCreationExtras.withCreationCallback<ArtistViewerViewModel.Factory>() {
                    it.create(artist = artist)
                }
            }
    )

    override val pageRecyclerView: RecyclerView
        get() = binding.recyclerView

    override val pageType: PageAdapter.PageType by lazy { PageAdapter.PageType.ArtistPage(artist) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPageArtistBinding.inflate(inflater, container, false)
        return binding.root
    }

     


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        collectPageData { artistViewerViewModel.data }
    }

     








    override fun onPageAdapterCreated() {
        viewLifecycleOwner.lifecycleScope.launch {
            artistViewerViewModel.artistInfo.collect { info ->
                pageAdapter?.setArtistInfo(info)
            }
        }
    }

     


    override fun resortPageData() {
        artistViewerViewModel.resort()
    }

     




    override fun onMenuClicked(view: View) {
        viewLifecycleOwner.lifecycleScope.launch {
            val currentData = artistViewerViewModel.data.value ?: return@launch

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
        const val TAG = "ArtistPage"

         





        fun newInstance(artist: Artist): ArtistPage {
            val args = Bundle()
            
            
            
            args.putParcelable(BundleConstants.ARTIST, artist.copy(songPaths = emptyList()))
            val fragment = ArtistPage()
            fragment.arguments = args
            return fragment
        }
    }
}

