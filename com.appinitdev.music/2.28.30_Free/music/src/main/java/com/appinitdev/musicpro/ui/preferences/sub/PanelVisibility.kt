
 









class PanelVisibility : PreferenceFragment() {

    private lateinit var binding: FragmentGenericRecyclerViewBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentGenericRecyclerViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        
        
        binding.recyclerView.setHasFixedSize(false)
        binding.recyclerView.adapter = AdapterPanelVisibility(buildPanelVisibilityList())
    }

     



    private fun buildPanelVisibilityList(): List<Preference> {
        val prefs = mutableListOf<Preference>()

        
        
        prefs.add(Preference(type = PreferenceType.HEADER, title = R.string.panel_visibility))

        
        prefs.add(Preference(type = PreferenceType.SUB_HEADER, title = R.string.library))
        prefs.add(makePanelToggle(R.string.album_artists, R.drawable.ic_artist, UserInterfacePreferences.PANEL_VISIBLE_ALBUM_ARTISTS))
        prefs.add(makePanelToggle(R.string.composers, R.drawable.ic_composer, UserInterfacePreferences.PANEL_VISIBLE_COMPOSERS))
        prefs.add(makePanelToggle(R.string.genres, R.drawable.ic_piano, UserInterfacePreferences.PANEL_VISIBLE_GENRES))
        prefs.add(makePanelToggle(R.string.year, R.drawable.ic_date_range, UserInterfacePreferences.PANEL_VISIBLE_YEAR))
        prefs.add(makePanelToggle(R.string.playlists, R.drawable.ic_list, UserInterfacePreferences.PANEL_VISIBLE_PLAYLISTS))

        
        prefs.add(Preference(type = PreferenceType.SUB_HEADER, title = R.string.activity))
        prefs.add(makePanelToggle(R.string.playing_queue, R.drawable.ic_queue, UserInterfacePreferences.PANEL_VISIBLE_PLAYING_QUEUE))
        prefs.add(makePanelToggle(R.string.recently_added, R.drawable.ic_recently_added, UserInterfacePreferences.PANEL_VISIBLE_RECENTLY_ADDED))
        prefs.add(makePanelToggle(R.string.recently_played, R.drawable.ic_history, UserInterfacePreferences.PANEL_VISIBLE_RECENTLY_PLAYED))
        prefs.add(makePanelToggle(R.string.most_played, R.drawable.ic_equalizer, UserInterfacePreferences.PANEL_VISIBLE_MOST_PLAYED))
        prefs.add(makePanelToggle(R.string.favorites, R.drawable.ic_favorite_filled, UserInterfacePreferences.PANEL_VISIBLE_FAVORITES))

        
        prefs.add(Preference(type = PreferenceType.SUB_HEADER, title = R.string.files))
        prefs.add(makePanelToggle(R.string.folders, R.drawable.ic_folder, UserInterfacePreferences.PANEL_VISIBLE_FOLDERS))
        prefs.add(makePanelToggle(R.string.folders_hierarchy, R.drawable.ic_tree, UserInterfacePreferences.PANEL_VISIBLE_FOLDERS_HIERARCHY))
        prefs.add(makePanelToggle(R.string.always_skipped, R.drawable.ic_skip_16dp, UserInterfacePreferences.PANEL_VISIBLE_ALWAYS_SKIPPED))
        prefs.add(makePanelToggle(R.string.most_skipped, R.drawable.ic_skip_16dp, UserInterfacePreferences.PANEL_MOST_SKIPPED))

        return prefs
    }

     






    private fun makePanelToggle(titleRes: Int, iconRes: Int, prefKey: String): Preference {
        return Preference(
                title = titleRes,
                summary = "",
                icon = iconRes,
                type = PreferenceType.SWITCH,
                onPreferenceAction = { view, _ ->
                    UserInterfacePreferences.setPanelVisible(prefKey, (view as FelicitySwitch).isChecked)
                },
                valueProvider = Supplier {
                    UserInterfacePreferences.isPanelVisible(prefKey)
                }
        )
    }

    override val wantsMiniPlayerVisible: Boolean
        get() = false

    override fun getTransitionType(): TransitionType {
        return TransitionType.DRIFT
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        
        
    }

    companion object {
         




        fun newInstance(): PanelVisibility {
            val args = Bundle()
            val fragment = PanelVisibility()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "PanelVisibility"
    }
}

