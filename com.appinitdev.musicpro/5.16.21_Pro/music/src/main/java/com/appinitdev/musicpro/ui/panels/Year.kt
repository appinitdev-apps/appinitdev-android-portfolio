
 




@AndroidEntryPoint
class Year : BasePanelFragment() {

    private val yearViewModel: YearViewModel by viewModels({ requireActivity() })

    private lateinit var binding: FragmentYearBinding
    private lateinit var headerBinding: HeaderYearBinding

    private var adapterYear: AdapterYear? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentYearBinding.inflate(inflater, container, false)
        headerBinding = HeaderYearBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.header.setContentView(headerBinding.root)
        binding.header.attachTo(binding.recyclerView, AppHeader.ScrollMode.HIDE_ON_SCROLL)
        binding.recyclerView.attachSlideFastScroller()
        binding.recyclerView.requireAttachedMiniPlayer()

        binding.recyclerView.setupGridLayoutManager(YearPreferences.getGridSize().spanCount)

        setupClickListeners()

        yearViewModel.years.collectListWhenStarted({ adapterYear != null }) { years ->
            updateYearList(years)
        }
    }

    override fun onDestroyView() {
        adapterYear = null
        super.onDestroyView()
    }

    private fun setupClickListeners() {
        headerBinding.menu.setOnClickListener {
            openPreferencesPanel()
        }

        headerBinding.listStyle.setOnClickListener {
            childFragmentManager.showListStyleDialog(GenericListStyleDialog.Companion.PANEL.YEAR)
        }

        headerBinding.sortStyle.setOnClickListener {
            childFragmentManager.showYearSortDialog()
        }

        headerBinding.search.setOnClickListener {
            openSearch()
        }
    }

    private fun updateYearList(years: List<YearGroup>) {
        if (adapterYear == null) {
            adapterYear = AdapterYear(years)
            adapterYear?.setHasStableIds(true)
            adapterYear?.setCallbackListener(object : GeneralAdapterCallbacks {
                override fun onYearGroupClicked(yearGroup: YearGroup, view: View) {
                    openFragment(YearPage.newInstance(yearGroup), YearPage.TAG)
                }
            })
            binding.recyclerView.adapter = adapterYear
        } else {
            adapterYear?.updateList(years)
            if (binding.recyclerView.adapter == null) {
                binding.recyclerView.adapter = adapterYear
            }
        }

        headerBinding.count.text = getString(R.string.x_years, years.size)
        binding.recyclerView.requireAttachedSectionScroller(
                sections = provideScrollPositions(years),
                header = binding.header,
                view = headerBinding.scroll
        )

        headerBinding.sortStyle.setCurrentSortStyle()
        headerBinding.scroll.hideOnUnfavorableSort(
                sorts = listOf(CommonPreferencesConstants.BY_YEAR),
                preference = YearPreferences.getSortStyle()
        )
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            YearPreferences.GRID_SIZE_PORTRAIT, YearPreferences.GRID_SIZE_LANDSCAPE -> {
                applyGridSizeUpdate(binding.recyclerView, YearPreferences.getGridSize().spanCount)
            }
        }
    }

    private fun provideScrollPositions(years: List<YearGroup>): List<SectionedFastScroller.Position> {
        val yearToIndex = linkedMapOf<String, Int>()
        years.forEachIndexed { index, yearGroup ->
            val key = yearGroup.year.takeIf { it.all { ch -> ch.isDigit() } } ?: "#"
            if (!yearToIndex.containsKey(key)) {
                yearToIndex[key] = index
            }
        }
        return yearToIndex.map { (year, index) ->
            SectionedFastScroller.Position(year, index)
        }
    }

    companion object {
        fun newInstance(): Year {
            val args = Bundle()
            val fragment = Year()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "Year"
    }
}