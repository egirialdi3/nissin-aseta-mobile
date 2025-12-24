package com.rfid.cf.h301.sdk

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.util.Log

@SuppressLint("MissingPermission")
class BluetoothLeService : Service() {
    private val binder = LocalBinder()
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private var scanning = false
    private val handler = Handler()

    private val SCAN_PERIOD: Long = 10000

    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
            }
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    inner class LocalBinder : Binder() {
        fun getService() : BluetoothLeService {
            return this@BluetoothLeService
        }
    }

    fun initialize(): Boolean {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
        if (bluetoothAdapter == null) {
            Log.e("H301", "Unable to obtain a BluetoothAdapter.")
            return false
        }
        return true
    }
    override fun onUnbind(intent: Intent?): Boolean {
        close()
        return super.onUnbind(intent)
    }
    private fun close() {
        bluetoothGatt?.let { gatt ->
            gatt.close()
            bluetoothGatt = null
        }
    }

    fun connect(address: String) {
        bluetoothAdapter?.let { adapter ->
            val device: BluetoothDevice? = try {
                 adapter.getRemoteDevice(address)
            } catch (_: IllegalArgumentException) {
                Log.w("H301", "Device not found with provided address.")
                null
            }
            bluetoothGatt = device?.connectGatt(this, false, bluetoothGattCallback)
        } ?: run {
            Log.w("H301", "BluetoothAdapter not initialized")
        }
    }
    fun disconnect(){
        bluetoothGatt?.disconnect()
    }

    fun scanLeDevice(leScanCallback: ScanCallback) {
        if (scanning) { return }
        handler.postDelayed({
            scanning = false
            bluetoothLeScanner?.stopScan(leScanCallback)
        }, SCAN_PERIOD)
        scanning = true
        bluetoothLeScanner?.startScan(leScanCallback)
    }

    fun stopScanLeDevice(leScanCallback: ScanCallback){
        scanning = false
        bluetoothLeScanner?.stopScan(leScanCallback)
    }
}