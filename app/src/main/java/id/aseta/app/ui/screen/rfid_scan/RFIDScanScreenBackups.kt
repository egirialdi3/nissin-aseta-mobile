//package id.aseta.app.ui.screen.rfid_scan
//
//import AuthViewModel
//import android.content.Context
//import android.media.AudioManager
//import android.media.ToneGenerator
//import android.util.Log
//import android.widget.Toast
//import androidx.compose.animation.core.InfiniteTransition
//import androidx.compose.animation.core.LinearEasing
//import androidx.compose.animation.core.RepeatMode
//import androidx.compose.animation.core.animateFloat
//import androidx.compose.animation.core.infiniteRepeatable
//import androidx.compose.animation.core.rememberInfiniteTransition
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//import id.aseta.app.data.model.LocationItem
//import id.aseta.app.viewmodel.AssetCategoryViewModel
//import id.aseta.app.viewmodel.UHFViewModel
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.material.Text
//import androidx.compose.material.icons.filled.Settings
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.alpha
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.text.style.TextAlign
//import androidx.hilt.navigation.compose.hiltViewModel
//import com.rscja.deviceapi.RFIDWithUHFBLE
//import com.rscja.deviceapi.RFIDWithUHFUART
//import com.rscja.deviceapi.interfaces.ConnectionStatus
//import com.rscja.deviceapi.interfaces.KeyEventCallback
//import id.aseta.app.R
//import id.aseta.app.data.local.PowerPreference
//import id.aseta.app.data.local.TokenDataStore
//import id.aseta.app.data.model.AssetDetail
//import id.aseta.app.ui.screen.inspection.FullScreenInspectionBottomSheet
//import id.aseta.app.ui.screen.relocation.FullScreenRelocationBottomSheet
//import id.aseta.app.viewmodel.DeviceSettingViewModel
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun RFIDScanScreen(
//    assetCategoryModel: AssetCategoryViewModel,
//    navController: NavController,
//    deviceSettingVM: DeviceSettingViewModel = hiltViewModel(),
//    type: String,
//) {
//    val context = LocalContext.current
//    var assetDetail by remember { mutableStateOf<AssetDetail?>(null) }
//    var showSheet by remember { mutableStateOf(false) }
//
//    // State dari ViewModel
//    val isScanning by deviceSettingVM.isUHFScanning
//    val epcList by remember { derivedStateOf { deviceSettingVM.epcList } }
//    val initMessage by deviceSettingVM.initMessage
//    val isInitializing by deviceSettingVM.isInitializing
//
//    // Radar animation
//    val infiniteTransition = rememberInfiniteTransition()
//    val (scale, alpha) = getRadarAnimation(infiniteTransition)
//
//    // Setup callback untuk hasil scan
//    LaunchedEffect(deviceSettingVM) {
//        deviceSettingVM.onEpcScanned = { epc ->
//            assetCategoryModel.searchAssetByRfid(context, epc)
//        }
//    }
//
//    // Lifecycle management
//    LaunchedEffect(Unit) {
//        deviceSettingVM.initializeUHF()
//        assetCategoryModel.clearRfidScanResult()
//    }
//
//
//    Box(modifier = Modifier.fillMaxSize()) {
//        // Background Image
//        Image(
//            painter = painterResource(id = R.drawable.bg_main),
//            contentDescription = null,
//            modifier = Modifier
//                .height(635.dp)
//                .width(618.dp),
//            contentScale = ContentScale.Crop
//        )
//
//        Scaffold(
//            topBar = { ScanningTopBar(navController) },
//            containerColor = Color.Transparent
//        ) { paddingValues ->
//            ScanningContent(
//                paddingValues = paddingValues,
//                isScanning = isScanning,
//                scale = scale,
//                alpha = alpha,
//                assetCategoryModel = assetCategoryModel,
//                epcListSize = epcList.size,
//                showSheet = { asset ->
//                    assetDetail = asset
//                    showSheet = true
//                }
//            )
//        }
//    }
//
//    // Bottom Sheet Handling
//    AssetBottomSheetHandler(
//        showSheet = showSheet,
//        assetDetail = assetDetail,
//        type = type,
//        assetCategoryModel = assetCategoryModel,
//        onDismiss = { showSheet = false }
//    )
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//private fun ScanningTopBar(navController: NavController) {
//    TopAppBar(
//        title = {
//            Text(
//                "Scanning Tag",
//                fontWeight = FontWeight.Medium,
//                fontSize = 18.sp,
//                color = Color.White
//            )
//        },
//        navigationIcon = {
//            IconButton(onClick = { navController.popBackStack() }) {
//                Icon(
//                    imageVector = Icons.Default.ArrowBack,
//                    contentDescription = "Back",
//                    tint = Color.White
//                )
//            }
//        },
//        colors = TopAppBarDefaults.topAppBarColors(
//            containerColor = Color(0xFF1E3A8A)
//        )
//    )
//}
//
//@Composable
//private fun ScanningContent(
//    paddingValues: PaddingValues,
//    isScanning: Boolean,
//    scale: Float,
//    alpha: Float,
//    assetCategoryModel: AssetCategoryViewModel,
//    epcListSize: Int,
//    showSheet: (AssetDetail) -> Unit
//) {
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(paddingValues)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .background(Color.Transparent),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Spacer(modifier = Modifier.height(24.dp))
//            RadarAnimation(isScanning, scale, alpha)
//            ScanningInstructionText()
//        }
//
//        ScanResultsList(
//            assetCategoryModel = assetCategoryModel,
//            epcListSize = epcListSize,
//            showSheet = showSheet
//        )
//    }
//}
//
//@Composable
//private fun RadarAnimation(isScanning: Boolean, scale: Float, alpha: Float) {
//    Box(
//        contentAlignment = Alignment.Center,
//        modifier = Modifier
//            .size(250.dp)
//            .background(Color.Transparent)
//    ) {
//        Box(
//            modifier = Modifier
//                .size(140.dp * scale)
//                .alpha(alpha)
//                .background(
//                    if(isScanning) Color(0xFF1E3A8A) else Color.Red,
//                    shape = CircleShape
//                )
//        )
//        Box(
//            modifier = Modifier
//                .size(200.dp)
//                .background(
//                    if(isScanning) Color(0xFFD0EBFF) else Color.Red.copy(alpha = 0.3f),
//                    shape = CircleShape
//                ),
//            contentAlignment = Alignment.Center
//        ) {
//            Text(
//                if(isScanning) "Scanning" else "Start Scanning",
//                fontSize = 24.sp,
//                fontWeight = FontWeight.Bold,
//                color = Color.White
//            )
//        }
//    }
//}
//
//@Composable
//private fun ScanningInstructionText() {
//    Text(
//        text = "Point the RFID reader at the asset you want to scan",
//        fontSize = 14.sp,
//        fontWeight = FontWeight.Normal,
//        color = Color.Gray,
//        textAlign = TextAlign.Center,
//        modifier = Modifier.padding(horizontal = 12.dp)
//    )
//}
//
//@Composable
//private fun ScanResultsList(
//    assetCategoryModel: AssetCategoryViewModel,
//    epcListSize: Int,
//    showSheet: (AssetDetail) -> Unit
//) {
//    LazyColumn(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(top = 280.dp)
//            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
//            .background(Color.White)
//    ) {
//        item {
//            ResultHeader(epcListSize)
//        }
//
//        items(assetCategoryModel.rfidScanResult!!) { asset ->
//            AssetItem(asset, onItemClick = { showSheet(asset) })
//        }
//
//        item {
//            Spacer(modifier = Modifier.height(40.dp))
//        }
//    }
//}
//
//@Composable
//private fun ResultHeader(epcListSize: Int) {
//    Column(
//        modifier = Modifier
//            .padding(horizontal = 16.dp)
//            .fillMaxWidth()
//    ) {
//        Spacer(modifier = Modifier.height(8.dp))
//        DragHandle()
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Text(
//            "Scan Result (${epcListSize})",
//            fontWeight = FontWeight.Bold,
//            fontSize = 16.sp
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//        InfoBox()
//    }
//}
//
//@Composable
//private fun DragHandle() {
//    Box(
//        modifier = Modifier
//            .width(50.dp)
//            .height(4.dp)
//            .clip(RoundedCornerShape(50))
//            .background(Color(0xFFC9CBCE))
////            .align(Alignment.CenterHorizontally)
//    )
//}
//
//@Composable
//private fun InfoBox() {
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clip(RoundedCornerShape(20.dp))
//            .background(Color(0xffF1F8FF))
//            .padding(16.dp)
//    ) {
//        Row {
//            Image(
//                painter = painterResource(id = R.drawable.ic_info),
//                contentDescription = "Info",
//                modifier = Modifier.size(24.dp)
//            )
//            Spacer(modifier = Modifier.width(4.dp))
//            Text(
//                "RFID Power affect results, adjust it using the button at the top right.",
//                fontSize = 12.sp,
//                fontWeight = FontWeight.W300
//            )
//        }
//    }
//}
//
//@Composable
//private fun AssetItem(asset: AssetDetail, onItemClick: () -> Unit) {
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable(onClick = onItemClick)
//            .padding(vertical = 8.dp, horizontal = 16.dp)
//    ) {
//        Text(
//            text = asset.no_register,
//            style = MaterialTheme.typography.bodyLarge,
//            fontWeight = FontWeight.SemiBold
//        )
//        Spacer(modifier = Modifier.height(2.dp))
//        Text(
//            text = "${asset.nama_barang} • ${asset.location}",
//            style = MaterialTheme.typography.bodyMedium,
//            color = Color.Gray
//        )
//        Divider(
//            color = Color(0xffC9CBCE),
//            thickness = 0.5.dp,
//            modifier = Modifier.padding(top = 8.dp)
//        )
//    }
//}
//
//@Composable
//private fun getRadarAnimation(infiniteTransition: InfiniteTransition): Pair<Float, Float> {
//    val scale by infiniteTransition.animateFloat(
//        initialValue = 1f,
//        targetValue = 2f,
//        animationSpec = infiniteRepeatable(
//            animation = tween(500, easing = LinearEasing),
//            repeatMode = RepeatMode.Restart
//        )
//    )
//    val alpha by infiniteTransition.animateFloat(
//        initialValue = 0.5f,
//        targetValue = 0f,
//        animationSpec = infiniteRepeatable(
//            animation = tween(500, easing = LinearEasing),
//            repeatMode = RepeatMode.Restart
//        )
//    )
//    return Pair(scale, alpha)
//}
//
//@Composable
//private fun AssetBottomSheetHandler(
//    showSheet: Boolean,
//    assetDetail: AssetDetail?,
//    type: String,
//    assetCategoryModel: AssetCategoryViewModel,
//    onDismiss: () -> Unit
//) {
//    if (showSheet && assetDetail != null) {
//        when (type) {
//            "relocation" -> FullScreenRelocationBottomSheet(
//                context = LocalContext.current,
//                assetDetail = assetDetail,
//                viewModel = assetCategoryModel,
//                onDismiss = onDismiss
//            )
//
//            "inspection" -> FullScreenInspectionBottomSheet(
//                context = LocalContext.current,
//                assetDetail = assetDetail,
//                viewModel = assetCategoryModel,
//                onDismiss = onDismiss
//            )
//        }
//    }
//}
//
//
//// ... (Lanjutan dari kode sebelumnya)
//
//@Composable
//private fun PowerLevelControl(
//    deviceSettingVM: DeviceSettingViewModel,
//    context: Context
//) {
//    var showPowerDialog by remember { mutableStateOf(false) }
//    val currentPower by remember { derivedStateOf { deviceSettingVM.currentPower } }
//
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp),
//        contentAlignment = Alignment.CenterEnd
//    ) {
//        IconButton(
//            onClick = { showPowerDialog = true },
//            modifier = Modifier.size(40.dp)
//        ) {
//            Icon(
//                imageVector = Icons.Default.Settings,
//                contentDescription = "Power Settings",
//                tint = Color.White
//            )
//        }
//    }
//
//    if (showPowerDialog) {
//        AlertDialog(
//            onDismissRequest = { showPowerDialog = false },
//            title = { Text("Adjust RFID Power") },
//            text = {
//                Column {
//                    Text("Current Power: ${currentPower.value.toString()}%")
//                    Slider(
//                        value = currentPower.value,
//                        onValueChange = { deviceSettingVM.setPower(it) },
//                        valueRange = 0f..100f,
//                        steps = 20
//                    )
//                }
//            },
//            confirmButton = {
//                TextButton(
//                    onClick = {
////                        deviceSettingVM.savePowerSetting(context)
//                        showPowerDialog = false
//                    }
//                ) {
//                    Text("Save")
//                }
//            }
//        )
//    }
//}
//
//@Composable
//private fun ConnectionStatusBanner(
//    deviceSettingVM: DeviceSettingViewModel,
//    context: Context
//) {
//    val connectionState by deviceSettingVM.connectionStatus
//    val message = when (connectionState) {
//        ConnectionState.CONNECTED -> "Connected to RFID Reader"
//        ConnectionState.CONNECTING -> "Connecting..."
//        ConnectionState.DISCONNECTED -> "Disconnected"
//        ConnectionState.ERROR -> "Connection Error"
//    }
//
//    if (connectionState != ConnectionState.CONNECTED) {
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .background(Color.Red.copy(alpha = 0.8f))
//                .padding(8.dp)
//        ) {
//            Text(
//                text = message,
//                color = Color.White,
//                textAlign = TextAlign.Center,
//                modifier = Modifier.fillMaxWidth()
//            )
//        }
//    }
//}
//
//@Composable
//private fun InitializationHandler(
//    deviceSettingVM: DeviceSettingViewModel,
//    context: Context
//) {
//    val initMessage by deviceSettingVM.initMessage
//    val isInitializing by deviceSettingVM.isInitializing
//
//    LaunchedEffect(initMessage) {
//        initMessage?.let {
//            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
////            deviceSettingVM.clearInitMessage()
//        }
//    }
//
//    if (isInitializing) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(Color.Black.copy(alpha = 0.5f)),
//            contentAlignment = Alignment.Center
//        ) {
//            CircularProgressIndicator(color = Color.White)
//        }
//    }
//}
//
//// Extension function untuk handling state
//@Composable
//private fun DeviceSettingViewModel.observeAsState() {
//    val context = LocalContext.current
//
//    LaunchedEffect(key1 = this) {
//        eventFlow.collect { event ->
//            when (event) {
//                is UHFEvent.ShowToast -> {
//                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
//                }
//                is UHFEvent.UpdateConnectionStatus -> {
//                    // Handle connection status update
//                }
//            }
//        }
//    }
//}
//
//// Event sealed class untuk ViewModel
//sealed class UHFEvent {
//    data class ShowToast(val message: String) : UHFEvent()
//    data class UpdateConnectionStatus(val status: ConnectionState) : UHFEvent()
//}
//
//// Enum class untuk status koneksi
//enum class ConnectionState {
//    CONNECTED, CONNECTING, DISCONNECTED, ERROR
//}