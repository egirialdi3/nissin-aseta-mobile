package id.aseta.app.ui.screen.stock_opname

import DetailStockAssetItem
//import StockAssetItem
import StockOpnameItem
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import id.aseta.app.R
import id.aseta.app.data.model.LocationItem
import id.aseta.app.data.model.StockOpnameGroup
import id.aseta.app.ui.screen.composable.LocationBottomSheet
import id.aseta.app.ui.screen.composable.ShimmerStockOpnameItem
import id.aseta.app.ui.screen.composable.buildLocationTree
import id.aseta.app.viewmodel.AssetCategoryViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockOpnameListScreen(navController: NavController,viewModel: AssetCategoryViewModel) {


    val context = LocalContext.current

    var showBottomSheetLocation by remember { mutableStateOf(false) }

    var selectedLocationName = viewModel.selectedLocation?.location ?:"Select Location"

    var selectedLocationId = viewModel.selectedLocation?.location_id ?:""

//    var selectedGroupName by remember {mutableStateOf("All Group")}
    var selectedGroupName by remember {mutableStateOf("")}

    var selectedLocationLocal by remember {mutableStateOf<LocationItem?>(LocationItem(
        location = "All Location",
        location_id = "",
        full_location = "All Location",
        parent = null,
        group = null,
        level = null,
        sort = null,
        area = null,
        detail = null,
        process_area = null,
        children = emptyList()
    ))}

    var selectedGroupId by remember {mutableStateOf("")}

    var showBottomSheetGroup by remember { mutableStateOf(false) }

    var selectedDate by remember { mutableStateOf("Select Date") }

    var showBottomSheetDate by remember { mutableStateOf(false) }

    val datePickerDialog = remember { mutableStateOf<DatePickerDialog?>(null) }

    val filter = remember(selectedLocationId, selectedGroupId, selectedDate) {
        Triple(selectedLocationId, selectedGroupId, selectedDate)
    }

    LaunchedEffect(filter) {
        viewModel.fetchStockOpnameItems(
            context,
            locationId = selectedLocationId,
            groupId = selectedGroupId,
            dateStart = selectedDate,
            dateEnd = selectedDate
        )
    }

    LaunchedEffect(Unit) {
        val calendar = Calendar.getInstance()
        val dialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val formattedDate = "${year}-${month + 1}-${dayOfMonth}"
                selectedDate = formattedDate
                viewModel.fetchStockOpnameItems(
                    context,
                    locationId = selectedLocationId,
                    groupId = selectedGroupId,
                    dateStart = selectedDate,
                    dateEnd = selectedDate
                )
                showBottomSheetDate = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Tambahkan tombol Reset (Button Neutral)
        dialog.setButton(
            DialogInterface.BUTTON_NEUTRAL, "Reset"
        ) { d, _ ->
            selectedDate = ""
            viewModel.fetchStockOpnameItems(
                context,
                locationId = selectedLocationId,
                groupId = selectedGroupId,
                dateStart = "",
                dateEnd = ""
            )
            d.dismiss()
            showBottomSheetDate = false
        }

        datePickerDialog.value = dialog
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stock Opname",
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A4383))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_stockopname") },
                containerColor = Color(0xFF1A4383),
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Text("+", color = Color.White, fontSize = 22.sp,
                    fontWeight = FontWeight.Bold)
            }
        },
        floatingActionButtonPosition = FabPosition.Center,

    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(16.dp)
                .padding(innerPadding)
        ) {
            FilterSection(
                selectedLocationName = selectedLocationName,
                selectedGroupName = selectedGroupName,
                selectedDate = selectedDate,
                onLocationClick = {
                    showBottomSheetLocation = true
                },
                onGroupClick = {
                    showBottomSheetGroup = true
                },
                onDateClick = {
                    datePickerDialog.value?.show()
                }
                ,
            )
            StockOpnameItemList(viewModel)
        }

        if(showBottomSheetLocation){
            LocationBottomSheet(
                onDismiss = {
                    showBottomSheetLocation = false
                },
                viewModel = viewModel,
                selectedLocation = selectedLocationLocal,
                callBack = {
                    locationName ->
                    selectedLocationLocal = locationName
                    selectedLocationName = locationName.location
                    selectedLocationId = locationName.location_id
                    showBottomSheetLocation = false
                    viewModel.fetchStockOpnameItems(context,
                        locationId = selectedLocationId,
                        groupId = selectedGroupId,
                        dateStart = selectedDate,
                        dateEnd = selectedDate
                    )
                }
            )
        }

        if (showBottomSheetGroup) {
            GroupBottomSheet(
                onDismiss = { showBottomSheetGroup = false },
                onGroupSelected = { groupName ->
                    selectedGroupName = groupName.stock_opname_group_name // Update selected group name
                    selectedGroupId = groupName.stock_opname_group_code
                    showBottomSheetGroup = false // Close the sheet
                    viewModel.fetchStockOpnameItems(context,
                        locationId = selectedLocationId,
                        groupId = selectedGroupId,
                        dateStart = selectedDate,
                        dateEnd = selectedDate
                    )
                },
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun FilterSection(selectedDate: String,selectedLocationName: String,selectedGroupName: String, onLocationClick: () -> Unit,onDateClick: () -> Unit,onGroupClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(label = selectedLocationName, modifier = Modifier.weight(1f),
            onClick = onLocationClick
        )
        FilterChip(label = selectedGroupName, modifier = Modifier.weight(1f),
            onClick = onGroupClick
            )
        FilterChip(label =  if (selectedDate.isNotEmpty()) selectedDate else "All Date", modifier = Modifier.weight(1f),
            onClick = onDateClick
        )
    }
}

@Composable
fun FilterChip(label: String,modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    OutlinedButton(
        onClick = onClick,
        border = BorderStroke(1.dp, Color.White),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        shape = RoundedCornerShape(50),
        modifier = modifier.height(36.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(label, fontSize = 10.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = if (label == "All Date") Icons.Default.CalendarToday else Icons.Default.ArrowDropDown,
            contentDescription = "Dropdown Arrow",
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun StockOpnameItemList(viewModel: AssetCategoryViewModel) {
    val items = viewModel.stockOpnameItems
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessageStockOpname
    val context = LocalContext.current

    Column(modifier = Modifier.padding(8.dp)) {
        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            errorMessage != null && items == emptyList<StockOpnameItem>() -> {
                Text(
                    text = errorMessage,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            else -> {
                LazyColumn {
                    items(items) { item ->
                        StockOpnameItem(item,viewModel,context)
                    }
                }
            }
        }
    }
}

@Composable
fun StockOpnameItem(item: StockOpnameItem,viewModel: AssetCategoryViewModel,context: Context) {
//    var expanded by remember { mutableStateOf(false) }
    val expanded = viewModel.currentExpandedCode == item.stock_opname_code

    LaunchedEffect(expanded) {
        if (expanded) {
            viewModel.fetchStockOpnameItemDetail(context, item.stock_opname_code)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    Image(
                        painter = painterResource(id = R.drawable.ic_checklist_black),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = item.stock_opname_code, fontSize = 14.sp)
                }
                Text(text = formatDate(item.stock_opname_date), fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Text(text = item.stock_opname_group_name, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = item.location)

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // --- Expand/collapse trigger
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.toggleExpanded(item.stock_opname_code)
                    }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_box_hitam),
                        contentDescription = "Assets",
                        modifier = Modifier.size(16.dp),
                        colorFilter = ColorFilter.tint(Color.Black)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${item.count_assets} Aset")
                }
                Icon(
                    imageVector = if (expanded) Icons.AutoMirrored.Filled.KeyboardArrowRight else Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Expand",
                    tint = Color.Black
                )
            }

//            if (expanded && viewModel.stockOpnameDetails != emptyList<DetailStockAssetItem>()) {
//                Spacer(modifier = Modifier.height(8.dp))
//                Column(
//                    verticalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    viewModel.stockOpnameDetails[item.stock_opname_code]?.forEach { asset->AssetItemRow(asset)
//                    }
////                    item.assets.data.forEach { asset ->
////                        AssetItemRow(asset)
////                    }
//                }
//            }
            // Show expanded details with LazyColumn for pagination
            if (expanded && viewModel.stockOpnameDetails.containsKey(item.stock_opname_code)) {
                Spacer(modifier = Modifier.height(8.dp))

                val assets = viewModel.stockOpnameDetails[item.stock_opname_code] ?: emptyList()
                val paginationState = viewModel.stockOpnameDetailsPagination[item.stock_opname_code]
                val totalCount = paginationState?.totalItems ?: 0
                val hasMore = paginationState?.hasMoreItems ?: false

                // Show total count if available
                if (totalCount > 0) {
                    Text(
                        text = "Showing ${assets.size} of $totalCount assets",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .heightIn(max = 300.dp) // Limit height to prevent overly long card
                ) {
                    itemsIndexed(assets) { index, asset ->
                        // Check if we should load more when approaching end of list
                        if (viewModel.shouldLoadMoreItems(item.stock_opname_code, index)) {
                            LaunchedEffect(Unit) {
                                viewModel.fetchStockOpnameItemDetail(
                                    context,
                                    item.stock_opname_code,
                                    loadMore = true
                                )
                            }
                        }

                        AssetItemRow(asset)
                    }

                    // Show loading indicator at bottom when loading more
                    if (hasMore && viewModel.isLoadingMore.value) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }

                    // Show load more button if there are more items
                    if (hasMore && !viewModel.isLoadingMore.value
                        ) {
                        item {
                            Button(
                                onClick = {
                                    viewModel.fetchStockOpnameItemDetail(
                                        context,
                                        item.stock_opname_code,
                                        loadMore = true
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1A4383)
                                )
                            ) {
                                Text("Load More")
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun AssetItemRow(asset: DetailStockAssetItem) {
    println("cekkk")
    println(asset)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* handle click */ }
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = asset.nama_barang,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${asset.no_register} • ${asset.prev_location?:"-"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = 8.dp)
                    .background(
                        color = if (asset.foreign) Color(0xffFFEBEE) else if (!asset.is_found) Color(
                            0xffF0F1F1
                        ) else Color(0xffE5F5EC),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if(asset.foreign) "Foreign" else if(!asset.is_found) "Not Found" else "Found",
                    color =  if(asset.foreign) Color(0xffEC2D30) else if(!asset.is_found) Color.Black else Color(0xff0C9D61),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = Color(0xffC9CBCE), thickness = 0.5.dp)
    }
}


fun formatDate(input: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val date = parser.parse(input)
        date?.let { formatter.format(it) } ?: input
    } catch (_: Exception) {
        input
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockOpnameLocationBottomSheet(
    onDismiss: () -> Unit,
    onLocationSelected: (LocationItem) -> Unit,
    viewModel: AssetCategoryViewModel
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    val searchQuery = remember { mutableStateOf("") }
    val expandedItems = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(Unit) {
        viewModel.fetchAllLocationList(context)
    }

    val allLocations = viewModel.allLocationList
    val isLoading = viewModel.isLoading

    val treeData = remember(allLocations) {
        buildLocationTree(allLocations)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Location List",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                else -> {
                    LazyColumn {
                        treeData
                            .filter { it.location.contains(searchQuery.value, true) || searchQuery.value.isEmpty() }
                            .forEach { rootItem ->
                                item {
                                    StockExpandableLocationItem(

                                        context,
                                        viewModel,
                                        item = rootItem,
                                        expandedItems = expandedItems,
                                        onToggle = { id ->
                                            expandedItems[id] = expandedItems[id] != true
                                        },
                                        onDismiss = {
                                            onDismiss()
                                        },
                                        onLocationClick = {
                                                selectedLocationName ->
                                            onLocationSelected(selectedLocationName)
                                        }
                                    )
                                }
                            }
                    }
                }
            }

            Spacer(modifier = Modifier.height(70.dp))
        }
    }
}


@Composable
fun StockExpandableLocationItem(
    context: Context,
    viewModel: AssetCategoryViewModel,
    item: LocationItem,
    level: Int = 0,
    expandedItems: SnapshotStateMap<String, Boolean>,
    onToggle: (String) -> Unit,
    onDismiss: () -> Unit,
    onLocationClick: (LocationItem) -> Unit
) {
    val isExpanded = expandedItems[item.location_id] == true

    Column(modifier = Modifier.padding(start = (level * 16).dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onToggle(item.location_id)
                    if (item.children.isEmpty()) {
                        viewModel.selectLocation(context, item)
                        onDismiss()
                        onLocationClick(item)
                    }
                }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = item.location, modifier = Modifier.weight(1f))
            if (item.children.isNotEmpty()) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }
        }
        if (isExpanded) {
            item.children.forEach { child ->
                StockExpandableLocationItem(context,viewModel,child, level + 1, expandedItems, onToggle,onDismiss,onLocationClick)
            }
        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupBottomSheet(
    onDismiss: () -> Unit,
    onGroupSelected: (StockOpnameGroup) -> Unit,
    viewModel: AssetCategoryViewModel
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.fetchGroups(context)
    }

    val groups = viewModel.groups
    val isLoading = viewModel.isLoading
    val error = viewModel.errorMessage

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        containerColor = Color.White
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            Text(
                text = "Select Group",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                error != null && groups == emptyList<StockOpnameGroup>() -> {
                    Text(text = error, color = Color.Red)
                }
                else -> {
                    LazyColumn {
                        groups.forEach { group ->
                            item {
                                GroupItem(
                                    group = group,
                                    onGroupClick = {
                                        onGroupSelected(group)
                                        onDismiss()
                                    }
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(70.dp))
        }
    }
}


@Composable
fun GroupItem(
    group: StockOpnameGroup,
    onGroupClick: () -> Unit
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onGroupClick)
        .padding(8.dp)) {
        Text(text = group.stock_opname_group_name)
    }
}