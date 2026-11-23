class SimpleHome : BaseHomeFragment() {
    private lateinit var binding: FragmentHomeSimpleBinding
    private lateinit var headerBinding: HeaderHomeBinding
    private var homeViewModel: SimpleHomeViewModel? = null
    private var adapterSimpleHome: AdapterSimpleHome? = null
    private var gridLayoutManager: GridLayoutManager? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeSimpleBinding.inflate(inflater, container, false)
        headerBinding = HeaderHomeBinding.inflate(inflater, container, false)
        homeViewModel = ViewModelProvider(requireActivity())[SimpleHomeViewModel::class.java]
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.appHeader.setContentView(headerBinding.root)
        binding.appHeader.attachTo(binding.recyclerView, AppHeader.ScrollMode.PINNED )
        binding.recyclerView.requireAttachedMiniPlayer()
        gridLayoutManager = GridLayoutManager(requireContext(), 1)
        updateGridLayoutSpanCount()
        binding.recyclerView.layoutManager = gridLayoutManager
        headerBinding.search.setOnClickListener {
            openSearch()
        }
        headerBinding.menu.setOnClickListener {
            childFragmentManager.showHomeMenu()
        }
        setupServerToggle(headerBinding.serverToggle)
        homeViewModel!!.getHomeData().observe(viewLifecycleOwner) { list ->
            adapterSimpleHome = AdapterSimpleHome(list)
            adapterSimpleHome!!.setLayoutType(HomePreferences.getHomeLayoutType())
            setupAdapterCallbacks()
            binding.recyclerView.adapter = adapterSimpleHome
        }
        headerBinding.label.setOnClickListener {
            childFragmentManager.showAppLabel()
        }
    }
    private fun setupAdapterCallbacks() {
        adapterSimpleHome?.setAdapterSimpleHomeCallbacks(object : AdapterSimpleHomeCallbacks {
            override fun onItemClicked(panel: Panel, position: Int, view: View) {
                navigateToPanel(panel)
            }
        })
    }
    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        binding.appHeader.post {
            binding.appHeader.resetScrollingState()
        }
    }
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            HomePreferences.HOME_LAYOUT_TYPE -> {
                val layoutType = HomePreferences.getHomeLayoutType()
                adapterSimpleHome?.setLayoutType(layoutType)
                updateGridLayoutSpanCount()
                binding.recyclerView.scheduleLayoutAnimation()
                adapterSimpleHome?.notifyItemRangeChanged(0, adapterSimpleHome?.itemCount ?: 0)
            }
            MainPreferences.APP_LABEL -> {
                headerBinding.label.setAppLabel()
            }
        }
    }
    private fun updateGridLayoutSpanCount() {
        val spanCount = if (HomePreferences.getHomeLayoutType() == CommonPreferencesConstants.GRID_TYPE_GRID) {
            resources.getInteger(R.integer.home_grid_span_count)
        } else {
            1
        }
        gridLayoutManager?.spanCount = spanCount
        gridLayoutManager?.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (adapterSimpleHome?.getItemViewType(position) == AdapterSimpleHome.VIEW_TYPE_GROUP) {
                    spanCount
                } else {
                    1
                }
            }
        }
    }
    companion object {
        fun newInstance(): SimpleHome {
            val args = Bundle()
            val fragment = SimpleHome()
            fragment.arguments = args
            return fragment
        }
        const val TAG = "SimpleHome"
    }
}
