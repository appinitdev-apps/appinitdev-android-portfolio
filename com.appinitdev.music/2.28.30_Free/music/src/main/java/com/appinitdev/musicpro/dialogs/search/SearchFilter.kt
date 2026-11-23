
 







class SearchFilter : ScopedBottomSheetFragment() {

    private lateinit var binding: DialogSearchFilterBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        binding = DialogSearchFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initChipStates()
        attachChipListeners()
    }

    private fun initChipStates() {
        binding.songs.isChecked = SearchPreferences.isSongsEnabled()
        binding.albums.isChecked = SearchPreferences.isAlbumsEnabled()
        binding.artists.isChecked = SearchPreferences.isArtistsEnabled()
        binding.genres.isChecked = SearchPreferences.isGenresEnabled()
    }

    private fun attachChipListeners() {
        binding.songs.setOnCheckedChangeListener { _, isChecked ->
            SearchPreferences.setSongsEnabled(isChecked)
        }
        binding.albums.setOnCheckedChangeListener { _, isChecked ->
            SearchPreferences.setAlbumsEnabled(isChecked)
        }
        binding.artists.setOnCheckedChangeListener { _, isChecked ->
            SearchPreferences.setArtistsEnabled(isChecked)
        }
        binding.genres.setOnCheckedChangeListener { _, isChecked ->
            SearchPreferences.setGenresEnabled(isChecked)
        }
    }

    companion object {
        private const val TAG = "SearchFilter"

        fun newInstance(): SearchFilter {
            return SearchFilter().apply { arguments = Bundle() }
        }

         


        fun FragmentManager.showSearchFilter(): SearchFilter {
            val dialog = newInstance()
            dialog.show(this, TAG)
            return dialog
        }
    }
}

