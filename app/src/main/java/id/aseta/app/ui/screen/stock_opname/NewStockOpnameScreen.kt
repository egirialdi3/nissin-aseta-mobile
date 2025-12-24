package id.aseta.app.ui.screen.stock_opname

import AuthViewModel
import GetMenuItem
import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
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
import id.aseta.app.data.model.LocationItem
import id.aseta.app.viewmodel.AssetCategoryViewModel
import id.aseta.app.viewmodel.UHFViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.compose.currentBackStackEntryAsState
import id.aseta.app.R
import id.aseta.app.data.source.local.TokenDataStore
import id.aseta.app.data.model.AssetDetail
import id.aseta.app.data.model.StockOpnameForeignItem
import id.aseta.app.data.model.StockOpnameGroup
import id.aseta.app.data.model.StockOpnameRequestItem
import id.aseta.app.ui.screen.relocation.LocationSelector
import id.aseta.app.ui.theme.RequiredLabel


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NewStockOpnameScreen(
    viewModel: AuthViewModel,
    uhfViewModel: UHFViewModel,
    assetCategoryModel: AssetCategoryViewModel,
    navController: NavController,
) {
    val context = LocalContext.current
    var selectedLocation by remember { mutableStateOf<LocationItem?>(null) }
    var selectedGroup by remember {mutableStateOf<StockOpnameGroup?>(null)}
    val assetMap by remember { derivedStateOf { assetCategoryModel.assetDetailsMap } }

    var showBottomBar by remember { mutableStateOf(false) }

    var dataUser by remember {mutableStateOf<GetMenuItem?>(null)}
    val navBackStackEntry by navController.currentBackStackEntryAsState()
//    val assetScanned by navBackStackEntry?.savedStateHandle?.getStateFlow("assetScanned", false)
//        ?.collectAsState(false) ?: remember { mutableStateOf(false) }
    val assetScanned by navBackStackEntry?.savedStateHandle
        ?.getStateFlow("assetScanned", false)
        ?.collectAsState() ?: remember { mutableStateOf(false) }
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = SheetState(
            skipPartiallyExpanded = true, // langsung full expand
            initialValue = SheetValue.Expanded // langsung terbuka
        )
    )

    // Efek untuk menangani perubahan ketika kembali dari scan
    LaunchedEffect(assetScanned) {
//        println("KEPANGGIL")
//        if(assetScanned){
////            println(navBackStackEntry?.savedStateHandle?.get<Boolean>("assetScanned"))
//            selectedLocation = assetCategoryModel.selectedLocationStockOpname
//            navBackStackEntry?.savedStateHandle?.remove<Boolean>("assetScanned")
//        }
        when (assetScanned) {
            true -> {
                selectedLocation = assetCategoryModel.selectedLocationStockOpname

                selectedGroup = assetCategoryModel.selectedGroup
                // Hapus agar tidak trigger ulang
                navBackStackEntry?.savedStateHandle?.remove<Boolean>("assetScanned")
            }
            null -> {
                // Tidak ada key "assetScanned" berarti user kembali tanpa scan

                selectedLocation = null
                selectedGroup = null
                assetCategoryModel.selectedLocation = null
                assetCategoryModel.selectedGroup = null
            }

            false -> {

            }
        }

    }



    // Efek inisialisasi
    LaunchedEffect(Unit) {
        assetCategoryModel.fetchGroups(context)
        scaffoldState.bottomSheetState.expand()
        dataUser = TokenDataStore.getDataUser(context)
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
                            "New Stock Opname",
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
            bottomBar = { BottomBar(dataUser = dataUser,context,assetCategoryModel,selectedLocation,navController) },
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
                        painter = painterResource(id = R.drawable.il_stock_opname),
                        contentDescription = "Stock Opname",
                        modifier = Modifier
                            .height(85.dp)
                            .fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Stock Opname",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Fill in the form below, make sure you are \nat the location you selected, then scan \nall the items at the location.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                val assetList = selectedLocation?.let { assetMap[it.location_id] } ?: emptyList()

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = 220.dp) // Geser ke bawah biar mirip bottom sheet
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .background(Color.White)
                        .padding(horizontal = 0.dp)
                    ,
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
                            RequiredLabel(
                                text = "Group Stock Opname",
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            GroupStockOpnameDropdown(
                                selectedGroup = selectedGroup,
                                viewModel = assetCategoryModel,
                                onAddClick = {
                                    showBottomBar = true
                                },
                                onSelectedGroup = {
                                    it->selectedGroup = it
                                }
                            )
                            Spacer(Modifier.height(16.dp))
                            RequiredLabel(text = "Location", fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))

