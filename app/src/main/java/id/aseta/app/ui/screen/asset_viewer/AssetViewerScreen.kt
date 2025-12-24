package id.aseta.app.ui.screen.asset_viewer

import AuthViewModel
import GetMenuItem
import android.annotation.SuppressLint
//import android.app.Dialog
import android.content.Context
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import id.aseta.app.R
import id.aseta.app.data.model.AssetCategory
import id.aseta.app.data.model.AssetDetail
import id.aseta.app.data.source.local.TokenDataStore
import id.aseta.app.ui.screen.corrective_maintenance.FullScreenCorrectiveBottomSheet
import id.aseta.app.ui.screen.qr_scan.AssetBottomSheetContent
import id.aseta.app.ui.screen.inspection.FullScreenInspectionBottomSheet
import id.aseta.app.ui.screen.mutasi.FullScreenMutationBottomSheet
import id.aseta.app.ui.screen.relocation.FullScreenRelocationBottomSheet
import id.aseta.app.viewmodel.AssetCategoryViewModel
import id.aseta.app.viewmodel.PaginationState
import id.aseta.app.viewmodel.UHFViewModel
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetViewerScreen(
    viewModel: AuthViewModel,
    uhfViewModel: UHFViewModel,
    assetCategoryModel: AssetCategoryViewModel,
    navController: NavController,
    onScanQrClick: () -> Unit = {},
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    var dataUser by remember {mutableStateOf<GetMenuItem?>(null)}
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        dataUser = TokenDataStore.getDataUser(context)
    }

    Box(modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd) {

        // Background Image
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
                            "Asset Viewer",
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
                    painter = painterResource( R.drawable.il_scanner),
                    contentDescription = if(dataUser?.barcode == true) "Scan QR Image" else "Scan RFID",
                    modifier = Modifier
                        .height(240.dp)
                        .fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "View Asset Details",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E3A8A)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text =  if(dataUser?.barcode == true) "Scan the QR code on the asset or select it from the asset list to view the details." else "Scan the RFID by tapping SCAN RFID to view the details.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if(dataUser?.barcode == true){
                            navController.navigate("scan_qr/asset_viewer")
                        }else{
                            navController.navigate("scan_rfid_asset_viewer")

                        }
                              },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E3A8A),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        painter = painterResource(id = if (dataUser?.barcode == true)R.drawable.ic_scan_barcode else  R.drawable.ic_power_rfid_blue), // icon QR kamu
                        contentDescription = "QR Icon",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (dataUser?.barcode == true) "Scan QR" else "Scan RFID")
                }

                Spacer(modifier = Modifier.height(8.dp))
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
            type = "asset_viewer"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetSelectorBottomSheet(
    onDismiss: () -> Unit,
    viewModel: AssetCategoryViewModel,
    type: String
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    val searchQuery = remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults
    var totalAsset = viewModel.totalAsset

    fun refreshQuery(onDismiss: () -> Unit){
        if (searchQuery.value.length >= 3) {
            viewModel.searchAssetByName(context, searchQuery.value)
        } else {
            totalAsset = viewModel.historyTotalAsset
            viewModel.clearSearchResults()
            viewModel.fetchCategoryAsset(context)
        }
        onDismiss()
    }

    LaunchedEffect(Unit) {
        snapshotFlow { searchQuery.value }
            .debounce(800)
            .collect { query ->
                if (query.length >= 3) {
                    viewModel.searchAssetByName(context, query)
                } else {
                    totalAsset = viewModel.historyTotalAsset
                    viewModel.clearSearchResults()
                    viewModel.fetchCategoryAsset(context)
                }
            }
    }

    val categories = viewModel.categories
    val isLoading = viewModel.isLoading
    val error = viewModel.errorMessage

    val expandedId by remember { derivedStateOf { viewModel.expandedCategory } }

    var showSheet by remember { mutableStateOf(false) }
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        containerColor = Color.White,
        modifier = Modifier.navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
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
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xffF0F1F1)),
                placeholder = { Text("Search Asset") },
                trailingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .height(screenHeight * 0.85f)
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
                searchQuery.value.length >= 3 && searchResults.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .height(screenHeight * 0.85f)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Data tidak ditemukan",
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
                else -> {
                    // Total
                    Text(
                        text = "Total Asset : $totalAsset",
                        color = Color(0xFF1E3A8A),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (searchQuery.value.isNotBlank()) {
                        // MODE: Cari asset berdasarkan nama
                        Box(
                            modifier = Modifier
                                .height(screenHeight * 0.85f)
                                .fillMaxWidth()
                        ) {
                            LazyColumn {
                                items(searchResults) { asset ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                showSheet = true
                                                viewModel.assetDetail = asset
                                            }
                                            .padding(vertical = 8.dp, horizontal = 16.dp)
                                    ) {
                                        Text(
                                            text = asset.no_register,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${asset.nama_barang} • ${asset.location}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.Gray
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Divider(
                                            color = Color(0xffC9CBCE),
                                            thickness = 0.5.dp
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        val filteredCategories = categories.filter {
                            it.nama_kel_barang.contains(searchQuery.value, ignoreCase = true)
                        }
                        LazyColumn {
                            items(filteredCategories) { category ->
                                val isLoading = viewModel.loadingCategoryMap[category.kd_kel_barang] ?: false
                                ExpandableAssetItem(
                                    context = context,
                                    category = category,
                                    isExpanded = category.kd_kel_barang == expandedId,
                                    assetDetails = viewModel.assetDetailsMap[category.kd_kel_barang] ?: emptyList(),
                                    isLoading = isLoading,
                                    type = type,
                                    viewModel = viewModel,
                                    onDismiss = {
                                        onDismiss()
                                    }
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
            }
            Spacer(modifier = Modifier.height(70.dp))
        }
    }

    if (showSheet && viewModel.assetDetail!= null) {
        when(type) {
            "asset_viewer" -> {
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
                            viewModel = viewModel,
                            asset = viewModel.assetDetail!!
                        )
                    }
                }
            }
            "mutation" -> {
                FullScreenMutationBottomSheet(
                    context = context,
                    assetDetail = viewModel.assetDetail!!,
                    viewModel = viewModel,
                    onDismiss = {
                        if (searchQuery.value.isNotBlank()) {
                            refreshQuery() {
                                showSheet = false
                            }
                        } else {
                            viewModel.fetchAssetDetailsByCategory(
                                context,
                                viewModel.assetDetail!!.kd_kel_barang,
                                true
                            ) {
                                onDismiss()
                            }
                            showSheet = false
                        }
                    }
                )
            }
            "relocation" -> {
                FullScreenRelocationBottomSheet(
                    context = context,
                    assetDetail = viewModel.assetDetail!!,
                    viewModel = viewModel,
                    onDismiss = {
                        if (searchQuery.value.isNotBlank()) {
                            refreshQuery() {
                                showSheet = false
                            }
                        } else {
                            viewModel.fetchAssetDetailsByCategory(
                                context,
                                viewModel.assetDetail!!.kd_kel_barang,
                                true
                            ) {
                                onDismiss()
                            }
                            showSheet = false
                        }
                    }
                )
            }
            "inspection" -> {
                FullScreenInspectionBottomSheet(
                    context = context,
                    assetDetail = viewModel.assetDetail!!,
                    viewModel = viewModel,
                    onDismiss = { showSheet = false }
                )
            }
            "corrective_maintenance"->{
                FullScreenCorrectiveBottomSheet(
                    context = context,
                    assetDetail = viewModel.assetDetail!!,
                    viewModel = viewModel,
                    onDismiss = { showSheet = false }
                )
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope", "StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandableAssetItem(
    context: Context,
    category: AssetCategory,
    isExpanded: Boolean,
    assetDetails: List<AssetDetail>,
    isLoading: Boolean,
    viewModel: AssetCategoryViewModel,
    type: String,
    onDismiss: () -> Unit,
    onExpandToggle: (String) -> Unit,
) {
    val backgroundColor = if (isExpanded) Color(0xF0EEF8FF) else Color.Transparent
    var showSheet by remember { mutableStateOf(false) }
    var selectedNoRegister by remember { mutableStateOf<String?>(null) }

    // Get pagination state untuk kategori ini
    val paginationState = viewModel.assetDetailsPaginationMap[category.kd_kel_barang]?.collectAsState()

    val listState = rememberLazyListState()

    // **FIXED: Improved infinite scroll logic**
    LaunchedEffect(listState, isExpanded, assetDetails.size) {
        if (isExpanded && assetDetails.isNotEmpty()) {
            snapshotFlow {
                val layoutInfo = listState.layoutInfo
                val totalItemsCount = layoutInfo.totalItemsCount
                val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1

                // Trigger load more when we're within 2 items from the end
                lastVisibleItem >= totalItemsCount - 2
            }
                .distinctUntilChanged()
                .collect { shouldLoadMore ->
                    val currentPaginationState = paginationState?.value

                    if (shouldLoadMore &&
                        currentPaginationState != null &&
                        currentPaginationState.hasMoreItems &&
                        !currentPaginationState.isLoadingMore) {

                        Log.d("InfiniteScroll", "Loading more for category: ${category.kd_kel_barang}")
                        viewModel.fetchAssetDetailsByCategory(context, category.kd_kel_barang, true)
                    }
                }
        }
    }

    val currentAssetDetail by remember(selectedNoRegister, viewModel.assetDetailsMap) {
        derivedStateOf {
            selectedNoRegister?.let { noReg ->
                viewModel.assetDetailsMap[category.kd_kel_barang]?.find { it.no_register == noReg }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpandToggle(category.kd_kel_barang) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(horizontal = 8.dp, vertical = 12.dp),
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
            if (isLoading && assetDetails.isEmpty()) {
                // Initial loading state
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 16.dp)
                )
            } else {
                if (assetDetails.isEmpty()) {
                    Text(
                        text = "Data tidak ditemukan",
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.CenterHorizontally),
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    // **FIXED: Improved pagination info display**
                    val totalItems = paginationState?.value?.totalItems ?: 0
                    val currentCount = assetDetails.size

                    Text(
                        text = "Showing $currentCount of $totalItems items",
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .align(Alignment.CenterHorizontally),
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.heightIn(max = 350.dp)
                    ) {
                        items(
                            items = assetDetails,
                            key = { asset -> asset.no_register } // Add key for better performance
                        ) { asset ->
                            Column(
                                modifier = Modifier.clickable {
                                    selectedNoRegister = asset.no_register
                                    showSheet = true
                                    viewModel.assetDetail = asset
                                }
                            ) {
                                Text(
                                    text = asset.no_register,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(top = 8.dp, start = 16.dp)
                                )
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

                        // **FIXED: Bottom loader when loading more data**
                        if (paginationState?.value?.isLoadingMore == true) {
                            item(key = "loading_more") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Loading more...",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }

                        // **ADDED: End of list indicator**
                        if (paginationState?.value?.hasMoreItems == false && assetDetails.isNotEmpty()) {
                            item(key = "end_of_list") {
                                Text(
                                    text = "— End of list —",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }

    // Bottom sheet handling (unchanged)
    if (showSheet && currentAssetDetail != null) {
        when (type) {
            "asset_viewer" -> {
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
                            viewModel = viewModel,
                            asset = currentAssetDetail!!
                        )
                    }
                }
            }
            "relocation" -> {
                FullScreenRelocationBottomSheet(
                    context = context,
                    assetDetail = currentAssetDetail!!,
                    viewModel = viewModel,
                    onDismiss = { showSheet = false }
                )
            }
            "mutation" -> {
                FullScreenMutationBottomSheet(
                    context = context,
                    assetDetail = currentAssetDetail!!,
                    viewModel = viewModel,
                    onDismiss = { showSheet = false }
                )
            }
            "inspection" -> {
                FullScreenInspectionBottomSheet(
                    context = context,
                    assetDetail = currentAssetDetail!!,
                    viewModel = viewModel,
                    onDismiss = { showSheet = false }
                )
            }
            "corrective_maintenance" -> {
                FullScreenCorrectiveBottomSheet(
                    context = context,
                    assetDetail = currentAssetDetail!!,
                    viewModel = viewModel,
                    onDismiss = { showSheet = false }
                )
            }

        }
    }
}