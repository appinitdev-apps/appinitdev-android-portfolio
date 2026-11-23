
internal open class SimpleSdpObserver : org.webrtc.SdpObserver {
    override fun onCreateSuccess(description: SessionDescription) = Unit
    override fun onSetSuccess() = Unit
    override fun onCreateFailure(error: String) {
        LogCat.e("webrtc: sdp create failed: $error")
    }

    override fun onSetFailure(error: String) {
        LogCat.e("webrtc: sdp set failed: $error")
    }
}
