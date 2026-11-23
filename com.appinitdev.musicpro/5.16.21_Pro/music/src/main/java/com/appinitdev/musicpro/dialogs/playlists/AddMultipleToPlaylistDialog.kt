
 




















@AndroidEntryPoint
class AddMultipleToPlaylistDialog : ScopedBottomSheetFragment() {

    private lateinit var binding: DialogAddToPlaylistBinding
    private var adapter: AdapterPlaylistCheckbox? = null

     
    private var adapterInitialized = false

    private val audioHashes: LongArray by lazy {
        requireArguments().getLongArray(KEY_HASHES) ?: LongArray(0)
    }

    private val viewModel: AddMultipleToPlaylistViewModel by viewModels(
            extrasProducer = {
                defaultViewModelCreationExtras.withCreationCallback<AddMultipleToPlaylistViewModel.Factory> {
                    it.create(audioHashes = audioHashes)
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
            
            viewModel.saveToPlaylists(adapter?.getCheckedIds() ?: emptySet())
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
                                emptySet(),
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
                viewModel.saveComplete.collect {
                    
                    SelectionManager.clear()
                    dismiss()
                }
            }
        }
    }

     




    private fun showCreatePlaylistDialog() {
        parentFragmentManager.showCreatePlaylistDialog()
    }

    companion object {
        private const val TAG = "AddMultipleToPlaylistDialog"
        private const val KEY_HASHES = "audio_hashes"

        fun newInstance(audios: List<Audio>): AddMultipleToPlaylistDialog {
            return AddMultipleToPlaylistDialog().apply {
                arguments = Bundle().apply {
                    
                    
                    putLongArray(KEY_HASHES, audios.map { it.hash }.toLongArray())
                }
            }
        }

         





        fun FragmentManager.showAddMultipleToPlaylistDialog(audios: List<Audio>): AddMultipleToPlaylistDialog {
            val dialog = newInstance(audios)
            dialog.show(this, TAG)
            return dialog
        }
    }
}
