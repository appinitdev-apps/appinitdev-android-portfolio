
 











class MusicFolders : MediaFragment() {

    private lateinit var binding: FragmentGenericRecyclerViewBinding

     



    private val folderPickerLauncher = registerForActivityResult(
            ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            
            
            requireContext().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            SAFPreferences.addTreeUri(uri.toString())
            refreshList()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentGenericRecyclerViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        requireHiddenMiniPlayer()

        binding.recyclerView.adapter = buildAdapter()
        view.startTransitionOnPreDraw()
    }

     


    private fun buildAdapter(): AdapterFolderList {
        return AdapterFolderList(
                uris = SAFPreferences.getTreeUris().toList(),
                onAdd = {
                    
                    
                    folderPickerLauncher.launch(null)
                },
                onRemove = { uriString ->
                    removeFolder(uriString)
                }
        )
    }

     



    private fun removeFolder(uriString: String) {
        val uri = uriString.toUri()

        
        
        try {
            requireContext().contentResolver.releasePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        } catch (_: SecurityException) {
            
            
        }

        SAFPreferences.removeTreeUri(uriString)
        refreshList()
    }

     


    private fun refreshList() {
        (binding.recyclerView.adapter as? AdapterFolderList)?.submitList(
                SAFPreferences.getTreeUris().toList()
        )
    }

    override fun getTransitionType(): TransitionType {
        return TransitionType.DRIFT
    }

    override val wantsMiniPlayerVisible: Boolean
        get() = false

    companion object {
        fun newInstance(): MusicFolders {
            return MusicFolders()
        }

        const val TAG = "MusicFolders"
    }
}

