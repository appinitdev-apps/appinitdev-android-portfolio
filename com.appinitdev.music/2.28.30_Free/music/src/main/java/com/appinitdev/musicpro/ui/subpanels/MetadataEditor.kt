@AndroidEntryPoint
class MetadataEditor : MediaFragment() {
    private lateinit var binding: FragmentMetadataEditorBinding
    @Inject
    lateinit var lrcRepository: LrcRepository
    private val audio: Audio by lazy {
        requireArguments().parcelable<Audio>(BundleConstants.AUDIO)!!
    }
    private val viewModel: MetadataEditorViewModel by viewModels(
            extrasProducer = {
                defaultViewModelCreationExtras.withCreationCallback<MetadataEditorViewModel.Factory> {
                    it.create(audio)
                }
            }
    )
    private var pendingArtworkFile: File? = null
    private val pickArtwork = registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri == null) return@registerForActivityResult
        handlePickedArtwork(uri)
    }
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentMetadataEditorBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireHiddenMiniPlayer()
        populateFields()
        setupAlbumArtPicker()
        observeViewModel()
        registerMetadataSearchResult()
        registerLyricsSearchResult()
        binding.saveButton.setOnClickListener {
            saveMetadata()
        }
        binding.searchButton.setOnClickListener {
            openFragment(MetadataSearch.newInstance(audio), MetadataSearch.TAG)
        }
        binding.searchLyrics.setOnClickListener {
            openFragment(LyricsSearch.newInstanceForEditor(audio), LyricsSearch.TAG)
        }
    }
    private fun registerLyricsSearchResult() {
        parentFragmentManager.setFragmentResultListener(
                LyricsSearch.REQUEST_KEY_LYRICS_FOR_EDITOR,
                viewLifecycleOwner
        ) { _, bundle ->
            val plain = bundle.getString(LyricsSearch.KEY_PLAIN_LYRICS)
            val synced = bundle.getString(LyricsSearch.KEY_SYNCED_LYRICS)
            if (!plain.isNullOrBlank()) {
            }
            if (!synced.isNullOrBlank()) {
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    lrcRepository.saveLrcToFile(synced, audio.uri)
                }
                binding.lyricsInput.setText(synced)
            }
        }
    }
    private fun registerMetadataSearchResult() {
        parentFragmentManager.setFragmentResultListener(
                MetadataSearch.REQUEST_KEY_METADATA_RESULT,
                viewLifecycleOwner
        ) { _, bundle ->
            val result = bundle.parcelable<MetadataSearchResult>(MetadataSearch.KEY_RESULT)
            if (result != null) {
                applySearchResult(result)
            }
        }
    }
    private fun applySearchResult(result: MetadataSearchResult) {
        with(binding) {
            if (result.title.isNotBlank()) titleInput.setText(result.title)
            if (result.artist.isNotBlank()) artistInput.setText(result.artist)
            result.album?.takeIf { it.isNotBlank() }?.let { albumInput.setText(it) }
            result.year?.takeIf { it.isNotBlank() }?.let { yearInput.setText(it) }
            result.genre?.takeIf { it.isNotBlank() }?.let { genreInput.setText(it) }
        }
    }
    private fun populateFields() {
        with(binding) {
            titleInput.setText(audio.title.orEmpty())
            artistInput.setText(audio.artist.orEmpty())
            albumInput.setText(audio.album.orEmpty())
            albumArtistInput.setText(audio.albumArtist.orEmpty())
            yearInput.setText(audio.year.orEmpty())
            genreInput.setText(audio.genre.orEmpty())
            trackNumberInput.setText(audio.trackNumber.orEmpty())
            numTracksInput.setText(audio.numTracks.orEmpty())
            composerInput.setText(audio.composer.orEmpty())
            writerInput.setText(audio.writer.orEmpty())
            compilationInput.setText(audio.compilation.orEmpty())
            val discParts = audio.discNumber.orEmpty().split("/")
            discNumberInput.setText(discParts.getOrNull(0).orEmpty())
            discTotalInput.setText(discParts.getOrNull(1).orEmpty())
            rgTrackGainInput.setText(audio.replayGainTrackGain.orEmpty())
            rgTrackPeakInput.setText(audio.replayGainTrackPeak.orEmpty())
            rgAlbumGainInput.setText(audio.replayGainAlbumGain.orEmpty())
            rgAlbumPeakInput.setText(audio.replayGainAlbumPeak.orEmpty())
        }
        binding.albumArt.loadArtCoverWithPayload(audio)
    }
    private fun setupAlbumArtPicker() {
        val launchPicker = View.OnClickListener {
            pickArtwork.launch(PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.albumArt.setOnClickListener(launchPicker)
    }
    private fun handlePickedArtwork(uri: Uri) {
        runCatching {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
                ?: return
            val tempFile = File(requireContext().cacheDir, "pending_artwork_${System.currentTimeMillis()}.jpg")
            FileOutputStream(tempFile).use { out ->
                inputStream.copyTo(out)
            }
            inputStream.close()
            pendingArtworkFile = tempFile
            val bitmap = BitmapFactory.decodeFile(tempFile.absolutePath)
            binding.albumArt.setImageBitmap(bitmap)
        }.onFailure {
            Log.e(TAG, "Failed to load picked artwork: ${it.message}", it)
        }
    }
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isSaving.collect { saving ->
                binding.saveButton.isEnabled = !saving
                binding.saveButton.alpha = if (saving) 0.5f else 1.0f
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.saveResult.collect { result ->
                when (result) {
                    is MetadataEditorViewModel.SaveResult.Success -> {
                        showWarning(R.string.metadata_saved)
                        popBackStack()
                    }
                    is MetadataEditorViewModel.SaveResult.Error -> {
                        showWarning(result.message)
                    }
                }
            }
        }
    }
    private fun saveMetadata() {
        val discNum = binding.discNumberInput.text?.toString()?.trim().orEmpty()
        val discTotal = binding.discTotalInput.text?.toString()?.trim().orEmpty()
        val discString = when {
            discNum.isNotEmpty() && discTotal.isNotEmpty() -> "$discNum/$discTotal"
            discNum.isNotEmpty() -> discNum
            else -> null
        }
        val fields = MetadataWriter.Fields(
                title = binding.titleInput.text?.toString()?.trim(),
                artist = binding.artistInput.text?.toString()?.trim(),
                album = binding.albumInput.text?.toString()?.trim(),
                albumArtist = binding.albumArtistInput.text?.toString()?.trim(),
                year = binding.yearInput.text?.toString()?.trim(),
                trackNumber = binding.trackNumberInput.text?.toString()?.trim(),
                numTracks = binding.numTracksInput.text?.toString()?.trim(),
                discNumber = discString,
                genre = binding.genreInput.text?.toString()?.trim(),
                composer = binding.composerInput.text?.toString()?.trim(),
                writer = binding.writerInput.text?.toString()?.trim(),
                compilation = binding.compilationInput.text?.toString()?.trim(),
                comment = binding.commentInput.text?.toString()?.trim(),
                lyrics = binding.lyricsInput.text?.toString()?.trim(),
                replayGainTrackGain = binding.rgTrackGainInput.text?.toString()?.trim(),
                replayGainTrackPeak = binding.rgTrackPeakInput.text?.toString()?.trim(),
                replayGainAlbumGain = binding.rgAlbumGainInput.text?.toString()?.trim(),
                replayGainAlbumPeak = binding.rgAlbumPeakInput.text?.toString()?.trim(),
        )
        val updatedAudio = audio.copy().apply {
            setTitle(fields.title)
            artist = fields.artist
            album = fields.album
            albumArtist = fields.albumArtist
            year = fields.year
            trackNumber = fields.trackNumber
            numTracks = fields.numTracks
            discNumber = discString
            genre = fields.genre
            composer = fields.composer
            writer = fields.writer
            compilation = fields.compilation
            replayGainTrackGain = fields.replayGainTrackGain
            replayGainTrackPeak = fields.replayGainTrackPeak
            replayGainAlbumGain = fields.replayGainAlbumGain
            replayGainAlbumPeak = fields.replayGainAlbumPeak
        }
        viewModel.saveMetadata(fields, updatedAudio, pendingArtworkFile)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        pendingArtworkFile?.delete()
        pendingArtworkFile = null
    }
    companion object {
        const val TAG = "MetadataEditor"
        fun newInstance(audio: Audio): MetadataEditor {
            return MetadataEditor().apply {
                arguments = Bundle().apply {
                    putParcelable(BundleConstants.AUDIO, audio)
                }
            }
        }
    }
}