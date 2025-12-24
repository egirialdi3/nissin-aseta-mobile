package id.aseta.app.ui.screen.inspection

import AuthViewModel
import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.AlertDialog
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
fun InspectionScreen(
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
                            "Inspection",
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
                    contentDescription = "Inspection Image Illustration",
                    modifier = Modifier
                        .height(240.dp)
                        .fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Asset Inspection",
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
                    onClick = { navController.navigate("scan_qr/inspection") },
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
//        InspectionAssetSelectorBottomSheet(
//            onDismiss = { showBottomSheet = false },
//            viewModel = assetCategoryModel
//        )
        AssetSelectorBottomSheet(
            onDismiss = { showBottomSheet = false },
            viewModel = assetCategoryModel,
            type = "inspection"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectionAssetSelectorBottomSheet(
    onDismiss: () -> Unit,
    viewModel: AssetCategoryViewModel
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val searchQuery = remember { mutableStateOf("") }

    // fetch data saat muncul
    LaunchedEffect(Unit) {
        viewModel.fetchCategoryAsset(context)
    }

    val categories = viewModel.categories
    val totalAsset = viewModel.totalAsset
    val isLoading = viewModel.isLoading
    val error = viewModel.errorMessage

    val assetMap by remember { derivedStateOf { viewModel.assetDetailsMap } }
    val expandedId by remember { derivedStateOf { viewModel.expandedCategory } }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        containerColor = Color.White

    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            Text(
                text = "Asset List",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Search bar
            OutlinedTextField(
                value = searchQuery.value,
                onValueChange = { searchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)) // <- biar background-nya ikut rounded
                    .background(Color(0xffF0F1F1)),   // <- kasih warna background custom
                placeholder = { Text("Search Asset") },
                trailingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                },
                shape = RoundedCornerShape(12.dp), // <- tetap declare shape di TextField
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                error != null -> {
                    Text(
                        text = error,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                else -> {
                    // Total
                    Text(
                        text = "Total Asset : $totalAsset",
                        color = Color(0xFF1E3A8A),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Filtered data
                    val filteredCategories = categories.filter {
                        it.nama_kel_barang.contains(searchQuery.value, ignoreCase = true)
                    }
                    LazyColumn {
                        items(filteredCategories) { category ->
                            InspectionExpandableAssetItem(
                                viewModel = viewModel,
                                context = context,
                                category = category,
                                isExpanded = category.kd_kel_barang == expandedId,
                                assetDetails = assetMap[category.kd_kel_barang] ?: emptyList()
                            ) { selectedId ->
                                if (expandedId == selectedId) {
                                    viewModel.expandedCategory = null
                                } else {
                                    viewModel.expandedCategory = selectedId
                                    viewModel.fetchAssetDetailsByCategory(context, selectedId)
                                }
                            }
                            Divider(
                                thickness = 0.5.dp,
                                color = Color(0xffC9CBCE),
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(70.dp))

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectionExpandableAssetItem(
    viewModel : AssetCategoryViewModel,
    context: Context,
    category: AssetCategory,
    isExpanded: Boolean,
    assetDetails: List<AssetDetail>,
    onExpandToggle: (String) -> Unit,
) {
    val backgroundColor = if (isExpanded) Color(0xF0EEF8FF) else Color.Transparent
    var showSheet by remember { mutableStateOf(false) }
    var assetDetail by remember { mutableStateOf<AssetDetail?>(null) }
    var selectedLocation by remember { mutableStateOf<LocationItem?>(null) }
    var selectedCondition by remember {mutableStateOf<String>("BAIK")}
    var newUser by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpandToggle(category.kd_kel_barang) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(horizontal = 8.dp, vertical = 12.dp)
            ,
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${category.nama_kel_barang} (${category.total_aset})",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowLeft,
                contentDescription = null
            )
        }
        Divider(
            color = Color(0xffC9CBCE),
            thickness = 0.5.dp
        )


        if (isExpanded) {

            Spacer(Modifier.height(4.dp))
            assetDetails.forEach { asset ->
                Column(
                    modifier = Modifier.
                    clickable {
                        showSheet = true
                        assetDetail = asset
//                        assetDetail = AssetItem(
//                            no_register = asset.no_register ?: "",
//                            nama_barang = asset.nama_barang ?: "",
//                            nama = asset.nama ?: "",
//                            location = asset.location ?: ""
//                        )
                    }
                ) {
                    Text(text = asset.no_register, fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 8.dp, start = 16.dp))
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${asset.nama_barang} • ${asset.location}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 6.dp, start = 16.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Divider(
                        color = Color(0xffC9CBCE),
                        thickness = 0.5.dp
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }

    if (showSheet && assetDetail!= null) {

        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = Color.White,

        ) {
            AssetInspectionSheet(
                assetDetail = assetDetail!!,
                selectedCondition = selectedCondition,
                onConditionSelected = { selectedCondition = it },
                description = description,
                onDescriptionChange = { description = it },
                onSave = { isMaintenance ->
                    viewModel.saveInspection(context, assetDetail, selectedCondition, description, isMaintenance)
                    showSheet = false
                },
                viewModel = viewModel
            )
        }
    }
}

@SuppressLint("SuspiciousIndentation")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AssetInspectionSheet(
    viewModel: AssetCategoryViewModel,
    assetDetail: AssetDetail,
    selectedCondition: String,
    onConditionSelected: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    onSave: (Boolean) -> Unit
) {
    val scrollState = rememberScrollState()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var isDescriptionFocused by remember { mutableStateOf(false) }
    val bringIntoViewRequester = remember { BringIntoViewRequester()}
    val coroutineScope = rememberCoroutineScope()

    var selectedCondition by remember { mutableStateOf<String?>(null) }
    var selectedConditionId by remember { mutableStateOf<Int?>(null) }
    var showMaintenanceDialog by remember { mutableStateOf(false) }

        LaunchedEffect(isDescriptionFocused) {
            if (isDescriptionFocused) {
                coroutineScope.launch {
                    bringIntoViewRequester.bringIntoView()
                    scrollState.animateScrollTo(scrollState.maxValue)
                }
            }
        }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(scrollState)
            .imePadding()
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
            AssetDetailRow(label = "User", value = assetDetail.nama)
            AssetDetailRow(label = "Location", value = assetDetail.location)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Condition Dropdown
        Text("Condition *", fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        AssetConditionDropdown(
            selectedCondition = selectedCondition,
            onConditionSelected = { kondisi, id_kondisi ->
                selectedCondition = kondisi
                selectedConditionId = id_kondisi
            },
            viewModel = viewModel
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text("Description", fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
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

        Spacer(modifier = Modifier.height(24.dp))

        // Save Button
        Button(
            onClick = { showMaintenanceDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A))
        ) {
            Text("Save", color = Color.White)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Maintenance Confirmation Dialog
    if (showMaintenanceDialog) {
        MaintenanceConfirmationDialog(
            onDismiss = { showMaintenanceDialog = false },
            onConfirm = { isMaintenance ->
                onSave(isMaintenance)
                showMaintenanceDialog = false
            }
        )
    }
}

@Composable
fun MaintenanceConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: (Boolean) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Maintenance Confirmation",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A8A)
            )
        },
        text = {
            Text(
                text = "Apakah Anda ingin memasukkan aset tersebut ke maintenance?",
                color = Color.Gray
            )
        },
        confirmButton = {
            Row {
                TextButton(
                    onClick = { onConfirm(false) }
                ) {
                    Text(
                        "Tidak",
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { onConfirm(true) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E3A8A)
                    )
                ) {
                    Text("Ya", color = Color.White)
                }
            }
        },
        containerColor = Color.White
    )
}


@Composable
fun InspectionInfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, fontSize = 14.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetConditionDropdown(
    viewModel: AssetCategoryViewModel,
    selectedCondition: String?,
    onConditionSelected: (String,Int) -> Unit
) {
//    val options = listOf("BAIK","SEDANG", "BURUK")
    val conditions by viewModel.conditionItems.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.fetchConditions(context)
        viewModel.getJenisDisposal(context)
    }


    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.background(Color.White)
    ) {
        OutlinedTextField(
            value = selectedCondition ?: "",
            onValueChange = {},
            readOnly = true,
            placeholder = { Text("Pilih Kondisi") },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.White,
                focusedIndicatorColor = Color.Gray,
                unfocusedIndicatorColor = Color.LightGray
            ),
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            if (conditions.isNotEmpty()) {
                conditions.forEach { conditionItem ->
                    DropdownMenuItem(
                        text = { Text(conditionItem.kondisi) },
                        onClick = {
                            onConditionSelected(conditionItem.kondisi, conditionItem.id_kondisi)
                            expanded = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                DropdownMenuItem(
                    text = { Text("Loading...") },
                    onClick = { },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false
                )
            }
        }
    }
}



@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FullScreenInspectionBottomSheet(
    context: Context,
    assetDetail: AssetDetail,
    viewModel: AssetCategoryViewModel,
    onDismiss: () -> Unit
) {

    var description by remember { mutableStateOf("") }
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val scrollState = rememberScrollState()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true  // ini opsional tapi membantu langsung ke full
    )

    val focusRequester = remember { FocusRequester() }
    var isDescriptionFocused by remember { mutableStateOf(false) }
    val bringIntoViewRequester = remember { BringIntoViewRequester()}

    var selectedCondition by remember { mutableStateOf<String?>(null) }
    var selectedConditionId by remember { mutableStateOf<Int?>(null) }
    var showMaintenanceDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        sheetState.show()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        windowInsets = WindowInsets(0),
        modifier = Modifier
            .fillMaxSize()
//            .navigationBarsPadding()// Ini yang membuat full screen
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)  // Enable scrolling
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
                    AssetDetailRow(label = "User", value = assetDetail.asset_holder?:"-")
                    AssetDetailRow(label = "Location", value = assetDetail.location)
                }

                Spacer(modifier = Modifier.height(16.dp))
                RequiredLabel(
                    text = "Condition",
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                AssetConditionDropdown(
                    selectedCondition = selectedCondition,
                    onConditionSelected = { kondisi, id_kondisi ->
                        selectedCondition = kondisi
                        selectedConditionId = id_kondisi
                    },
                    viewModel = viewModel
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Text("Description", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                    },
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

                Spacer(modifier = Modifier.height(24.dp))

                // Save Button
                Button(
                    onClick = {
                        if(selectedCondition == null ){
                            Toast.makeText(context,"Please select the Condition",Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if(description == "" ){
                            Toast.makeText(context,"Please fill the Description",Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        showMaintenanceDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A))
                ) {
                    Text("Save", color = Color.White)
                }
            }
        }

    }

    // Maintenance Confirmation Dialog
    if (showMaintenanceDialog) {
        MaintenanceConfirmationDialog(
            onDismiss = { showMaintenanceDialog = false },
            onConfirm = { isMaintenance ->
                viewModel.saveInspection(context, assetDetail, selectedConditionId.toString(), description, isMaintenance)
                showMaintenanceDialog = false
                onDismiss()
            }
        )
    }
}

