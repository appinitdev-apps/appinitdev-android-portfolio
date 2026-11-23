
 







@AndroidEntryPoint
class DefaultPlayer : BasePlayerFragment() {

    private lateinit var binding: FragmentDefaultPlayerBinding

    override val pager: FelicityPager
        get() = binding.pager

    override val count: TextView
        get() = binding.count

    override val mediaControls: FelicityMediaControls
        get() = binding.mediaControls

    override val queue: View
        get() = binding.queue

    override val search: View
        get() = binding.search

    override val menu: View
        get() = binding.menu

    override val repeat: ImageView
        get() = binding.repeat

    override val pcmInfo: TextView
        get() = binding.pcmInfo

    override val seekbar: WaveformSeekbar
        get() = binding.seekbar

    override val lyrics: View
        get() = binding.lyrics

    override val favorite: FavoriteButton
        get() = binding.favorite

    override val equalizer: View
        get() = binding.equalizer

    override val visualizerButton: View
        get() = binding.visualizerButton

    override val visualizer: FelicityVisualizer
        get() = binding.visualizer


    override val title: TextView
        get() = binding.title

    override val artist: TextView
        get() = binding.artist

    override val album: TextView
        get() = binding.album

    override val shuffle: HighlightTextView
        get() = binding.shuffle

    override val lrc: LrcLineView
        get() = binding.lrc

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDefaultPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lrc.setShowBackground(true)
    }

    companion object {
         




        fun newInstance(): DefaultPlayer {
            val args = Bundle()
            val fragment = DefaultPlayer()
            fragment.arguments = args
            return fragment
        }


        const val TAG = "DefaultPlayer"
    }
}