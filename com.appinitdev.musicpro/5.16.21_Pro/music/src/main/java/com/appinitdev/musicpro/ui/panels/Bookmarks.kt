@AndroidEntryPoint
class Bookmarks : BasePanelFragment() {
    private lateinit var binding: FragmentBookmarksBinding
    private lateinit var headerBinding: HeaderBookmarksBinding
    private var adapter: AdapterBookmarks? = null
    private val viewModel: BookmarksListViewModel by viewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBookmarksBinding.inflate(inflater, container, false)
        headerBinding = HeaderBookmarksBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.requireAttachedMiniPlayer()
        binding.appHeader.setContentView(headerBinding.root)
        binding.appHeader.attachTo(binding.recyclerView, AppHeader.ScrollMode.HIDE_ON_SCROLL)
        binding.recyclerView.setupGridLayoutManager(1)
        headerBinding.menu.setOnClickListener {
            openPreferencesPanel()
        }
        viewModel.bookmarks.collectWhenStarted { items ->
            updateList(items)
        }
    }
    override fun onDestroyView() {
        adapter = null
        super.onDestroyView()
    }
    private fun updateList(items: List<AudioWithBookmarks>) {
        headerBinding.count.text = getString(R.string.x_songs, items.size)
        if (adapter == null) {
            adapter = AdapterBookmarks(items)
            adapter!!.callbacks = object : AdapterBookmarks.Callbacks {
                override fun onSongClicked(item: AudioWithBookmarks) {
                    openBookmarksList(item)
                }
            }
            binding.recyclerView.adapter = adapter
        } else {
            adapter!!.updateItems(items)
        }
    }
    private fun openBookmarksList(item: AudioWithBookmarks) {
        val bookmarks = item.bookmarks.sortedBy { it.timestampMs }
        openBookmarksList(
                bookmarks = bookmarks,
                onTimestampClicked = { bookmark, dismiss ->
                    setMediaItemWithSeek(item.audio, bookmark.timestampMs)
                    openDefaultPlayer()
                    dismiss()
                },
                onDelete = { bookmark ->
                    viewModel.deleteBookmark(bookmark)
                }
        )
    }
    companion object {
        const val TAG = "Bookmarks"
        fun newInstance(): Bookmarks {
            return Bookmarks().also { it.arguments = Bundle() }
        }
    }
}
