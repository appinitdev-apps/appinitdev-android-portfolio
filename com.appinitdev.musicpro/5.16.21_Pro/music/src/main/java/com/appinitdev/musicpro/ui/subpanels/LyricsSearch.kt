
 












@AndroidEntryPoint
class LyricsSearch : MediaFragment() {

    private lateinit var binding: FragmentLyricsSearchBinding
    private lateinit var headerBinding: HeaderGenericSearchBinding

    private var adapterLrcSearch: AdapterLrcSearch? = null

     




    private val editorAudio: Audio? by lazy {
        arguments?.parcelable<Audio>(BundleConstants.AUDIO)
    }

     




    private var isProgrammaticTextSet = false

    private val viewModel: LyricsSearchViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLyricsSearchBinding.inflate(inflater, container, false)
        headerBinding = HeaderGenericSearchBinding.inflate(inflater, container, false)
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

        
        
        editorAudio?.let { audio ->
            val keyword = audio.title?.takeIf { it.isNotBlank() } ?: audio.name
            isProgrammaticTextSet = true
            headerBinding.editText.setText(keyword)
            headerBinding.editText.setSelection(keyword.length)
            isProgrammaticTextSet = false
            viewModel.setUserKeyword(keyword)
            viewModel.searchWithCurrentKeyword()
        }
    }

    override fun onDestroyView() {
        adapterLrcSearch = null
        super.onDestroyView()
    }

    private fun setupSearchBox() {
        
        isProgrammaticTextSet = true
        headerBinding.editText.setText(viewModel.keyword)
        headerBinding.editText.setSelection(headerBinding.editText.text?.length ?: 0)
        isProgrammaticTextSet = false

        headerBinding.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                if (!isProgrammaticTextSet) {
                    viewModel.setUserKeyword(s?.toString() ?: "")
                }
            }
        })

        
        headerBinding.editText.setOnEditorActionListener { _, actionId, _ ->
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
                    headerBinding.count.text = getString(R.string.no_lyrics_found)
                }
            }
        }

        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            ensureAdapterCreated()
            adapterLrcSearch?.updateResults(results)

            val isLoading = viewModel.isLoading.value ?: false

            headerBinding.count.text = if (results.isNotEmpty()) {
                getString(R.string.x_results, results.size)
            } else {
                if (isLoading) {
                    getString(R.string.loading)
                } else {
                    getString(R.string.no_lyrics_found)
                }
            }
        }

        viewModel.lrcSaved.observe(viewLifecycleOwner) { saved ->
            if (saved) {
                
                
                parentFragmentManager.setFragmentResult(REQUEST_KEY_LYRICS_SAVED, Bundle())
                goBack()
            }
        }

        viewModel.getWarning().observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrBlank()) {
                viewModel.warning.postValue(null)
            }
        }
    }

    private fun ensureAdapterCreated() {
        if (adapterLrcSearch == null) {
            adapterLrcSearch = AdapterLrcSearch { lrcResponse ->
                if (editorAudio != null) {
                    
                    
                    parentFragmentManager.setFragmentResult(
                            REQUEST_KEY_LYRICS_FOR_EDITOR,
                            Bundle().apply {
                                putString(KEY_PLAIN_LYRICS, lrcResponse.plainLyrics)
                                putString(KEY_SYNCED_LYRICS, lrcResponse.syncedLyrics)
                            }
                    )
                    goBack()
                } else {
                    viewModel.downloadAndSaveLrc(lrcResponse)
                }
            }
            binding.recyclerView.adapter = adapterLrcSearch
        }
    }

    companion object {
         



        fun newInstance(): LyricsSearch {
            return LyricsSearch().apply { arguments = Bundle() }
        }

         






        fun newInstanceForEditor(audio: Audio): LyricsSearch {
            return LyricsSearch().apply {
                arguments = Bundle().apply {
                    putParcelable(BundleConstants.AUDIO, audio)
                }
            }
        }

        const val TAG = "LyricsSearch"

         
        const val REQUEST_KEY_LYRICS_FOR_EDITOR = "lyrics_search_for_editor"

         
        const val KEY_PLAIN_LYRICS = "plain_lyrics"

         
        const val KEY_SYNCED_LYRICS = "synced_lyrics"

         



        const val REQUEST_KEY_LYRICS_SAVED = "lyrics_search_lyrics_saved"
    }
}