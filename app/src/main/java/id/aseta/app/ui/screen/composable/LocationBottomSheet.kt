package id.aseta.app.ui.screen.composable

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.aseta.app.R
import id.aseta.app.data.model.LocationItem
import id.aseta.app.viewmodel.AssetCategoryViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationBottomSheet(
    onDismiss: () -> Unit,
    viewModel: AssetCategoryViewModel,
    selectedLocation: LocationItem?,
    callBack: (LocationItem) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val searchQuery = remember { mutableStateOf("") }
    val expandedItems = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(Unit) {
        viewModel.fetchAllLocationList(context)
    }

    val allLocations = viewModel.allLocationList
    val isLoading = viewModel.isLoading
    val error = viewModel.errorMessage

    val treeData = remember(allLocations) {
        if (allLocations.isNotEmpty()) buildLocationTree(allLocations) else emptyList()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        containerColor = Color.White,
        dragHandle = {},
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with close button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = {
                    scope.launch {
                        onDismiss()
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close"
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Location Setting",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Content area with error handling
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when {
                    isLoading -> {
                        LocationLoadingState()
                    }
                    error != null -> {
                        LocationErrorState(
                            errorMessage = error,
                            onRetry = {
                                viewModel.fetchAllLocationList(context)
                            }
                        )
                    }
                    allLocations.isEmpty() -> {
                        LocationEmptyDataState(
                            onRetry = {
                                viewModel.fetchAllLocationList(context)
                            }
                        )
                    }
                    else -> {
                        val filteredItems = treeData.filter {
                            it.location.contains(searchQuery.value, true) || searchQuery.value.isEmpty()
                        }

                        if (filteredItems.isEmpty()) {
                            LocationSearchEmptyState()
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(filteredItems.size) { index ->
                                    val rootItem = filteredItems[index]
                                    ExpandableLocationItem(
                                        context,
                                        viewModel,
                                        item = rootItem,
                                        expandedItems = expandedItems,
                                        onToggle = { id ->
                                            expandedItems[id] = !(expandedItems[id] ?: false)
                                        },
                                        onDismiss = { id ->
                                            callBack(id)
                                            scope.launch {
                                                onDismiss()
                                            }
                                        },
                                        selectedLocation = selectedLocation
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LocationLoadingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Loading locations...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LocationErrorState(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Error Icon
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Failed to load Locationz data",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))
//        Spacer(modifier = Modifier.height(16.dp))

//        Text(
//            text = "Oops! Something went wrong",
//            style = MaterialTheme.typography.headlineSmall,
//            fontWeight = FontWeight.Bold,
//            textAlign = TextAlign.Center,
//            color = MaterialTheme.colorScheme.onSurface
//        )
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        Text(
//            text = "Failed to load locations data",
//            style = MaterialTheme.typography.bodyMedium,
//            textAlign = TextAlign.Center,
//            color = MaterialTheme.colorScheme.onSurfaceVariant
//        )

//        if (errorMessage.isNotEmpty() && errorMessage != "null") {
//            Spacer(modifier = Modifier.height(4.dp))
//            Text(
//                text = errorMessage,
//                style = MaterialTheme.typography.bodySmall,
//                textAlign = TextAlign.Center,
//                color = MaterialTheme.colorScheme.error,
//                modifier = Modifier
//                    .clip(RoundedCornerShape(8.dp))
//                    .background(MaterialTheme.colorScheme.errorContainer)
//                    .padding(8.dp)
//            )
//        }

//        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Retry",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Try Again",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun LocationEmptyDataState(
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Empty state illustration
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(60.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "📍",
                style = MaterialTheme.typography.displayLarge
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No Locations Found",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "There are no locations available at the moment. Please try again later or contact support if this issue persists.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Refresh",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun LocationSearchEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🔍",
            style = MaterialTheme.typography.displayMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No Results Found",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Try adjusting your search criteria",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun buildLocationTree(locations: List<LocationItem>): List<LocationItem> {
    val childrenMap = mutableMapOf<String, MutableList<LocationItem>>()

    locations.forEach { location ->
        val parentId = location.parent
        if (parentId != null) {
            val children = childrenMap.getOrPut(parentId) { mutableListOf() }
            children.add(location)
        }
    }

    return locations.filter { it.parent == null }.map { root ->
        attachChildren(root, childrenMap)
    }
}

fun attachChildren(
    node: LocationItem,
    childrenMap: Map<String, List<LocationItem>>
): LocationItem {
    val children = childrenMap[node.location_id].orEmpty()
    return node.copy(
        children = children.map { child -> attachChildren(child, childrenMap) }
    )
}

@Composable
fun ExpandableLocationItem(
    context: Context,
    viewModel: AssetCategoryViewModel,
    item: LocationItem,
    level: Int = 0,
    expandedItems: SnapshotStateMap<String, Boolean>,
    onToggle: (String) -> Unit,
    onDismiss: (LocationItem) -> Unit,
    selectedLocation: LocationItem?
) {
    val isExpanded = expandedItems[item.location_id] == true

    Column(modifier = Modifier.padding(start = (level * 16).dp)) {
        Row(
            modifier = Modifier
                .background(if (isExpanded) Color(0xffF1F8FF) else Color.White)
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(
                    id = if (selectedLocation?.location != item.location) R.drawable.ic_unchecked else R.drawable.ic_checked
                ),
                contentDescription = "Checked",
                tint = if (selectedLocation?.location == item.location) Color(0xFF1E3A8A) else Color(0xffC9CBCE),
                modifier = Modifier.clickable {
                    onDismiss(item)
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = item.location,
                modifier = Modifier
                    .clickable {
                        onDismiss(item)
                    }
                    .weight(1f)
            )
            if (item.children.isNotEmpty()) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.clickable {
                        onToggle(item.location_id)
                    }
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 0.5.dp,
            color = Color.Gray
        )
        if (isExpanded) {
            item.children.forEach { child ->
                ExpandableLocationItem(
                    context,
                    viewModel,
                    child,
                    level + 1,
                    expandedItems,
                    onToggle,
                    onDismiss,
                    selectedLocation
                )
            }
        }
    }
}