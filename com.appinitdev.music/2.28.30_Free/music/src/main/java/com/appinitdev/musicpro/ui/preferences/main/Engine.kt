
class Engine : PreferenceFragment() {

    private lateinit var binding: FragmentPreferenceAppearanceBinding
    private lateinit var headerBinding: HeaderPreferencesGenericBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPreferenceAppearanceBinding.inflate(inflater, container, false)
        headerBinding = HeaderPreferencesGenericBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        headerBinding.title.text = getString(R.string.audio)
        headerBinding.icon.setImageResource(R.drawable.ic_volume_up)
        binding.header.setContentView(headerBinding.root)
        binding.recyclerView.setHasFixedSize(false)
        binding.recyclerView.adapter = GenericPreferencesAdapter(createEnginePanel())
        binding.header.attachTo(binding.recyclerView, AppHeader.ScrollMode.HIDE_ON_SCROLL)
    }

    override val wantsMiniPlayerVisible: Boolean
        get() = false

    companion object {
        fun newInstance(): Engine {
            val args = Bundle()
            val fragment = Engine()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "Engine"
    }
}