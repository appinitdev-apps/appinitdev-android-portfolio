class AudioPipelineDialog(container: ViewGroup, anchorView: View) : SharedDialogPopup(container, anchorView) {
    private lateinit var binding: DialogAudioStateSnapshotBinding
    companion object {
        private const val TAG = "AudioPipelineDialog"
        fun Fragment.showAudioPipeline(anchorView: View) {
            AudioPipelineDialog(
                    container = requireActivity().findViewById(R.id.app_container),
                    anchorView = anchorView
            ).show()
        }
    }
    override fun onCreateContentView(): View {
        binding = DialogAudioStateSnapshotBinding.inflate(LayoutInflater.from(context), null, false)
        return binding.root
    }
    override fun onDialogCreated() {
        AudioPipelineManager.snapshotFlow.value?.let {
            bindSnapshot(it)
        }
        AudioPipelineManager.requestRefresh()
        lifecycleScope.launch {
            AudioPipelineManager.snapshotFlow
                .filterNotNull()
                .collect { snapshot ->
                    Log.d(TAG, "Received new snapshot: $snapshot")
                    bindSnapshot(snapshot)
                }
        }
    }
    private fun bindSnapshot(snapshot: AudioPipelineSnapshot) {
        val b = binding
        b.valueFormat.text = createSpannedString(
                context.getString(R.string.format),
                snapshot.trackFormat.ifBlank { "—" })
        b.valueBitDepth.text = createSpannedString(
                context.getString(R.string.bit_depth),
                if (snapshot.bitDepth > 0) context.getString(R.string.format_bit_depth, snapshot.bitDepth) else "—")
        b.valueSampleRate.text = createSpannedString(
                context.getString(R.string.sample_rate),
                if (snapshot.sampleRateHz > 0) context.getString(R.string.format_hz, snapshot.sampleRateHz) else "—")
        b.valueBitrate.text = createSpannedString(
                context.getString(R.string.bitrate),
                if (snapshot.bitrateKbps > 0) context.getString(R.string.format_kbps, snapshot.bitrateKbps) else "—")
        b.valueChannels.text = createSpannedString(
                context.getString(R.string.channels),
                when (snapshot.channels) {
                    0 -> "—"
                    1 -> context.getString(R.string.channel_mono)
                    2 -> context.getString(R.string.channel_stereo)
                    else -> "${snapshot.channels}"
                })
        b.valueDecoderName.text = createSpannedString(
                context.getString(R.string.decoder_name),
                snapshot.decoderName.ifBlank { "—" })
        val inRateStr = if (snapshot.effectiveInputSampleRate > 0) context.getString(R.string.format_hz, snapshot.effectiveInputSampleRate) else "—"
        val outRateStr = if (snapshot.effectiveOutputSampleRate > 0) context.getString(R.string.format_hz, snapshot.effectiveOutputSampleRate) else "—"
        b.valueResamplerRates.text = createSpannedString(
                context.getString(R.string.io_rate),
                context.getString(R.string.format_io_rate, inRateStr, outRateStr)
        )
        b.valueResamplerType.text = createSpannedString(
                context.getString(R.string.resampler_type),
                snapshot.resamplerType)
        b.valueResamplerCutoff.text = createSpannedString(
                context.getString(R.string.resampler_cutoff),
                if (snapshot.resamplerCutoffHz > 0) {
                    val cutOffPercent: Int = snapshot.resamplerCutoffHz * 100 / snapshot.inputSampleRate
                    context.getString(R.string.format_hz_cutoff, snapshot.resamplerCutoffHz, cutOffPercent)
                } else {
                    "—"
                })
        b.valueResamplerQuality.text = createSpannedString(
                context.getString(R.string.quality),
                snapshot.resamplerQuality)
        b.valueDspFormat.text = createSpannedString(
                context.getString(R.string.pcm_format),
                snapshot.dspFormat.ifBlank { "—" })
        b.valueDspSampleRate.text = createSpannedString(
                context.getString(R.string.sample_rate),
                if (snapshot.dspSampleRate > 0) context.getString(R.string.format_hz, snapshot.dspSampleRate) else "—")
        b.valueEqPreset.text = createSpannedString(
                context.getString(R.string.eq_preset),
                snapshot.activeEqName ?: context.getString(R.string.disabled))
        b.valueStereoExpand.text = createSpannedString(
                context.getString(R.string.stereo_expand),
                context.getString(R.string.format_percent, snapshot.stereoExpandPercent))
        b.valueBuffers.text = createSpannedString(
                context.getString(R.string.buffers),
                snapshot.buffers.ifBlank { "—" })
        b.valueLatency.text = createSpannedString(
                context.getString(R.string.latency),
                context.getString(R.string.format_approx_ms, snapshot.latencyMs))
        b.valueVizLatency.text = createSpannedString(
                context.getString(R.string.visualizer_latency),
                context.getString(R.string.format_approx_ms, snapshot.visualizerLatencyMs))
        b.valueAudioOutputMode.text = createSpannedString(
                context.getString(R.string.audio_output_mode),
                snapshot.audioOutputMode.ifBlank { "—" })
        b.valueDeviceName.text = createSpannedString(
                context.getString(R.string.device_name),
                snapshot.deviceName.ifBlank { "—" })
        b.valueDeviceBitDepth.text = createSpannedString(
                context.getString(R.string.bit_depth),
                context.getString(R.string.format_bit_depth_io, snapshot.deviceBitDepthIn, snapshot.deviceBitDepthOut))
        b.valueDeviceSampleRate.text = createSpannedString(
                context.getString(R.string.sample_rate),
                if (snapshot.deviceSampleRate > 0) context.getString(R.string.format_hz, snapshot.deviceSampleRate) else "—")
    }
    private fun createSpannedString(label: String, value: String): Spanned {
        val separator = ": "
        val full = "$label$separator$value"
        val spannable = SpannableString(full)
        val valueStart = label.length + separator.length
        val primaryColor = ThemeManager.theme.textViewTheme.primaryTextColor
        val secondaryColor = ThemeManager.theme.textViewTheme.secondaryTextColor
        spannable.setSpan(
                ForegroundColorSpan(primaryColor),
                0, valueStart,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(
                ForegroundColorSpan(secondaryColor),
                valueStart, full.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannable
    }
}
