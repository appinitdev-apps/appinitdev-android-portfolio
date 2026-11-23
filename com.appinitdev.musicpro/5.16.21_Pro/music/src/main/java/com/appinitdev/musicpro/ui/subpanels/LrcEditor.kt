
 
























@AndroidEntryPoint
class LrcEditor : MediaFragment() {

    private lateinit var binding: FragmentLrcEditorBinding

    private val audio: Audio by lazy {
        requireArguments().parcelable<Audio>(BundleConstants.AUDIO)!!
    }

    private val viewModel: LrcEditorViewModel by viewModels(
            extrasProducer = {
                defaultViewModelCreationExtras.withCreationCallback<LrcEditorViewModel.Factory> {
                    it.create(audio)
                }
            }
    )

    private var adapter: AdapterLrcEditor? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentLrcEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireHiddenMiniPlayer()

        binding.name.text = audio.getProperTitle()
        binding.artist.text = audio.getProperArtists()

        setupRecyclerView()
        setupSeekbar()
        setupControls()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.entriesRecycler.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupSeekbar() {
        val durationMs = audio.duration
        binding.seekbar.setMax(durationMs.toFloat())
        binding.seekbar.setMin(0f)
        binding.seekbar.setDefaultIndicatorEnabled(false)

        binding.seekbar.setLeftLabelProvider { progress, _, _ ->
            DateUtils.formatElapsedTime(progress.toLong().div(1000))
        }

        binding.seekbar.setRightLabelProvider { _, _, max ->
            DateUtils.formatElapsedTime(max.toLong().div(1000))
        }

        binding.seekbar.setOnSeekChangeListener(object : FelicitySeekbar.OnSeekChangeListener {
            override fun onProgressChanged(
                    seekbar: FelicitySeekbar,
                    progress: Float,
                    fromUser: Boolean
            ) {
                if (fromUser) {
                    viewModel.seekTo(progress.toLong())
                }
            }

            override fun onStopTrackingTouch(seekbar: FelicitySeekbar) {
                viewModel.seekTo(seekbar.getProgress().toLong())
            }
        })
    }

    private fun setupControls() {
        binding.play.setOnClickListener {
            viewModel.flipPlayback()
        }

        binding.rewind.setOnClickListener {
            viewModel.seekRelative(-SEEK_JUMP_MS)
        }

        binding.forward.setOnClickListener {
            viewModel.seekRelative(SEEK_JUMP_MS)
        }

        binding.rewind.setOnLongClickListener {
            viewModel.seekTo(0L)
            true
        }

        binding.paste.setOnClickListener {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = clipboard?.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val text = clip.getItemAt(0).coerceToText(requireContext()).toString()
                if (text.isNotBlank()) {
                    viewModel.pasteLines(text)
                } else {
                    Toast.makeText(requireContext(), R.string.lrc_clipboard_empty, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), R.string.lrc_clipboard_empty, Toast.LENGTH_SHORT).show()
            }
        }

        binding.save.setOnClickListener {
            viewModel.saveLrc()
        }
    }

    private fun observeViewModel() {
        viewModel.getEntries().observe(viewLifecycleOwner) { entries ->
            if (adapter == null) {
                adapter = AdapterLrcEditor(
                        entries = entries,
                        onStamp = { index -> viewModel.stampTimestamp(index) },
                        onDelete = { index ->
                            viewModel.removeEntry(index)
                        }
                )
                binding.entriesRecycler.adapter = adapter
            } else {
                adapter?.submitList(entries)
            }
        }

        viewModel.getSeekPosition().observe(viewLifecycleOwner) { positionMs ->
            binding.seekbar.setProgress(positionMs.toFloat(), fromUser = false, animate = false)
        }

        viewModel.getDuration().observe(viewLifecycleOwner) { durationMs ->
            binding.seekbar.setMax(durationMs.toFloat())
        }

        viewModel.isPlayingLiveData().observe(viewLifecycleOwner) { isPlaying ->
            if (isPlaying) binding.play.setPlaying() else binding.play.setPaused()
        }

        viewModel.getSaved().observe(viewLifecycleOwner) { saved ->
            if (saved == true) {
                Toast.makeText(requireContext(), R.string.lyrics_saved, Toast.LENGTH_SHORT).show()
                
                parentFragmentManager.setFragmentResult(REQUEST_KEY_LRC_SAVED, Bundle())
                goBack()
            }
        }
    }

    
    
    

    override fun onAudio(audio: Audio) = Unit

    override fun onSeekChanged(seek: Long) = Unit

    override fun onPlaybackStateChanged(state: Int) = Unit

    override val wantsMiniPlayerVisible: Boolean
        get() = false

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
    }

    override fun onDestroyView() {
        adapter = null
        super.onDestroyView()
    }

    companion object {
        const val TAG = "LrcEditor"

         



        const val REQUEST_KEY_LRC_SAVED = "lrc_editor_lrc_saved"

         
        private const val SEEK_JUMP_MS = 2000L

         





        fun newInstance(audio: Audio): LrcEditor {
            return LrcEditor().also { fragment ->
                fragment.arguments = Bundle().apply {
                    putParcelable(BundleConstants.AUDIO, audio)
                }
            }
        }
    }
}