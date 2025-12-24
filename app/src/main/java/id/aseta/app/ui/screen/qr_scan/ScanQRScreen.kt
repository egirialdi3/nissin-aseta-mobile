package id.aseta.app.ui.screen.qr_scan

import android.content.Context
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import id.aseta.app.R
import id.aseta.app.data.model.AssetDetail
import id.aseta.app.data.source.local.TokenDataStore
import id.aseta.app.ui.screen.corrective_maintenance.FullScreenCorrectiveBottomSheet
import id.aseta.app.ui.screen.inspection.FullScreenInspectionBottomSheet
import id.aseta.app.ui.screen.mutasi.FullScreenMutationBottomSheet
import id.aseta.app.ui.screen.relocation.FullScreenRelocationBottomSheet
import id.aseta.app.viewmodel.AssetCategoryViewModel
import id.aseta.app.viewmodel.AssetViewerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.max
import android.graphics.Rect as AndroidRect
import androidx.compose.ui.draw.blur
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.input.pointer.pointerInput


@androidx.annotation.OptIn(ExperimentalGetImage::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanQRScreen(
    navController: NavController,
    viewModel: AssetViewerViewModel,
    assetCategoryViewModel: AssetCategoryViewModel,
    type: String
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val flashlightEnabled = remember { mutableStateOf(false) }
    var cameraRef by remember { mutableStateOf<Camera?>(null) }
    var showSheet by remember { mutableStateOf(false) }
    var asset by remember { mutableStateOf<AssetDetail?>(null) }
    var isCameraActive by remember { mutableStateOf(true) }
    var focusBoxRect by remember { mutableStateOf<Rect?>(null) }
    var isProcessingQrCode by remember { mutableStateOf(false) }
    var cameraZoom by remember { mutableStateOf(1.5f) }

    val scanBoxSize = 200.dp

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (isCameraActive && focusBoxRect != null) {
            Box(modifier = Modifier
                .align(Alignment.Center)
                .size(scanBoxSize)
                .clip(RoundedCornerShape(16.dp))
            ) {
                AndroidView(factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        scaleType = PreviewView.ScaleType.FILL_START
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val barcodeScanner = BarcodeScanning.getClient()

                        val analysisUseCase = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build().also {
                                it.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                                    if (isProcessingQrCode) {
                                        imageProxy.close()
                                        return@setAnalyzer
                                    }

                                    val mediaImage = imageProxy.image
                                    if (mediaImage != null) {
                                        // Konversi koordinat UI ke koordinat gambar
                                        val imageWidth = mediaImage.width
                                        val imageHeight = mediaImage.height
                                        val viewWidth = previewView.width.toFloat()
                                        val viewHeight = previewView.height.toFloat()

                                        // Hitung scaling dan offset
                                        val scale = max(imageWidth/viewWidth, imageHeight/viewHeight)
                                        val offsetX = (viewWidth * scale - imageWidth) / 2
                                        val offsetY = (viewHeight * scale - imageHeight) / 2

                                        // Hitung area crop
                                        val focusLeft = ((focusBoxRect!!.left * scale - offsetX).coerceAtLeast(0f)).toInt()
                                        val focusTop = ((focusBoxRect!!.top * scale - offsetY).coerceAtLeast(0f)).toInt()
                                        val focusRight = ((focusBoxRect!!.right * scale - offsetX).coerceAtMost(imageWidth.toFloat())).toInt()
                                        val focusBottom = ((focusBoxRect!!.bottom * scale - offsetY).coerceAtMost(imageHeight.toFloat())).toInt()

                                        // Buat Rect menggunakan Android Rect
                                        val cropRect = AndroidRect(
                                            focusLeft,
                                            focusTop,
                                            focusRight,
                                            focusBottom
                                        )

                                        // Proses gambar yang dicrop
                                        val croppedImage = InputImage.fromMediaImage(
                                            mediaImage,
                                            imageProxy.imageInfo.rotationDegrees,
                                        )

                                        barcodeScanner.process(croppedImage)
                                            .addOnSuccessListener { barcodes ->
                                                // Karena kamera hanya menampilkan box tengah,
                                                // semua QR code yang terdeteksi pasti ada di area fokus
                                                if (barcodes.isNotEmpty() && !isProcessingQrCode) {
                                                    val rawValue = barcodes.first().rawValue
                                                    if (rawValue != null) {
                                                        isProcessingQrCode = true
                                                        viewModel.fetchAssetByQRCode(context, rawValue) {
                                                            val result = viewModel.assetData
                                                            if (result != null) {
                                                                asset = result
                                                                showSheet = true
                                                                cameraProvider.unbindAll()
                                                            } else {
                                                                Toast.makeText(
                                                                    ctx,
                                                                    viewModel.errorMessage ?: "Asset Tidak Ditemukan",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                                isProcessingQrCode = false
                                                            }
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
//                            camera.cameraControl.setZoomRatio(1.0f)
                            camera.cameraControl.setZoomRatio(cameraZoom)
                            camera.cameraControl.enableTorch(flashlightEnabled.value)
                            cameraRef = camera
                        } catch (exc: Exception) {
                            exc.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                }, modifier = Modifier.fillMaxSize())
            }
        }

        // Tampilkan overlay hitam di seluruh layar
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (focusBoxRect != null) {
                // Gambar background hitam dengan alpha
                drawRect(color = Color.Black.copy(alpha = 0.8f), size = size)

                // Buat lubang transparan di area focus box
                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = focusBoxRect!!.topLeft,
                    size = focusBoxRect!!.size,
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    blendMode = BlendMode.Clear
                )

                // Gambar garis putih di pojok
                val cornerLength = 20.dp.toPx()
                val strokeWidth = 3.dp.toPx()
                val rect = focusBoxRect!!
                val paint = Paint().apply {
                    this.color = Color.White
                    this.strokeWidth = strokeWidth
                    this.style = PaintingStyle.Stroke
                }

                // Pojok kiri atas
                drawLine(
                    paint.color,
                    Offset(rect.left, rect.top + cornerLength),
                    Offset(rect.left, rect.top),
                    strokeWidth
                )
                drawLine(
                    paint.color,
                    Offset(rect.left, rect.top),
                    Offset(rect.left + cornerLength, rect.top),
                    strokeWidth
                )

                // Pojok kanan atas
                drawLine(
                    paint.color,
                    Offset(rect.right - cornerLength, rect.top),
                    Offset(rect.right, rect.top),
                    strokeWidth
                )
                drawLine(
                    paint.color,
                    Offset(rect.right, rect.top),
                    Offset(rect.right, rect.top + cornerLength),
                    strokeWidth
                )

                // Pojok kiri bawah
                drawLine(
                    paint.color,
                    Offset(rect.left, rect.bottom - cornerLength),
                    Offset(rect.left, rect.bottom),
                    strokeWidth
                )
                drawLine(
                    paint.color,
                    Offset(rect.left, rect.bottom),
                    Offset(rect.left + cornerLength, rect.bottom),
                    strokeWidth
                )

                // Pojok kanan bawah
                drawLine(
                    paint.color,
                    Offset(rect.right - cornerLength, rect.bottom),
                    Offset(rect.right, rect.bottom),
                    strokeWidth
                )
                drawLine(
                    paint.color,
                    Offset(rect.right, rect.bottom),
                    Offset(rect.right, rect.bottom - cornerLength),
                    strokeWidth
                )
            }
        }

        // TopAppBar tetap di atas
        TopAppBar(
            title = { Text("Scan QR") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            modifier = Modifier.background(Color.Transparent)
        )

        // Box putih sebagai tempat kamera fokus (ini hanya border, kamera ada di dalamnya)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(scanBoxSize)
                .border(2.dp, Color.White, RoundedCornerShape(16.dp))
                .onGloballyPositioned { coordinates ->
                    val bounds = coordinates.boundsInRoot()
                    focusBoxRect = Rect(
                        bounds.left,
                        bounds.top,
                        bounds.right,
                        bounds.bottom
                    )
                }
        )

        // Tambahkan petunjuk di bawah box
        Text(
            text = "Align the QR code within the box.",
            color = Color.White,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = (scanBoxSize.value + 32).dp)
        )
        Text(
            text = "Zoom: ${String.format("%.1f", cameraZoom)}x",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )

        // Row untuk tombol zoom dan flashlight
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Tombol zoom out
            IconButton(
                onClick = {
                    // Kurangi zoom, minimal 1.0
                    val newZoom = (cameraZoom - 0.5f).coerceAtLeast(1.0f)
                    cameraZoom = newZoom
                    cameraRef?.cameraControl?.setZoomRatio(newZoom)
                },
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.8f), shape = CircleShape)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Zoom Out",
                    tint = Color.Black
                )
            }

            // Tombol flashlight
            IconButton(
                onClick = {
                    flashlightEnabled.value = !flashlightEnabled.value
                    cameraRef?.cameraControl?.enableTorch(flashlightEnabled.value)
                },
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.8f), shape = CircleShape)
                    .size(48.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_flashlight),
                    contentDescription = "Flashlight",
                    tint = if (flashlightEnabled.value) Color.Yellow else Color.Black
                )
            }

            // Tombol zoom in
            IconButton(
                onClick = {
                    // Tambah zoom, maksimal 5.0
                    val newZoom = (cameraZoom + 0.5f).coerceAtMost(5.0f)
                    cameraZoom = newZoom
                    cameraRef?.cameraControl?.setZoomRatio(newZoom)
                },
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.8f), shape = CircleShape)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Zoom In",
                    tint = Color.Black
                )
            }
        }


    }

    if (showSheet && asset != null) {
        isCameraActive = false
        val dismiss = {
            showSheet = false
            isCameraActive = true
            isProcessingQrCode = false
        }
        when (type) {
            "asset_viewer" -> {
//                ModalBottomSheet(
//                    onDismissRequest = dismiss,
//                    sheetState = rememberModalBottomSheetState(),
//                    windowInsets = WindowInsets(0),
//
//                ) {
//                    AssetBottomSheetContent(asset!!, assetCategoryViewModel, context)
//                }
                ModalBottomSheet(
                    onDismissRequest = dismiss,
                    sheetState = rememberModalBottomSheetState(
                        skipPartiallyExpanded = true,  // Pastikan tidak ada state partially expanded
                    ),
                    windowInsets = WindowInsets(0),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.8f)  // Set tinggi konten menjadi 80% layar
                    ) {
                        AssetBottomSheetContent(asset!!, context,assetCategoryViewModel)
                    }
                }
            }
            "inspection" -> {
                FullScreenInspectionBottomSheet(context, asset!!, assetCategoryViewModel, dismiss)
            }
            "corrective_maintenance"->{
                FullScreenCorrectiveBottomSheet(context, asset!!, assetCategoryViewModel, dismiss)
            }
            "mutation"->{
                FullScreenMutationBottomSheet(context, asset!!, assetCategoryViewModel, dismiss)
            }
            else -> {
                FullScreenRelocationBottomSheet(context, asset!!, assetCategoryViewModel, dismiss)
            }
        }
    }
}

