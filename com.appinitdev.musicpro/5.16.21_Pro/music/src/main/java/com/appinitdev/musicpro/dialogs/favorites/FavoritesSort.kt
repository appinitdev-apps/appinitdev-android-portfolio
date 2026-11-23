class FavoritesSort : ScopedBottomSheetFragment() {
    private lateinit var binding: DialogSortSongsBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogSortSongsBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (FavoritesPreferences.getSongSort()) {
            CommonPreferencesConstants.BY_TITLE -> binding.title.isChecked = true
            CommonPreferencesConstants.BY_ARTIST -> binding.artist.isChecked = true
            CommonPreferencesConstants.BY_ALBUM -> binding.album.isChecked = true
            CommonPreferencesConstants.BY_PATH -> binding.path.isChecked = true
            CommonPreferencesConstants.BY_DATE_ADDED -> binding.dateAdded.isChecked = true
            CommonPreferencesConstants.BY_DATE_MODIFIED -> binding.dateModified.isChecked = true
            CommonPreferencesConstants.BY_DURATION -> binding.duration.isChecked = true
            CommonPreferencesConstants.BY_YEAR -> binding.year.isChecked = true
            CommonPreferencesConstants.BY_TRACK_NUMBER -> binding.trackNumber.isChecked = true
            CommonPreferencesConstants.BY_COMPOSER -> binding.composer.isChecked = true
        }
        binding.normal.isChecked = FavoritesPreferences.getSortingStyle() == CommonPreferencesConstants.ASCENDING
        binding.reversed.isChecked = FavoritesPreferences.getSortingStyle() == CommonPreferencesConstants.DESCENDING
        binding.sortByChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            when (checkedIds.firstOrNull()) {
                binding.title.id -> FavoritesPreferences.setSongSort(CommonPreferencesConstants.BY_TITLE)
                binding.artist.id -> FavoritesPreferences.setSongSort(CommonPreferencesConstants.BY_ARTIST)
                binding.album.id -> FavoritesPreferences.setSongSort(CommonPreferencesConstants.BY_ALBUM)
                binding.path.id -> FavoritesPreferences.setSongSort(CommonPreferencesConstants.BY_PATH)
                binding.dateAdded.id -> FavoritesPreferences.setSongSort(CommonPreferencesConstants.BY_DATE_ADDED)
                binding.dateModified.id -> FavoritesPreferences.setSongSort(CommonPreferencesConstants.BY_DATE_MODIFIED)
                binding.duration.id -> FavoritesPreferences.setSongSort(CommonPreferencesConstants.BY_DURATION)
                binding.year.id -> FavoritesPreferences.setSongSort(CommonPreferencesConstants.BY_YEAR)
                binding.trackNumber.id -> FavoritesPreferences.setSongSort(CommonPreferencesConstants.BY_TRACK_NUMBER)
                binding.composer.id -> FavoritesPreferences.setSongSort(CommonPreferencesConstants.BY_COMPOSER)
            }
        }
        binding.sortingStyleChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            when (checkedIds.firstOrNull()) {
                binding.normal.id -> FavoritesPreferences.setSortingStyle(CommonPreferencesConstants.ASCENDING)
                binding.reversed.id -> FavoritesPreferences.setSortingStyle(CommonPreferencesConstants.DESCENDING)
            }
        }
    }
    companion object {
        private const val TAG = "FavoritesSort"
        fun newInstance(): FavoritesSort {
            val args = Bundle()
            val fragment = FavoritesSort()
            fragment.arguments = args
            return fragment
        }
        fun FragmentManager.showFavoritesSort(): FavoritesSort {
            val dialog = newInstance()
            dialog.show(this, TAG)
            return dialog
        }
    }
}
