package com.rfid.cf

import android.content.Context
import android.content.SharedPreferences

abstract class RFIDClass {
    abstract fun open(context: Context)
    abstract fun close()
    abstract fun connect(address: String)
    abstract fun disconnect()
    abstract fun startInventory(readType: RFIDReadType): Boolean
    abstract fun stopInventory()
    abstract fun getBattery(): Int
    abstract fun getPower(): Int
    abstract fun getMaxPower(): Int
    abstract fun setPower(power: Int)
    abstract fun setDeviceLog(callback: RFIDDeviceCallback)
    abstract fun startScanBt()
    abstract fun stopScanBt()
    abstract fun isConnect(): Boolean

    protected fun setRFIDDeviceType(sharedPreferences: SharedPreferences, value: RFIDDeviceType){
        val editor = sharedPreferences.edit()
        editor.putInt("RFIDDevice_type", when (value) {
            RFIDDeviceType.CFU909 -> { 1 }
            RFIDDeviceType.H301 -> { 2 }
        })
        editor.apply()
    }
    protected fun getRFIDDeviceType(sharedPreferences: SharedPreferences): RFIDDeviceType?{
        return when (sharedPreferences.getInt("RFIDDevice_type", -1)) {
            1 -> { RFIDDeviceType.CFU909 }
            2 -> { RFIDDeviceType.H301 }
            else -> { null }
        }
    }

    protected fun setLastDeviceAddress(sharedPreferences: SharedPreferences,value: String){
        val editor = sharedPreferences.edit()
        editor.putString("RFIDDevice_last",value)
        editor.apply()
    }
    protected fun getLastDeviceAddress(sharedPreferences: SharedPreferences): String?{
        return sharedPreferences.getString("RFIDDevice_last", null)
    }
    protected fun setAutoReconnect(sharedPreferences: SharedPreferences,value: Boolean){
        val editor = sharedPreferences.edit()
        editor.putBoolean( "RFIDDevice_reconnect",value)
        editor.apply()
    }
    protected fun isAutoReconnect(sharedPreferences: SharedPreferences): Boolean{
        return sharedPreferences.getBoolean("RFIDDevice_reconnect", false)
    }
}