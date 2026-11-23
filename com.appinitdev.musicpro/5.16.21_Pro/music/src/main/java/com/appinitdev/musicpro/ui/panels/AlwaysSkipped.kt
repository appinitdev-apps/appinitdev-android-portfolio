
 













@AndroidEntryPoint
class AlwaysSkipped : BasePanelFragment() {

    private lateinit var binding: FragmentAlwaysSkippedBinding
    private lateinit var headerBinding: HeaderAlwaysSkippedBinding

     
    private var adapterSongs: AdapterSongs? = null

    private val alwaysSkippedViewModel: AlwaysSkippedViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAlwaysSkippedBinding.inflate(inflater, container, false)
        headerBinding = HeaderAlwaysSkippedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.requireAttachedMiniPlayer()
        binding.appHeader.setContentView(headerBinding.root)
        binding.appHeader.attachTo(binding.recyclerView, AppHeader.ScrollMode.HIDE_ON_SCROLL)

        
        binding.recyclerView.setupGridLayoutManager(1)

        
        alwaysSkippedViewModel.skippedSongs.collectListWhenStarted({ adapterSongs != null }) { songs ->
            updateList(songs)
        }
    }

    override fun onDestroyView() {
        adapterSongs = null
        super.onDestroyView()
    }

     





    private fun updateList(songs: List<Audio>) {
        if (adapterSongs == null) {
            adapterSongs = AdapterSongs(songs)
            adapterSongs?.setHasStableIds(true)
            adapterSongs?.setGeneralAdapterCallbacks(object : GeneralAdapterCallbacks {
                override fun onSongClicked(songs: MutableList<Audio>, position: Int, view: View) {
                    
                    
                    val tappedSong = songs.getOrNull(position) ?: return
                    setMediaItems(listOf(tappedSong), 0)
                }

                override fun onSongLongClicked(audios: MutableList<Audio>, position: Int, imageView: ImageView?) {
                    
                    openSongsMenu(audios, position, imageView)
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
    }

    companion object {
        const val TAG = "AlwaysSkipped"

        fun newInstance(): AlwaysSkipped {
            return AlwaysSkipped().apply {
                arguments = Bundle()
            }
        }
    }
}