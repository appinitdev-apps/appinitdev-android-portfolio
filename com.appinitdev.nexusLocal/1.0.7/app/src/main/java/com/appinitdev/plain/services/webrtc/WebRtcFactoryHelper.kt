
private var webrtcInitialized = false

internal fun createWebRtcFactory(
    context: Context,
    eglBase: EglBase,
    projection: MediaProjection,
    onAudioRecordStart: () -> Unit,
    onAudioRecordStop: () -> Unit,
): Pair<PeerConnectionFactory, JavaAudioDeviceModule> {
    if (!webrtcInitialized) {
        
        
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context.applicationContext)
                .setEnableInternalTracer(false)
                .createInitializationOptions(),
        )
        webrtcInitialized = true
    }

    val adm = JavaAudioDeviceModule.builder(context)
        .setUseHardwareAcousticEchoCanceler(false)
        .setUseHardwareNoiseSuppressor(false)
        .setAudioRecordStateCallback(object : JavaAudioDeviceModule.AudioRecordStateCallback {
            override fun onWebRtcAudioRecordStart() = onAudioRecordStart()
            override fun onWebRtcAudioRecordStop() = onAudioRecordStop()
        })
        .createAudioDeviceModule()

    val encoderFactory = DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true)
    val decoderFactory = DefaultVideoDecoderFactory(eglBase.eglBaseContext)
    
    
    
    val options = PeerConnectionFactory.Options().apply {
        networkIgnoreMask = 1 shl 3
        
        
        
        
        disableNetworkMonitor = true
    }
    val factory = PeerConnectionFactory.builder()
        .setOptions(options)
        .setVideoEncoderFactory(encoderFactory)
        .setVideoDecoderFactory(decoderFactory)
        .setAudioDeviceModule(adm)
        .createPeerConnectionFactory()

    return Pair(factory, adm)
}
