import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.text.TextPaint
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rscja.deviceapi.RFIDWithUHFBLE
//import com.rfid.cf.CFDeviceManager
import com.rscja.deviceapi.RFIDWithUHFUART
import id.aseta.app.R
import id.aseta.app.data.source.local.PowerPreference
import id.aseta.app.data.source.local.TokenDataStore
import id.aseta.app.data.model.LocationItem
import id.aseta.app.ui.screen.composable.LocationBottomSheet
import id.aseta.app.viewmodel.AssetCategoryViewModel
import id.aseta.app.viewmodel.DeviceSettingViewModel
//import id.aseta.app.viewmodel.BluetoothViewModel
import id.aseta.app.viewmodel.UHFViewModel
import kotlinx.coroutines.launch
import kotlin.collections.forEach
import kotlin.math.roundToInt

data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: AuthViewModel, uhfViewModel: UHFViewModel,assetCategoryViewModel: AssetCategoryViewModel, navController: NavController,deviceSettingViewModel: DeviceSettingViewModel = hiltViewModel(), onLogout: () -> Unit) {
    val context = LocalContext.current
    // load power from storage with initial 30 dBm
    val powerFromPref by PowerPreference.getPowerLevel(context).collectAsState(initial = 30f)
    var powerLevel by remember { mutableStateOf(powerFromPref) }
    val scope = rememberCoroutineScope()

    var mReader by remember { mutableStateOf<RFIDWithUHFUART?>(null) }
    var mSled by remember { mutableStateOf<RFIDWithUHFBLE?>(null) }
    var isInitializing by remember { mutableStateOf(false) }
    var initMessage by remember { mutableStateOf<String?>(null) }
    var showDropdown by remember { mutableStateOf(false) }
    var showBottomRfid by remember { mutableStateOf(false) }
    var showBottomSheetLocation by remember { mutableStateOf(false) }

    // --- State BottomSheet power RFID ---
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }

    var dataUser by remember {mutableStateOf<GetMenuItem?>(null)}
    var selectedLocation by remember {mutableStateOf<LocationItem?>(null)}
    var typeHH by remember { mutableStateOf("1") }


    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        listOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA

        )
    } else {
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        results.forEach { (perm, granted) ->
            if (!granted) println("Permission not granted: $perm")
        }
    }

    SideEffect {
        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (notGranted.isNotEmpty()) {
            permissionLauncher.launch(notGranted.toTypedArray())
        }
    }

    suspend fun initUHF() {
        try {
            mReader = RFIDWithUHFUART.getInstance()
            if(mReader != null){
                TokenDataStore.saveSelectedHHType1(context)
            }
        } catch (e: Exception) {
            initMessage = e.message
            return
        }


    }

