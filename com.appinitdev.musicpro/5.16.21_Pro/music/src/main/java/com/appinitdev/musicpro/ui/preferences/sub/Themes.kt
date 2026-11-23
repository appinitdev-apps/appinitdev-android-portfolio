class Themes : MediaFragment() {
    private lateinit var binding: FragmentGenericRecyclerViewBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGenericRecyclerViewBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        binding.recyclerView.adapter = AdapterTheme()
        view.startTransitionOnPreDraw()
        requireHiddenMiniPlayer()
    }
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            AppearancePreferences.THEME -> {
                handler.postDelayed({ ThemeUtils.setAppTheme(resources) }, 25)
            }
        }
    }
    override fun getTransitionType(): TransitionType {
        return TransitionType.DRIFT
    }
    override val wantsMiniPlayerVisible: Boolean
        get() = false
    companion object {
        fun newInstance(): Themes {
            val args = Bundle()
            val fragment = Themes()
            fragment.arguments = args
            return fragment
        }
        const val TAG = "Theme"
    }
}