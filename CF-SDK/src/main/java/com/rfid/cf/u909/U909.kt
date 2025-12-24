package com.rfid.cf.u909

import android.content.Context
import android.content.SharedPreferences
import com.rfid.cf.RFIDClass
import com.rfid.cf.RFIDDeviceCallback
import com.rfid.cf.RFIDReadType
import com.rscja.deviceapi.RFIDWithUHFBLE
import com.rscja.deviceapi.entity.UHFTAGInfo
import com.rscja.deviceapi.interfaces.ConnectionStatus
import java.util.Timer
import java.util.TimerTask


class U909(private val uhf: RFIDWithUHFBLE, private val sharedPreferences: SharedPreferences) : RFIDClass() {

    private var callback: RFIDDeviceCallback? =null
    private var timer: Timer? = null
    private var onScanning = false

    override fun open(context: Context) {
        uhf.init(context)
        uhf.setEPCAndTIDMode()
        uhf.setKeyEventCallback {
            if (onScanning){
                stopInventory()
                return@setKeyEventCallback
            }
            startInventory(RFIDReadType.Continuous)
        }
        if (isAutoReconnect(sharedPreferences) && !getLastDeviceAddress(sharedPreferences).isNullOrEmpty()){
            connect(getLastDeviceAddress(sharedPreferences)!!)
        }
    }

    override fun close() {
        disconnect()
        uhf.free()
    }

    override fun connect(address: String) {
        stopScanBt()
        uhf.connect(address) { connectionStatus, _ ->
            if (connectionStatus == ConnectionStatus.CONNECTED){
                setLastDeviceAddress(sharedPreferences, address)
            }
            callback?.onBtConnectionChanged(connectionStatus.toString())
        }
    }

    override fun disconnect() {
        uhf.disconnect()
    }

    fun trimLeadingAndTrailingChars(input: String, numChars: Int): String {
        if (input.length <= numChars * 2) {
            // If the string is shorter than or equal to twice the number of characters to trim, return an empty string
            return ""
        }
        if(input.length >24){
            return input.substring(numChars, input.length - numChars)

        }else{
            return input
        }
    }

    override fun startInventory(readType: RFIDReadType): Boolean {
        if (uhf.connectStatus != ConnectionStatus.CONNECTED && !onScanning){ return false }

        onScanning = uhf.startInventoryTag()
        if (!onScanning){ return false }
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                try {
                    val rfid: String = trimLeadingAndTrailingChars(uhf.readTagFromBuffer().epc, 4)
//                    val rfid: String = uhf.readTagFromBufferList()[0].epc
                    if (rfid.isNotEmpty()){
                        callback?.onRfidScan(rfid)
                        if (readType == RFIDReadType.Single){
                            stopInventory()
                        }
                    }
                }catch (_ : Exception){}
            }
        }, 1000, 500)
        callback?.onStateChange(onScanning)
        return onScanning
    }

//    override fun startInventory(readType: RFIDReadType): Boolean {
//        if (uhf.connectStatus != ConnectionStatus.CONNECTED && !onScanning) {
//            return false
//        }
//        onScanning = uhf.startInventoryTag()
//        if (!onScanning) {
//            return false
//        }
//        timer = Timer()
//        timer?.scheduleAtFixedRate(object : TimerTask() {
//            override fun run() {
//                try {
//                    val tagInfo: UHFTAGInfo? = uhf.inventorySingleTag()
//                    tagInfo?.let {
//                        val rfid: String = it.epc
//                        println(rfid)
//                        if (rfid.isNotEmpty()) {
//                            callback?.onRfidScan(rfid)
//                            if (readType == RFIDReadType.Single) {
//                                stopInventory()
//                            }
//                        }
//                    }
//                } catch (_: Exception) {
//                    // Handle exception if necessary
//                }
//            }
//        }, 1000, 500)
//        callback?.onStateChange(onScanning)
//        return onScanning
//    }

    override fun stopInventory() {
        uhf.stopInventory()
        timer?.cancel()
        onScanning = false
        callback?.onStateChange(false)
    }

    override fun getBattery(): Int {
        return uhf.battery
    }

    override fun getPower(): Int {
        return uhf.power
    }

    override fun getMaxPower(): Int {
        return 30
    }

    override fun setPower(power: Int) {
        uhf.power = power
    }

    override fun setDeviceLog(callback: RFIDDeviceCallback) {
        this.callback = callback
    }

    override fun startScanBt() {
        uhf.startScanBTDevices { bluetoothDevice, _, _ ->
            callback?.onBtScan(bluetoothDevice)
        }
    }

    override fun stopScanBt() {
        uhf.stopScanBTDevices()
    }

    override fun isConnect(): Boolean {
        return uhf.connectStatus == ConnectionStatus.CONNECTED
    }
}