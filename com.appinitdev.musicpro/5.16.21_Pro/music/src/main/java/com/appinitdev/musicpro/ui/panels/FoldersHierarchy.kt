
@AndroidEntryPoint
class FoldersHierarchy : BasePanelFragment() {

    /** Path passed via bundle, or null when this is the root level. */
    private val folderPath: String? by lazy {
        requireArguments().getString(KEY_FOLDER_PATH)
    }

    private val viewModel: FolderHierarchyViewModel by viewModels(
            extrasProducer = {
                defaultViewModelCreationExtras.withCreationCallback<FolderHierarchyViewModel.Factory> {
                    it.create(folderPath = folderPath)
                }
            }
    )

    private lateinit var binding: FragmentFoldersHierarchyBinding
    private lateinit var headerBinding: HeaderFoldersHierarchyBinding

    private var adapter: AdapterFolderHierarchy? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentFoldersHierarchyBinding.inflate(inflater, container, false)
        headerBinding = HeaderFoldersHierarchyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (folderPath == null) {
            headerBinding.headerTitle.text = getString(R.string.folders_hierarchy)
        } else {
            // folderPath is a document ID like "primary:Music/Artist" or "primary:Music".
            // substringAfterLast('/') gives us "Artist" for sub-folders, or "primary:Music"
            // for the root — in that case we strip the volume prefix after the colon.
            val segment = folderPath!!.substringAfterLast('/')
            headerBinding.headerTitle.text = if (segment.contains(':')) segment.substringAfter(':') else segment
        }

        binding.header.setContentView(headerBinding.root)
        binding.header.attachTo(binding.recyclerView, AppHeader.ScrollMode.HIDE_ON_SCROLL)
        binding.recyclerView.attachSlideFastScroller()
        binding.recyclerView.requireAttachedMiniPlayer()

        binding.recyclerView.setupGridLayoutManager(FolderHierarchyPreferences.getLayoutMode().spanCount)

        setupClickListeners()

        viewModel.contents.collectWhenStarted { contents ->
            updateContents(contents)
        }
    }

    override fun onDestroyView() {
        adapter = null
        super.onDestroyView()
    }

    private fun setupClickListeners() {
        headerBinding.menu.setOnClickListener {
            openPreferencesPanel()
        }

        headerBinding.sortStyle.setOnClickListener {
            childFragmentManager.showFolderHierarchySortDialog()
        }

        headerBinding.search.setOnClickListener {
            openSearch()
        }

        headerBinding.listStyle.setOnClickListener {
            childFragmentManager.showListStyleDialog(GenericListStyleDialog.Companion.PANEL.FOLDERS_HIERARCHY)
        }
    }

    private fun updateContents(contents: FolderHierarchyViewModel.FolderHierarchyContents) {
        if (adapter == null) {
            adapter = AdapterFolderHierarchy(contents)
            adapter?.setCallbacks(object : GeneralAdapterCallbacks {
                override fun onFolderClicked(folder: Folder, view: View) {
                    openFragment(newInstance(folder.path), TAG)
                }

                override fun onSongClicked(songs: MutableList<Audio>, position: Int, view: View) {
                    setMediaItems(songs, position)
                }

                override fun onSongLongClicked(songs: List<Audio>, position: Int, imageView: ImageView?) {
                    openSongsMenu(songs, position, imageView)
                }
            })
            binding.recyclerView.adapter = adapter
        } else {
            adapter?.updateContents(contents)
            if (binding.recyclerView.adapter == null) {
                binding.recyclerView.adapter = adapter
            }
        }

        binding.recyclerView.scheduleLayoutAnimation()

        val folderCount = contents.subFolders.size
        val songCount = contents.songs.size

        headerBinding.count.text = buildString {
            if (folderCount > 0) append(getString(R.string.x_folders, folderCount))
            if (folderCount > 0 && songCount > 0) append("  ·  ")
            if (songCount > 0) append(resources.getQuantityString(R.plurals.number_of_songs, songCount, songCount))
            if (folderCount == 0 && songCount == 0) append(getString(R.string.folders_hierarchy))
        }

        headerBinding.sortStyle.setCurrentSortStyle()
        headerBinding.scroll.hideOnUnfavorableSort(
                sorts = listOf(CommonPreferencesConstants.BY_NAME, CommonPreferencesConstants.BY_PATH),
                preference = FolderHierarchyPreferences.getSortStyle()
        )
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            FolderHierarchyPreferences.LAYOUT_MODE_PORTRAIT,
            FolderHierarchyPreferences.LAYOUT_MODE_LANDSCAPE -> {
                adapter?.layoutMode = FolderHierarchyPreferences.getLayoutMode()
                applyGridSizeUpdate(binding.recyclerView, FolderHierarchyPreferences.getLayoutMode().spanCount)
            }
        }
    }

    companion object {
        const val TAG = "FoldersHierarchy"
        private const val KEY_FOLDER_PATH = "folder_path"

        /** Root entry point — no folder path, shows top-level folders. */
        fun newInstance(): FoldersHierarchy {
            return FoldersHierarchy().apply {
                arguments = Bundle()
            }
        }

        /** Opens the contents of [folderPath] in a new hierarchy level. */
        fun newInstance(folderPath: String): FoldersHierarchy {
            return FoldersHierarchy().apply {
                arguments = Bundle().apply {
                    putString(KEY_FOLDER_PATH, folderPath)
                }
            }
        }
    }
}
