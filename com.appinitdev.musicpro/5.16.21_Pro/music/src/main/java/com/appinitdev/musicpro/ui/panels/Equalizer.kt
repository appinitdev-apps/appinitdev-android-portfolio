
 




















class Equalizer : MediaFragment() {

    private lateinit var binding: FragmentEqualizerBinding
    private val viewModel: EqualizerViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentEqualizerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireHiddenMiniPlayer()

        setupViewFlipper(savedInstanceState)

        
        
        binding.equalizerScreen.equalizerSwitch.setOnCheckedChangeListener { _, isChecked ->
            EqualizerPreferences.setEqEnabled(isChecked)
            updateEqualizerEnabledState(isChecked)
        }

        binding.equalizerScreen.reset.setOnClickListener {
            withSureDialog { sure ->
                if (sure) {
                    EqualizerManager.resetAllBands()
                    EqualizerPreferences.setPreampDb(0f)
                }
            }
        }

        
        
        binding.equalizerScreen.savePreset.setOnClickListener {
            childFragmentManager.showSaveEqualizerPreset { name ->
                viewModel.savePreset(
                        name = name,
                        gains = EqualizerManager.getAllGains(),
                        preampDb = EqualizerManager.getPreamp(),
                        onDuplicate = {   },
                        onSuccess = {   }
                )
            }
        }

        
        
        binding.equalizerScreen.presetsButton.setOnClickListener {
            openFragment(EqualizerPresets.newInstance(), EqualizerPresets.TAG)
        }

        
        
        setupEqualizerSliderObservers()

        
        observeCurrentPresetName()

        
        
