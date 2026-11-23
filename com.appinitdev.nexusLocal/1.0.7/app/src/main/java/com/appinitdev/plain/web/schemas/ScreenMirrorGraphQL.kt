fun SchemaBuilder.addScreenMirrorSchema() {
    query("screenMirrorState") {
        resolver { ->
            ScreenMirrorService.instance?.isRunning() == true
        }
    }
    query("screenMirrorControlEnabled") {
        resolver { ->
            PlainAccessibilityService.isEnabled()
        }
    }
    query("screenMirrorQuality") {
        resolver { ->
            ScreenMirrorQualityPreference.getValueAsync().toModel()
        }
    }
    mutation("startScreenMirror") {
        resolver { audio: Boolean ->
            ScreenMirrorService.qualityData = ScreenMirrorQualityPreference.getValueAsync()
            sendEvent(HStartScreenMirrorEvent(audio))
            true
        }
    }
    mutation("requestScreenMirrorAudio") {
        resolver { ->
            if (Permission.RECORD_AUDIO.can(MainApp.instance)) {
                true
            } else {
                sendEvent(HRequestScreenMirrorAudioEvent())
                false
            }
        }
    }
    mutation("stopScreenMirror") {
        resolver { ->
            ScreenMirrorService.instance?.stop()
            ScreenMirrorService.instance = null
            true
        }
    }
    mutation("updateScreenMirrorQuality") {
        resolver { mode: ScreenMirrorMode ->
            val resolution = when (mode) {
                ScreenMirrorMode.AUTO -> 1080
                ScreenMirrorMode.HD -> 1080
                ScreenMirrorMode.SMOOTH -> 720
            }
            val qualityData = DScreenMirrorQuality(mode, resolution)
            ScreenMirrorQualityPreference.putAsync(qualityData)
            ScreenMirrorService.qualityData = qualityData
            ScreenMirrorService.instance?.onQualityChanged()
            true
        }
    }
    mutation("sendWebRtcSignaling") {
        resolver { payload: WebRtcSignalingMessage, context: Context ->
            val call = context.get<ApplicationCall>()
            val clientId = call?.request?.header("c-id") ?: ""
            ScreenMirrorService.instance?.handleWebRtcSignaling(clientId, payload)
            true
        }
    }
    mutation("sendScreenMirrorControl") {
        resolver { input: ScreenMirrorControlInput ->
            val service = PlainAccessibilityService.instance
                ?: throw GraphQLError("Accessibility service is not enabled")
            val screenSize = PlainAccessibilityService.getScreenSize(MainApp.instance)
            service.dispatchControl(input, screenSize.x, screenSize.y)
            true
        }
    }
}
