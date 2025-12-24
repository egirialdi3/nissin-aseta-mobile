package id.aseta.app.ui.screen.stock_opname

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import id.aseta.app.R
import id.aseta.app.data.model.AssetDetail
import id.aseta.app.data.model.AssetItem
import id.aseta.app.data.model.LocationItem
import id.aseta.app.viewmodel.AssetCategoryViewModel
import id.aseta.app.viewmodel.AssetViewerViewModel

@SuppressLint("UnusedBoxWithConstraintsScope")
@androidx.annotation.OptIn(ExperimentalGetImage::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanAssetScreen(
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
    val assetList = assetMap[type]?: emptyList()

    val lastScannedValue = remember { mutableStateOf<String?>(null) }
    val lastScannedTime = remember { mutableStateOf(0L) }

    LaunchedEffect(asset) {
        if (asset != null) {
            assetCategoryViewModel.markAssetAsFound(asset!!.no_register,selectedLocation!!.location_id)
            navController.previousBackStackEntry?.savedStateHandle?.set(
                "assetScanned", true
            )
        }else{
            navController.previousBackStackEntry?.savedStateHandle?.set(
                "assetScanned", true
            )
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenHeightPx = constraints.maxHeight.toFloat()
        val bottomSheetHeightPx = with(LocalDensity.current) { 300.dp.toPx() } // misalnya tinggi bottom sheet 300dp
        val availableHeight = screenHeightPx - bottomSheetHeightPx
        val focusBoxOffset = availableHeight / 2  // tengah dari area di atas bottom sheet

        if (isCameraActive) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val barcodeScanner = BarcodeScanning.getClient()

                        val analysisUseCase = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also {
                                it.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                                    val mediaImage = imageProxy.image
                                    if (mediaImage != null) {
                                        val image = InputImage.fromMediaImage(
                                            mediaImage,
                                            imageProxy.imageInfo.rotationDegrees
                                        )
                                        barcodeScanner.process(image)
                                            .addOnSuccessListener { barcodes ->
                                                for (barcode in barcodes) {
                                                    val rawValue = barcode.rawValue
                                                    if (rawValue != null) {
                                                        val currentTime = System.currentTimeMillis()
                                                        if (lastScannedValue.value == rawValue && (currentTime - lastScannedTime.value) < 2000) {
                                                            // Kalau barcode sama dalam waktu 2 detik, abaikan
                                                            imageProxy.close()
                                                            return@addOnSuccessListener
                                                        }

                                                        lastScannedValue.value = rawValue
                                                        lastScannedTime.value = currentTime


                                                        val foundAsset = assetList?.find { it.no_register == rawValue }

                                                        if (foundAsset != null) {
                                                            assetCategoryViewModel.markAssetAsFound(rawValue, type)
                                                            Toast.makeText(
                                                                ctx,
                                                                "Asset ditemukan: ${foundAsset.nama_barang}",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        } else {
                                                            // todo : disini , jika foreign maka get asset tersebut berdasarkan no register
                                                            // todo : dan masukan kedalam assetcategoryviewmodel.addAsset dengan tanda isForeign = True
                                                            Toast.makeText(
                                                                ctx,
                                                                "asset tidak ada dalam daftar pada location berikut",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    }

                                                }
                                            }
                                            .addOnCompleteListener {
                                                imageProxy.close()
                                            }
                                    } else {
                                        imageProxy.close()
                                    }
                                }
                            }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        try {
                            val camera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                analysisUseCase
                            )

                            // Flash control
                            camera.cameraControl.enableTorch(flashlightEnabled.value)
                            cameraRef = camera
                        } catch (exc: Exception) {
                            exc.printStackTrace()
                        }

                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // TopAppBar
        TopAppBar(
            title = { Text("Scan QR") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )

        // Focus Box
        Box(
            modifier = Modifier
                .offset { IntOffset(0, focusBoxOffset.toInt()) }
                .align(Alignment.TopCenter)
                .size(200.dp)
                .border(2.dp, Color.White, RoundedCornerShape(16.dp))
        )

        // Flashlight Button
        IconButton(
            onClick = {
                flashlightEnabled.value = !flashlightEnabled.value
                cameraRef?.cameraControl?.enableTorch(flashlightEnabled.value)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .background(Color.White.copy(alpha = 0.8f), shape = CircleShape)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_flashlight),
                contentDescription = "Flashlight",
                tint = Color.Black
            )
        }

        CustomBottomSheet(
            assetList = assetList,
            navController = navController,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .height(300.dp)
        )
    }
}


@Composable
fun CustomBottomSheet(
    assetList: List<AssetDetail>,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp
    val maxHeight = (screenHeight * 0.4f).dp

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = maxHeight),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        color = Color.White,
        shadowElevation = 16.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = Color.Black
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Daftar Asset",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Divider()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                assetList.forEach { asset ->
                    AssetRow(asset = asset)
                    Divider(color = Color(0xffC9CBCE), thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
fun AssetRow(asset: AssetDetail) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* handle click */ }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = asset.no_register,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${asset.nama_barang} • ${asset.location}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        Box(
            modifier = Modifier
                .background(
                    color = if (asset.isFound) Color(0xFFE8F5E9) else Color(0xFFF5F5F5),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (asset.isFound) "Found" else "Not Found",
                color = if (asset.isFound) Color(0xFF2E7D32) else Color.Black,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}