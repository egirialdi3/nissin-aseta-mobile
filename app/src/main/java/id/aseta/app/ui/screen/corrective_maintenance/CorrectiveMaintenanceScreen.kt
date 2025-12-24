package id.aseta.app.ui.screen.corrective_maintenance

import AuthViewModel
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
//import android.view.WindowInsets
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import id.aseta.app.R
import id.aseta.app.data.model.AssetCategory
import id.aseta.app.data.model.AssetDetail
import id.aseta.app.data.model.LocationItem
import id.aseta.app.ui.screen.qr_scan.AssetDetailRow
import id.aseta.app.ui.screen.asset_viewer.AssetSelectorBottomSheet
import id.aseta.app.ui.theme.RequiredLabel
import id.aseta.app.viewmodel.AssetCategoryViewModel
import id.aseta.app.viewmodel.UHFViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CorrectiveMaintenanceScreen(
    viewModel: AuthViewModel,
    uhfViewModel: UHFViewModel,
    assetCategoryModel: AssetCategoryViewModel,
    navController: NavController,
    onScanQrClick: () -> Unit = {},
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd) {

        Image(
            painter = painterResource(id = R.drawable.bg_main), // ganti sesuai nama drawable kamu
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
                            "Corrective Maintenance",
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            navController.popBackStack()
                        }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF1E3A8A) // Warna biru tua
                    )
                )
            },
            containerColor = Color.Transparent // biar background image tetap kelihatan
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Image(
                    painter = painterResource(id = R.drawable.il_inspection),
                    contentDescription = "Corrective Maintenance Image Illustration",
                    modifier = Modifier
                        .height(240.dp)
                        .fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Corrective Maintenance",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E3A8A)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Scan the QR code on the asset or select it from the asset list to view the details.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { navController.navigate("scan_qr/corrective_maintenance") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E3A8A),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_scan_barcode), // icon QR kamu
                        contentDescription = "QR Icon",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scan QR")
                }

                Spacer(modifier = Modifier.height(24.dp))

                TextButton(onClick = { showBottomSheet = true }) {
                    Text(
                        text = "Select from asset list",
                        color = Color(0xFF1E3A8A),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    if (showBottomSheet) {
        AssetSelectorBottomSheet(
            onDismiss = { showBottomSheet = false },
            viewModel = assetCategoryModel,
            type = "corrective_maintenance"
        )
    }
}
//
//@SuppressLint("UnusedBoxWithConstraintsScope")
//@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
//@Composable
//fun FullScreenCorrectiveBottomSheet(
//    context: Context,
//    assetDetail: AssetDetail,
//    viewModel: AssetCategoryViewModel,
//    onDismiss: () -> Unit
//) {
//
//    var description by remember { mutableStateOf("") }
//    val configuration = LocalConfiguration.current
//    val screenHeight = configuration.screenHeightDp.dp
//    val scrollState = rememberScrollState()
//    val sheetState = rememberModalBottomSheetState(
//        skipPartiallyExpanded = true  // ini opsional tapi membantu langsung ke full
//    )
//
//    val focusRequester = remember { FocusRequester() }
//    var isDescriptionFocused by remember { mutableStateOf(false) }
//    val bringIntoViewRequester = remember { BringIntoViewRequester()}
//
//    var selectedCondition by remember { mutableStateOf<String?>(null) }
//    var selectedConditionId by remember { mutableStateOf<Int?>(null) }
//
//    LaunchedEffect(Unit) {
//        sheetState.show()
//    }
//
//    ModalBottomSheet(
//        onDismissRequest = onDismiss,
//        sheetState = sheetState,
//        containerColor = Color.White,
//        windowInsets = WindowInsets(0),
//        modifier = Modifier
//            .fillMaxSize()
////            .navigationBarsPadding()// Ini yang membuat full screen
//    ) {
//        BoxWithConstraints(
//            modifier = Modifier
//                .fillMaxSize()
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .verticalScroll(scrollState)  // Enable scrolling
//                    .padding(16.dp)
//            ) {
//
//                // Header Image & Info
//                Column {
//                    Image(
//                        painter = painterResource(id = R.drawable.il_scanner),
//                        contentDescription = "Asset",
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(180.dp)
//                            .clip(RoundedCornerShape(12.dp))
//                    )
//                    Spacer(modifier = Modifier.height(12.dp))
//                    AssetDetailRow(label = "Name", value = assetDetail.nama_barang)
//                    AssetDetailRow(label = "Register No", value = assetDetail.no_register)
//                    AssetDetailRow(label = "User", value = assetDetail.asset_holder?:"-")
//                    AssetDetailRow(label = "Location", value = assetDetail.location)
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//                RequiredLabel(
//                    text = "Condition",
//                    fontWeight = FontWeight.SemiBold
//                )
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Description
//                Text("Description", fontWeight = FontWeight.Medium)
//                Spacer(modifier = Modifier.height(4.dp))
//                OutlinedTextField(
//                    value = description,
//                    onValueChange = {
//                        description = it
//                    },
//                    placeholder = { Text("Write Description") },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(120.dp)
//                        .focusRequester(focusRequester)
//                        .onFocusChanged {
//                            isDescriptionFocused = it.isFocused
//                        }
//                        .bringIntoViewRequester(bringIntoViewRequester),
//                    maxLines = 4
//                )
//
//                Spacer(modifier = Modifier.height(24.dp))
//
//                // Save Button
//                Button(
//                    onClick = {
//                        if(selectedCondition == null ){
//                            Toast.makeText(context,"Please select the Condition",Toast.LENGTH_SHORT).show()
//                            return@Button
//                        }
//                        if(description == "" ){
//                            Toast.makeText(context,"Please fill the Description",Toast.LENGTH_SHORT).show()
//                            return@Button
//                        }
//                        println(assetDetail.no_register)
//                        println(selectedCondition)
//                        println(description)
//                        viewModel.saveInspection(context, assetDetail, selectedConditionId.toString(), description)
//                        onDismiss()
//                    },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(56.dp),
//                    shape = RoundedCornerShape(12.dp),
//                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A))
//                ) {
//                    Text("Save", color = Color.White)
//                }
//            }
//        }
//
//    }
//}

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FullScreenCorrectiveBottomSheet(
    context: Context,
    assetDetail: AssetDetail,
    viewModel: AssetCategoryViewModel,
    onDismiss: () -> Unit
) {
    var description by remember { mutableStateOf("") }
    var issueCondition by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf<String?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showImagePicker by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val scrollState = rememberScrollState()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val focusRequester = remember { FocusRequester() }
    var isDescriptionFocused by remember { mutableStateOf(false) }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    val priorityOptions = listOf("urgent", "medium", "low")
    var expandedPriority by remember { mutableStateOf(false) }

    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    val isLoading by viewModel.isLoadingSaveCorrective

//    // Camera and Gallery Launchers
//    val cameraLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.TakePicture()
//    ) { success ->
//        if (success) {
//            // Image captured successfully, selectedImageUri already contains the URI
//        }
//    }
//
//    val galleryLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.GetContent()
//    ) { uri ->
//        uri?.let {
//            selectedImageUri = it
//        }
//    }
//
//    // Create URI for camera capture
//    val createImageUri: () -> Uri = {
//        val contentResolver = context.contentResolver
//        val contentValues = ContentValues().apply {
//            put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
//            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
//        }
//        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!
//    }

    // Camera and Gallery Launchers
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // Image captured successfully, use the cameraImageUri
            cameraImageUri?.let { uri ->
                selectedImageUri = uri
                Log.d("CameraCapture", "Image captured successfully: $uri")
            }
        } else {
            Log.d("CameraCapture", "Camera capture failed or cancelled")
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            Log.d("GalleryPicker", "Image selected from gallery: $it")
        } ?: run {
            Log.d("GalleryPicker", "No image selected from gallery")
        }
    }

    // Create URI for camera capture
    val createImageUri: () -> Uri = {
        val contentResolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MaintenanceApp")
        }
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!
        Log.d("CameraURI", "Created camera URI: $uri")
        uri
    }

    LaunchedEffect(Unit) {
        sheetState.show()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        windowInsets = WindowInsets(0),
        modifier = Modifier.fillMaxSize()
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                // Header Image & Info
                Column {
                    Image(
                        painter = painterResource(id = R.drawable.il_scanner),
                        contentDescription = "Asset",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    AssetDetailRow(label = "Name", value = assetDetail.nama_barang)
                    AssetDetailRow(label = "Register No", value = assetDetail.no_register)
                    AssetDetailRow(label = "User", value = assetDetail.asset_holder ?: "-")
                    AssetDetailRow(label = "Location", value = assetDetail.location)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Priority Selection
                RequiredLabel(
                    text = "Priority",
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = expandedPriority,
                    onExpandedChange = { expandedPriority = !expandedPriority }
                ) {
                    OutlinedTextField(
                        value = selectedPriority ?: "",
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Select Priority") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPriority)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPriority,
                        onDismissRequest = { expandedPriority = false }
                    ) {
                        priorityOptions.forEach { priority ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = priority.replaceFirstChar {
                                            if (it.isLowerCase()) it.titlecase() else it.toString()
                                        }
                                    )
                                },
                                onClick = {
                                    selectedPriority = priority
                                    expandedPriority = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Issue/Condition
                RequiredLabel(
                    text = "Issue or Condition",
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = issueCondition,
                    onValueChange = { issueCondition = it },
                    placeholder = { Text("Describe the issue or condition") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                RequiredLabel(
                    text = "Description",
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Write Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .focusRequester(focusRequester)
                        .onFocusChanged {
                            isDescriptionFocused = it.isFocused
                        }
                        .bringIntoViewRequester(bringIntoViewRequester),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Attachment Section
                RequiredLabel(
                    text = "Attachment",
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Image Preview (if selected)
//                selectedImageUri?.let { uri ->
//                    Card(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(200.dp),
//                        shape = RoundedCornerShape(12.dp)
//                    ) {
//                        Box(modifier = Modifier.fillMaxSize()) {
//                            AsyncImage(
//                                model = uri,
//                                contentDescription = "Selected Image",
//                                modifier = Modifier.fillMaxSize(),
//                                contentScale = ContentScale.Crop
//                            )
//                            IconButton(
//                                onClick = { selectedImageUri = null },
//                                modifier = Modifier.align(Alignment.TopEnd)
//                            ) {
//                                Icon(
//                                    Icons.Default.Close,
//                                    contentDescription = "Remove Image",
//                                    tint = Color.White,
//                                    modifier = Modifier
//                                        .background(
//                                            Color.Black.copy(alpha = 0.5f),
//                                            CircleShape
//                                        )
//                                        .padding(4.dp)
//                                )
//                            }
//                        }
//                    }
//                    Spacer(modifier = Modifier.height(8.dp))
//                }
//
//                // Camera and Gallery Buttons
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    OutlinedButton(
//                        onClick = {
//                            val uri = createImageUri()
//                            selectedImageUri = uri
//                            cameraLauncher.launch(uri)
//                        },
//                        modifier = Modifier.weight(1f),
//                        shape = RoundedCornerShape(12.dp)
//                    ) {
//                        Icon(
//                            Icons.Default.CameraAlt,
//                            contentDescription = "Camera",
//                            modifier = Modifier.size(20.dp)
//                        )
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text("Camera")
//                    }
//
//                    OutlinedButton(
//                        onClick = {
//                            galleryLauncher.launch("image/*")
//                        },
//                        modifier = Modifier.weight(1f),
//                        shape = RoundedCornerShape(12.dp)
//                    ) {
//                        Icon(
//                            Icons.Default.PhotoLibrary,
//                            contentDescription = "Gallery",
//                            modifier = Modifier.size(20.dp)
//                        )
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text("Gallery")
//                    }
//                }

                // Image Preview (if selected)
                selectedImageUri?.let { uri ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = uri,
                                contentDescription = "Selected Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { selectedImageUri = null },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove Image",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .background(
                                            Color.Black.copy(alpha = 0.5f),
                                            CircleShape
                                        )
                                        .padding(4.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Camera and Gallery Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            try {
                                val uri = createImageUri()
                                cameraImageUri = uri
                                Log.d("CameraButton", "Launching camera with URI: $uri")
                                cameraLauncher.launch(uri)
                            } catch (e: Exception) {
                                Log.e("CameraButton", "Error launching camera: ${e.message}")
                                Toast.makeText(context, "Error opening camera: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Camera",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Camera")
                    }

                    OutlinedButton(
                        onClick = {
                            try {
                                Log.d("GalleryButton", "Launching gallery picker")
                                galleryLauncher.launch("image/*")
                            } catch (e: Exception) {
                                Log.e("GalleryButton", "Error launching gallery: ${e.message}")
                                Toast.makeText(context, "Error opening gallery: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.PhotoLibrary,
                            contentDescription = "Gallery",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gallery")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Save Button
                Button(
                    onClick = {
                        // Validation
                        when {
                            selectedPriority == null -> {
                                Toast.makeText(context, "Please select Priority", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            issueCondition.isBlank() -> {
                                Toast.makeText(context, "Please fill the Issue or Condition", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            description.isBlank() -> {
                                Toast.makeText(context, "Please fill the Description", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            selectedImageUri == null -> {
                                Toast.makeText(context, "Please attach an image", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            else -> {
                                if (!isLoading) { // Only proceed if not loading
                                    viewModel.saveCorrectiveMaintenance(
                                        context = context,
                                        assetDetail = assetDetail,
                                        priority = selectedPriority!!,
                                        condition = issueCondition,
                                        description = description,
                                        imageUri = selectedImageUri!!,
                                        onComplete = {
                                            onDismiss()
                                        }
                                    )
                                }
                                // Call API with multipart data
//                                viewModel.saveCorrectiveMaintenance(
//                                    context = context,
//                                    assetDetail = assetDetail,
//                                    priority = selectedPriority!!,
//                                    condition = issueCondition,
//                                    description = description,
//                                    imageUri = selectedImageUri!!,
//                                    onComplete = {
//                                        onDismiss()
//                                    }
//                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White)
                    } else {
                        Text("Save", color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

            }
        }
    }
}

