
class CastViewModel : ViewModel() {
    private val _itemsFlow = MutableStateFlow(mutableStateListOf<DlnaDevice>())
    val itemsFlow: StateFlow<List<DlnaDevice>> get() = _itemsFlow
    var castMode = mutableStateOf(false)
    var showCastDialog = mutableStateOf(false)
    val isLoading = mutableStateOf(false)
    
    internal var positionUpdateJob: Job? = null

    fun enterCastMode() {
        castMode.value = true
        showCastDialog.value = false
    }

    fun selectDevice(device: DlnaDevice) {
        CastPlayer.currentDevice = device
    }

    fun exitCastMode() {
        castMode.value = false
        val device = CastPlayer.currentDevice ?: return
        viewModelScope.launch(Dispatchers.IO) {
            DlnaTransportController.stopAVTransportAsync(device)
            CastPlayer.isPlaying.value = false
            
            
            if (CastPlayer.sid.isNotEmpty()) {
                DlnaTransportController.unsubscribeEvent(device, CastPlayer.sid)
                CastPlayer.sid = ""
            }
            CastPlayer.supportsCallback.value = false
            CastPlayer.progress.value = 0f
            CastPlayer.duration.value = 0f
            
            
            positionUpdateJob?.cancel()
            positionUpdateJob = null
        }
    }

    fun cast(path: String) = castPath(path)

    fun cast(item: IMedia) = castItem(item)

    suspend fun searchAsync(context: Context) {
        DlnaDeviceScanner.search(context).flowOn(Dispatchers.IO).buffer().collect { device ->
            try {
                val client = HttpClient(CIO)
                val response = withIO { client.get(device.location) }
                if (response.status != HttpStatusCode.OK) {
                    return@collect
                }
                val xml = response.body<String>()
                LogCat.e(xml)
                device.update(xml)
                if (device.isAVTransport()) {
                    addDevice(device)
                }
            } catch (ex: Exception) {
                LogCat.e(ex.toString())
            }
        }
    }

    private fun addDevice(device: DlnaDevice) {
        if (!_itemsFlow.value.any { it.hostAddress == device.hostAddress }) {
            _itemsFlow.value.add(device)
        }
    }

    fun playCast() {
        val device = CastPlayer.currentDevice ?: return
        viewModelScope.launch(Dispatchers.IO) {
            DlnaTransportController.playAVTransportAsync(device)
            CastPlayer.isPlaying.value = true
        }
    }

    fun pauseCast() {
        val device = CastPlayer.currentDevice ?: return
        viewModelScope.launch(Dispatchers.IO) {
            DlnaTransportController.pauseAVTransportAsync(device)
            CastPlayer.isPlaying.value = false
        }
    }
}
