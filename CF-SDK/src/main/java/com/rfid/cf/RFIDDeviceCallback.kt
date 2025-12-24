package com.rfid.cf

import android.bluetooth.BluetoothDevice
import com.rscja.deviceapi.interfaces.ConnectionStatus

interface RFIDDeviceCallback {
    fun onBtScan(device: BluetoothDevice)
    fun onBtConnectionChanged(status: String)
    fun onRfidScan(rfid: String)
    fun onStateChange(scanning: Boolean)
}