@Composable
fun AssetBottomSheetContent(
    asset: AssetDetail,
    context: Context,
    viewModel: AssetCategoryViewModel,
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Asset Detail", "Asset User Info", "Maintenance Log")
//    val tabTitles = listOf("Asset Detail")

    val isLoading = viewModel.loadingGetImage[asset.no_register] == true
//    var imageUrl by remember { mutableStateOf<String?>(null) }
    var imageUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var showImagePreview by remember { mutableStateOf(false) }

//    LaunchedEffect(asset.no_register) {
//        viewModel.getImageAsset(context, asset.no_register)
//        val dataUser = TokenDataStore.getDataUser(context)
//        val namaPrsh = dataUser?.nama_prsh
////        val imageFile = viewModel.assetFullDetailsMap[asset.no_register]?.firstOrNull()?.images?.firstOrNull()
////        if (!imageFile.isNullOrBlank() && !namaPrsh.isNullOrBlank()) {
////            imageUrl = "https://app.aseta.id/api/images/$namaPrsh/$imageFile"
////        }
//        val imageFiles = viewModel.assetFullDetailsMap[asset.no_register]?.firstOrNull()?.images ?: emptyList()
//        println("gambar disini")
//        println(imageFiles)
//        if (imageFiles.isNotEmpty() && !namaPrsh.isNullOrBlank()) {
//            imageUrls = imageFiles.map { "https://app.aseta.id/api/images/$namaPrsh/$it" }
//        }
//    }

    LaunchedEffect(selectedTab, viewModel.assetFullDetailsMap[asset.no_register]) {
        if (selectedTab == 0) {
            val dataUser = TokenDataStore.getDataUser(context)
            val namaPrsh = dataUser?.nama_prsh

            if (viewModel.assetFullDetailsMap[asset.no_register].isNullOrEmpty()) {
                viewModel.getImageAsset(context, asset.no_register)
            } else {
                val imageFiles = viewModel.assetFullDetailsMap[asset.no_register]?.firstOrNull()?.images ?: emptyList()
                println("Update gambar setelah ViewModel berubah: $imageFiles")
                if (imageFiles.isNotEmpty() && !namaPrsh.isNullOrBlank()) {
                    imageUrls = imageFiles.map { "https://app.aseta.id/api/images/$namaPrsh/$it" }
                }
            }
        }
    }

    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            1 -> viewModel.fetchLogMovingAsset(context, asset.no_register)
            2 -> viewModel.fetchLogMaintenance(context, asset.no_register)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 0.dp,
            divider = { Divider(color = Color(0xFFE0E0E0)) },
            containerColor = Color.Transparent
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) Color(0xFF1E3A8A) else Color.Gray
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            0 -> TabDetail(asset, context, isLoading, imageUrls, showImagePreview) { showImagePreview = it }
            1 -> TabUserInfo(viewModel)
            2 -> TabMaintenanceLog(viewModel)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TabDetail(
    asset: AssetDetail,
    context: Context,
    isLoading: Boolean,
    imageUrls: List<String>,
    showImagePreview: Boolean,
    onImagePreviewChange: (Boolean) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { imageUrls.size })
    var selectedImageIndex by remember { mutableStateOf(0) }
    var isPreviewOpen by remember { mutableStateOf(false) }

    // 🔁 Sync pager swipe ke thumbnail & indicator
    LaunchedEffect(pagerState.currentPage) {
        selectedImageIndex = pagerState.currentPage
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            when {
                isLoading -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                imageUrls.isNotEmpty() -> {
                    // ✅ Slider (pager) untuk gambar
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) { page ->
                        AsyncImage(
                            model = imageUrls[page],
                            contentDescription = "Asset Image",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedImageIndex = page
                                    isPreviewOpen = true
                                },
                            contentScale = ContentScale.Crop
                        )
                    }

                    // ✅ Indicator dots sinkron
//                    Row(
//                        horizontalArrangement = Arrangement.Center,
//                        verticalAlignment = Alignment.CenterVertically,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(top = 8.dp)
//                    ) {
//                        repeat(imageUrls.size) { index ->
//                            val isSelected = selectedImageIndex == index
//                            Box(
//                                modifier = Modifier
//                                    .padding(horizontal = 4.dp)
//                                    .size(if (isSelected) 10.dp else 8.dp)
//                                    .clip(CircleShape)
//                                    .background(if (isSelected) Color(0xFF1E3A8A) else Color.Gray)
//                            )
//                        }
//                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // ✅ Thumbnail row sinkron dengan pager & indicator
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(imageUrls.size) { index ->
                            AsyncImage(
                                model = imageUrls[index],
                                contentDescription = "Thumbnail $index",
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = if (index == selectedImageIndex) 2.dp else 0.dp,
                                        color = if (index == selectedImageIndex) Color(0xFF1E3A8A) else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        selectedImageIndex = index
                                        // 🔁 Sinkronkan swipe ke halaman yang sama
                                        CoroutineScope(Dispatchers.Main).launch {
                                            pagerState.scrollToPage(index)
                                        }
                                    },
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                else -> Image(
                    painter = painterResource(id = R.drawable.il_scanner),
                    contentDescription = "Asset",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            AssetDetailRow("Name", asset.nama_barang)
            AssetDetailRow("Register No", asset.no_register)
            AssetDetailRow("User", asset.asset_holder ?: "-")
            AssetDetailRow("Location", asset.location)
        }
    }

    // ✅ Full-screen image preview sesuai gambar yang diklik
    if (isPreviewOpen && imageUrls.isNotEmpty()) {
        Dialog(
            onDismissRequest = { isPreviewOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
            ) {
                // 🔹 Lapisan blur di belakang gambar
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .blur(20.dp)
                        .background(Color.Black.copy(alpha = 0.6f))
                )

                // 🔹 Gesture untuk swipe kiri/kanan
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .pointerInput(imageUrls, selectedImageIndex) {
                            detectHorizontalDragGestures{ change, dragAmount ->
                                change.consume() // supaya tidak bentrok gesture lain
                                if (dragAmount > 50 && selectedImageIndex > 0) {
                                    // swipe ke kanan → gambar sebelumnya
                                    selectedImageIndex--
                                } else if (dragAmount < -50 && selectedImageIndex < imageUrls.lastIndex) {
                                    // swipe ke kiri → gambar berikutnya
                                    selectedImageIndex++
                                }
                            }
                        }
                ) {
                    AsyncImage(
                        model = imageUrls[selectedImageIndex],
                        contentDescription = "Full Image Preview",
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )
                }

                // 🔹 Tombol Close
                IconButton(
                    onClick = { isPreviewOpen = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(24.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }

                // 🔹 Indikator posisi
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(imageUrls.size) { index ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (index == selectedImageIndex) 10.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == selectedImageIndex) Color.White else Color.Gray
                                )
                        )
                    }
                }
            }
        }
    }


}


