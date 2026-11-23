@AndroidEntryPoint
class AddToPlaylistDialog : ScopedBottomSheetFragment() {
    private lateinit var binding: DialogAddToPlaylistBinding
    private var adapter: AdapterPlaylistCheckbox? = null
    private var adapterInitialized = false
    private val audio: Audio by lazy {
        requireArguments().parcelable<Audio>(BundleConstants.AUDIO)
            ?: error("AddToPlaylistDialog requires an Audio argument")
    }
    private val viewModel: AddToPlaylistViewModel by viewModels(
            extrasProducer = {
                defaultViewModelCreationExtras.withCreationCallback<AddToPlaylistViewModel.Factory> {
                    it.create(audio = audio)
                }
            }
    )
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogAddToPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.createPlaylist.setOnClickListener {
            showCreatePlaylistDialog()
        }
        binding.cancel.setOnClickListener {
            dismiss()
        }
        binding.save.setOnClickListener {
            viewModel.saveMembership(adapter?.getCheckedIds() ?: emptySet())
        }
        observeViewModel()
    }
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    state ?: return@collect
                    if (!adapterInitialized) {
                        adapterInitialized = true
                        adapter = AdapterPlaylistCheckbox(
                                state.playlists,
                                state.preCheckedIds,
                                state.songCounts
                        )
                        binding.recyclerView.adapter = adapter
                    } else {
                        adapter?.updateData(state.playlists, state.songCounts)
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.saveComplete.collect { dismiss() }
            }
        }
    }
    private fun showCreatePlaylistDialog() {
        parentFragmentManager.showCreatePlaylistDialog()
    }
    companion object {
        private const val TAG = "AddToPlaylistDialog"
        fun newInstance(audio: Audio): AddToPlaylistDialog {
            return AddToPlaylistDialog().apply {
                arguments = Bundle().apply {
                    putParcelable(BundleConstants.AUDIO, audio)
                }
            }
        }
        fun FragmentManager.showAddToPlaylistDialog(audio: Audio): AddToPlaylistDialog {
            val dialog = newInstance(audio)
            dialog.show(this, TAG)
            return dialog
        }
    }
}
