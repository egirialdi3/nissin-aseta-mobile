package com.rfid.cf

import android.content.Context
import com.rfid.cf.h301.H301
import com.rfid.cf.u909.U909
import com.rscja.deviceapi.RFIDWithUHFBLE

class CFDeviceManager constructor(context: Context) : RFIDClass() {

    private val sharedPreferences = context.getSharedPreferences("RFIDDevice_config", Context.MODE_PRIVATE)

    companion object {
        private var instance: CFDeviceManager? = null
        fun getInstance(context: Context): CFDeviceManager {
            if (instance == null) {
                instance = CFDeviceManager(context)
            }
            return instance!!
        }
    }
    private val cfu909: U909 = U909(RFIDWithUHFBLE.getInstance(), sharedPreferences)
    private val h301: H301 = H301()

    private val device: RFIDClass? = when (getRFIDDeviceType()){
        RFIDDeviceType.CFU909 -> { cfu909 }
        RFIDDeviceType.H301 -> { h301 }
        else -> { null }
    }

    fun setRFIDDeviceType(value: RFIDDeviceType){
        super.setRFIDDeviceType(sharedPreferences, value)
    }
    fun getRFIDDeviceType(): RFIDDeviceType?{
        return super.getRFIDDeviceType(sharedPreferences)
    }

    fun getLastDeviceAddress(): String?{
        return super.getLastDeviceAddress(sharedPreferences)
    }

    fun setAutoReconnect(auto: Boolean){
        super.setAutoReconnect(sharedPreferences, auto)
    }
    fun isAutoReconnect(): Boolean{
        return super.isAutoReconnect(sharedPreferences)
    }

    override fun open(context: Context) {
        device?.open(context)
    }

    override fun close() {
        device?.close()
    }

    override fun connect(address: String) {
        device?.connect(address)
    }

    override fun disconnect() {
        device?.disconnect()
    }


    override fun startInventory(readType: RFIDReadType): Boolean {
        return device?.startInventory(readType)!!
    }

    override fun stopInventory() {
        device?.stopInventory()
    }

    override fun getBattery(): Int {
        return device?.getBattery()!!
    }

    override fun getPower(): Int {
        return device?.getPower()!!
    }

    override fun getMaxPower(): Int {
        return device?.getMaxPower()!!
    }

    override fun setPower(power: Int) {
        device?.setPower(power)
    }

    override fun setDeviceLog(callback: RFIDDeviceCallback) {
        device?.setDeviceLog(callback)
    }

    override fun startScanBt() {
        device?.startScanBt()
    }

    override fun stopScanBt() {
        device?.stopScanBt()
    }

    override fun isConnect(): Boolean {
        return device?.isConnect()?:false
    }
}