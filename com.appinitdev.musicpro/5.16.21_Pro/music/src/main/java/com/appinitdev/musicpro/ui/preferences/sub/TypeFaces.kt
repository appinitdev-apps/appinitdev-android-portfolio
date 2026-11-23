class TypeFaces : MediaFragment() {
    private lateinit var binding: FragmentGenericRecyclerViewBinding
    private lateinit var typeFacesViewModel: TypeFacesViewModel
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentGenericRecyclerViewBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        typeFacesViewModel = ViewModelProvider(this)[TypeFacesViewModel::class.java]
        typeFacesViewModel.typefaceCache.observe(viewLifecycleOwner) {
            binding.recyclerView.adapter = AdapterTypeface()
            binding.recyclerView.scheduleLayoutAnimation()
            view.startTransitionOnPreDraw()
        }
        requireHiddenMiniPlayer()
    }
    override fun getTransitionType(): TransitionType {
        return TransitionType.DRIFT
    }
    override val wantsMiniPlayerVisible: Boolean
        get() = false
    companion object {
        fun newInstance(): TypeFaces {
            val args = Bundle()
            val fragment = TypeFaces()
            fragment.arguments = args
            return fragment
        }
        const val TAG = "TypeFaces"
    }
}