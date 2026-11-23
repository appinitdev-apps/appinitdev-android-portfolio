class PreferenceSearch : PreferenceFragment() {
    private lateinit var binding: FragmentPreferenceSearchBinding
    private lateinit var headerBinding: HeaderPreferenceSearchBinding
    private val viewModel: PreferenceSearchViewModel by viewModels()
    private var allPreferences: List<Preference> = emptyList()
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        binding = FragmentPreferenceSearchBinding.inflate(inflater, container, false)
        headerBinding = HeaderPreferenceSearchBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.appHeader.setContentView(headerBinding.root)
        binding.appHeader.attachTo(binding.recyclerView, AppHeader.ScrollMode.HIDE_ON_SCROLL)
        binding.recyclerView.setHasFixedSize(false)
        allPreferences = buildAllPreferences()
        setupSearchBox()
        observeSearchQuery()
    }
    override val wantsMiniPlayerVisible: Boolean
        get() = false
    private fun buildAllPreferences(): List<Preference> {
        val all = mutableListOf<Preference>()
        all.addAll(createAppearancePanel())
        all.addAll(createUserInterfacePanel())
        all.addAll(createBehaviorPanel())
        all.addAll(createEnginePanel())
        all.addAll(createLibraryPanel())
        all.addAll(createAccessibilityPanel())
        all.addAll(createAboutPanel())
        return all.filter {
            it.type != PreferenceType.HEADER &&
                    it.type != PreferenceType.SUB_HEADER &&
                    it.type != PreferenceType.WARN
        }
    }
    private fun setupSearchBox() {
        val currentQuery = viewModel.searchQuery.value
        if (currentQuery.isNotEmpty()) {
            headerBinding.editText.setText(currentQuery)
            headerBinding.editText.setSelection(currentQuery.length)
        }
        headerBinding.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                viewModel.setSearchQuery(s?.toString() ?: "")
            }
        })
    }
    private fun observeSearchQuery() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchQuery.collect { query ->
                    applyFilter(query)
                }
            }
        }
    }
    private fun applyFilter(query: String) {
        if (query.isBlank()) {
            binding.recyclerView.adapter = null
            headerBinding.count.text = ""
            return
        }
        val lowerQuery = query.trim().lowercase()
        val filtered = allPreferences.filter { pref ->
            val titleString = runCatching { getString(pref.title) }.getOrElse { "" }
            val summaryString = when (val summary = pref.summary) {
                is Int -> runCatching { getString(summary) }.getOrElse { "" }
                is String -> summary
                else -> ""
            }
            titleString.lowercase().contains(lowerQuery) || summaryString.lowercase().contains(lowerQuery)
        }
        binding.recyclerView.adapter = GenericPreferencesAdapter(filtered, keyword = lowerQuery)
        headerBinding.count.text = getString(R.string.x_results, filtered.size)
    }
    companion object {
        fun newInstance(): PreferenceSearch {
            val args = Bundle()
            val fragment = PreferenceSearch()
            fragment.arguments = args
            return fragment
        }
        const val TAG = "PreferenceSearch"
    }
}