class VolumeKnob : ScopedBottomSheetFragment() {
    private var audioManager: AudioManager? = null
    private lateinit var binding: DialogVolumeKnobBinding
    private val volumeFlow = MutableStateFlow(-1)
    private var volumeListener: VolumeListener? = null
    private val maxVolume: Int by lazy {
        audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
    }
    private val volumeObserver by lazy {
        object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                setVolumeKnobPosition()
            }
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogVolumeKnobBinding.inflate(inflater, container, false)
        requireActivity().volumeControlStream = AudioManager.STREAM_MUSIC
        audioManager = requireActivity().getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            volumeFlow
                .filter { it >= 0 }
                .distinctUntilChanged()
                .collect { index ->
                    audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0)
                }
        }
        setVolumeKnobPosition()
        binding.volumeKnob.setTickTexts("0", "100")
        binding.volumeKnob.setListener(object : FelicityKnobListener {
            override fun onIncrement(value: Float) {
                Log.d(TAG, "Increment: $value")
            }
            override fun onRotate(value: Float) {
                volumeFlow.value = ((value / 100.0f) * maxVolume).roundToInt()
            }
            override fun onUserInteractionStart(value: Float) {
                requireContext().contentResolver.unregisterContentObserver(volumeObserver)
            }
            override fun onUserInteractionEnd(value: Float) {
                postDelayed(delayMillis = 1000) {
                    requireContext().contentResolver.registerContentObserver(
                            Settings.System.CONTENT_URI, true, volumeObserver)
                }
            }
        })
        binding.equalizer.setOnClickListener {
            volumeListener?.onEqualizerClicked().also {
                dismiss()
            }
        }
        dialog?.setOnKeyListener { _, keyCode, event ->
            when (keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    if (event.action == KeyEvent.ACTION_DOWN) {
                        raiseByFivePercent()
                    }
                    true
                }
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    if (event.action == KeyEvent.ACTION_DOWN) {
                        lowerByFivePercent()
                    }
                    true
                }
                else -> false
            }
        }
    }
    private fun withUnregisteredVolumeObserver(action: () -> Unit) {
        requireContext().contentResolver.unregisterContentObserver(volumeObserver)
        try {
            action()
        } finally {
            postDelayed(delayMillis = 1000) {
                requireContext().contentResolver.registerContentObserver(
                        Settings.System.CONTENT_URI, true, volumeObserver)
            }
        }
    }
    private fun raiseByFivePercent() {
        withUnregisteredVolumeObserver {
            val currentVolume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)?.toFloat() ?: 0f
            val maxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)?.toFloat() ?: 1f
            val newVolume = (currentVolume + 0.05f * maxVolume).coerceAtMost(maxVolume)
            audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume.roundToInt(), 0)
            setVolumeKnobPosition()
        }
    }
    private fun lowerByFivePercent() {
        withUnregisteredVolumeObserver {
            val currentVolume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)?.toFloat() ?: 0f
            val maxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)?.toFloat() ?: 1f
            val newVolume = (currentVolume - 0.05f * maxVolume).coerceAtLeast(0f)
            audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume.roundToInt(), 0)
            setVolumeKnobPosition()
        }
    }
    override fun onStart() {
        super.onStart()
        requireContext().contentResolver.registerContentObserver(
                Settings.System.CONTENT_URI, true, volumeObserver)
    }
    override fun onStop() {
        super.onStop()
        requireContext().contentResolver.unregisterContentObserver(volumeObserver)
    }
    private fun setVolumeKnobPosition() {
        val current = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)?.toFloat() ?: 0f
        val max = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)?.toFloat() ?: 1f
        binding.volumeKnob.setKnobPosition((current / max) * 100f)
    }
    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
    fun setVolumeListener(listener: VolumeListener) {
        this.volumeListener = listener
    }
    companion object {
        fun newInstance(): VolumeKnob {
            val fragment = VolumeKnob()
            fragment.arguments = Bundle()
            return fragment
        }
        fun AppCompatActivity.showVolumeKnob(): VolumeKnob {
            if (!supportFragmentManager.isVolumeKnobShowing()) {
                val dialog = newInstance()
                dialog.show(supportFragmentManager, TAG)
                return dialog
            }
            return supportFragmentManager.findFragmentByTag(TAG) as VolumeKnob
        }
        private fun FragmentManager.isVolumeKnobShowing(): Boolean = findFragmentByTag(TAG) != null
        interface VolumeListener {
            fun onEqualizerClicked()
        }
        const val TAG = "VolumeKnob"
    }
}
