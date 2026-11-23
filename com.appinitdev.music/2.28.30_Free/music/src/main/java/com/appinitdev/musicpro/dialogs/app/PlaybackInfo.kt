@AndroidEntryPoint
class PlaybackInfo : ScopedBottomSheetFragment() {
    private lateinit var binding: DialogPlaybackInfoBinding
    private val audio: Audio by lazy {
        requireArguments().parcelable(BundleConstants.AUDIO)
            ?: throw IllegalArgumentException("Audio is required")
    }
    private val viewModel by viewModels<PlaybackInfoViewModel>(
            extrasProducer = {
                defaultViewModelCreationExtras.withCreationCallback<PlaybackInfoViewModel.Factory> {
                    it.create(audio = audio)
                }
            }
    )
    companion object {
        private const val TAG = "PlaybackInfo"
        fun newInstance(audio: Audio): PlaybackInfo {
            val args = Bundle()
            args.putParcelable(BundleConstants.AUDIO, audio)
            val fragment = PlaybackInfo()
            fragment.arguments = args
            return fragment
        }
        fun FragmentManager.showPlaybackInfo(audio: Audio) {
            val dialog = newInstance(audio)
            dialog.show(this, TAG)
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogPlaybackInfoBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.title.text = audio.getProperTitle()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stat.collect { stat ->
                    if (stat != null) {
                        binding.playCount.text = resources.getQuantityString(R.plurals.total_times, stat.playCount, stat.playCount)
                        binding.skipCount.text = resources.getQuantityString(R.plurals.total_times, stat.skipCount, stat.skipCount)
                        binding.replayCount.text = resources.getQuantityString(R.plurals.total_times, stat.replayCount, stat.replayCount)
                        binding.lastPlayed.text = if (stat.lastPlayed > 0) {
                            stat.lastPlayed.toDate()
                        } else {
                            getString(R.string.never_played)
                        }
                        binding.lastPlayedRelative.text = TimeUtils.getLocalizedRelativeTime(
                                stat.lastPlayed, LocaleUtils.getAppLocale())
                    } else {
                        binding.playCount.text = 0.toString()
                        binding.skipCount.text = 0.toString()
                        binding.replayCount.text = 0.toString()
                        binding.lastPlayed.text = getString(R.string.never_played)
                        binding.lastPlayedRelative.text = "—"
                    }
                }
            }
        }
        binding.clearStats.setOnClickListener {
            viewModel.clearStats()
        }
    }
}
