
/**
 * Panel fragment displaying the user's genres with sort, grid layout, and search support.
 *
 * @author Hamza417
 */
@AndroidEntryPoint
class Genres : BasePanelFragment() {

    private val genresViewModel: GenresViewModel by viewModels({ requireActivity() })

    private lateinit var binding: FragmentGenresBinding
    private lateinit var headerBinding: HeaderGenresBinding

    private var adapterGenres: AdapterGenres? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentGenresBinding.inflate(inflater, container, false)
        headerBinding = HeaderGenresBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.header.setContentView(headerBinding.root)
        binding.header.attachTo(binding.recyclerView, AppHeader.ScrollMode.HIDE_ON_SCROLL)
        binding.recyclerView.attachSlideFastScroller()
        binding.recyclerView.requireAttachedMiniPlayer()

        binding.recyclerView.setupGridLayoutManager(GenresPreferences.getGridSize().spanCount)

        adapterGenres?.let { binding.recyclerView.adapter = it }

        setupClickListeners()

        genresViewModel.genres.collectListWhenStarted({ adapterGenres != null }) { genres ->
            updateGenresList(genres)
        }
    }

    override fun onDestroyView() {
        adapterGenres = null
        super.onDestroyView()
    }

    private fun setupClickListeners() {
        headerBinding.menu.setOnClickListener {
            openPreferencesPanel()
        }

        headerBinding.listStyle.setOnClickListener {
            childFragmentManager.showListStyleDialog(GenericListStyleDialog.Companion.PANEL.GENRES)
        }

        headerBinding.sortStyle.setOnClickListener {
            childFragmentManager.showGenresSortDialog()
        }

        headerBinding.search.setOnClickListener {
            openSearch()
        }
    }

    private fun updateGenresList(genres: List<Genre>) {
        if (adapterGenres == null) {
            adapterGenres = AdapterGenres(genres)
            adapterGenres?.setHasStableIds(true)
            adapterGenres?.setCallbackListener(object : GeneralAdapterCallbacks {
                override fun onMenuClicked(view: View) {
                    childFragmentManager.showListStyleDialog(GenericListStyleDialog.Companion.PANEL.GENRES)
                }

                override fun onGenreClicked(genre: Genre, view: View) {
                    openFragment(GenrePage.newInstance(genre), GenrePage.TAG)
                }
            })
            binding.recyclerView.adapter = adapterGenres
        } else {
            adapterGenres?.updateList(genres)
            if (binding.recyclerView.adapter == null) {
                binding.recyclerView.adapter = adapterGenres
            }
        }

        headerBinding.count.text = getString(R.string.x_genres, genres.size)
        binding.recyclerView.requireAttachedSectionScroller(
                sections = provideScrollPositionDataBasedOnSortStyle(genres),
                header = binding.header,
                view = headerBinding.scroll)

        headerBinding.sortStyle.setCurrentSortStyle()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            GenresPreferences.GRID_SIZE_PORTRAIT, GenresPreferences.GRID_SIZE_LANDSCAPE -> {
                applyGridSizeUpdate(binding.recyclerView, GenresPreferences.getGridSize().spanCount)
            }
        }
    }

    private fun provideScrollPositionDataBasedOnSortStyle(genres: List<Genre>): List<SectionedFastScroller.Position> {
        return when (GenresPreferences.getSortStyle()) {
            CommonPreferencesConstants.BY_NAME -> {
                val firstAlphabetToIndex = linkedMapOf<String, Int>()
                genres.forEachIndexed { index, genre ->
                    val firstChar = genre.name?.firstOrNull()?.uppercaseChar()
                    val key = if (firstChar != null && firstChar.isLetter()) firstChar.toString() else "#"
                    if (!firstAlphabetToIndex.containsKey(key)) firstAlphabetToIndex[key] = index
                }
                firstAlphabetToIndex.map { (char, index) -> SectionedFastScroller.Position(char, index) }
            }
            else -> listOf()
        }
    }

    companion object {
        const val TAG = "GenresFragment"

        fun newInstance(): Genres {
            val args = Bundle()
            val fragment = Genres()
            fragment.arguments = args
            return fragment
        }
    }
}