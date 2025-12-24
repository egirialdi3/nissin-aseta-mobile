package id.aseta.app.viewmodel

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rscja.deviceapi.RFIDWithUHFBLE
import com.rscja.deviceapi.RFIDWithUHFUART
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class UHFViewModel : ViewModel() {
    lateinit var appContext: Context
    private val _uiState = MutableStateFlow<UHFState>(UHFState.Idle)
    val uiState: StateFlow<UHFState> = _uiState
    var scanrfid by mutableStateOf(false)


    var powerLevel: Int = 30 // Default power level
        private set

    var mReader: RFIDWithUHFUART? = null

    var mSled: RFIDWithUHFBLE? = null

    fun initReaderIfNeeded() {
        if (mReader == null) {
            try {
                mReader = RFIDWithUHFUART.getInstance()
                val success = mReader!!.init(appContext)
                mSled = RFIDWithUHFBLE.getInstance()
                Log.d("UHFViewModel", "Init success: $success")
            } catch (e: Exception) {
                Log.e("UHFViewModel", "Init failed: ${e.message}")
            }
        }
    }

    fun initUHF(context: Context) {
        appContext = context.applicationContext
        if (_uiState.value is UHFState.Initialized) return

        _uiState.value = UHFState.Initializing

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    mReader = RFIDWithUHFUART.getInstance() ?: throw Exception("Failed to get UHF instance")
                }

                val result = withContext(Dispatchers.IO) {
                    mReader!!.init(appContext)
                }

                if (result) {
                    mReader!!.setPower(powerLevel) // Set default power
                    _uiState.value = UHFState.Initialized("Init berhasil")
                } else {
                    _uiState.value = UHFState.Error("Init gagal")
                }
            } catch (e: Exception) {

                _uiState.value = UHFState.Error(e.message ?: "Unknown error occurred")
                mReader = null
            }
        }
    }


    fun setPowerLevel(newPower: Int) {
        powerLevel = newPower.coerceIn(0, 30)
        mReader?.setPower(powerLevel)
    }

    override fun onCleared() {
        mReader?.free()
        super.onCleared()
    }

    sealed class UHFState {
        object Idle : UHFState()
        object Initializing : UHFState()
        data class Initialized(val message: String) : UHFState()
        data class Error(val message: String) : UHFState()
    }

    private var scanJob: Job? = null
    private val _scannedTags = MutableStateFlow<List<String>>(emptyList())
    val scannedTags: StateFlow<List<String>> = _scannedTags

    fun startScan(context: Context, onTagDetected: (String) -> Unit) {
        println("MASUK START")

        initReaderIfNeeded()
        if (_uiState.value !is UHFState.Initialized || mReader == null || scanJob?.isActive == true) return
        mReader?.startInventoryTag()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                while (isActive) {
                    var tag = mReader?.inventorySingleTag()
//                    if(tag == null){
//                        mReader?.stopInventory()
//                        mReader?.startInventoryTag()
//                        tag = mReader?.inventorySingleTag()
//                    }

                    if (tag != null) {
                        val epc = tag.epc
                        val rssi = tag.rssi
                        Log.d("TAG_READ", "EPC: $epc, RSSI: $rssi")
                        if (!_scannedTags.value.contains(epc)) {
                            _scannedTags.update { it + epc }
                            withContext(Dispatchers.Main) {
                                onTagDetected(epc)
                            }
                        }
                    } else {
                        Log.d("UHFViewModel", "Tag is null")
                    }

                    delay(1000) // Delay agar tidak membebani CPU
                }
            }
        }

    }


    fun stopScan() {
        println("MASUK STOP")
        mReader?.stopInventory()

    }

    fun toggleScan() {
//        if (_isScanning.value) {
//            stopScan()
//        } else {
//            startScan(appContext) { epc ->
//                _onTagDetectedCallback?.invoke(epc)
//            }
//        }
//        _isScanning.value = !_isScanning.value
        println("SEKARANG VALUENYA" + scanrfid)
        if(scanrfid){
            startScan(appContext) { epc ->
                _onTagDetectedCallback?.invoke(epc)

            }
            scanrfid = !scanrfid

        }else{
            stopScan()
            scanrfid = !scanrfid

        }
    }

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    private var _onTagDetectedCallback: ((String) -> Unit)? = null

    fun registerTagCallback(callback: (String) -> Unit) {
        _onTagDetectedCallback = callback
    }
}