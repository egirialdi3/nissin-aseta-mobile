package id.aseta.app.ui.screen.relocation

import AuthViewModel
import LocationSelector
import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import id.aseta.app.R
import id.aseta.app.data.model.AssetDetail
import id.aseta.app.data.model.LocationItem
import id.aseta.app.ui.theme.RequiredLabel
import id.aseta.app.viewmodel.AssetCategoryViewModel
import id.aseta.app.viewmodel.UHFViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.text.input.ImeAction
import id.aseta.app.data.model.DepartmentItem
import id.aseta.app.ui.screen.asset_viewer.AssetSelectorBottomSheet
import id.aseta.app.ui.screen.composable.DepartmentSelector
import id.aseta.app.ui.screen.composable.ExpandableLocationItem
import id.aseta.app.ui.screen.composable.LocationBottomSheet
import id.aseta.app.ui.screen.composable.buildLocationTree


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelocationScreen(
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
                            "Relocation",
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
                    painter = painterResource(id = R.drawable.il_relocation),
                    contentDescription = "Relocation Image Illustration",
                    modifier = Modifier
                        .height(240.dp)
                        .fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Asset Relocation",
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
                    onClick = { navController.navigate("scan_qr/relocation") },
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
            type = "relocation"
        )
    }
}



@Composable
fun InfoRow(icon: ImageVector, text: String) {
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
fun LocationDropdown(
    viewModel: AssetCategoryViewModel,
    selectedLocation: LocationItem?,
    onLocationSelected: (LocationItem) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.fetchRootLocations(context = context )
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier
            .background(Color.White)
    ) {
        OutlinedTextField(
            value = selectedLocation?.location?:"",
            onValueChange = {},
            readOnly = true,
            placeholder = { Text("Select New Location") },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.White, // <- pastikan background TextField putih
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
            modifier = Modifier
                .background(Color.White)
        ) {
            viewModel.locationList.forEach { location ->
                DropdownMenuItem(
                    text = { Text(location.location) },
                    onClick = {
                        onLocationSelected(location)
                        expanded = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                )
            }
        }
    }
}




@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FullScreenRelocationBottomSheet(
    context: Context,
    assetDetail: AssetDetail,
    viewModel: AssetCategoryViewModel,
    onDismiss: () -> Unit
) {

    val updatedAssetDetail = viewModel.assetDetailsMap.values
        .flatten()
        .find { it.no_register == assetDetail.no_register } ?: assetDetail

    var selectedLocation by remember { mutableStateOf<LocationItem?>(null) }
    var selectedDepartment by remember { mutableStateOf<DepartmentItem?>(null) }
    var newUser by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    var isUserFocused by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { targetValue ->
            targetValue != SheetValue.Hidden
        }
    )

    LaunchedEffect(Unit) {
        viewModel.fetchAllDepartmentList(context)
        sheetState.show()
    }

    val newUserBringIntoViewRequester = remember { BringIntoViewRequester() }
    val descriptionBringIntoViewRequester = remember { BringIntoViewRequester() }


    ModalBottomSheet(
        dragHandle = null,
        onDismissRequest = {
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = Color.White,
        windowInsets = WindowInsets(0),

        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        onDismiss()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                    Text("Asset Details", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                // Asset Info Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F6F7)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.il_scanner),
                            contentDescription = "Asset Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(text = updatedAssetDetail.nama_barang, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = updatedAssetDetail.no_register)
                            Spacer(modifier = Modifier.height(6.dp))

                            InfoRow(icon = Icons.Default.LocationOn, text = updatedAssetDetail.location)
                            Spacer(modifier = Modifier.height(2.dp))

                            InfoRow(icon = Icons.Default.LocationCity, text = updatedAssetDetail.dept_name.toString())
                            Spacer(modifier = Modifier.height(2.dp))

                            InfoRow(icon = Icons.Default.Work, text = updatedAssetDetail.nama_kel_barang)
                            Spacer(modifier = Modifier.height(2.dp))

                            InfoRow(icon = Icons.Default.Person, text = updatedAssetDetail.asset_holder.toString())
                        }
                    }
                }

                // New Location
                RequiredLabel(text = "New Location", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                LocationSelector(
                    viewModel = viewModel,
                    selectedLocation = selectedLocation,
                    onLocationSelected = {
                        selectedLocation = it
                    },
                    placeholder = "Select Location",
                )


                Spacer(modifier = Modifier.height(16.dp))
                // New Department
                RequiredLabel(text = "New Department", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                DepartmentSelector(
                    viewModel = viewModel,
                    selectedDepartment = selectedDepartment,
                    onDepartmentSelected = {
                        selectedDepartment = it
                    },
                    placeholder = "Select Department",
                )

                Spacer(modifier = Modifier.height(16.dp))

                // New User
                val focusRequester = remember { FocusRequester() }
                RequiredLabel(text = "New User", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = newUser,
                    onValueChange = { newUser = it },
                    placeholder = { Text("Enter New User") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .bringIntoViewRequester(newUserBringIntoViewRequester)
                        .onFocusChanged { focus ->
                            isUserFocused = focus.isFocused
                            if (focus.isFocused) {
                                coroutineScope.launch {
                                    newUserBringIntoViewRequester.bringIntoView()
                                }
                            }
                        },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description
//                Text("Description", fontWeight = FontWeight.Medium)
                RequiredLabel(text = "Description", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Write Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .bringIntoViewRequester(descriptionBringIntoViewRequester)
                        .onFocusChanged { focus ->
                            if (focus.isFocused) {
                                coroutineScope.launch {
                                    descriptionBringIntoViewRequester.bringIntoView()
                                }
                            }
                        },
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Save Button
                Button(
                    onClick = {
                        if(selectedLocation == null ){
                            Toast.makeText(context,"Please select the location",Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if(selectedDepartment == null ){
                            Toast.makeText(context,"Please select the department",Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if(newUser.isEmpty()){
                            Toast.makeText(context,"Please fill the New User",Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if(description.isEmpty()){
                            Toast.makeText(context,"Please fill the Description",Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.insertRelocation(
                            context,
                            selectedLocation!!,
                            selectedDepartment!!,
                            newUser,
                            description,
                            listOf(updatedAssetDetail)
                        )

                        val updated = updatedAssetDetail.copy(
                            location_id = selectedLocation!!.location_id,
                            location = selectedLocation!!.location,
                            dept_id = selectedDepartment!!.dept_id.toString(),
                            dept_name = selectedDepartment!!.dept_name,
                            nama = newUser
                        )

                        viewModel.assetDetailsMap = viewModel.assetDetailsMap.mapValues { (key, list) ->
                            list.map {
                                if (it.no_register == updated.no_register) updated else it
                            }
                        }


                        viewModel.assetDetail = updated


                        onDismiss()


                    },
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
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LocationSelector(
    viewModel: AssetCategoryViewModel,
    selectedLocation: LocationItem?,
    onLocationSelected: (LocationItem) -> Unit,
    placeholder: String = "Select Location",
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() },
    bringIntoViewRequester: BringIntoViewRequester = remember { BringIntoViewRequester() }
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var isTextFieldFocused by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }

    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures {
                    showBottomSheet = true
                    focusRequester.requestFocus()
                }
            }
    ) {
        OutlinedTextField(
            value = selectedLocation?.location ?: "",
            onValueChange = { /* Read-only */ },
            readOnly = true,
            placeholder = { Text(placeholder) },
            modifier = modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .bringIntoViewRequester(bringIntoViewRequester)
                .onFocusChanged { focus ->
                    isTextFieldFocused = focus.isFocused
                    if (focus.isFocused) {
                        showBottomSheet = true
                        coroutineScope.launch {
                            bringIntoViewRequester.bringIntoView()
                        }
                    }
                },
            interactionSource = interactionSource,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Select Location"
                )
            },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.White,
                focusedIndicatorColor = Color.Gray,
                unfocusedIndicatorColor = Color.LightGray
            )
        )
    }

    if (showBottomSheet) {
        LocationBottomSheet(
            onDismiss = {
                showBottomSheet = false
                focusManager.clearFocus()
            },
            viewModel = viewModel,
            selectedLocation = selectedLocation,
            callBack = { location ->
                onLocationSelected(location)
                viewModel.selectLocation(context, location)
                focusManager.clearFocus()
            }
        )
    }
}

//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun DepartmentSelector(
//    viewModel: AssetCategoryViewModel,
//    selectedDepartment: DepartmentItem?,
//    onDepartmentSelected: (DepartmentItem) -> Unit,
//    placeholder: String
//) {
//    var expanded by remember { mutableStateOf(false) }
//    val departmentList = viewModel.allDepartmentList
//
//    ExposedDropdownMenuBox(
//        expanded = expanded,
//        onExpandedChange = { expanded = !expanded },
//    ) {
//        OutlinedTextField(
//            modifier = Modifier
//                .fillMaxWidth()
//                .menuAnchor(), // Penting untuk menghubungkan TextField dengan menu
//            readOnly = true,
//            value = selectedDepartment?.dept_name ?: "",
//            onValueChange = {},
//            placeholder = { Text(placeholder) },
//            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
//            colors = ExposedDropdownMenuDefaults.textFieldColors(
//                unfocusedContainerColor = Color.Transparent,
//                focusedContainerColor = Color.Transparent
//            ),
//        )
//        ExposedDropdownMenu(
//            expanded = expanded,
//            onDismissRequest = { expanded = false },
//        ) {
//            departmentList.forEach { departmentItem ->
//                DropdownMenuItem(
//                    text = { Text(departmentItem.dept_name) },
//                    onClick = {
//                        onDepartmentSelected(departmentItem)
//                        expanded = false
//                    },
//                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
//                )
//            }
//        }
//    }
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSelectionScreen(
    onDismiss: () -> Unit,
    viewModel: AssetCategoryViewModel,
    selectedLocation: LocationItem?,
    callBack: (LocationItem) -> Unit
) {
    val context = LocalContext.current
    val searchQuery = remember { mutableStateOf("") }
    val expandedItems = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(Unit) {
        viewModel.fetchAllLocationList(context)
    }

    val allLocations = viewModel.allLocationList
    val isLoading = viewModel.isLoading
    val error = viewModel.errorMessage

    val treeData = remember(allLocations) {
        buildLocationTree(allLocations)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { onDismiss() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Select Location",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery.value,
                onValueChange = { searchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text("Search location...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF1E3A8A),
                    unfocusedBorderColor = Color.LightGray
                )
            )

            // Content
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = error, color = Color.Red)
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        treeData
                            .filter { it.location.contains(searchQuery.value, true) || searchQuery.value.isEmpty() }
                            .forEach { rootItem ->
                                item {
                                    ExpandableLocationItem(
                                        context,
                                        viewModel,
                                        item = rootItem,
                                        expandedItems = expandedItems,
                                        onToggle = { id ->
                                            expandedItems[id] = !(expandedItems[id] ?: false)
                                        },
                                        onDismiss = { item ->
                                            onDismiss()
                                            callBack(item)
                                        },
                                        selectedLocation = selectedLocation
                                    )
                                }
                            }
                    }
                }
            }
        }
    }
}