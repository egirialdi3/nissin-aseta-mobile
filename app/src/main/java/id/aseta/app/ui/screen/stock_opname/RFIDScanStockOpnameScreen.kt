package id.aseta.app.ui.screen.stock_opname

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import android.widget.Toast
import androidx.camera.core.Camera
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.rscja.deviceapi.RFIDWithUHFBLE
import com.rscja.deviceapi.RFIDWithUHFUART
import com.rscja.deviceapi.interfaces.KeyEventCallback
import id.aseta.app.R
import id.aseta.app.data.source.local.PowerPreference
import id.aseta.app.data.source.local.TokenDataStore
import id.aseta.app.data.model.AssetItem
import id.aseta.app.data.model.LocationItem
import id.aseta.app.viewmodel.AssetCategoryViewModel
import id.aseta.app.viewmodel.AssetViewerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.orEmpty
import kotlin.collections.plus

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RFIDScanStockOpnameScreen(
    navController: NavController,
    viewModel: AssetViewerViewModel,
    assetCategoryViewModel: AssetCategoryViewModel,
    type: String
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val flashlightEnabled = remember { mutableStateOf(false) }
    var cameraRef by remember { mutableStateOf<Camera?>(null) }

    // State untuk bottom sheet dan asset
    var showSheet by remember { mutableStateOf(true) } // Selalu tampilkan bottom sheet
    var asset by remember { mutableStateOf<AssetItem?>(null) }
    var isCameraActive by remember { mutableStateOf(true) }

    val assetMap by remember { derivedStateOf { assetCategoryViewModel.assetDetailsMap } }
    var selectedLocation by remember { mutableStateOf<LocationItem?>(null) } // atau dari ViewModel juga kalau mau
    val assetList = assetMap[type] ?: emptyList()

    val lastScannedValue = remember { mutableStateOf<String?>(null) }
    val lastScannedTime = remember { mutableStateOf(0L) }
    var resultEpc by remember {mutableStateOf<List<String>?>(emptyList())}



    var typeHH by remember { mutableStateOf("0") }

    var isKeyDownUP by remember { mutableStateOf(false) }

    var mReader by remember { mutableStateOf<RFIDWithUHFUART?>(null) }
    var mSled by remember { mutableStateOf<RFIDWithUHFBLE?>(null) }

    var initMessage by remember { mutableStateOf<String?>(null) }
    var isInitializing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val powerFromPref by PowerPreference.getPowerLevel(context).collectAsState(initial = 30f)
    var isRunning by remember { mutableStateOf(false) }
    val isScanning by UHFState.isScanning


    //added in version 2.0.13 Fix : Scan loading dan terus terusan
    var scannedEpcs by remember { mutableStateOf<Set<String>>(emptySet()) }
    var toneGenerator by remember { mutableStateOf<ToneGenerator?>(null) }


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
            scope.launch {
                val result = withContext(Dispatchers.IO) { mReader!!.init(context) }
                isInitializing = false
                initMessage = if (result) {
//                    mReader!!.setPower(powerLevel.toInt())
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
            typeHH = "2"
            scope.launch {
                val result = withContext(Dispatchers.IO) { mSled!!.init(context) }
                isInitializing = false
                initMessage = if (result) {
                    // Set key event callback setelah delay
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
                                                val epc = it.epc
                                                if (!resultEpc.orEmpty().contains(epc)) {
                                                    resultEpc = resultEpc.orEmpty() + epc
                                                    assetCategoryViewModel.searchAssetByRfid(context, epc)
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

    DisposableEffect(Unit) {
        onDispose {
            // Stop scanning
            UHFState.isScanning.value = false

            // Cleanup tone generator
            try {
                toneGenerator?.stopTone()
                toneGenerator?.release()
            } catch (e: Exception) {
                // Ignore
            }
            toneGenerator = null

            // Cleanup readers
            mSled?.setKeyEventCallback(null)
            mReader?.stopInventory()
            mSled?.stopInventory()

            // Reset running state
            isRunning = false

            scannedEpcs = emptySet()

        }
    }

    LaunchedEffect(assetCategoryViewModel.rfidScanResult) {
        assetCategoryViewModel.rfidScanResult?.let { results ->
            if (results.isNotEmpty()) {
                results.firstOrNull()?.let { firstAsset ->
                    assetCategoryViewModel.markAssetAsFound(
                        firstAsset.no_register,
                        type
                    )
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        "assetScanned", true
                    )

                    // Reset hasil scan setelah diproses
                    assetCategoryViewModel.rfidScanResult = emptyList()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        typeHH =  TokenDataStore.getSelectedHH(context)

        if(typeHH!= null){
            if(typeHH == "1"){
                initUHF()
            }else{
                initSled()

            }
        }
    }

    LaunchedEffect(isScanning) {
        // Initialize ToneGenerator hanya sekali
        if (toneGenerator == null) {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        }
        if (isScanning) {
//            if (typeHH == "1") {
//                if (mReader == null) {
//                    Toast.makeText(context, "UHF Reader Tidak terkoneksi", Toast.LENGTH_SHORT).show()
//                } else {
//                    if (!isRunning) {
//                        isRunning = true
//                        val power = mReader?.power
//                        // Set callback untuk mReader
//                        mReader?.setInventoryCallback { tag ->
//                            tag?.let {
//                                if (!isScanning) return@let
//
//                                CoroutineScope(Dispatchers.IO).launch { // Background thread
//                                    try {
//                                        val epc = it.epc
//                                        val rssi = it.rssi
//
//                                        // Null safety checks
//                                        if (epc.isNullOrEmpty()) return@launch
//
//                                        // **SOLUSI 1: Cek apakah EPC sudah pernah di-scan**
//                                        if (scannedEpcs.contains(epc)) {
//                                            return@launch // Skip jika sudah pernah di-scan
//                                        }
//
//                                        // Tambahkan ke set EPC yang sudah di-scan
//                                        withContext(Dispatchers.Main) {
//                                            scannedEpcs = scannedEpcs + epc
//                                        }
//
//
//                                        // 🔊 Beep!
//                                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
//
//                                        // Check if EPC is not already in result
//                                        if (!resultEpc!!.contains(epc)) {
//                                            resultEpc = resultEpc.orEmpty() + epc
//
//                                            // Switch ke Main thread untuk UI operations
//                                            withContext(Dispatchers.Main) {
//                                                delay(50) // Tambah small delay untuk mengurangi lag
//                                                if (isScanning) { // Cek sekali lagi
//                                                    assetCategoryViewModel.searchAssetByRfid(context, epc, rssi, power)
//                                                }
//                                            }
//                                        }
//                                    } catch (e: Exception) {
//                                        e.printStackTrace()
//                                    }
//                                }
//                            }
//                        }
//
//                        // Start inventory dengan callback
//                        val started = mReader?.startInventoryTag() ?: false
//                        if (!started) {
//                            isRunning = false
//                        }
//                    }
//                }
//            }
            if (typeHH == "1") {
                val uhfReader = RFIDWithUHFUART.getInstance()
                val power = mReader?.power
                if (uhfReader == null) {
                    Log.e("UHF", "UHF Reader UART tidak tersedia")
//                    return
                }

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // Inisialisasi device
                        if (uhfReader.init(context)) {
                            Log.i("UHF", "UHF Reader UART berhasil diinisialisasi")

                            // Aktifkan fast mode (biar lebih cepat baca EPC)
                            uhfReader.setFastInventoryMode(true)
                            uhfReader.setFastID(true)

                            // Mulai inventory
                            val started = uhfReader.startInventoryTag()
                            if (!started) {
                                Log.e("UHF", "Gagal memulai inventory")
                                return@launch
                            }

//                            isScanning = true
                            Log.i("UHF", "Mulai scan tag...")

                            while (isScanning) {
                                val tag = uhfReader.readTagFromBuffer()
                                if (tag != null) {
                                    val epc = tag.epc
                                    val rssi = tag.rssi
//                                    val power = powerLevel.toInt()

                                    Log.d("UHF", "Tag terbaca: EPC=$epc RSSI=$rssi Power=$power")

                                    withContext(Dispatchers.Main) {
                                        // Kirim data ke ViewModel / UI
                                        assetCategoryViewModel.searchAssetByRfid(context, epc, rssi, power)
                                        // Non-blocking beep
                                        CoroutineScope(Dispatchers.Default).launch {
                                            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 40)
                                        }
                                    }
                                } else {
                                    delay(5) // delay kecil untuk hemat CPU (bisa 0–10 ms)
                                }
                            }

                            // Stop inventory jika berhenti scan
                            uhfReader.stopInventory()
                            uhfReader.free()
                            Log.i("UHF", "Scan dihentikan dan device ditutup")

                        } else {
                            Log.e("UHF", "Gagal inisialisasi UHF Reader UART")
                        }

                    } catch (e: Exception) {
                        Log.e("UHF", "Error pada proses scanning: ${e.message}", e)
                        uhfReader.free()
                    }
                }
            }
            else if (typeHH == "2") {
                if (mSled == null) {
                    Toast.makeText(context, "UHF Reader Tidak terkoneksi", Toast.LENGTH_SHORT).show()
                } else {
                    if (!isRunning) {
                        isRunning = true

                        // Set callback untuk mSled
                        mSled?.setInventoryCallback { tag ->
                            tag?.let {
                                if (!isScanning) return@let

                                CoroutineScope(Dispatchers.IO).launch { // Background thread
                                    try {
                                        val epc = it.epc
                                        val rssi = it.rssi

                                        // Null safety checks
                                        if (epc.isNullOrEmpty()) return@launch

                                        // **SOLUSI 1: Cek apakah EPC sudah pernah di-scan**
                                        if (scannedEpcs.contains(epc)) {
                                            return@launch // Skip jika sudah pernah di-scan
                                        }
                                        // 🔊 Beep!
//                                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 100)

                                        withContext(Dispatchers.Main) {
                                            scannedEpcs = scannedEpcs + epc
                                        }

                                        // Check if EPC is not already in result
                                        if (!resultEpc!!.contains(epc)) {
                                            resultEpc = resultEpc.orEmpty() + epc
                                            if (isScanning) {
                                                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
                                                withContext(Dispatchers.Main) {
                                                    delay(50) // Tambah small delay untuk mengurangi lag
                                                    if (isScanning) { // Cek sekali lagi
                                                        assetCategoryViewModel.searchAssetByRfid(context, epc)
                                                    }
                                                }
                                            }
                                            // Switch ke Main thread untuk UI operations
//                                            withContext(Dispatchers.Main) {
//                                                assetCategoryViewModel.searchAssetByRfid(context, epc)
//                                            }
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
                delay(100)
                // Delay sebentar untuk memastikan stop inventory selesai
                delay(100)

                // Hentikan semua tone yang mungkin masih berjalan
                try {
                    toneGenerator?.stopTone()
                } catch (e: Exception) {
                    // Ignore jika error
                }
                isRunning = false
            }


        }
    }

    // Tambahkan LaunchedEffect untuk reset scannedEpcs saat mulai scan baru
    LaunchedEffect(isScanning) {
        if (isScanning) {
            // Reset set EPC yang sudah di-scan saat mulai scanning baru
            scannedEpcs = emptySet()
        }
    }

    // buat radar kedip kedip biru
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
// Background Image
        Image(
            painter = painterResource(id = R.drawable.bg_main),
            contentDescription = null,
            modifier = Modifier
                .height(635.dp)
                .width(618.dp),
            contentScale = ContentScale.Crop
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        androidx.compose.material.Text(
                            "Scanning Tag",
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(250.dp)
                            .background(Color.Transparent)
                    ) {
                        // Radar effect (blue pulsing circle)
                        Box(
                            modifier = Modifier
                                .size(140.dp * scale)
                                .alpha(alpha)
                                .background(
                                    if(isScanning) Color(0xFF1E3A8A) else Color.Red,
                                    shape = CircleShape
                                )
                        )
                        // Middle Circle
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .background(
                                    if(isScanning) Color(0xFFD0EBFF) else Color.Red,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if(isScanning) "Scanning" else "Start Scanning",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    androidx.compose.material.Text(
                        text = "Point the RFID reader at the asset you want to scan",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

        }
        CustomBottomSheet(
            assetList = assetList,
            navController = navController,
            modifier = Modifier
                .align(Alignment.BottomCenter) // Ini akan berfungsi karena dalam Box
        )
    }
}
