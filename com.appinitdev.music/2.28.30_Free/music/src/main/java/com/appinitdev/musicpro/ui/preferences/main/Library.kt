class Library : PreferenceFragment() {
    private lateinit var binding: FragmentPreferenceAppearanceBinding
    private lateinit var headerBinding: HeaderPreferencesGenericBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPreferenceAppearanceBinding.inflate(inflater, container, false)
        headerBinding = HeaderPreferencesGenericBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        headerBinding.title.text = getString(R.string.library)
        headerBinding.icon.setImageResource(R.drawable.ic_library)
        binding.header.setContentView(headerBinding.root)
        binding.recyclerView.setHasFixedSize(false)
        binding.recyclerView.adapter = GenericPreferencesAdapter(createLibraryPanel())
        binding.header.attachTo(binding.recyclerView, AppHeader.ScrollMode.HIDE_ON_SCROLL)
    }
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            LibraryPreferences.MINIMUM_AUDIO_SIZE,
            LibraryPreferences.MINIMUM_AUDIO_LENGTH,
            LibraryPreferences.SKIP_HIDDEN_FILES,
            LibraryPreferences.SKIP_HIDDEN_FOLDERS -> {
                postDelayed {
                    AudioDatabaseService.refreshScan(requireContext())
                }
            }
            LibraryPreferences.ALBUM_ARTIST_OVER_ARTIST -> {
                AudioUtils.albumArtistOverArtist = LibraryPreferences.isAlbumArtistOverArtist()
            }
        }
    }
    override val wantsMiniPlayerVisible: Boolean
        get() = false
    companion object {
        fun newInstance(): Library {
            val args = Bundle()
            val fragment = Library()
            fragment.arguments = args
            return fragment
        }
        const val TAG = "Appearance"
    }
}
