import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import id.aseta.app.data.source.local.TokenDataStore
import id.aseta.app.viewmodel.DeviceSettingViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceSettingBottomSheet(
    onDismissRequest: () -> Unit,
    viewModel: DeviceSettingViewModel = hiltViewModel()
) {
    val devices by remember { derivedStateOf { viewModel.deviceList } }
    val isScanning by viewModel.isScanning
    val isConnected by viewModel.isConnected
    val context = LocalContext.current
    val connectedDevice by viewModel.connectedDevice
    val coroutineScope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val permissions = listOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.BLUETOOTH_ADVERTISE,
    )

    // Initialize scanning when bottom sheet opens
    LaunchedEffect(Unit) {
        // Reset state and start scanning when bottom sheet opens
        viewModel.clearDeviceList()
        viewModel.startScan(context)
    }

    // Clean up when bottom sheet is dismissed
    DisposableEffect(Unit) {
        onDispose {
            // Stop scanning when bottom sheet is closed
            viewModel.stopScan(context)
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            // Ensure proper cleanup when dismissed
            viewModel.stopScan(context)
            onDismissRequest()
        },
        sheetState = bottomSheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = if (isConnected) "Connected to $connectedDevice" else "Bluetooth Devices",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (isScanning) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scanning for devices...")
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false) // Don't force fill height
            ) {
                if (devices.isEmpty() && !isScanning) {
                    item {
                        Text(
                            text = "No devices found. Try scanning again.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    items(devices) { device ->
                        ListItem(
                            headlineContent = { Text(device.name ?: "Unknown Device") },
                            supportingContent = { Text(device.address) },
                            modifier = Modifier.clickable {
                                println("Device selected: ${device.name} - ${device.address}")
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.BLUETOOTH_CONNECT
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    coroutineScope.launch {
                                        // Stop scanning before connecting
                                        viewModel.stopScan(context)
                                        // Save device info
                                        TokenDataStore.saveSelectedHH(context, "2", device.address)
                                        // Connect to device
                                        viewModel.connect(device.address, context)
                                        // Close bottom sheet after connection attempt
                                        bottomSheetState.hide()
                                        onDismissRequest()
                                    }
                                } else {
                                    println("Bluetooth connect permission not granted")
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        if (isScanning) {
                            viewModel.stopScan(context)
                        } else {
                            // Clear list and restart scan
                            viewModel.clearDeviceList()
                            viewModel.startScan(context)
                        }
                    }
                ) {
                    Text(if (isScanning) "Stop Scan" else "Rescan")
                }

                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            // Ensure scanning is stopped before closing
                            viewModel.stopScan(context)
                            bottomSheetState.hide()
                            onDismissRequest()
                        }
                    }
                ) {
                    Text("Close")
                }
            }

            // Add bottom padding for better UX
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}