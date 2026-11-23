internal fun NearbyViewModel.startEventListening() {
    eventJob = viewModelScope.launch {
        Channel.sharedFlow.collect { event ->
            when (event) {
                is NearbyDeviceFoundEvent -> {
                    val existingIndex = nearbyDevices.indexOfFirst { it.id == event.device.id }
                    if (existingIndex >= 0) {
                        nearbyDevices[existingIndex] = event.device
                    } else {
                        nearbyDevices.add(event.device)
                    }
                }
                is PairingSuccessEvent -> {
                    pairingInProgress.removeIf { it == event.deviceId }
                    loadPairedDevicesAsync()
                }
                is PairingFailedEvent -> {
                    pairingInProgress.removeIf { it == event.deviceId }
                }
            }
        }
    }
}
internal fun NearbyViewModel.loadPairedDevicesAsync() {
    viewModelScope.launch(Dispatchers.IO) {
        loadAsync()
    }
}
internal fun NearbyViewModel.unpairDeviceAsync(deviceId: String) {
    viewModelScope.launch(Dispatchers.IO) {
        try {
            if (PeerManager.markUnpaired(deviceId)) {
                loadAsync()
            }
        } catch (e: Exception) {
            LogCat.e("Error unpairing device: ${e.message}")
        }
    }
}
internal fun NearbyViewModel.startPairingDevice(deviceId: String) {
    viewModelScope.launch(Dispatchers.IO) {
        val device = nearbyDevices.find { it.id == deviceId } ?: return@launch
        NearbyPairManager.startPairingAsync(device)
    }
}
