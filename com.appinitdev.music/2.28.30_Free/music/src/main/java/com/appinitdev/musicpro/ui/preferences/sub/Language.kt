
class Language : MediaFragment() {

    private lateinit var binding: FragmentGenericRecyclerViewBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGenericRecyclerViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        binding.recyclerView.adapter = AdapterLanguage()
        binding.recyclerView.scheduleLayoutAnimation()

        view.startTransitionOnPreDraw()
        requireHiddenMiniPlayer()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            ConfigurationPreferences.LANGUAGE -> {
                (binding.recyclerView.adapter as? AdapterLanguage)?.notifyDataSetChanged()
            }
        }
    }

    override fun getTransitionType(): TransitionType {
        return TransitionType.DRIFT
    }

    override val wantsMiniPlayerVisible: Boolean
        get() = false

    companion object {
        fun newInstance(): Language {
            val args = Bundle()
            val fragment = Language()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "TypeFaces"
    }
}