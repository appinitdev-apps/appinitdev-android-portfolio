
class NearbyViewModel : ViewModel() {
    val nearbyDevices = mutableStateListOf<DNearbyDevice>()
    val pairedDevices = mutableStateListOf<DPeer>()
    var isDiscovering = mutableStateOf(false)
    val pairingInProgress = mutableStateListOf<String>()

    internal var eventJob: Job? = null
    private var cleanupJob: Job? = null

    init {
        startEventListening()
        loadPairedDevicesAsync()
    }

    internal suspend fun loadAsync() {
        val peers = AppDatabase.instance.peerDao().getAll()
        pairedDevices.clear()
        pairedDevices.addAll(peers)
    }

    fun toggleDiscovering() {
        if (isDiscovering.value) {
            stopDiscovering()
        } else {
            startDiscovering()
        }
    }

    private fun startDiscovering() {
        isDiscovering.value = true
        sendEvent(StartNearbyDiscoveryEvent())
        startDeviceCleanup()
    }

    private fun stopDiscovering() {
        isDiscovering.value = false
        sendEvent(StopNearbyDiscoveryEvent())
        stopDeviceCleanup()
    }

    private fun startDeviceCleanup() {
        cleanupJob = viewModelScope.launch {
            while (isDiscovering.value) {
                delay(20000)
                val currentTime = TimeHelper.now()
                nearbyDevices.removeIf { (currentTime - it.lastSeen).inWholeSeconds > 60 }
            }
        }
    }

    private fun stopDeviceCleanup() {
        cleanupJob?.cancel()
        cleanupJob = null
    }

    fun startPairing(device: DNearbyDevice) {
        pairingInProgress.add(device.id)
        startPairingDevice(device.id)
    }

    fun unpairDevice(deviceId: String) {
        unpairDeviceAsync(deviceId)
    }

    fun cancelPairing(deviceId: String) {
        pairingInProgress.removeIf { it == deviceId }
        NearbyPairManager.cancelPairing(deviceId)
    }

    fun startPairingFromDevice(device: DNearbyDevice) {
        if (!nearbyDevices.any { it.id == device.id }) {
            nearbyDevices.add(device)
        }
        startPairing(device)
    }

    suspend fun getQrDataAsync(): DQrPairData {
        val context = MainApp.instance
        val allIps = NetworkHelper.getDeviceIP4s().toList()
        return DQrPairData(
            id = TempData.clientId,
            name = TempData.deviceName.value,
            port = TempData.httpsPort.value,
            deviceType = PhoneHelper.getDeviceType(context),
            ips = allIps,
        )
    }

    fun isPaired(deviceId: String): Boolean {
        return pairedDevices.any { it.id == deviceId && it.status == "paired" }
    }

    fun isPairing(deviceId: String): Boolean {
        return pairingInProgress.contains(deviceId)
    }

    override fun onCleared() {
        super.onCleared()
        eventJob?.cancel()
        cleanupJob?.cancel()
        sendEvent(StopNearbyDiscoveryEvent())
    }
}
