package id.aseta.app.ui.screen.find_asset

import android.media.AudioManager
import android.media.ToneGenerator
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.rscja.deviceapi.RFIDWithUHFBLE
import com.rscja.deviceapi.RFIDWithUHFUART
import com.rscja.deviceapi.interfaces.KeyEventCallback
import id.aseta.app.R
import id.aseta.app.data.source.local.PowerPreference
import id.aseta.app.data.source.local.TokenDataStore
import id.aseta.app.data.model.AssetDetail
import id.aseta.app.viewmodel.AssetCategoryViewModel
import id.aseta.app.viewmodel.UHFViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun FindingAssetScreen(
    epcTarget: String,
    uhfViewModel: UHFViewModel,
    navController: NavController,
    assetCategoryViewModel: AssetCategoryViewModel
) {
    val context = LocalContext.current
    var resultScan by remember { mutableStateOf<List<AssetDetail>?>(emptyList()) }
    var mReader by remember { mutableStateOf<RFIDWithUHFUART?>(null) }
    var mSled by remember { mutableStateOf<RFIDWithUHFBLE?>(null) }
    var initMessage by remember { mutableStateOf<String?>(null) }
    var isInitializing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val powerFromPref by PowerPreference.getPowerLevel(context).collectAsState(initial = 30f)
    var powerLevel by remember { mutableStateOf(powerFromPref) }
    var isRunning by remember { mutableStateOf(false) }
    var isScanning by UHFState.isScanning
    var resultEpc by remember { mutableStateOf<List<String>?>(emptyList()) }
    var typeHH by remember { mutableStateOf("1") }
    var isKeyDownUP by remember { mutableStateOf(false) }
    var isNear by remember { mutableStateOf(false) }
    var rssiValue by remember { mutableStateOf<String?>(null) }

    // Initialize UHF reader
    fun initUHF() {
        try {
            mReader = RFIDWithUHFUART.getInstance()
        } catch (e: Exception) {
            initMessage = e.message
            return
        }

        if (mReader != null) {
            isInitializing = true
            typeHH = "1"
            mReader!!.setPower(powerLevel.toInt())
            scope.launch {
                val result = withContext(Dispatchers.IO) { mReader!!.init(context) }
                isInitializing = false
                initMessage = if (result) {
                    mReader!!.setPower(powerLevel.toInt())
                    "Init berhasil"
                } else {
                    "Init gagal"
                }
            }
        }
    }

    // Initialize BLE SLED
    fun initSled() {
        try {
            mSled = RFIDWithUHFBLE.getInstance()
        } catch (e: Exception) {
            initMessage = e.message
            return
        }

        if (mSled != null) {
            isInitializing = true
            typeHH = "2"
            scope.launch {
                val result = withContext(Dispatchers.IO) { mSled!!.init(context) }
                isInitializing = false
                initMessage = if (result) {
                    // Set key event callback after delay
                    scope.launch {
                        delay(200)
                        mSled!!.setPower(powerLevel.toInt())
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
                                                val epc = it.epc
                                                if (epc == epcTarget) {
                                                    rssiValue = it.rssi
                                                    val normalizedRssi = rssiValue!!.replace(",", ".")
                                                    normalizedRssi?.toDoubleOrNull()?.let { doubleValue ->
                                                        if (doubleValue >= -35.0) {
                                                            isNear = true
                                                            isScanning = false
                                                            mReader?.stopInventory()
                                                            isRunning = false
                                                        }
                                                    }
                                                }
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

    // Clean up callback when leaving the screen
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

    // Initialize on first launch
    LaunchedEffect(Unit) {
        typeHH = TokenDataStore.getSelectedHH(context)
        powerLevel = PowerPreference.getPowerLevelInit(context)

        if (typeHH != null) {
            if (typeHH == "1") {
                initUHF()
            } else {
                initSled()
            }
        }
        assetCategoryViewModel.clearRfidScanResult()
    }



    // Handle scanning state changes
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
                                    try {
                                        val epc = it.epc
                                        val rssi = it.rssi

                                        // Null safety checks
                                        if (epc.isNullOrEmpty()) return@launch

                                        println("TAG GUYS: $epc")

                                        // 🔊 Beep!
                                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 100)

                                        // Check if this is the target EPC
                                        if (epc == epcTarget) {
                                            rssiValue = rssi
                                            val normalizedRssi = rssiValue!!.replace(",", ".")
                                            normalizedRssi.toDoubleOrNull()?.let { doubleValue ->
                                                if (doubleValue >= -35.0) {
                                                    // Switch ke Main thread untuk UI operations
                                                    withContext(Dispatchers.Main) {
                                                        isNear = true
                                                        isScanning = false
                                                        mReader?.stopInventory()
                                                        isRunning = false
                                                    }
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
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
                                    try {
                                        val epc = it.epc
                                        val rssi = it.rssi

                                        // Null safety checks
                                        if (epc.isNullOrEmpty()) return@launch

                                        println("SLED: $epc")

                                        // 🔊 Beep!
                                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 100)

                                        // Check if this is the target EPC
                                        if (epc == epcTarget) {
                                            rssiValue = rssi
                                            val normalizedRssi = rssiValue!!.replace(",", ".")
                                            normalizedRssi.toDoubleOrNull()?.let { doubleValue ->
                                                if (doubleValue >= -35.0) {
                                                    // Switch ke Main thread untuk UI operations
                                                    withContext(Dispatchers.Main) {
                                                        isNear = true
                                                        isScanning = false
                                                        mSled?.stopInventory()
                                                        isRunning = false
                                                    }
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
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
                if (typeHH == "1") {
                    mReader?.stopInventory()
                } else {
                    mSled?.stopInventory()
                }
                isRunning = false
            }
        }
    }

    // Animation properties for radar effect
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Finding", fontSize = 16.sp)
                Text(
                    assetCategoryViewModel.selectedAssetFindAsset!!.nama_barang, // TODO: buat dynamic
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${assetCategoryViewModel.selectedAssetFindAsset!!.no_register} • ${assetCategoryViewModel.selectedAssetFindAsset!!.nama} • ${assetCategoryViewModel.selectedAssetFindAsset!!.location}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    "RFID • ${assetCategoryViewModel.selectedAssetFindAsset!!.rfid}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(250.dp)
                    .background(Color.Transparent)
            ) {
                // Radar effect (blue pulsing circle)
                Box(
                    modifier = Modifier
                        .size(200.dp * scale)
                        .alpha(alpha)
                        .background(
                            Color(0xFF1E3A8A),
                            shape = CircleShape
                        )
                )
                // Middle Circle
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(
                            if (isNear) Color(0xFFD0EBFF) else Color(0xFFF0F0F0),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${rssiValue ?: "--"} dBm",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFEAF2FF),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_info),
                            contentDescription = "Icon Information",
                            tint = Color(0xFF1E3A8A)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "The closer the RSSI value is to 0, the nearer the asset is to the reader.",
                            color = Color(0xFF1E3A8A),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .padding(16.dp)
                            .size(46.dp)
                            .background(Color(0xFFEAF2FF), shape = CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = "Close"
                        )
                    }
                    Button(
                        onClick = { navController.popBackStack() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if(isNear) Color(0xFF1E3A8A) else Color(0xffF0F1F1),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Text("Asset Founded", color = if(isNear) Color.White else Color(0xff7C7F83))
                    }
                }
            }
        }
    }
}