@Composable
private fun TabUserInfo(viewModel: AssetCategoryViewModel) {
    val logList = viewModel.allLogMovingAssetList

    if (logList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Belum ada data log aset", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 4.dp)
        ) {
            items(logList) { log ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("📅 ${log.asset_holder_date}", fontWeight = FontWeight.Bold)
                        Text("👤 From: ${log.previous_holder ?: "-"} → ${log.asset_holder ?: "-"}")
                        Text("🧾 PIC: ${log.asset_pic ?: "-"}")
                        if (!log.note.isNullOrEmpty()) {
                            Text("📝 ${log.note}", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabMaintenanceLog(viewModel: AssetCategoryViewModel) {
    val logList = viewModel.allLogMaintenanceList

    if (logList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Belum ada data maintenance", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 4.dp)
        ) {
            items(logList) { log ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("📅 ${log.history_maint_date}", fontWeight = FontWeight.Bold)
                        Text("🧰 Maintenance: ${log.maintenance_title ?: "-"}")
                        Text("💰 Cost: ${log.total_biaya ?: "-"}")
                        Text("⚙️ Status: ${log.history_status ?: "-"}")
                        if (!log.note.isNullOrEmpty()) {
                            Text("📝 ${log.note}", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AssetDetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(text = label, fontWeight = FontWeight.SemiBold)
        Text(text = value, color = Color.Gray)
        Divider(modifier = Modifier.padding(top = 4.dp))
    }
}
