@AndroidEntryPoint
class MetadataSearch : MediaFragment() {
    private lateinit var binding: FragmentMetadataSearchBinding
    private lateinit var headerBinding: HeaderMetadataSearchBinding
    private var adapter: AdapterMetadataSearch? = null
    private var isProgrammaticTextSet = false
    private val audio: Audio by lazy {
        requireArguments().parcelable<Audio>(BundleConstants.AUDIO)
            ?: throw IllegalArgumentException("Audio argument is required for MetadataSearch")
    }
    private val viewModel: MetadataSearchViewModel by viewModels(
            extrasProducer = {
                defaultViewModelCreationExtras.withCreationCallback<MetadataSearchViewModel.Factory> {
                    it.create(audio)
                }
            }
    )
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMetadataSearchBinding.inflate(inflater, container, false)
        headerBinding = HeaderMetadataSearchBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireHiddenMiniPlayer()
        binding.appHeader.setContentView(headerBinding.root)
        binding.appHeader.attachTo(binding.recyclerView, AppHeader.ScrollMode.HIDE_ON_SCROLL)
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 1)
        setupSearchBox()
        setupSearchButton()
        observeViewModel()
    }
    override fun onDestroyView() {
        adapter = null
        super.onDestroyView()
    }
    private fun setupSearchBox() {
        isProgrammaticTextSet = true
        headerBinding.titleEditText.setText(viewModel.titleKeyword)
        headerBinding.titleEditText.setSelection(headerBinding.titleEditText.text?.length ?: 0)
        headerBinding.artistEditText.setText(viewModel.artistKeyword)
        headerBinding.artistEditText.setSelection(headerBinding.artistEditText.text?.length ?: 0)
        isProgrammaticTextSet = false
        headerBinding.titleEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                if (!isProgrammaticTextSet) viewModel.setUserTitle(s?.toString() ?: "")
            }
        })
        headerBinding.artistEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                if (!isProgrammaticTextSet) viewModel.setUserArtist(s?.toString() ?: "")
            }
        })
        headerBinding.titleEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.searchWithCurrentKeyword()
                true
            } else {
                false
            }
        }
        headerBinding.artistEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.searchWithCurrentKeyword()
                true
            } else {
                false
            }
        }
    }
    private fun setupSearchButton() {
        headerBinding.search.setOnClickListener {
            viewModel.searchWithCurrentKeyword()
        }
    }
    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            headerBinding.progress.isVisible = isLoading
            if (isLoading) {
                headerBinding.count.text = getString(R.string.loading)
            } else {
                if (viewModel.searchResults.value.isNullOrEmpty()) {
                    headerBinding.count.text = getString(R.string.no_metadata_found)
                }
            }
        }
        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            ensureAdapterCreated()
            adapter?.updateResults(results)
            val isLoading = viewModel.isLoading.value ?: false
            headerBinding.count.text = if (results.isNotEmpty()) {
                getString(R.string.x_results, results.size)
            } else {
                if (isLoading) getString(R.string.loading)
                else getString(R.string.no_lyrics_found)
            }
        }
        viewModel.getWarning().observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrBlank()) {
                viewModel.warning.postValue(null)
            }
        }
    }
    private fun ensureAdapterCreated() {
        if (adapter == null) {
            adapter = AdapterMetadataSearch { recording ->
                val artist = recording.artistCredit
                    ?.mapNotNull { it.name?.takeIf { n -> n.isNotBlank() } }
                    ?.joinToString(", ")
                    .orEmpty()
                val firstRelease = recording.releases?.firstOrNull()
                val album = firstRelease?.title?.takeIf { it.isNotBlank() }
                val year = firstRelease?.date?.take(4)?.takeIf { it.isNotBlank() }
                val genre = recording.tags
                    ?.maxByOrNull { it.count }
                    ?.name
                    ?.takeIf { it.isNotBlank() }
                val result = MetadataSearchResult(
                        title = recording.title.orEmpty(),
                        artist = artist,
                        album = album,
                        year = year,
                        genre = genre
                )
                parentFragmentManager.setFragmentResult(
                        REQUEST_KEY_METADATA_RESULT,
                        Bundle().apply { putParcelable(KEY_RESULT, result) }
                )
                goBack()
            }
            binding.recyclerView.adapter = adapter
        }
    }
    companion object {
        const val TAG = "MetadataSearch"
        const val REQUEST_KEY_METADATA_RESULT = "metadata_search_result"
        const val KEY_RESULT = "result"
        fun newInstance(audio: Audio): MetadataSearch {
            return MetadataSearch().apply {
                arguments = Bundle().apply {
                    putParcelable(BundleConstants.AUDIO, audio)
                }
            }
        }
    }
}
