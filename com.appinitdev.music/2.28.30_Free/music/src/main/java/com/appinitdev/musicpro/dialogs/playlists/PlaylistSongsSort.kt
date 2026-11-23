@AndroidEntryPoint
class PlaylistSongsSort : ScopedBottomSheetFragment() {
    private lateinit var binding: DialogSortPlaylistSongsBinding
    @Inject
    lateinit var playlistRepository: PlaylistRepository
    private val playlist: Playlist by lazy {
        requireArguments().parcelable(ARG_PLAYLIST)
            ?: throw IllegalArgumentException("Playlist argument is required")
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogSortPlaylistSongsBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (playlist.sortOrder) {
            -1 -> binding.asAdded.isChecked = true
            CommonPreferencesConstants.BY_TITLE -> binding.title.isChecked = true
            CommonPreferencesConstants.BY_ARTIST -> binding.artist.isChecked = true
            CommonPreferencesConstants.BY_ALBUM -> binding.album.isChecked = true
            CommonPreferencesConstants.BY_DURATION -> binding.duration.isChecked = true
            CommonPreferencesConstants.BY_DATE_ADDED -> binding.dateAdded.isChecked = true
            else -> binding.asAdded.isChecked = true
        }
        binding.normal.isChecked = playlist.sortStyle == CommonPreferencesConstants.ASCENDING
        binding.reversed.isChecked = playlist.sortStyle == CommonPreferencesConstants.DESCENDING
        binding.sortByChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val newSortOrder = when (checkedIds.firstOrNull()) {
                binding.asAdded.id -> -1
                binding.title.id -> CommonPreferencesConstants.BY_TITLE
                binding.artist.id -> CommonPreferencesConstants.BY_ARTIST
                binding.album.id -> CommonPreferencesConstants.BY_ALBUM
                binding.duration.id -> CommonPreferencesConstants.BY_DURATION
                binding.dateAdded.id -> CommonPreferencesConstants.BY_DATE_ADDED
                else -> return@setOnCheckedStateChangeListener
            }
            saveSortOrder(newSortOrder, playlist.sortStyle)
        }
        binding.sortingStyleChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val newSortStyle = when (checkedIds.firstOrNull()) {
                binding.normal.id -> CommonPreferencesConstants.ASCENDING
                binding.reversed.id -> CommonPreferencesConstants.DESCENDING
                else -> return@setOnCheckedStateChangeListener
            }
            saveSortOrder(playlist.sortOrder, newSortStyle)
        }
    }
    private fun saveSortOrder(sortOrder: Int, sortStyle: Int) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            playlistRepository.updateSortPreference(playlist.id, sortOrder, sortStyle)
        }
    }
    companion object {
        private const val TAG = "PlaylistSongsSort"
        private const val ARG_PLAYLIST = "playlist"
        fun newInstance(playlist: Playlist): PlaylistSongsSort {
            return PlaylistSongsSort().apply {
                arguments = Bundle().apply { putParcelable(ARG_PLAYLIST, playlist) }
            }
        }
        fun FragmentManager.showPlaylistSongsSort(playlist: Playlist): PlaylistSongsSort {
            val dialog = newInstance(playlist)
            dialog.show(this, TAG)
            return dialog
        }
    }
}
