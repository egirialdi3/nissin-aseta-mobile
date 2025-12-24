//package id.aseta.app.viewmodel
//
//import android.bluetooth.BluetoothDevice
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateListOf
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.setValue
//import androidx.lifecycle.ViewModel
//import com.rfid.cf.CFDeviceManager
//import com.rfid.cf.RFIDDeviceCallback
//
//class BluetoothViewModel(
//    private val cfDeviceManager: CFDeviceManager
//) : ViewModel(), RFIDDeviceCallback {
//
//    private val _devices = mutableStateListOf<BluetoothDevice>()
//    val devices: List<BluetoothDevice> get() = _devices
//
//    var isScanning by mutableStateOf(false)
//        private set
//
//    var connectionStatus by mutableStateOf("Disconnected")
//        private set
//
//    init {
//        cfDeviceManager.setDeviceLog(this)
//    }
//
//    fun startScan() {
//        _devices.clear()
//        isScanning = true
//        cfDeviceManager.startScanBt()
//    }
//
//    fun stopScan() {
//        isScanning = false
//        cfDeviceManager.stopScanBt()
//    }
//
//    fun connectToDevice(device: BluetoothDevice) {
//        stopScan()
//        cfDeviceManager.connect(device.address)
//    }
//
//    override fun onBtScan(device: BluetoothDevice) {
//        if (_devices.none { it.address == device.address }) {
//            _devices.add(device)
//        }
//    }
//
//    override fun onBtConnectionChanged(status: String) {
//        connectionStatus = if (cfDeviceManager.isConnect()) {
//            "Connected: ${cfDeviceManager.getLastDeviceAddress().orEmpty()}"
//        } else {
//            "Disconnected"
//        }
//    }
//
//    override fun onRfidScan(rfid: String) {
//        // Handle RFID scan if necessary
//    }
//
//    override fun onStateChange(scanning: Boolean) {
//        isScanning = scanning
//    }
//
//    override fun onCleared() {
//        super.onCleared()
//        cfDeviceManager.stopScanBt()
//    }
//}
