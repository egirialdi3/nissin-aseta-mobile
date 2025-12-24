package id.aseta.app.ui.screen.find_asset

import AuthViewModel
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import id.aseta.app.data.model.LocationItem
import id.aseta.app.viewmodel.AssetCategoryViewModel
import id.aseta.app.viewmodel.UHFViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import id.aseta.app.R
import id.aseta.app.data.model.AssetCategory
import id.aseta.app.data.model.AssetDetail
import id.aseta.app.ui.theme.RequiredLabel
import kotlinx.coroutines.flow.debounce
import kotlin.collections.forEach
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.coerceAtMost
import kotlinx.coroutines.flow.distinctUntilChanged


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindAssetScreen(
    viewModel: AuthViewModel,
    uhfViewModel: UHFViewModel,
    assetCategoryModel: AssetCategoryViewModel,
    navController: NavController,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    var selectedLocation by remember { mutableStateOf<LocationItem?>(null) }
    val assetMap by remember { derivedStateOf { assetCategoryModel.assetDetailsMap } }
    val searchQuery = remember { mutableStateOf("") }

    // State untuk kontrol posisi bottom sheet
    var bottomSheetOffset by remember { mutableStateOf(220.dp) }
    val maxOffset = 220.dp // Posisi awal (tidak full screen)
    val minOffset = with(density) { (LocalContext.current.resources.displayMetrics.heightPixels * 0.1f).toDp() } // 10% dari layar

    val categories = assetCategoryModel.categories
    val expandedId by remember { derivedStateOf { assetCategoryModel.expandedCategory } }

    LaunchedEffect(Unit) {
        assetCategoryModel.fetchGroups(context)
        assetCategoryModel.fetchCategoryAsset(context)
    }

    // Handle search dengan debounce
    LaunchedEffect(Unit) {
        snapshotFlow { searchQuery.value }
            .debounce(800)
            .collect { query ->
                if (query.length >= 3) {
                    assetCategoryModel.searchAssetByName(context, query)
                } else {
                    assetCategoryModel.totalAsset = assetCategoryModel.historyTotalAsset
                    assetCategoryModel.clearSearchResults()
                    assetCategoryModel.fetchCategoryAsset(context)
                }
            }
    }

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
                            "Find Asset",
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

                    Image(
                        painter = painterResource(id = R.drawable.il_find_asset),
                        contentDescription = "Find Asset",
                        modifier = Modifier
                            .height(115.dp)
                            .fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Find Asset",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Select asset from the asset list to find it",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Custom Bottom Sheet dengan kontrol drag terbatas
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = bottomSheetOffset)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .background(Color.White),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header yang bisa di-drag (Search Asset sampai Total Asset)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDrag = { change, dragAmount ->
                                        val newOffset = bottomSheetOffset + with(density) { dragAmount.y.toDp() }
                                        // Batasi pergerakan hanya antara minOffset dan maxOffset
                                        bottomSheetOffset = newOffset.coerceAtLeast(minOffset).coerceAtMost(maxOffset)
                                    }
                                )
                            }
                            .padding(horizontal = 16.dp, vertical = 22.dp)
                    ) {
                        // Drag handle
                        Box(
                            modifier = Modifier
                                .width(50.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFFC9CBCE))
                                .align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        RequiredLabel(
                            text = "Search Asset",
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
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

                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Total Asset : ${assetCategoryModel.totalAsset}",
                            color = Color(0xFF1E3A8A),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // LazyColumn untuk konten yang bisa di-scroll (tanpa drag gesture)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        contentPadding = PaddingValues(
                            start = 0.dp,
                            top = 0.dp,
                            end = 0.dp,
                            bottom = 80.dp // Padding untuk menghindari navbar sistem Android
                        )
                    ) {

                        when {
                            assetCategoryModel.isLoading -> {
                                item {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                            .wrapContentWidth(Alignment.CenterHorizontally)
                                    )
                                }
                            }

                            searchQuery.value.length >= 3 && assetCategoryModel.searchResults.value.isEmpty() -> {
                                item {
                                    Text(
                                        text = "Data tidak ditemukan",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        textAlign = TextAlign.Center,
                                        color = Color.Gray
                                    )
                                }
                            }

                            searchQuery.value.isNotBlank() -> {
                                items(assetCategoryModel.searchResults.value) { asset ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                if (asset.rfid != null) {
                                                    navController.navigate("finding_asset_screen/${asset.rfid}")
                                                    assetCategoryModel.selectedAssetFindAsset = asset
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "Data Asset tidak memiliki RFID",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
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

                            else -> {
                                val filteredCategories = categories.filter {
                                    it.nama_kel_barang.contains(
                                        searchQuery.value,
                                        ignoreCase = true
                                    )
                                }
                                items(filteredCategories) { category ->
                                    val isLoading = assetCategoryModel.loadingCategoryMap[category.kd_kel_barang] ?: false

                                    FindExpandableAssetItem(
                                        navController = navController,
                                        context = context,
                                        category = category,
                                        isExpanded = category.kd_kel_barang == expandedId,
                                        assetDetails = assetMap[category.kd_kel_barang] ?: emptyList(),
                                        isLoading = isLoading,
                                        assetCategoryViewModel = assetCategoryModel
                                    ) { selectedId ->
                                        if (expandedId == selectedId) {
                                            assetCategoryModel.expandedCategory = null
                                        } else {
                                            assetCategoryModel.expandedCategory = selectedId
                                            assetCategoryModel.fetchAssetDetailsByCategory(
                                                context,
                                                selectedId
                                            )
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
            }
        }
    }



}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindExpandableAssetItem(
    navController: NavController,
    assetCategoryViewModel: AssetCategoryViewModel,
    context: Context,
    category: AssetCategory,
    isExpanded: Boolean,
    assetDetails: List<AssetDetail>,
    isLoading:Boolean,
    onExpandToggle: (String) -> Unit
) {
    val backgroundColor = if (isExpanded) Color(0xF0EEF8FF) else Color.Transparent
    var showSheet by remember { mutableStateOf(false) }
    var assetDetail by remember { mutableStateOf<AssetDetail?>(null) }


    // Get pagination state untuk kategori ini
    val paginationState = assetCategoryViewModel.assetDetailsPaginationMap[category.kd_kel_barang]?.collectAsState()

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
                        assetCategoryViewModel.fetchAssetDetailsByCategory(context, category.kd_kel_barang, true)
                    }
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpandToggle(category.kd_kel_barang) }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
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
            }else{
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
                        modifier = Modifier.heightIn(max = 350.dp),
                        contentPadding = PaddingValues(
                            start = 0.dp,
                            top = 0.dp,
                            end = 0.dp,
                            bottom = 120.dp // Padding yang lebih besar untuk menghindari navbar sistem Android pada item kategori terakhir
                        )
                    ) {
                        items(
                            items = assetDetails,
                            key = { asset -> asset.no_register } // Add key for better performance
                        ) { asset ->
                            Column(
                                modifier = Modifier.clickable {
                                    showSheet = true
                                    assetDetail = asset
                                    assetCategoryViewModel.selectedAssetFindAsset = assetDetail
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

    if (showSheet && assetDetail != null) {
        if(assetDetail!!.rfid != null){
            LaunchedEffect(Unit) {
                navController.navigate("finding_asset_screen/" + assetDetail!!.rfid)
            }
        } else {
            Toast.makeText(context,"Data Asset Tersebut tidak terdapat RFID",Toast.LENGTH_SHORT).show()
        }
    }
}