package com.rfid.cf.h301

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.rfid.cf.RFIDClass
import com.rfid.cf.RFIDDeviceCallback
import com.rfid.cf.RFIDReadType
import com.rfid.cf.h301.sdk.BluetoothLeService

class H301 : RFIDClass() {

    private var bluetoothService : BluetoothLeService? = null
    private lateinit var gattServiceIntent: Intent
    private val detectedBt: HashMap<String, BluetoothDevice> = HashMap()
    private lateinit var callback: RFIDDeviceCallback

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            componentName: ComponentName,
            service: IBinder
        ) {
            bluetoothService = (service as BluetoothLeService.LocalBinder).getService()
            bluetoothService?.let { bluetooth ->
                if (!bluetooth.initialize()) {
                    Log.e("H301", "Unable to initialize Bluetooth")
                }
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            bluetoothService = null
        }
    }
    private val leScanCallback: ScanCallback = object : ScanCallback(){
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            val device: BluetoothDevice = result?.device ?: return
            if (!detectedBt.containsKey(device.address)){
                detectedBt[device.address] = device
                callback.onBtScan(device)
            }
        }
    }

    override fun open(context: Context) {
        gattServiceIntent = Intent(context, BluetoothLeService::class.java)
        context.bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun close() {
        stopInventory()
        bluetoothService?.onUnbind(gattServiceIntent)
    }

    override fun connect(address: String) {
        stopScanBt()
        bluetoothService?.connect(address)
    }

    override fun disconnect() {
        bluetoothService?.disconnect()
    }

    override fun startInventory(readType: RFIDReadType): Boolean {
        TODO("Not yet implemented")
    }

    override fun stopInventory() {
        TODO("Not yet implemented")
    }

    override fun getBattery(): Int {
        TODO("Not yet implemented")
    }

    override fun getPower(): Int {
        TODO("Not yet implemented")
    }

    override fun getMaxPower(): Int {
        TODO("Not yet implemented")
    }

    override fun setPower(power: Int) {
        TODO("Not yet implemented")
    }

    override fun setDeviceLog(callback: RFIDDeviceCallback) {
        this.callback = callback
    }

    override fun startScanBt() {
        bluetoothService?.scanLeDevice(leScanCallback)
    }

    override fun stopScanBt() {
        bluetoothService?.stopScanLeDevice(leScanCallback)
    }

    override fun isConnect(): Boolean {
        return false
    }
}