        viewLifecycleOwner.lifecycleScope.launch {
            val state = viewModel.initialState.filterNotNull().first()
            applyInitialState(state)
        }
    }

     







    private fun applyInitialState(state: EqualizerViewModel.EqualizerInitialState) {
        binding.equalizerScreen.equalizerSwitch.isChecked = state.isEqEnabled
        updateEqualizerEnabledState(state.isEqEnabled, animate = false)
        setupEqualizerSliders(state)
        setupEqKnobs(state)
        setupSpeakerKnobs(state)
        setupReverbKnobs(state)
    }

    fun setupViewFlipper(savedInstanceState: Bundle?) {
        val initialScreen = savedInstanceState?.getInt(SCREEN_STATE_KEY) ?: 0
        binding.viewFlipper.displayedChild = initialScreen

        binding.panelGroup.setButtons(
                listOf(
                        Button(iconResId = R.drawable.ic_tune_16dp),
                        Button(iconResId = R.drawable.ic_knob_16dp),
                        Button(iconResId = R.drawable.ic_speaker_16dp)
                )
        )

        binding.panelGroup.setSelectedIndex(initialScreen, animate = false, notifyListener = false)
        setHeaderTitle(initialScreen)

        binding.panelGroup.setOnButtonSelectedListener { index ->
            binding.viewFlipper.displayedChild = index
        }

        binding.viewFlipper.setOnScreenChangedListener { index ->
            binding.panelGroup.setSelectedIndex(index, animate = true, notifyListener = false)
            setHeaderTitle(index)
        }
    }

    
    
    

     




    private fun setupEqualizerSliderObservers() {
        binding.equalizerScreen.equalizerSliders.setOnBandChangedListener { bandIndex, gain, fromUser ->
            if (fromUser) {
                Log.d(TAG, "Band $bandIndex changed to ${gain}dB by user")
                if (bandIndex == FelicityEqualizerSliders.PREAMP_BAND_INDEX) {
                    EqualizerManager.setPreamp(gain)
                } else {
                    EqualizerManager.setBandGain(bandIndex, gain)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                EqualizerManager.bandGainsFlow.collect { gains ->
                    binding.equalizerScreen.equalizerSliders.setAllGains(gains, animate = true)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                EqualizerManager.preampFlow.collect { db ->
                    binding.equalizerScreen.equalizerSliders.setPreampGain(db, animate = true)
                }
            }
        }
    }

     




    private fun setupEqualizerSliders(state: EqualizerViewModel.EqualizerInitialState) {
        binding.equalizerScreen.equalizerSliders.setAllGains(state.bandGains, animate = false)
        binding.equalizerScreen.equalizerSliders.setPreampGain(state.preampDb, animate = false)
    }

    private fun updateEqualizerEnabledState(isEnabled: Boolean, animate: Boolean = true) {
        if (animate) {
            binding.equalizerScreen.equalizerSliders
                .animate()
                .alpha(if (isEnabled) 1f else 0.5f)
                .setDuration(300)
                .start()

            binding.equalizerScreen.equalizerSliders.isEnabled = isEnabled
        } else {
            binding.equalizerScreen.equalizerSliders.alpha = if (isEnabled) 1f else 0.5f
            binding.equalizerScreen.equalizerSliders.isEnabled = isEnabled
        }
    }

     




    private fun observeCurrentPresetName() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentPresetName.collect { name ->
                    binding.equalizerScreen.presetName.setTextWithFade(name)
                }
            }
        }
    }

    
    
    

     




    private fun setupEqKnobs(state: EqualizerViewModel.EqualizerInitialState) {
        
        
        binding.equalizerScreen.bassKnob.centerSnapEnabled = true
        binding.equalizerScreen.bassKnob.setTickTexts("-12", "+12")
        binding.equalizerScreen.bassKnob.divisionCount = 48 * 2
        binding.equalizerScreen.bassKnob.setKnobPosition(bassDbToKnobValue(state.bassDb), animate = false)
        binding.equalizerScreen.bassKnob.setListener(object : FelicityKnobListener {
            override fun onIncrement(value: Float) {}

            override fun onRotate(value: Float) {
                val db = knobValueToBassDb(value)
                EqualizerPreferences.setBassDb(db)
                Log.d(TAG, "Bass gain updated: ${db}dB")
            }

            override fun onLabel(value: Float): String {
                val db = knobValueToBassDb(value)
                return when {
                    db > 0.05f -> "+${"%.1f".format(db)} dB"
                    db < -0.05f -> "${"%.1f".format(db)} dB"
                    else -> "0 dB"
                }
            }
        })

        
        
        binding.equalizerScreen.trebleKnob.centerSnapEnabled = true
        binding.equalizerScreen.trebleKnob.setTickTexts("-12", "+12")
        binding.equalizerScreen.trebleKnob.divisionCount = 48 * 2
        binding.equalizerScreen.trebleKnob.setKnobPosition(trebleDbToKnobValue(state.trebleDb), animate = false)
        binding.equalizerScreen.trebleKnob.setListener(object : FelicityKnobListener {
            override fun onIncrement(value: Float) {}

            override fun onRotate(value: Float) {
                val db = knobValueToTrebleDb(value)
                EqualizerPreferences.setTrebleDb(db)
                Log.d(TAG, "Treble gain updated: ${db}dB")
            }

            override fun onLabel(value: Float): String {
                val db = knobValueToTrebleDb(value)
                return when {
                    db > 0.05f -> "+${"%.1f".format(db)} dB"
                    db < -0.05f -> "${"%.1f".format(db)} dB"
                    else -> "0 dB"
                }
            }
        })
    }

    
    
    

     



    private fun setupSpeakerKnobs(state: EqualizerViewModel.EqualizerInitialState) {
        
        
        binding.speakerScreen.balanceKnob.centerSnapEnabled = true
        binding.speakerScreen.balanceKnob.setTickTexts("L", "R")
        binding.speakerScreen.balanceKnob.setKnobPosition(panToKnobValue(state.balance), animate = false)
        binding.speakerScreen.balanceKnob.setListener(object : FelicityKnobListener {
            override fun onIncrement(value: Float) {}

            override fun onRotate(value: Float) {
                val pan = knobValueToPan(value)
                EqualizerPreferences.setBalance(pan)
                Log.d(TAG, "Balance updated: pan=$pan")
            }

            override fun onLabel(value: Float): String {
                val pan = knobValueToPan(value)
                return when {
                    pan < -0.02f -> "L ${"%.0f".format(-pan * 100)}%"
                    pan > 0.02f -> "R ${"%.0f".format(pan * 100)}%"
                    else -> "C"
                }
            }
        })

        
        
        binding.speakerScreen.stereoWideningKnob.centerSnapEnabled = true
        binding.speakerScreen.stereoWideningKnob.setTickTexts("M", "W")
        binding.speakerScreen.stereoWideningKnob.setKnobPosition(widthToKnobValue(state.stereoWidth), animate = false)
        binding.speakerScreen.stereoWideningKnob.divisionCount = 10 * 10
        binding.speakerScreen.stereoWideningKnob.setListener(object : FelicityKnobListener {
            override fun onIncrement(value: Float) {}

            override fun onRotate(value: Float) {
                val width = knobValueToWidth(value)
                EqualizerPreferences.setStereoWidth(width)
                Log.d(TAG, "Stereo width updated: width=$width")
            }

            override fun onLabel(value: Float): String {
                val width = knobValueToWidth(value)
                return when {
                    width < 0.02f -> "Mono"
                    width in 0.98f..1.02f -> getString(R.string.normal)
                    width > 1.0f -> "+${"%.0f".format((width - 1f) * 100)}%"
                    else -> "-${"%.0f".format((1f - width) * 100)}%"
                }
            }
        })

        
        
        binding.speakerScreen.tapeSaturationKnob.setTickTexts("0", "4")
        binding.speakerScreen.tapeSaturationKnob.setKnobPosition(driveToKnobValue(state.tapeSaturationDrive), animate = false)
        binding.speakerScreen.tapeSaturationKnob.divisionCount = 4 * 10
        binding.speakerScreen.tapeSaturationKnob.setListener(object : FelicityKnobListener {
            override fun onIncrement(value: Float) {}

            override fun onRotate(value: Float) {
                val drive = knobValueToDrive(value)
                EqualizerPreferences.setTapeSaturationDrive(drive)
                Log.d(TAG, "Tape saturation drive updated: drive=$drive")
            }

            override fun onLabel(value: Float): String {
                val drive = knobValueToDrive(value)
                return if (drive < 0.05f) "Off" else "%.1f".format(drive)
            }
        })

        
        
        
        binding.speakerScreen.pitchKnob.centerSnapEnabled = true
        binding.speakerScreen.pitchKnob.setTickTexts("-12", "+12")
        binding.speakerScreen.pitchKnob.divisionCount = 24  
        binding.speakerScreen.pitchKnob.setKnobPosition(pitchSemitonesToKnobValue(state.pitch), animate = false)
        binding.speakerScreen.pitchKnob.setListener(object : FelicityKnobListener {
            override fun onIncrement(value: Float) {}

            override fun onRotate(value: Float) {
                
                
            }

            override fun onUserInteractionEnd(value: Float) {
                val semitones = knobValueToPitchSemitones(value)
                EqualizerPreferences.setPitch(semitones)
                Log.d(TAG, "Pitch updated: ${semitones} st → ×${"%.3f".format(pitchSemitonesToMultiplier(semitones))}")

                
                
                if (EqualizerPreferences.isPitchSpeedLocked()) {
                    val linkedSpeed = pitchSemitonesToMultiplier(semitones)
                    EqualizerPreferences.setPlaybackSpeed(linkedSpeed)
                    binding.speakerScreen.speedKnob.setKnobPosition(speedToKnobValue(linkedSpeed), animate = true)
                    Log.d(TAG, "Pitch lock → speed synced to ${linkedSpeed}x")
                }
            }

            override fun onLabel(value: Float): String {
                val st = knobValueToPitchSemitones(value)
                return when {
                    st > 0.05f -> "+${"%.1f".format(st)} st"
                    st < -0.05f -> "${"%.1f".format(st)} st"
                    else -> getString(R.string.normal)
                }
            }
        })

        
        
        
        binding.speakerScreen.speedKnob.centerSnapEnabled = true
        binding.speakerScreen.speedKnob.setTickTexts("0.5x", "2x")
        binding.speakerScreen.speedKnob.divisionCount = 100
        binding.speakerScreen.speedKnob.setKnobPosition(speedToKnobValue(state.playbackSpeed), animate = false)
        binding.speakerScreen.speedKnob.setListener(object : FelicityKnobListener {
            override fun onIncrement(value: Float) {}

            override fun onRotate(value: Float) {
                
            }

            override fun onUserInteractionEnd(value: Float) {
                val speed = knobValueToSpeed(value)
                EqualizerPreferences.setPlaybackSpeed(speed)
                Log.d(TAG, "Playback speed updated: ${speed}x")

                
                
                if (EqualizerPreferences.isPitchSpeedLocked()) {
                    val linkedSemitones = log2(speed.toDouble()).toFloat() * 12f
                    EqualizerPreferences.setPitch(linkedSemitones)
                    binding.speakerScreen.pitchKnob.setKnobPosition(pitchSemitonesToKnobValue(linkedSemitones), animate = true)
                    Log.d(TAG, "Speed lock → pitch synced to $linkedSemitones st")
                }
            }

            override fun onLabel(value: Float): String {
                val speed = knobValueToSpeed(value)
                return when {
                    speed in 0.99f..1.01f -> getString(R.string.normal)
                    else -> "${"%.2f".format(speed)}x"
                }
            }
        })

        
        
        val lockButton = binding.speakerScreen.lockPitchSpeedButton
        lockButton.setImageResource(
                if (state.pitchSpeedLocked) R.drawable.ic_locked_16dp else R.drawable.ic_unlocked_16dp
        )

        lockButton.setOnClickListener {
            val nowLocked = !EqualizerPreferences.isPitchSpeedLocked()
            EqualizerPreferences.setPitchSpeedLocked(nowLocked)
            lockButton.setImageResource(if (nowLocked) R.drawable.ic_locked_16dp else R.drawable.ic_unlocked_16dp)
            Log.d(TAG, "Pitch/speed lock toggled: $nowLocked")
        }

        
        
        
        binding.speakerScreen.replayGainKnob.centerSnapEnabled = true
        binding.speakerScreen.replayGainKnob.setTickTexts("-15", "+15")
        binding.speakerScreen.replayGainKnob.divisionCount = 30 * 2
        binding.speakerScreen.replayGainKnob.setKnobPosition(replayGainDbToKnobValue(state.replayGainDb), animate = false)
        binding.speakerScreen.replayGainKnob.setListener(object : FelicityKnobListener {
            override fun onIncrement(value: Float) {}

            override fun onRotate(value: Float) {
                val db = knobValueToReplayGainDb(value)
                EqualizerPreferences.setReplayGainDb(db)
                Log.d(TAG, "Replay gain updated: ${db}dB")
            }

            override fun onLabel(value: Float): String {
                val db = knobValueToReplayGainDb(value)
                return when {
                    db > 0.05f -> "+${"%.1f".format(db)} dB"
                    db < -0.05f -> "${"%.1f".format(db)} dB"
                    else -> "0 dB"
                }
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SCREEN_STATE_KEY, binding.viewFlipper.displayedChild)
    }

    
    
    

     






    private fun setupReverbKnobs(state: EqualizerViewModel.EqualizerInitialState) {
        
        
        binding.reverbScreen.reverbMixKnob.setTickTexts("0%", "100%")
        binding.reverbScreen.reverbMixKnob.divisionCount = 100
        binding.reverbScreen.reverbMixKnob.setKnobPosition(
                reverbMixToKnob(state.reverbMix), animate = false)
        binding.reverbScreen.reverbMixKnob.setListener(object : FelicityKnobListener {
            override fun onIncrement(value: Float) {}

            override fun onRotate(value: Float) {
                val mix = knobToReverbMix(value)
                EqualizerPreferences.setReverbMix(mix)
                Log.d(TAG, "Reverb mix updated: ${(mix * 100).toInt()}%")
            }

            override fun onLabel(value: Float): String {
                val mix = knobToReverbMix(value)
                return if (mix < 0.005f) "Off" else "${"%.0f".format(mix * 100)}%"
            }
        })

        
        
        binding.reverbScreen.reverbFadeKnob.setTickTexts("S", "L")
        binding.reverbScreen.reverbFadeKnob.divisionCount = 100
        binding.reverbScreen.reverbFadeKnob.setKnobPosition(
                reverbDecayToKnob(state.reverbDecay), animate = false)
        binding.reverbScreen.reverbFadeKnob.setListener(object : FelicityKnobListener {
            override fun onIncrement(value: Float) {}

            override fun onRotate(value: Float) {
                val decay = knobToReverbDecay(value)
                EqualizerPreferences.setReverbDecay(decay)
                Log.d(TAG, "Reverb decay updated: ${"%.2f".format(decay)}")
            }

            override fun onLabel(value: Float): String {
                val decay = knobToReverbDecay(value)
                return when {
                    decay < 0.05f -> "Short"
                    decay > 0.95f -> "Long"
                    else -> "${"%.0f".format(decay * 100)}%"
                }
            }
        })

        
        
        binding.reverbScreen.reverbSizeKnob.setTickTexts("S", "L")
        binding.reverbScreen.reverbSizeKnob.divisionCount = 100
        binding.reverbScreen.reverbSizeKnob.setKnobPosition(
                reverbSizeToKnob(state.reverbSize), animate = false)
        binding.reverbScreen.reverbSizeKnob.setListener(object : FelicityKnobListener {
            override fun onIncrement(value: Float) {}

            override fun onRotate(value: Float) {
                val size = knobToReverbSize(value)
                EqualizerPreferences.setReverbSize(size)
                Log.d(TAG, "Reverb size updated: ${"%.2f".format(size)}")
            }

            override fun onLabel(value: Float): String {
                val size = knobToReverbSize(value)
                return when {
                    size < 0.05f -> "S"
                    size > 0.95f -> "L"
                    else -> "${"%.0f".format(size * 100)}%"
                }
            }
        })

        
        
        
        binding.reverbScreen.reverbDampKnob.setTickTexts("B", "D")
        binding.reverbScreen.reverbDampKnob.divisionCount = 100
        binding.reverbScreen.reverbDampKnob.setKnobPosition(
                reverbDampToKnob(state.reverbDamp), animate = false)
        binding.reverbScreen.reverbDampKnob.setListener(object : FelicityKnobListener {
            override fun onIncrement(value: Float) {}

            override fun onRotate(value: Float) {
                val damp = knobToReverbDamp(value)
                EqualizerPreferences.setReverbDamp(damp)
                Log.d(TAG, "Reverb damp updated: ${"%.2f".format(damp)}")
            }

            override fun onLabel(value: Float): String {
                val damp = knobToReverbDamp(value)
                return when {
                    damp < 0.05f -> "Bright"
                    damp > 0.95f -> "Dark"
                    else -> "${"%.0f".format(damp * 100)}%"
                }
            }
        })
    }

    private fun setHeaderTitle(position: Int) {
        val title = when (position) {
            0 -> getString(R.string.equalizer)
            1 -> getString(R.string.output)
            2 -> getString(R.string.reverb)
            else -> ""
        }

        binding.eqTitle.setTextWithFade(title)
    }

    override val wantsMiniPlayerVisible: Boolean
        get() = false

    companion object {
        fun newInstance(): Equalizer {
            val args = Bundle()
            val fragment = Equalizer()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "Equalizer"

         
        fun knobValueToPan(knobValue: Float): Float = ((knobValue - 50f) / 50f).coerceIn(-1f, 1f)

         
        fun panToKnobValue(pan: Float): Float = ((pan * 50f) + 50f).coerceIn(0f, 100f)

         
        fun knobValueToWidth(knobValue: Float): Float = (knobValue / 50f).coerceIn(0f, 2f)

         
        fun widthToKnobValue(width: Float): Float = (width * 50f).coerceIn(0f, 100f)

         
        fun knobValueToDrive(knobValue: Float): Float = (knobValue / 100f * 4f).coerceIn(0f, 4f)

         
        fun driveToKnobValue(drive: Float): Float = (drive / 4f * 100f).coerceIn(0f, 100f)

         
        fun knobValueToBassDb(knobValue: Float): Float = ((knobValue - 50f) / 50f * 12f).coerceIn(-12f, 12f)

         
        fun bassDbToKnobValue(db: Float): Float = ((db / 12f * 50f) + 50f).coerceIn(0f, 100f)

         
        fun knobValueToTrebleDb(knobValue: Float): Float = ((knobValue - 50f) / 50f * 12f).coerceIn(-12f, 12f)

         
        fun trebleDbToKnobValue(db: Float): Float = ((db / 12f * 50f) + 50f).coerceIn(0f, 100f)

         
        fun knobToReverbMix(knobValue: Float): Float = (knobValue / 100f).coerceIn(0f, 1f)

         
        fun reverbMixToKnob(mix: Float): Float = (mix * 100f).coerceIn(0f, 100f)

         
        fun knobToReverbDecay(knobValue: Float): Float = (knobValue / 100f).coerceIn(0f, 1f)

         
        fun reverbDecayToKnob(decay: Float): Float = (decay * 100f).coerceIn(0f, 100f)

         
        fun knobToReverbSize(knobValue: Float): Float = (knobValue / 100f).coerceIn(0f, 1f)

         
        fun reverbSizeToKnob(size: Float): Float = (size * 100f).coerceIn(0f, 100f)

         
        fun knobToReverbDamp(knobValue: Float): Float = (knobValue / 100f).coerceIn(0f, 1f)

         
        fun reverbDampToKnob(damp: Float): Float = (damp * 100f).coerceIn(0f, 100f)

         






        fun knobValueToPitchSemitones(knobValue: Float): Float =
            ((knobValue - 50f) / 50f * 12f).coerceIn(-12f, 12f)

         
        fun pitchSemitonesToKnobValue(semitones: Float): Float =
            ((semitones / 12f) * 50f + 50f).coerceIn(0f, 100f)

         





        fun pitchSemitonesToMultiplier(semitones: Float): Float =
            2f.pow(semitones / 12f).coerceIn(0.5f, 2.0f)

         







        fun knobValueToSpeed(knobValue: Float): Float =
            2f.pow((knobValue - 50f) / 50f).coerceIn(0.5f, 2.0f)

         
        fun speedToKnobValue(speed: Float): Float =
            (ln(speed.toDouble()) / ln(2.0) * 50.0 + 50.0).toFloat().coerceIn(0f, 100f)

         
        fun knobValueToReplayGainDb(knobValue: Float): Float =
            ((knobValue - 50f) / 50f * 15f).coerceIn(-15f, 15f)

         
        fun replayGainDbToKnobValue(db: Float): Float =
            ((db / 15f * 50f) + 50f).coerceIn(0f, 100f)

        private const val SCREEN_STATE_KEY = "screen_state"
    }
}