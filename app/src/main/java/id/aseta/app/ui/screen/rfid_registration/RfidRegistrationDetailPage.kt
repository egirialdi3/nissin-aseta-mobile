package id.aseta.app.ui.screen.rfid_registration

import AuthViewModel
import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import id.aseta.app.data.model.LocationItem
import id.aseta.app.viewmodel.AssetCategoryViewModel
import id.aseta.app.viewmodel.UHFViewModel
import androidx.compose.ui.Modifier
import com.rscja.deviceapi.RFIDWithUHFBLE
import com.rscja.deviceapi.RFIDWithUHFUART
import com.rscja.deviceapi.interfaces.KeyEventCallback
import id.aseta.app.KeyEventBus
import id.aseta.app.data.source.local.TokenDataStore
import id.aseta.app.data.model.AssetDetail
import id.aseta.app.ui.screen.composable.RfidRadarIndicator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RfidRegistrationDetailPage(
    viewModel: AuthViewModel,
    uhfViewModel: UHFViewModel,
    assetCategoryModel: AssetCategoryViewModel,
    navController: NavController,
) {
    val context = LocalContext.current

    var mReader by remember { mutableStateOf<RFIDWithUHFUART?>(null) }
    var mSled by remember { mutableStateOf<RFIDWithUHFBLE?>(null) }
    var isRunning by remember { mutableStateOf(false) }
    var initMessage by remember { mutableStateOf<String?>(null) }
    var isInitializing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var isScanning by UHFState.isScanning // Gunakan state yang sama seperti RFIDScanScreen
    var isScanningLocal = isScanning

    var showManualInputSheet by remember { mutableStateOf(false) }
    var manualInputAsset by remember { mutableStateOf<AssetDetail?>(assetCategoryModel.assetDetail) }

    var typeHH by remember { mutableStateOf("0") }
    var isKeyDownUP by remember { mutableStateOf(false) }
    var scannedRfid by remember { mutableStateOf("") }

    fun initUHF() {
        try {
            mReader = RFIDWithUHFUART.getInstance()
        } catch (e: Exception) {
            initMessage = e.message
            return
        }

        if (mReader != null) {
            isInitializing = true
            scope.launch {
                val result = withContext(Dispatchers.IO) { mReader!!.init(context) }
                isInitializing = false
                initMessage = if (result) {
                    "Init berhasil"
                } else {
                    "Init gagal"
                }
            }
        }
    }

    fun initSled() {
        try {
            mSled = RFIDWithUHFBLE.getInstance()
        } catch (e: Exception) {
            initMessage = e.message
            return
        }

        if (mSled != null) {
            isInitializing = true
            scope.launch {
                val result = withContext(Dispatchers.IO) { mSled!!.init(context) }
                isInitializing = false
                initMessage = if (result) {
                    // Set key event callback setelah delay (sama seperti RFIDScanScreen)
                    scope.launch {
                        delay(200)
                        mSled?.setKeyEventCallback(object : KeyEventCallback {
                            override fun onKeyDown(keycode: Int) {
                                scope.launch {
                                    when (keycode) {
                                        3 -> {
                                            isKeyDownUP = true
                                            UHFState.isScanning.value = true
                                        }
                                        1 -> {
                                            if (!isKeyDownUP) {
                                                UHFState.isScanning.value = !UHFState.isScanning.value
                                            }
                                        }
                                        2 -> {
                                            if (UHFState.isScanning.value) {
                                                withContext(Dispatchers.IO) {
                                                    mSled?.stopInventory()
                                                    delay(100)
                                                }
                                                UHFState.isScanning.value = false
                                            }
                                            val tag = withContext(Dispatchers.IO) {
                                                mSled?.inventorySingleTag()
                                            }
                                            tag?.let {
                                                scannedRfid = it.epc
                                            }
                                        }
                                    }
                                }
                            }

                            override fun onKeyUp(keycode: Int) {
                                scope.launch {
                                    when (keycode) {
                                        4 -> {
                                            UHFState.isScanning.value = false
                                            isKeyDownUP = false
                                        }
                                    }
                                }
                            }
                        })
                    }
                    "Init berhasil"
                } else {
                    "Init gagal"
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            // Stop scanning
            UHFState.isScanning.value = false

            // Cleanup readers
            mSled?.setKeyEventCallback(null)
            mReader?.stopInventory()
            mSled?.stopInventory()

            // Reset running state
            isRunning = false
        }
    }

    LaunchedEffect(Unit) {
        UHFState.isScanning.value = false
        scannedRfid = ""

        typeHH = TokenDataStore.getSelectedHH(context)
        if (typeHH != null) {
            when (typeHH) {
                "1" -> initUHF()
                "2" -> initSled()
            }
        }
    }

    LaunchedEffect(isScanning) {
        val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)

        if (isScanning) {
            if (typeHH == "1") {
                if (mReader == null) {
                    Toast.makeText(context, "UHF Reader Tidak terkoneksi", Toast.LENGTH_SHORT).show()
                } else {
                    if (!isRunning) {
                        isRunning = true

                        // Set callback untuk mReader
                        mReader?.setInventoryCallback { tag ->
                            tag?.let {
                                CoroutineScope(Dispatchers.IO).launch { // Background thread
                                    val epc = it.epc
                                    val rssi = it.rssi

                                    // 🔊 Beep!
                                    toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 100)

                                    scannedRfid = epc

                                    // Switch ke Main thread untuk UI operations
                                    withContext(Dispatchers.Main) {
                                        UHFState.isScanning.value = false
                                    }
                                }
                            }
                        }

                        // Start inventory dengan callback
                        val started = mReader?.startInventoryTag() ?: false
                        if (!started) {
                            isRunning = false
                        }
                    }
                }
            } else if (typeHH == "2") {
                if (mSled == null) {
                    Toast.makeText(context, "UHF Reader Tidak terkoneksi", Toast.LENGTH_SHORT).show()
                } else {
                    if (!isRunning) {
                        isRunning = true

                        // Set callback untuk mSled
                        mSled?.setInventoryCallback { tag ->
                            tag?.let {
                                CoroutineScope(Dispatchers.IO).launch { // Background thread
                                    val epc = it.epc
                                    val rssi = it.rssi

                                    // 🔊 Beep!
                                    toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 100)

                                    scannedRfid = epc

                                    // Switch ke Main thread untuk UI operations
                                    withContext(Dispatchers.Main) {
                                        UHFState.isScanning.value = false
                                    }
                                }
                            }
                        }

                        // Start inventory dengan callback
                        val started = mSled?.startInventoryTag() ?: false
                        if (!started) {
                            isRunning = false
                        }
                    }
                }
            }
        } else {
            if (isRunning) {
                when (typeHH) {
                    "1" -> mReader?.stopInventory()
                    "2" -> mSled?.stopInventory()
                }
                isRunning = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Form RFID Registration",
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF1E3A8A)
                    )
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .fillMaxHeight()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Register RFID for: ${manualInputAsset!!.nama_barang}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f)
                        )

                        // Radar indicator with toggle capability
                        Box(
                            modifier = Modifier
                                .clickable {
                                    isScanningLocal = !isScanningLocal
                                    isScanning = isScanningLocal
                                }
                        ) {
                            RfidRadarIndicator(
                                isScanning = isScanningLocal
                            )
                        }
                    }
                    Text(
                        text = if (isScanning) "Scanning active - tap radar to stop" else "Tap radar to start scanning",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isScanning) Color(0xFF1E3A8A) else Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(Modifier.height(24.dp))

                    OutlinedTextField(
                        value = scannedRfid,
                        onValueChange = { scannedRfid = it },
                        label = { Text("RFID") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (scannedRfid.isNotBlank() || scannedRfid != "") {
                                Log.d("RFIDRegistration", "Submitting RFID: $scannedRfid")
                                // Stop scanning when submitting


                                assetCategoryModel.saveRfid(context, scannedRfid, manualInputAsset!!){
                                    scannedRfid = ""
                                    isScanningLocal = false
                                    isScanning = isScanningLocal
                                    assetCategoryModel.assetDetail = null
                                    assetCategoryModel.fetchAssetDetailsByCategory(
                                        context,
                                        manualInputAsset!!.kd_kel_barang
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Submit RFID")
                    }

                    Spacer(Modifier.height(16.dp))
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }


}