//    val cfDeviceManager = CFDeviceManager.getInstance(context)



    if (isInitializing) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Inisialisasi") },
            text = { Text("Sedang inisialisasi RFID...") },
            confirmButton = {}
        )
    }

    initMessage?.let { msg ->
        LaunchedEffect(msg) {
//            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            initMessage = null
        }
    }

    fun loadSelectedLocation(context: Context) {
        scope.launch {
            val stored = TokenDataStore.getSelectedLocation(context)
            if (stored != null) {
                assetCategoryViewModel.selectedLocation = stored
                selectedLocation = stored
            }


        }
    }



    LaunchedEffect(Unit) {
        if(dataUser?.barcode != true){
            initUHF()
        }
        loadSelectedLocation(context)
        dataUser = TokenDataStore.getDataUser(context)
        typeHH =  TokenDataStore.getSelectedHH(context)

        val macAddress = TokenDataStore.getSelectedAddress(context)
        if(macAddress != ""){
            deviceSettingViewModel.connect(macAddress, context)
        }



        powerLevel = PowerPreference.getPowerLevelInit(context)
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION, // optional but often needed
                )
            )
        }
    }

    Scaffold { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {

            // HEADER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(1f)
                    .background(Color(0xff1C4488))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.head_main),
                    contentDescription = "Head",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .width(200.dp)
                        .height(250.dp)
                )

                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(25.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Hello,\n" + dataUser?.nama?:"-", fontSize = 26.sp, color = Color.White)
                        Box {
                            Image(
                                painter = painterResource(id = R.drawable.ic_option),
                                contentDescription = "Option",
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable { showDropdown = true }
                            )
                            DropdownMenu(
                                expanded = showDropdown,
                                onDismissRequest = { showDropdown = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                if(dataUser?.barcode != true){
                                    if(typeHH != "1"){
                                        if(deviceSettingViewModel.isConnected.value){
                                            DropdownMenuItem(
                                                text = { Text("Connect To " + deviceSettingViewModel.connectedDevice.value) },
                                                onClick = {
                                                }
                                            )
                                        }else{
                                            DropdownMenuItem(
                                                text = { Text("Connect To RFID SLED") },
                                                onClick = {
                                                    showDropdown = false  // Close dropdown first
                                                    showBottomRfid = true // Then show bottom sheet
                                                }
                                            )
                                        }
                                    }


                                }

                                DropdownMenuItem(
                                    text = { Text("Logout") },
                                    onClick = {
                                        showDropdown = false
                                        assetCategoryViewModel.selectedLocation = null
                                        viewModel.logout(context)
                                        onLogout()
                                    }
                                )
                            }
                        }
                    }

                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 25.dp)) {
                        LocationSelector(
                            viewModel = assetCategoryViewModel,
                            onClick = {
                                showBottomSheetLocation = true
                            }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        if(dataUser?.barcode != true)
                        PowerSelector(context,powerLevel, onClick= {
                            showBottomSheet = true
                        }, dataUser?.barcode == true)
                        else
                            Box{}
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight(0.74f)
                    .fillMaxWidth()
                    .align(Alignment.BottomEnd)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(Color(0xffE0EBF3))
            ) {
                val isBarcode = dataUser?.barcode == true
                val isEnterprise = dataUser?.enterprise == true

                val (menus, icons, bodies, routes) = when {
                    isBarcode && isEnterprise -> Quadruple(
                        listOf("Asset Viewer", "Relocation", "Stock Opname", "Corrective\nMaintenance"),
                        listOf(R.drawable.ic_asset_viewer, R.drawable.ic_relocation, R.drawable.ic_stock_opname, R.drawable.ic_find_asset),
                        listOf(R.drawable.menu_container_asset_viewer, R.drawable.menu_container_relocation, R.drawable.menu_container_stock_opname, R.drawable.menu_container_find_asset),
                        listOf<() -> Unit>(
                            { navController.navigate("asset_viewer") },
                            { navController.navigate("relocation") },
                            { navController.navigate("stockopname") },
                            { navController.navigate("corrective_maintenance") }
                        )
                    )

                    isBarcode && !isEnterprise -> Quadruple(
                        listOf("Asset Viewer", "Mutation", "Stock Opname", "Inspection"),
                        listOf(R.drawable.ic_asset_viewer, R.drawable.ic_relocation, R.drawable.ic_stock_opname, R.drawable.ic_inspection),
                        listOf(R.drawable.menu_container_asset_viewer, R.drawable.menu_container_relocation, R.drawable.menu_container_stock_opname, R.drawable.menu_container_inspection),
                        listOf<() -> Unit>(
                            { navController.navigate("asset_viewer") },
                            { navController.navigate("mutation") },
                            { navController.navigate("stockopname") },
                            { navController.navigate("inspection") }
                        )
                    )

                    !isBarcode && isEnterprise -> Quadruple(
                        listOf("Asset Viewer", "Relocation", "Stock Opname", "Find Asset", "RFID Registration", "RFID Replace Tag", "Corrective\nMaintenance"),
                        listOf(
                            R.drawable.ic_asset_viewer,
                            R.drawable.ic_relocation,
                            R.drawable.ic_stock_opname,
                            R.drawable.ic_find_asset,
                            R.drawable.rfid_register,
                            R.drawable.rfid_replace_tag,
                            R.drawable.corrective_maint
                        ),
                        listOf(
                            R.drawable.menu_container_asset_viewer,
                            R.drawable.menu_container_relocation,
                            R.drawable.menu_container_stock_opname,
                            R.drawable.menu_container_relocation,
                            R.drawable.menu_container_find_asset,
                            R.drawable.menu_container_relocation,
                            R.drawable.menu_container_stock_opname
                        ),
                        listOf<() -> Unit>(
                            { navController.navigate("asset_viewer") },
                            { navController.navigate("relocation_rfid/relocation") },
                            { navController.navigate("stockopname") },
                            { navController.navigate("find_asset") },
                            { navController.navigate("rfid_registration") },
                            { navController.navigate("rfid_replace_tag") },
                            { navController.navigate("relocation_rfid/corrective_maintenance") }
                        )
                    )

                    else -> Quadruple(
                        listOf("Asset Viewer", "Inspection", "Stock Opname", "Find Asset", "RFID Registration", "RFID Replace Tag", "Mutation"),
                        listOf(
                            R.drawable.ic_asset_viewer,
                            R.drawable.ic_inspection,
                            R.drawable.ic_stock_opname,
                            R.drawable.ic_find_asset,
                            R.drawable.rfid_register,
                            R.drawable.rfid_replace_tag,
                            R.drawable.mutation
                        ),
                        listOf(
                            R.drawable.menu_container_asset_viewer,
                            R.drawable.menu_container_inspection,
                            R.drawable.menu_container_stock_opname,
                            R.drawable.menu_container_inspection,
                            R.drawable.menu_container_find_asset,
                            R.drawable.menu_container_inspection,
                            R.drawable.menu_container_find_asset
                        ),
                        listOf<() -> Unit>(
                            { navController.navigate("asset_viewer") },
                            { navController.navigate("relocation_rfid/inspection") },
                            { navController.navigate("stockopname") },
                            { navController.navigate("find_asset") },
                            { navController.navigate("rfid_registration") },
                            { navController.navigate("rfid_replace_tag") },
                            { navController.navigate("relocation_rfid/mutation") }
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.fillMaxSize()) {
                        items(menus.size) { index ->
                            MenuItem(
                                title = menus[index],
                                icon = icons[index],
                                id = bodies[index],
                                onClick = routes[index]
                            )
                        }
                    }
                }
            }

        }
    }

    if (showBottomRfid) {
        DeviceSettingBottomSheet(
            onDismissRequest = {
                showBottomRfid = false
            }
        )
    }


    // --- Bottom Sheet for Power Selector ---
    if (showBottomSheet) {
        var sliderWidthPx by remember { mutableStateOf(1f) }
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
                scope.launch { bottomSheetState.hide() }
            },
            sheetState = bottomSheetState,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("RFID Power Setting", fontSize = 16.sp, fontWeight = FontWeight.W500)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xffF1F8FF))
                        .padding(16.dp)
                ) {
                    Row {
                        Image(
                            painter = painterResource(id = R.drawable.ic_power_rfid_blue),
                            contentDescription = "Head",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "RFID Power Setting controls reading range. " +
                                    "0 dBm for short-range, 10 dBm up to 3 meters, 20 dBm up to 7 meters, " +
                                    "and 30 dBm reaching 15 meters. Adjust power for efficiency and compliance.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.W300
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .onGloballyPositioned {
                            sliderWidthPx = it.size.width.toFloat() - 142f // margin kiri-kanan
                        }
                ) {
                    val thumbOffsetX = remember(powerLevel, sliderWidthPx) {
                        val fraction = (powerLevel - 0f) / (30f - 0f)
                        fraction * sliderWidthPx
                    }

                    // Ukur panjang teks secara dinamis menggunakan LocalDensity
                    var textWidth by remember { mutableStateOf(0f) }
                    val text = "${powerLevel.toInt()}"
                    val density = LocalDensity.current.density
                    val textPaint = TextPaint().apply {
                        textSize = with(LocalDensity.current) { 12.sp.toPx() }
                    }
                    textWidth = textPaint.measureText(text)
                    Text(
                        text = "Power Setting : "  + powerLevel.toInt().toString()
                    )

                    // Slider Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0", fontSize = 14.sp)
                        Slider(
                            value = powerLevel,
                            onValueChange = { newValue ->
                                powerLevel = newValue.roundToInt().toFloat()
                            },
                            valueRange = 0f..30f,
                            steps = 29,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = Color.Transparent,
                                activeTrackColor = Color(0xFF1E3A8A),
                                inactiveTrackColor = Color(0xFFD3D3D3),
                                activeTickColor = Color.Transparent,
                                inactiveTickColor = Color.Transparent
                            ),
                            thumb = {
                                SliderDefaults.Thumb(
                                    modifier = Modifier.clip(CircleShape),
                                    interactionSource = remember { MutableInteractionSource() },
                                    thumbSize = DpSize(24.dp, 24.dp),
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color(0xFF1E3A8A)
                                    )
                                )
                            }
                        )
                        Text("30", fontSize = 14.sp)
                    }
                }

                Button(
                    onClick = {
                        showBottomSheet = false
                        if(mReader != null){
                            mReader?.setPower(powerLevel.toInt())
                        }
                        Toast.makeText(
                            context,
                            "Power diset ke ${powerLevel.toInt()} dBm",
                            Toast.LENGTH_SHORT
                        ).show()
                        scope.launch { PowerPreference.savePowerLevel(context, powerLevel) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E3A8A),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save")
                }
                Spacer(Modifier.height(30.dp))
            }
        }
    }


    // -- Bottom sheet for location list --
    if(showBottomSheetLocation){
        LocationBottomSheet(onDismiss = {
            showBottomSheetLocation = false
        }
            ,assetCategoryViewModel,
            selectedLocation,{
            it->selectedLocation = it
                assetCategoryViewModel.selectLocation(context, it)
        })
    }
}

