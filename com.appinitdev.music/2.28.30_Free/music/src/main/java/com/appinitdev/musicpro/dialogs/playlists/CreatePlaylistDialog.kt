
 


















@AndroidEntryPoint
class CreatePlaylistDialog : ScopedBottomSheetFragment() {

    private lateinit var binding: DialogCreatePlaylistBinding

    @Inject
    lateinit var playlistRepository: PlaylistRepository

    private var onPlaylistCreatedListener: OnPlaylistCreatedListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogCreatePlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.playlistNameInput.requestFocus()

        binding.cancel.setOnClickListener {
            dismiss()
        }

        binding.create.setOnClickListener {
            val name = binding.playlistNameInput.text?.toString()?.trim()
            if (name.isNullOrEmpty()) return@setOnClickListener
            val description = binding.playlistDescriptionInput.text?.toString()?.trim()
                .takeUnless { it.isNullOrEmpty() }

            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                playlistRepository.createPlaylist(name, description)
                withContext(Dispatchers.Main) {
                    onPlaylistCreatedListener?.onPlaylistCreated(name)
                    dismiss()
                }
            }
        }
    }

     




    fun setOnPlaylistCreatedListener(listener: OnPlaylistCreatedListener) {
        onPlaylistCreatedListener = listener
    }

     


    interface OnPlaylistCreatedListener {
         




        fun onPlaylistCreated(name: String)
    }

    companion object {
        private const val TAG = "CreatePlaylistDialog"

        fun newInstance(): CreatePlaylistDialog {
            return CreatePlaylistDialog().apply { arguments = Bundle() }
        }

         





        fun FragmentManager.showCreatePlaylistDialog(
                listener: OnPlaylistCreatedListener? = null
        ): CreatePlaylistDialog {
            val dialog = newInstance()
            listener?.let { dialog.setOnPlaylistCreatedListener(it) }
            dialog.show(this, TAG)
            return dialog
        }
    }
}

