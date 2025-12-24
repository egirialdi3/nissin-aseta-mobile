package id.aseta.app.ui.screen.composable

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.aseta.app.R
import id.aseta.app.data.model.DepartmentItem
import id.aseta.app.viewmodel.AssetCategoryViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DepartmentSelector(
    viewModel: AssetCategoryViewModel,
    selectedDepartment: DepartmentItem?,
    onDepartmentSelected: (DepartmentItem) -> Unit,
    placeholder: String = "Select Department",
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
            value = selectedDepartment?.dept_name ?: "",
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
                    contentDescription = "Select Department"
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
        DepartmentBottomSheet(
            onDismiss = {
                showBottomSheet = false
                focusManager.clearFocus()
            },
            viewModel = viewModel,
            selectedDepartment = selectedDepartment,
            callBack = { department ->
                onDepartmentSelected(department)
                focusManager.clearFocus()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepartmentBottomSheet(
    onDismiss: () -> Unit,
    viewModel: AssetCategoryViewModel,
    selectedDepartment: DepartmentItem?,
    callBack: (DepartmentItem) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val searchQuery = remember { mutableStateOf("") }

    // Fetch department list if needed
    LaunchedEffect(Unit) {
        // viewModel.fetchAllDepartmentList(context)
    }

    val allDepartments = viewModel.allDepartmentList
    val isLoading = viewModel.isLoading
    val error = viewModel.errorMessage

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
                    text = "Department Setting",
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
                        LoadingState()
                    }
                    error != null -> {
                        ErrorState(
                            errorMessage = error,
                            onRetry = {
                                // Retry fetching data
                                 viewModel.fetchAllDepartmentList(context)
                            }
                        )
                    }
                    allDepartments.isEmpty() -> {
                        EmptyDataState(
                            onRetry = {
                                // Retry fetching data
                                // viewModel.fetchAllDepartmentList(context)
                            }
                        )
                    }
                    else -> {
                        val filteredDepartments = allDepartments.filter {
                            it.dept_name.contains(searchQuery.value, true) || searchQuery.value.isEmpty()
                        }

                        if (filteredDepartments.isEmpty()) {
                            SearchEmptyState()
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(filteredDepartments.size) { index ->
                                    val department = filteredDepartments[index]
                                    DepartmentItem(
                                        department = department,
                                        isSelected = selectedDepartment?.dept_id == department.dept_id,
                                        onSelect = {
                                            callBack(department)
                                            scope.launch {
                                                onDismiss()
                                            }
                                        }
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
private fun LoadingState() {
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
            text = "Loading departments...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorState(
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
            text = "Failed to load departments data",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

//        Text(
//            text = "Failed to load departments data",
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

        Spacer(modifier = Modifier.height(12.dp))

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
private fun EmptyDataState(
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Empty state illustration - you can replace with your own drawable
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(60.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "📂",
                style = MaterialTheme.typography.displayLarge
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No Departments Found",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "There are no departments available at the moment. Please try again later or contact support if this issue persists.",
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
private fun SearchEmptyState() {
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

@Composable
fun DepartmentItem(
    department: DepartmentItem,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 16.dp)
                .clickable { onSelect() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(
                    id = if (isSelected) R.drawable.ic_checked else R.drawable.ic_unchecked
                ),
                contentDescription = "Selection indicator",
                tint = if (isSelected) Color(0xFF1E3A8A) else Color(0xffC9CBCE)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = department.dept_name,
                modifier = Modifier.weight(1f)
            )
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 0.5.dp,
            color = Color.Gray
        )
    }
}