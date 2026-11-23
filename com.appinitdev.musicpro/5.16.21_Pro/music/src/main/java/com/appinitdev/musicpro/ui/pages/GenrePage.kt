
@AndroidEntryPoint
class GenrePage : BasePageFragment() {

    private lateinit var binding: FragmentPageArtistBinding

    private val genre: Genre by lazy {
        requireArguments().parcelable(BundleConstants.GENRE)
            ?: throw IllegalArgumentException("Genre is required")
    }

    private val genreViewerViewModel by viewModels<GenreViewerViewModel>(
            extrasProducer = {
                defaultViewModelCreationExtras.withCreationCallback<GenreViewerViewModel.Factory> {
                    it.create(genre = genre)
                }
            }
    )

    override val pageRecyclerView: RecyclerView
        get() = binding.recyclerView

    override val pageType: PageAdapter.PageType by lazy { PageAdapter.PageType.GenrePage(genre) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPageArtistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        collectPageData { genreViewerViewModel.data }
    }

    override fun resortPageData() {
        genreViewerViewModel.resort()
    }

    override fun onMenuClicked(view: View) {
        viewLifecycleOwner.lifecycleScope.launch {
            val currentData = genreViewerViewModel.data.value ?: return@launch

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
        const val TAG = "GenreSongs"

        fun newInstance(genre: Genre): GenrePage {
            val args = Bundle()
            
            
            
            args.putParcelable(BundleConstants.GENRE, genre.copy(songPaths = emptyList()))
            val fragment = GenrePage()
            fragment.arguments = args
            return fragment
        }
    }
}