@Composable
fun MenuItem(title: String, icon: Int, id: Int,onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(145.dp)
            .padding(5.dp)
            .clickable {onClick()},
        contentAlignment = Alignment.Center
    ) {
        Image(painter = painterResource(id = id), contentDescription = title, modifier = Modifier.matchParentSize())
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Image(painter = painterResource(id = icon), contentDescription = title, modifier = Modifier.fillMaxSize(0.4f))
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun LocationSelector(viewModel: AssetCategoryViewModel,onClick: () -> Unit) {
    val selected = viewModel.selectedLocation

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xff29559F8F))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(painter = painterResource(id = R.drawable.ic_location), contentDescription = "Location", modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = selected?.location ?: "Select Location",
            color = Color.White,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Image(painter = painterResource(id = R.drawable.ic_arrow_down), contentDescription = "Arrow Down", modifier = Modifier.size(8.dp))
    }
}

@Composable
fun PowerSelector(context:Context,powerLevel: Float,  onClick: () -> Unit,isDisable:Boolean) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xff29559F8F))
            .clickable {
                if(!isDisable){
                    onClick()
                }else{
                    Toast.makeText(context,"No RFID Found, Use device RFID",Toast.LENGTH_SHORT).show()
                }
            }
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(painter = painterResource(id = R.drawable.ic_power_rfid), contentDescription = "Power", modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = if(isDisable) "No RFID Found" else powerLevel.toString(), color = Color.White)
        Spacer(modifier = Modifier.width(4.dp))
        Image(painter = painterResource(id = R.drawable.ic_arrow_down), contentDescription = "Arrow Down", modifier = Modifier.size(8.dp))
    }
}