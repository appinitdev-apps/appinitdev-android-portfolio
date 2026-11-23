
 











@AndroidEntryPoint
class ComposerPage : BasePageFragment() {

    private lateinit var binding: FragmentPageComposerBinding

    private val composer: Artist by lazy {
        requireArguments().parcelable(BundleConstants.COMPOSER)
            ?: throw IllegalArgumentException("Composer is required")
    }

    private val composerViewerViewModel: ComposerViewerViewModel by viewModels(
            ownerProducer = { this },
            extrasProducer = {
                defaultViewModelCreationExtras.withCreationCallback<ComposerViewerViewModel.Factory>() {
                    it.create(composer = composer)
                }
            }
    )

    override val pageRecyclerView: RecyclerView
        get() = binding.recyclerView

    override val pageType: PageAdapter.PageType by lazy { PageAdapter.PageType.ComposerPage(composer) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPageComposerBinding.inflate(inflater, container, false)
        return binding.root
    }

     


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        collectPageData { composerViewerViewModel.data }
    }

     



    override fun onPageAdapterCreated() {
        viewLifecycleOwner.lifecycleScope.launch {
            composerViewerViewModel.artistInfo.collect { info ->
                pageAdapter?.setArtistInfo(info)
            }
        }
    }

     


    override fun resortPageData() {
        composerViewerViewModel.resort()
    }

     




    override fun onMenuClicked(view: View) {
        viewLifecycleOwner.lifecycleScope.launch {
            val currentData = composerViewerViewModel.data.value ?: return@launch

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
        const val TAG = "ComposerPage"

         







        fun newInstance(composer: Artist): ComposerPage {
            val args = Bundle()
            args.putParcelable(BundleConstants.COMPOSER, composer.copy(songPaths = emptyList()))
            val fragment = ComposerPage()
            fragment.arguments = args
            return fragment
        }
    }
}