//                            LocationDropdown(
//                                viewModel = assetCategoryModel,
//                                selectedLocation = selectedLocation,
//                                onLocationSelected = {
//                                    selectedLocation = it
//                                    assetCategoryModel.fetchAssetDetailsByLocation(
//                                        context,
//                                        selectedLocation!!.location_id
//                                    )
//                                    assetCategoryModel.selectedLocationStockOpname =
//                                        selectedLocation
//
//                                }
//                            )
                            LocationSelector(
                                viewModel = assetCategoryModel,
                                selectedLocation = selectedLocation,
                                onLocationSelected = {
                                    selectedLocation = it
                                    assetCategoryModel.fetchAssetDetailsByLocation(
                                        context,
                                        selectedLocation!!.location_id
                                    )
                                    assetCategoryModel.selectedLocationStockOpname =
                                        selectedLocation
                                },
                                placeholder = "Select Location",
//                    focusRequester = locationFocusRequester
                            )
                        }
                    }

                    items(assetList) { asset ->
                        AssetItem(asset = asset)
                    }

                    item(){
                        Spacer(modifier = Modifier.height(240.dp))
                    }

                }
            }
        }
    }

    if (showBottomBar) {
        NewGroupBottomSheet(
            onSave = { it ->selectedGroup = it },
            onDismiss = { showBottomBar = false },
            viewModel = assetCategoryModel,
        )
    }
}


