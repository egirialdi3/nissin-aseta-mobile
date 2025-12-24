
package id.aseta.app.ui.screen.asset_viewer

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import id.aseta.app.viewmodel.AssetCategoryViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.rscja.deviceapi.RFIDWithUHFBLE
import com.rscja.deviceapi.RFIDWithUHFUART
import com.rscja.deviceapi.interfaces.KeyEventCallback
import id.aseta.app.R
import id.aseta.app.data.source.local.PowerPreference
import id.aseta.app.data.source.local.TokenDataStore
import id.aseta.app.data.model.AssetDetail
import id.aseta.app.ui.screen.corrective_maintenance.FullScreenCorrectiveBottomSheet
import id.aseta.app.ui.screen.inspection.FullScreenInspectionBottomSheet
import id.aseta.app.ui.screen.mutasi.FullScreenMutationBottomSheet
import id.aseta.app.ui.screen.qr_scan.AssetBottomSheetContent
import id.aseta.app.ui.screen.relocation.FullScreenRelocationBottomSheet
import id.aseta.app.viewmodel.DeviceSettingViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RFIDAssetViewerScreen(
    assetCategoryModel: AssetCategoryViewModel,
    navController: NavController,
    deviceSettingViewModel: DeviceSettingViewModel = hiltViewModel()
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
    val isScanning by UHFState.isScanning
    var assetDetail by remember { mutableStateOf<AssetDetail?>(null) }
    var showSheet by remember { mutableStateOf(false) }
    var resultEpc by remember {mutableStateOf<List<String>?>(emptyList())}
    var typeHH by remember { mutableStateOf("1") }
    var isKeyDownUP by remember { mutableStateOf(false) }

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
                        mSled!!.setPower(powerLevel.toInt())
//                        mSled?.setBeep(false)
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
                                                    assetCategoryModel.searchAssetByRfid(context, epc)
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

    LaunchedEffect(Unit) {
        UHFState.isScanning.value = false
        typeHH =  TokenDataStore.getSelectedHH(context)
        powerLevel = PowerPreference.getPowerLevelInit(context)

        if(typeHH!= null){
            if(typeHH == "1"){
                initUHF()

            }else{
                initSled()

            }
        }
        assetCategoryModel.clearRfidScanResult()
    }

    fun parseEPC(rawEpc: String): String {
        return rawEpc
//        return try {
//            when {
//                // Jika sudah format E280, return as is
//                rawEpc.startsWith("E280", ignoreCase = true) -> rawEpc
//
//                // Jika format E26xxxxx (yang perlu diubah ke E280xxxxx)
//                rawEpc.startsWith("E26", ignoreCase = true) -> {
//                    if (rawEpc.length >= 26) {
//                        // E2699500004014687864C200 -> E280699500004014687864C2
//                        // Ganti E26 dengan E280 dan hapus 2 karakter terakhir
//                        "E280" + rawEpc.substring(3, rawEpc.length - 2)
//                    } else {
//                        // Jika terlalu pendek, hanya ganti E26 dengan E280
//                        "E280" + rawEpc.substring(3)
//                    }
//                }
//
//                // Jika format E2 lainnya (bukan E26)
//                rawEpc.startsWith("E2", ignoreCase = true) -> {
//                    // Kemungkinan sudah format yang benar atau perlu penanganan khusus
//                    rawEpc
//                }
//
//                // Jika panjang > 24 karakter dan tidak dimulai dengan E2
//                rawEpc.length > 24 -> {
//                    // Coba cari pola E26 di dalam string
//                    val e26Index = rawEpc.indexOf("E26", ignoreCase = true)
//                    if (e26Index >= 0) {
//                        val relevantPart = rawEpc.substring(e26Index)
//                        if (relevantPart.length >= 26) {
//                            "E280" + relevantPart.substring(3, relevantPart.length - 2)
//                        } else {
//                            "E280" + relevantPart.substring(3)
//                        }
//                    } else {
//                        // Coba cari pola E2 biasa
//                        val e2Index = rawEpc.indexOf("E2", ignoreCase = true)
//                        if (e2Index >= 0 && e2Index + 24 <= rawEpc.length) {
//                            rawEpc.substring(e2Index, e2Index + 24)
//                        } else {
//                            // Fallback: ambil bagian yang relevan dan tambahkan E280
//                            "E280" + rawEpc.take(20)
//                        }
//                    }
//                }
//
//                // Jika tidak dimulai dengan E2 dan panjang normal
//                !rawEpc.startsWith("E2", ignoreCase = true) -> {
//                    "E280" + rawEpc
//                }
//
//                // Default return raw EPC
//                else -> rawEpc
//            }
//        } catch (e: Exception) {
//            println("Error parsing EPC: ${e.message}")
//            rawEpc // Return original jika ada error
//        }
    }

    LaunchedEffect(isScanning) {
        if (toneGenerator == null) {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        }
        if (isScanning) {
            if (typeHH == "1") {
                val uhfReader = RFIDWithUHFUART.getInstance()
                val power = powerLevel.toInt()
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
                                        assetCategoryModel.searchAssetByRfid(context, epc, rssi, power)
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

            else if(typeHH == "2"){
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
                                        val rawEpc = it.epc
                                        val rssi = it.rssi
                                        val power = powerLevel.toInt()  // Note: Menggunakan mReader?.power, sesuaikan jika perlu mSled?.power

                                        // Null safety checks
                                        if (rawEpc.isNullOrEmpty()) return@launch

//                                        println("SLED: $rawEpc")

                                        val epc = parseEPC(rawEpc)

                                        // **SOLUSI 1: Cek apakah EPC sudah pernah di-scan**
                                        if (scannedEpcs.contains(epc)) {
                                            return@launch // Skip jika sudah pernah di-scan
                                        }

                                        // Tambahkan ke set EPC yang sudah di-scan
                                        withContext(Dispatchers.Main) {
                                            scannedEpcs = scannedEpcs + epc
                                        }

                                        // 🔊 Beep!

//                                if (!resultEpc!!.contains(epc)) {
//                                    resultEpc = resultEpc.orEmpty() + epc
//                                }

                                        // Switch ke Main thread untuk UI operations
                                        if(isScanning){
                                            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
                                            withContext(Dispatchers.Main) {
                                                delay(50)
                                                assetCategoryModel?.searchAssetByRfid(context, epc, rssi, power)
                                            }
                                        }

//                                        println("EPC : $epc , RSSI : $rssi , Power : $powerLevel.toInt()")
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
                if(typeHH == "1"){
                    mReader?.stopInventory()
                } else {
                    mSled?.stopInventory()
                }

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
    // end buat radar

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
                        Text(
                            "Asset Viewer",
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
                                    if(isScanning) Color(0xFFD0EBFF) else Color.Red.copy(alpha = 0.3f),
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

                    Text(
                        text = "Point the RFID reader at the asset you want to scan",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 280.dp) // GANTI offset menjadi padding!
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .background(Color.White)
                        .padding(horizontal = 0.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                                .background(Color.White)
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth()
                        ) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .width(50.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0xFFC9CBCE))
                                    .align(Alignment.CenterHorizontally)
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Row {
                                Text(
                                    "Scan Result",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "(${assetCategoryModel.rfidScanResult?.size ?: 0})", // Changed from resultScan to assetCategoryModel.rfidScanResult
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(Modifier.height(16.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xffF1F8FF))
                                    .padding(16.dp)
                            ) {
                                Row {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_info),
                                        contentDescription = "Head",
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    androidx.compose.material3.Text(
                                        "RFID Power affect results, adjust it using the button at the top right.",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.W300
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }

                    items(assetCategoryModel.rfidScanResult!!) { asset ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showSheet = true
                                    assetCategoryModel.assetDetail = asset
                                }
                                .padding(vertical = 8.dp, horizontal = 16.dp)
                        ) {
                            androidx.compose.material3.Text(
                                text = asset.no_register,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            androidx.compose.material3.Text(
                                text = "${asset.nama_barang} • ${asset.location}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            androidx.compose.material3.Text(
                                text = "Distance Asset : ${asset.jarak} CM",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            HorizontalDivider(
                                color = Color(0xffC9CBCE),
                                thickness = 0.5.dp
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }
    }

    if (showSheet && assetCategoryModel.assetDetail!= null) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White,
            modifier = Modifier.navigationBarsPadding(),
            windowInsets = BottomSheetDefaults.windowInsets // ✅ gunakan default
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                color = Color.White
            ) {
                AssetBottomSheetContent(
                    context = context,
                    viewModel = assetCategoryModel,
                    asset = assetCategoryModel.assetDetail!!
                )
            }
        }
    }


}