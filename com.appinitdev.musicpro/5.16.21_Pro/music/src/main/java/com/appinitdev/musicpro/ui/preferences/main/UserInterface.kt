
class UserInterface : PreferenceFragment() {

    private lateinit var binding: FragmentPreferenceAppearanceBinding
    private lateinit var headerBinding: HeaderPreferencesGenericBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPreferenceAppearanceBinding.inflate(inflater, container, false)
        headerBinding = HeaderPreferencesGenericBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        headerBinding.title.text = getString(R.string.user_interface)
        headerBinding.icon.setImageResource(R.drawable.ic_carousel)
        binding.header.setContentView(headerBinding.root)
        binding.recyclerView.setHasFixedSize(false)
        binding.recyclerView.adapter = GenericPreferencesAdapter(createUserInterfacePanel())
        binding.header.attachTo(binding.recyclerView, AppHeader.ScrollMode.HIDE_ON_SCROLL)
    }

    override val wantsMiniPlayerVisible: Boolean
        get() = false

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            UserInterfacePreferences.HOME_INTERFACE -> {
                // Nothing extra to do here — the home switch handles reloading on its own.
            }
            UserInterfacePreferences.PLAYER_INTERFACE -> {
                // Nothing extra to do here either — the player fragment picks up the change.
            }
        }
    }

    companion object {
        fun newInstance(): UserInterface {
            val args = Bundle()
            val fragment = UserInterface()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "UserInterface"
    }
}