@Composable
fun BottomBar(dataUser:GetMenuItem?,context: Context,viewModel: AssetCategoryViewModel,selectedLocation: LocationItem?,navController: NavController) {
    val isSelected = selectedLocation != null
    val iconRes = if (isSelected) R.drawable.ic_scan_barcode_blue else R.drawable.ic_scan_barcode
    val scanTextColor = if (isSelected) Color(0xff1C4488) else Color(0xffA1A4A9)
    val saveButtonColor = if (isSelected) Color(0xff1C4488) else Color(0xffA1A4A9)

    val iconPainter = painterResource(id = iconRes)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(14.dp, shape = RoundedCornerShape(8.dp))
            .background(Color.White, shape = RoundedCornerShape(8.dp))
            .padding(16.dp)
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            onClick = {
                if(dataUser!!.barcode){
                    navController.currentBackStackEntry?.savedStateHandle?.set("locationId", selectedLocation?.location_id)
                    navController.navigate("scan_asset/" + selectedLocation?.location_id)
                }else{
                    navController.currentBackStackEntry?.savedStateHandle?.set("locationId", selectedLocation?.location_id)
                    navController.navigate("scan_asset_rfid/" + selectedLocation?.location_id)
                }
                },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xffF0F1F1)),
            enabled = true,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                painter = iconPainter,
                contentDescription = "QR Icon",
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) Color(0xff1C4488) else Color(0xffA1A4A9)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Scan Asset",
                color = scanTextColor
            )
        }

        Spacer(Modifier.width(10.dp))



        Button(
            onClick = {

                val mainAssets = viewModel.assetDetailsMap[selectedLocation!!.location_id]?.filter {  it.isFound }
                    ?.mapIndexed { index, asset ->
                        StockOpnameRequestItem(
                            no_register = asset.no_register,
                            kd_barang = asset.kd_barang,
                            stock_opname_sort = index + 1
                        )
                    } ?: emptyList()

                val foreignAssets = viewModel.assetDetailsMap[selectedLocation!!.location_id]?.filter {  it.isForeign }
                    ?.map { asset ->
                        StockOpnameForeignItem(
                            no_register = asset.no_register,
                            prev_location = asset.location_id ?: ""
                        )
                    } ?: emptyList()
                viewModel.insertStockOpname(
                    context,
                    groupCode = viewModel.selectedGroup!!.stock_opname_group_code,
                    locationId = selectedLocation!!.location_id,
                    description = "",
                    asset = mainAssets,
                    data_foreign = foreignAssets,
                )
                navController.popBackStack()

            },
            colors = ButtonDefaults.buttonColors(containerColor = saveButtonColor),
            enabled = true,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Save",
                color = Color.White
            )
        }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewGroupBottomSheet(
    onDismiss: () -> Unit,
    onSave: (StockOpnameGroup) -> Unit,
    viewModel: AssetCategoryViewModel
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var groupCode by remember { mutableStateOf("") }
    var groupName by remember { mutableStateOf("") }


    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        containerColor = Color.White
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {

            Spacer(modifier = Modifier.height(12.dp))
            RequiredLabel(
                text = "Group Code",
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = groupCode,
                onValueChange = {
                    it->groupCode = it
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            RequiredLabel(
                text = "Group Name",
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = groupName,
                onValueChange = {
                        it->groupName = it
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Save Button
            Button(
                onClick = {
                    viewModel.insertNewGroup(context,groupCode,groupName)
                    onSave(StockOpnameGroup(
                        stock_opname_group_code = groupCode,
                        stock_opname_group_name = groupName
                    ))
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A))
            ) {
                Text("Save  & Use new group", color = Color.White)
            }
            Spacer(modifier = Modifier.height(70.dp))
        }
    }
}

@Composable
fun AssetItem(asset: AssetDetail) {
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
                    text = asset.no_register,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(2.dp))
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

//            Box(
//                modifier = Modifier
//                    .align(Alignment.CenterVertically)
//                    .padding(start = 8.dp)
//                    .background(
//                        color = Color(0xffF0F1F1),
//                        shape = RoundedCornerShape(12.dp)
//                    )
//                    .padding(horizontal = 12.dp, vertical = 4.dp)
//            ) {
//                Text(
//                    text = if(asset.isFound)"Found" else "Not Found",
//                    color = Color.Black,
//                    style = MaterialTheme.typography.bodySmall
//                )
//            }
        }
        Spacer(Modifier.height(8.dp))
        Divider(color = Color(0xffC9CBCE), thickness = 0.5.dp)
    }
}

@Composable
fun GroupStockOpnameDropdown(
    selectedGroup: StockOpnameGroup?,
    viewModel: AssetCategoryViewModel,
    onAddClick: () -> Unit,
    onSelectedGroup: (StockOpnameGroup) -> Unit,
) {
    val groups = viewModel.groups
    var expanded by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
                .background(Color.White)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = selectedGroup?.stock_opname_group_name ?: "",
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
                placeholder = { Text("Select group stock opname") },
                enabled = false,
                trailingIcon = {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                groups.forEach { group ->
                    DropdownMenuItem(
                        text = { Text(group.stock_opname_group_name) },
                        onClick = {
                            onSelectedGroup(group)
                            viewModel.selectGroup(group)
                            expanded = false
                        },
                        modifier = Modifier
                            .background(Color.White)

                    )
                }
            }
        }


        Image(
            painter = painterResource(id = R.drawable.button_add_blue),
            contentDescription = null,
            modifier = Modifier
                .size(60.dp)
                .clickable { onAddClick() }
        )
    }
}