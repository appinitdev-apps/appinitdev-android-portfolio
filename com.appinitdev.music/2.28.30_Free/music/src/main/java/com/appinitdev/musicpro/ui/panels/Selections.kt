
 









class Selections : BasePanelFragment() {

    private lateinit var binding: FragmentSelectionsBinding
    private lateinit var headerBinding: HeaderSelectionsBinding

     



    private var adapterSongs: AdapterSelections? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSelectionsBinding.inflate(inflater, container, false)
        headerBinding = HeaderSelectionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.requireAttachedMiniPlayer()
        binding.appHeader.setContentView(headerBinding.root)
        binding.appHeader.attachTo(binding.recyclerView, AppHeader.ScrollMode.HIDE_ON_SCROLL)

        
        binding.recyclerView.setupGridLayoutManager(1)

        setupClickListeners()

        
        viewLifecycleOwner.lifecycleScope.launch {
            SelectionManager.selectedAudios.collect { selected ->
                updateList(selected)
            }
        }
    }

    override fun onDestroyView() {
        adapterSongs = null
        super.onDestroyView()
    }

    private fun setupClickListeners() {
        
        
        headerBinding.close.setOnClickListener {
            SelectionManager.clear()
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        
        headerBinding.play.setOnClickListener {
            val songs = SelectionManager.selectedAudios.value
            if (songs.isNotEmpty()) setMediaItems(songs, 0)
        }

        
        
        headerBinding.shuffle.setOnClickListener {
            val songs = SelectionManager.selectedAudios.value
            if (songs.isNotEmpty()) shuffleMediaItems(songs)
        }
    }

     






    private fun updateList(songs: List<Audio>) {
        if (adapterSongs == null) {
            adapterSongs = AdapterSelections(songs)
            adapterSongs?.setHasStableIds(true)
            adapterSongs?.setGeneralAdapterCallbacks(object : GeneralAdapterCallbacks {
                override fun onSongClicked(songs: MutableList<Audio>, position: Int, view: View) {
                    
                    setMediaItems(songs, position)
                }

                override fun onSongLongClicked(audios: MutableList<Audio>, position: Int, imageView: ImageView?) {
                    
                    
                    audios.getOrNull(position)?.let { SelectionManager.deselect(it) }
                }
            })
            binding.recyclerView.adapter = adapterSongs
        } else {
            adapterSongs?.updateSongs(songs)
            if (binding.recyclerView.adapter == null) {
                binding.recyclerView.adapter = adapterSongs
            }
        }

        
        
        headerBinding.count.text = songs.size.toString()
        headerBinding.hours.text = songs.sumOf { it.duration }.toDynamicTimeString()

        
        
        if (songs.isEmpty()) {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    companion object {
        const val TAG = "Selections"

        fun newInstance(): Selections {
            return Selections().apply {
                arguments = Bundle()
            }
        }
    }
}

