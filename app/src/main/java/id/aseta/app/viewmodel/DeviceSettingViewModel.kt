package id.aseta.app.viewmodel

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import com.rscja.deviceapi.RFIDWithUHFBLE
import dagger.hilt.android.lifecycle.HiltViewModel
//import id.aseta.app.ui.screen.rfid_scan.ConnectionState
//import id.aseta.app.ui.screen.rfid_scan.UHFEvent
import javax.inject.Inject

@HiltViewModel
class DeviceSettingViewModel @Inject constructor(
    private val application: Application
) : AndroidViewModel(application) {

    var uhf :RFIDWithUHFBLE? = null

    private val _deviceList = mutableStateListOf<BluetoothDevice>()
    val deviceList: List<BluetoothDevice> = _deviceList

    private val _isScanning = mutableStateOf(false)
    val isScanning: State<Boolean> = _isScanning

    private val _isConnected = mutableStateOf(false)
    val isConnected: State<Boolean> = _isConnected

    private val bluetoothAdapter: BluetoothAdapter? =
        (application.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        if (!device.name.isNullOrBlank() &&
                            _deviceList.none { it.address == device.address }) {
                            _deviceList.add(device)
                        }
                    }
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _isScanning.value = false
                }
            }
        }
    }

    init {
        uhf = RFIDWithUHFBLE.getInstance()
        uhf!!.init(application.applicationContext)

        // Register receiver saat ViewModel hidup
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        application.registerReceiver(receiver, filter)
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(getApplication(), permission) == PackageManager.PERMISSION_GRANTED
    }

    fun clearDeviceList() {
        _deviceList.clear()
    }

    fun startScan(context:Context) {
        // Stop any ongoing scan first
        stopScan(context)

        // Clear previous results
        clearDeviceList()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
                println("Permission BLUETOOTH_SCAN tidak diberikan ❌")
                return
            }
        }

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        bluetoothAdapter?.cancelDiscovery()
        bluetoothAdapter?.startDiscovery()
        _isScanning.value = true
    }

    fun stopScan(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
                println("Permission BLUETOOTH_SCAN tidak diberikan ❌")
                return
            }
        }

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        bluetoothAdapter?.cancelDiscovery()
        _isScanning.value = false
    }

    private val _connectedDevice = mutableStateOf("")
    val connectedDevice: State<String> = _connectedDevice

    fun connect(address: String,context:Context) {
        stopScan(context = context)
        uhf!!.connect(address)
        uhf!!.setBeep(true)
        _isScanning.value = true
        _isConnected.value = true
        _connectedDevice.value = address
    }

    override fun onCleared() {
        super.onCleared()
        application.unregisterReceiver(receiver)
    }



}
