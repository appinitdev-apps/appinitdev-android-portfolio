
class RequestEnableBluetoothEvent: ChannelEvent()

class RequestScanConnectBluetoothEvent: ChannelEvent()

class RequestBluetoothLocationPermissionEvent: ChannelEvent()

class RequestBluetoothLocationGPSPermissionEvent: ChannelEvent()

class BluetoothPermissionResultEvent: ChannelEvent()

class BluetoothFindOneEvent(val mac: String): ChannelEvent()

class ScanBTDeviceTimeoutEvent: ChannelEvent()

class BTDeviceFoundEvent(val device: BTDevice): ChannelEvent()

