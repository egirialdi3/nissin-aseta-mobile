package id.aseta.app.viewmodel

import DetailStockAssetItem
import GroupResponse
import InspectionProcessResponse
import SetGroupRequest
import SetInspectionProcessRequest
import SetRegisterProcessRequest
import SetRegisterProcessResponse
import SetReplaceTagProcessRequest
import SetReplaceTagProcessRespone
import StockOpnameItem
import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.aseta.app.data.source.local.TokenDataStore
import id.aseta.app.data.model.AssetCategory
import id.aseta.app.data.model.AssetDetail
import id.aseta.app.data.model.AssetFullDetail
import id.aseta.app.data.model.ConditionItem
import id.aseta.app.data.model.DataJenisDisposalItem
import id.aseta.app.data.model.DepartmentItem
import id.aseta.app.data.model.LocationItem
import id.aseta.app.data.model.LogMaintenanceItem
import id.aseta.app.data.model.LogMovingAssetItem
import id.aseta.app.data.model.MoveAssetDataRequest
import id.aseta.app.data.model.RelocationRequest
import id.aseta.app.data.model.RelocationResponse
import id.aseta.app.data.model.StockOpnameForeignItem
import id.aseta.app.data.model.StockOpnameGroup
import id.aseta.app.data.model.StockOpnameRequest
import id.aseta.app.data.model.StockOpnameRequestItem
import id.aseta.app.data.model.StockOpnameRequestResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import org.json.JSONObject
import retrofit2.HttpException
import java.io.File
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class PaginationState(
    val currentPage: Int = 1,
    val isLoadingMore: Boolean = false,
    val hasMoreItems: Boolean = true,
    val itemsPerPage: Int = 10,
    val totalItems: Int = 0
)

class AssetCategoryViewModel : ViewModel() {

    var categories by mutableStateOf<List<AssetCategory>>(emptyList())
        private set

    var totalAsset by mutableStateOf(0)

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var errorMessageStockOpname by mutableStateOf<String?>(null)
        private set


    var expandedCategory by mutableStateOf<String?>(null)



    var assetDetail by   mutableStateOf<AssetDetail?>(null)



    var locationList by mutableStateOf<List<LocationItem>>(emptyList())
        private set

    var allLocationList by mutableStateOf<List<LocationItem>>(emptyList())
        private set


    var allDepartmentList by mutableStateOf<List<DepartmentItem>>(emptyList())
        private set

    var selectedLocation by mutableStateOf<LocationItem?>(null)
    var selectedLocationStockOpname by mutableStateOf<LocationItem?>(null)

    var resultSimpanInspeksi by mutableStateOf<InspectionProcessResponse?>(null)
    var resultSimpanGroup by mutableStateOf<GroupResponse?>(null)
    var resultSimpanRfid by mutableStateOf<SetRegisterProcessResponse?>(null)
    var resultReplaceRfid by mutableStateOf<SetReplaceTagProcessRespone?>(null)
    var resultSimpanMutation by mutableStateOf<RelocationResponse?>(null)
    var resultSimpanStockOpname by mutableStateOf<StockOpnameRequestResponse?>(null)


    var groups by mutableStateOf<List<StockOpnameGroup>>(emptyList())
        private set

    var selectedGroup by mutableStateOf<StockOpnameGroup?>(null)


    var stockOpnameItems by mutableStateOf<List<StockOpnameItem>>(emptyList())
        private set



    val searchResults = mutableStateOf<List<AssetDetail>>(emptyList())


    var rfidScanResult by mutableStateOf<List<AssetDetail>?>(emptyList())

    var selectedAssetFindAsset by mutableStateOf<AssetDetail?>(null)
    var historyTotalAsset by mutableStateOf<Int>(0)


    private val _conditionItems = MutableStateFlow<List<ConditionItem>>(emptyList())
    val conditionItems: StateFlow<List<ConditionItem>> = _conditionItems

    private val _disposalItems = MutableStateFlow<List<DataJenisDisposalItem>>(emptyList())
    val disposalItems: StateFlow<List<DataJenisDisposalItem>> = _disposalItems

    // Tetap gunakan assetDetailsMap yang sudah ada (untuk kompatibilitas)
    var assetDetailsMap by mutableStateOf<Map<String, List<AssetDetail>>>(emptyMap())

    // Private state untuk internal management
    private val _assetDetailsMap = mutableStateMapOf<String, List<AssetDetail>>()

    private val _assetDetailsPaginationMap = mutableMapOf<String, MutableStateFlow<PaginationState>>()
    val assetDetailsPaginationMap: Map<String, StateFlow<PaginationState>> get() = _assetDetailsPaginationMap

    fun fetchAssetDetailsByCategory(
        context: Context,
        kdKelBarang: String,
        loadMore: Boolean = false,
        onComplete: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            try {
                // Make sure we have a StateFlow for this category
                if (!_assetDetailsPaginationMap.containsKey(kdKelBarang)) {
                    _assetDetailsPaginationMap[kdKelBarang] = MutableStateFlow(PaginationState())
                }

                val currentPaginationState = _assetDetailsPaginationMap[kdKelBarang]!!.value

                // Don't load if already loading or no more items
                if (loadMore && (!currentPaginationState.hasMoreItems || currentPaginationState.isLoadingMore)) {
                    onComplete?.invoke()
                    return@launch
                }

                val pageToLoad = if (loadMore) currentPaginationState.currentPage + 1 else 1

                // Update loading state
                loadingCategoryMap[kdKelBarang] = true
                _assetDetailsPaginationMap[kdKelBarang]?.update {
                    it.copy(isLoadingMore = loadMore) // Set true only when loading more
                }

                if (!loadMore) {
                    // Clear existing data if not loading more
                    _assetDetailsMap[kdKelBarang] = emptyList()
                }

                val token = TokenDataStore.getToken(context) ?: ""
                val response = RetrofitClient.api.getAssetsByCategory(
                    kdKelBarang = kdKelBarang,
                    token = "Bearer $token",
                    page = pageToLoad.toString(),
                    limit = currentPaginationState.itemsPerPage.toString()
                )

                if (response.isSuccessful && response.body()?.metadata?.code == 200) {
                    val newData = response.body()?.response?.data ?: emptyList()
                    val totalItems = response.body()?.response?.countFullData ?: 0

                    // Combine with existing data if loading more
                    val currentData = if (loadMore) _assetDetailsMap[kdKelBarang].orEmpty() else emptyList()
                    val combinedData = currentData + newData

                    // **PENTING: Update KEDUA state untuk sinkronisasi**
                    _assetDetailsMap[kdKelBarang] = combinedData

                    // Update assetDetailsMap yang digunakan di View lain
                    assetDetailsMap = assetDetailsMap.toMutableMap().apply {
                        this[kdKelBarang] = combinedData
                    }

                    // Update pagination state
                    val hasMore = combinedData.size < totalItems
                    _assetDetailsPaginationMap[kdKelBarang]?.update {
                        it.copy(
                            currentPage = pageToLoad,
                            hasMoreItems = hasMore,
                            isLoadingMore = false,
                            totalItems = totalItems
                        )
                    }

                    Log.d("AssetViewModel", "Category: $kdKelBarang, Loaded: ${newData.size} items, Total: ${combinedData.size}/$totalItems, HasMore: $hasMore")

                } else {
                    errorMessage = response.body()?.metadata?.message ?: "Gagal memuat data aset."
                    if (!loadMore) {
                        _assetDetailsMap[kdKelBarang] = emptyList()
                        // Sync dengan assetDetailsMap
                        assetDetailsMap = assetDetailsMap.toMutableMap().apply {
                            this[kdKelBarang] = emptyList()
                        }
                    }
                    _assetDetailsPaginationMap[kdKelBarang]?.update {
                        it.copy(
                            hasMoreItems = false,
                            isLoadingMore = false
                        )
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Gagal mengambil detail aset: ${e.localizedMessage}"
                _assetDetailsPaginationMap[kdKelBarang]?.update {
                    it.copy(isLoadingMore = false)
                }
                Log.e("AssetViewModel", "Error fetching assets", e)
            } finally {
                loadingCategoryMap[kdKelBarang] = false
                onComplete?.invoke()
            }
        }
    }

//    // Function to fetch conditions
    fun fetchConditions(context: Context) {
        viewModelScope.launch {
            try {
                val token = TokenDataStore.getToken(context) ?: ""
                val response = RetrofitClient.api.getConditions(
                    token = "Bearer $token"
                )

                if (response.isSuccessful && response.body()?.metadata?.code == 200) {
                    _conditionItems.value = response.body()?.response?.data.orEmpty()
                } else {
                    errorMessage = response.body()?.metadata?.message ?: "Gagal memuat data kondisi."
                }
            } catch (e: Exception) {
                errorMessage = "Gagal mengambil data kondisi: ${e.localizedMessage}"
            }
        }
    }

    fun getJenisDisposal(context: Context){
        viewModelScope.launch {
            try {
                val token = TokenDataStore.getToken(context) ?: ""
                val response = RetrofitClient.api.getJenisDisposal(
                    token = "Bearer $token"
                )

                if (response.isSuccessful && response.body()?.metadata?.code == 200) {
                    _disposalItems.value = response.body()?.response?.data.orEmpty()
                } else {
                    errorMessage = response.body()?.metadata?.message ?: "Gagal memuat data kondisi."
                }
            } catch (e: Exception) {
                errorMessage = "Gagal mengambil data kondisi: ${e.localizedMessage}"
            }
        }
    }



    // For pagination state
    private val _stockOpnameDetailsPagination = mutableStateMapOf<String, StockOpnamePaginationState>()
    var stockOpnameDetailsPagination: Map<String, StockOpnamePaginationState> = _stockOpnameDetailsPagination

    // Existing property for details
    private val _stockOpnameDetails = mutableStateMapOf<String, List<DetailStockAssetItem>>()
    var stockOpnameDetails: Map<String, List<DetailStockAssetItem>> = _stockOpnameDetails

    // Add a flag to track if more data is loading
//    private val _isLoadingMore = mutableStateOf(false)
//    val isLoadingMore: State<Boolean> = _isLoadingMore
    private val _isLoadingMore = mutableStateOf(false)
    val isLoadingMore: androidx.compose.runtime.State<Boolean> = _isLoadingMore


    // Create a data class to track pagination state
    data class StockOpnamePaginationState(
        val currentPage: Int = 1,
        val itemsPerPage: Int = 10,
        val hasMoreItems: Boolean = true,
        val isLoadingMore: Boolean = false,
        val totalItems: Int = 0
    )

    // Update your fetch function to support pagination
    fun fetchStockOpnameItemDetail(context: Context, stockOpnameCode: String, loadMore: Boolean = false) {
        viewModelScope.launch {
            try {
                // Get current pagination state or create a new one
                val paginationState = _stockOpnameDetailsPagination[stockOpnameCode] ?: StockOpnamePaginationState()

                // If we're loading more and there are no more items, or we're already loading, return
                if (loadMore && (!paginationState.hasMoreItems || paginationState.isLoadingMore)) {
                    return@launch
                }

                // Determine which page to load
                val pageToLoad = if (loadMore) paginationState.currentPage + 1 else 1

                // Update loading state
                _stockOpnameDetailsPagination[stockOpnameCode] = paginationState.copy(isLoadingMore = true)
                if (loadMore) {
                    _isLoadingMore.value = true
                } else {
                    // Reset details if we're loading the first page
                    _stockOpnameDetails[stockOpnameCode] = emptyList()
                }

                val token = TokenDataStore.getToken(context) ?: ""
                val response = RetrofitClient.api.getDataStockOpnameMobileDetail(
                    token = "Bearer $token",
                    stockOpnameCode = stockOpnameCode,
                    page = pageToLoad.toString(),
                    limit = paginationState.itemsPerPage.toString()
                )

                println("tessss")
                println(response)
                if (response.isSuccessful && response.body()?.metadata?.code == 200) {
                    val newData = response.body()?.response?.data ?: emptyList()
                    val totalItems = response.body()?.response?.countFull ?: 0

                    println("dede")
                    println(newData)

                    // Append new data if loading more, otherwise replace
                    val currentData = if (loadMore) _stockOpnameDetails[stockOpnameCode] ?: emptyList() else emptyList()
                    val combinedData = currentData + newData

                    _stockOpnameDetails[stockOpnameCode] = combinedData

                    // Update pagination state
                    val hasMore = combinedData.size < totalItems
                    _stockOpnameDetailsPagination[stockOpnameCode] = paginationState.copy(
                        currentPage = pageToLoad,
                        hasMoreItems = hasMore,
                        isLoadingMore = false,
                        totalItems = totalItems
                    )
                } else {
                    errorMessageStockOpname = "Data Stock Opname Tidak tersedia"
                    if (!loadMore) {
                        _stockOpnameDetails[stockOpnameCode] = emptyList()
                    }
                    _stockOpnameDetailsPagination[stockOpnameCode] = paginationState.copy(
                        hasMoreItems = false,
                        isLoadingMore = false
                    )
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Parse error: ${e.localizedMessage}")
                _stockOpnameDetailsPagination[stockOpnameCode]?.let { state ->
                    _stockOpnameDetailsPagination[stockOpnameCode] = state.copy(isLoadingMore = false)
                }
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    // Add a function to check if we should load more data
    fun shouldLoadMoreItems(stockOpnameCode: String, currentItemIndex: Int): Boolean {
        val pagination = _stockOpnameDetailsPagination[stockOpnameCode] ?: return false
        val details = _stockOpnameDetails[stockOpnameCode] ?: return false

        // If we're near the end of the current list (e.g., last 3 items) and there are more items to load
        return pagination.hasMoreItems &&
                !pagination.isLoadingMore &&
                currentItemIndex >= details.size - 3
    }

    // Add a function to reset pagination state when expanding/collapsing
    fun resetPagination(stockOpnameCode: String) {
        _stockOpnameDetailsPagination[stockOpnameCode] = StockOpnamePaginationState()
    }

    // Update your toggle function to reset pagination
    fun toggleExpanded(stockOpnameCode: String) {
        if (currentExpandedCode == stockOpnameCode) {
            // Collapsing the currently expanded item
            currentExpandedCode = ""
        } else {
            // Expanding a new item, reset pagination for it
            currentExpandedCode = stockOpnameCode
            resetPagination(stockOpnameCode)
        }
    }


//    fun searchAssetByRfid(context: Context, rfid: String, rssi: String = "0",power: Int? = 0) {
//        viewModelScope.launch {
//            try {
//                val token = TokenDataStore.getToken(context) ?: ""
//                val response = RetrofitClient.api.getAssetByRfid(
//                    rfid = rfid,
//                    token = "Bearer $token"
//                )
//                println(rfid)
//                println("HEHEHEHEH")
//                println(response.body())
//
//                if (response.body()?.metadata?.code == 200) {
//                    // Get the new assets from the response
//                    val newAssets = response.body()?.response?.data!!
//                    val existingList = rfidScanResult.orEmpty().toMutableList()
//
//                    // Calculate distance if RSSI is provided
//                    if (rssi != "0") {
//                        val rssiValue = rssi.toDouble()
//                        val distanceInCm = calculateDistanceInCm(rssiValue)
//
//                        // Update each asset in the new assets with its distance
//                        newAssets.forEach { newAsset ->
//                            newAsset.jarak = distanceInCm.toString()
//                        }
//                    }
//
//                    // Check if assets already exist and update them, otherwise add new ones
//                    var assetsAdded = false
//                    for (newAsset in newAssets) {
//                        val existingIndex = existingList.indexOfFirst { it.no_register == newAsset.no_register }
//
//                        if (existingIndex != -1) {
//                            // Update existing asset with new RSSI value
//                            existingList[existingIndex] = newAsset
//                        } else {
//                            // Add new asset to the list
//                            existingList.add(newAsset)
//                            assetsAdded = true
//                        }
//                    }
//
//                    // Update the result list
//                    rfidScanResult = existingList
//                }
//            } catch (e: Exception) {
//                errorMessage = "Gagal mengambil detail aset: ${e.localizedMessage}"
//            }
//        }
//    }

//    fun searchAssetByRfid(context: Context, rfid: String, rssi: String = "0", power: Int? = 0) {
//        viewModelScope.launch {
//            try {
//                println("=== DEBUG START ===")
//                println("RFID: $rfid")
//                println("RSSI: $rssi")
//                println("Power: $power")
//
//                val token = TokenDataStore.getToken(context) ?: ""
//                val response = RetrofitClient.api.getAssetByRfid(
//                    rfid = rfid,
//                    token = "Bearer $token"
//                )
//
//                println("Response body: ${response.body()}")
//                println("Response code: ${response.body()?.metadata?.code}")
//
//                if (response.body()?.metadata?.code == 200) {
//                    // Get the new assets from the response
//                    val newAssets = response.body()?.response?.data!!
//                    println("New assets count: ${newAssets.size}")
//
//                    val existingList = rfidScanResult.orEmpty().toMutableList()
//                    println("Existing list size before: ${existingList.size}")
//
//                    // Calculate distance if RSSI is provided
//                    if (rssi != "0" && rssi.isNotBlank()) {
//                        try {
//                            val normalizedRssi = rssi.replace(",", ".")
//                            val rssiValue = normalizedRssi.toDouble()
//                            println("RSSI Value converted: $rssiValue")
//
//                            val distanceInCm = calculateDistanceInCm(rssiValue)
//                            println("Distance calculated: $distanceInCm")
//
//                            // Update each asset in the new assets with its distance
//                            newAssets.forEachIndexed { index, newAsset ->
//                                println("Before update - Asset $index jarak: ${newAsset.jarak}")
//                                newAsset.jarak = distanceInCm.toString()
//                                println("After update - Asset $index jarak: ${newAsset.jarak}")
//                            }
//                        } catch (e: NumberFormatException) {
//                            println("ERROR: Invalid RSSI format - $rssi")
//                            errorMessage = "Format RSSI tidak valid: $rssi"
//                            return@launch
//                        } catch (e: Exception) {
//                            println("ERROR: Distance calculation failed - ${e.message}")
//                            errorMessage = "Gagal menghitung jarak: ${e.localizedMessage}"
//                            return@launch
//                        }
//                    }
//
//                    // Check if assets already exist and update them, otherwise add new ones
//                    var assetsAdded = false
//                    for (newAsset in newAssets) {
//                        println("Processing asset: ${newAsset.no_register}")
//
//                        val existingIndex = existingList.indexOfFirst { it.no_register == newAsset.no_register }
//                        println("Existing index: $existingIndex")
//
//                        if (existingIndex != -1) {
//                            // Update existing asset with new RSSI value
//                            println("Updating existing asset at index $existingIndex")
//                            existingList[existingIndex] = newAsset
//                        } else {
//                            // Add new asset to the list
//                            println("Adding new asset: ${newAsset.no_register}")
//                            existingList.add(newAsset)
//                            assetsAdded = true
//                        }
//                    }
//
//                    println("Final list size: ${existingList.size}")
//                    println("Assets added: $assetsAdded")
//
//                    // Update the result list
//                    rfidScanResult = existingList
//
//                    println("rfidScanResult updated, size: ${rfidScanResult?.size}")
//                    println("=== DEBUG END ===")
//
//                } else {
//                    println("API Response code is not 200: ${response.body()?.metadata?.code}")
//                    errorMessage = "Response tidak valid: ${response.body()?.metadata?.message}"
//                }
//            } catch (e: Exception) {
//                println("EXCEPTION CAUGHT: ${e.message}")
//                println("Exception type: ${e.javaClass.simpleName}")
//                e.printStackTrace()
//                errorMessage = "Gagal mengambil detail aset: ${e.localizedMessage}"
//            }
//        }
//    }
    fun searchAssetByRfid(context: Context, rfid: String, rssi: String = "0", power: Int? = 0) {
        viewModelScope.launch {
            try {
                println("=== DEBUG START ===")
                println("RFID: $rfid")
                println("RSSI: $rssi")
                println("Power: $power")

                val token = TokenDataStore.getToken(context)
                if (token.isNullOrBlank()) {
                    println("ERROR: Token is null or empty")
                    errorMessage = "Token tidak valid"
                    return@launch
                }

                val response = RetrofitClient.api.getAssetByRfid(
                    rfid = rfid,
                    token = "Bearer $token"
                )

                println("Response body: ${response.body()}")

                val responseBody = response.body()
                if (responseBody == null) {
                    println("ERROR: Response body is null")
                    errorMessage = "Response body kosong"
                    return@launch
                }

                println("Response code: ${responseBody.metadata?.code}")

                if (responseBody.metadata?.code == 200) {
                    // Safely get the new assets from the response
                    val responseData = responseBody.response?.data
                    if (responseData == null) {
                        println("ERROR: Response data is null")
                        errorMessage = "Data response kosong"
                        return@launch
                    }

                    val newAssets = responseData.toMutableList()
                    println("New assets count: ${newAssets.size}")

                    val existingList = rfidScanResult?.toMutableList() ?: mutableListOf()
                    println("Existing list size before: ${existingList.size}")

                    // Calculate distance if RSSI is provided
                    if (rssi != "0" && rssi.isNotBlank()) {
                        try {
                            val normalizedRssi = rssi.replace(",", ".")
                            val rssiValue = normalizedRssi.toDouble()
                            println("RSSI Value converted: $rssiValue")

                            val distanceInCm = calculateDistanceInCm(rssiValue)
                            println("Distance calculated: $distanceInCm")

                            // Update each asset in the new assets with its distance
                            newAssets.forEachIndexed { index, newAsset ->
                                println("Before update - Asset $index jarak: ${newAsset?.jarak}")
                                newAsset?.jarak = distanceInCm.toString()
                                println("After update - Asset $index jarak: ${newAsset?.jarak}")
                            }
                        } catch (e: NumberFormatException) {
                            println("ERROR: Invalid RSSI format - $rssi")
                            errorMessage = "Format RSSI tidak valid: $rssi"
                            return@launch
                        } catch (e: Exception) {
                            println("ERROR: Distance calculation failed - ${e.message}")
                            errorMessage = "Gagal menghitung jarak: ${e.localizedMessage}"
                            return@launch
                        }
                    }

                    // Check if assets already exist and update them, otherwise add new ones
                    var assetsAdded = false
                    for (newAsset in newAssets) {
                        if (newAsset == null) {
                            println("WARNING: Skipping null asset")
                            continue
                        }

                        println("Processing asset: ${newAsset.no_register}")

                        val existingIndex = existingList.indexOfFirst {
                            it?.no_register == newAsset.no_register
                        }
                        println("Existing index: $existingIndex")

                        if (existingIndex != -1) {
                            // Update existing asset with new RSSI value
                            println("Updating existing asset at index $existingIndex")
                            existingList[existingIndex] = newAsset
                        } else {
                            // Add new asset to the list
                            println("Adding new asset: ${newAsset.no_register}")
                            existingList.add(newAsset)
                            assetsAdded = true
                        }
                    }

                    println("Final list size: ${existingList.size}")
                    println("Assets added: $assetsAdded")

                    // Update the result list
                    rfidScanResult = existingList

                    println("rfidScanResult updated, size: ${rfidScanResult?.size}")
                    println("=== DEBUG END ===")

                } else {
                    val errorCode = responseBody.metadata?.code ?: "Unknown"
                    val errorMsg = responseBody.metadata?.message ?: "Unknown error"
                    println("API Response code is not 200: $errorCode")
                    errorMessage = "Response tidak valid: $errorMsg"
                }
            } catch (e: Exception) {
                println("EXCEPTION CAUGHT: ${e.message}")
                println("Exception type: ${e.javaClass.simpleName}")
                e.printStackTrace()
                errorMessage = "Gagal mengambil detail aset: ${e.localizedMessage ?: e.message}"
            }
        }
    }

    /**
     * Menghitung jarak dalam sentimeter berdasarkan nilai RSSI
     * Menggunakan Log-Distance Path Loss Model
     */
//    private fun calculateDistanceInCm(rssi: Double): Double {
//        // Parameter-parameter model RFID
//        val txPower = -59.0 // Power transmisi pada jarak 1 meter (dBm), nilai ini perlu dikalibrasi
//        val pathLossExponent = 2.0 // Eksponen path loss, biasanya 2.0 untuk ruang terbuka
//
//        // Menghitung jarak dalam meter menggunakan Log-Distance Path Loss Model
//        val distanceInMeter = Math.pow(10.0, (txPower - rssi) / (10 * pathLossExponent))
//
//        // Konversi meter ke sentimeter
//        return distanceInMeter * 100.0
//    }
    private fun calculateDistanceInCm(rssi: Double, rssiRef: Double = -66.0, pathLossExponent: Double = 2.0): String {
        val distanceInMeter = Math.pow(10.0, (rssiRef - rssi) / (10 * pathLossExponent))
        val distanceInCm = distanceInMeter * 100.0
        return String.format("%.1f", distanceInCm)
    }

    fun clearRfidScanResult(){
        rfidScanResult = emptyList()
    }

    fun searchAssetByName(context: Context, query: String,onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            try {
                isLoading = true
                val token = TokenDataStore.getToken(context) ?: ""
                val response = RetrofitClient.api.getAssetsByName(
                    assetName = query,
                    token = "Bearer $token"
                )

                if (response.body()?.metadata?.code == 200) {
                    val newMap = assetDetailsMap.toMutableMap()
                    newMap[query] = response.body()?.response?.data ?: emptyList()
                    searchResults.value = response.body()?.response?.data ?: emptyList()
                    assetDetailsMap = newMap
                    totalAsset = response.body()?.response?.data?.size ?: totalAsset
                }else{
                    clearSearchResults()
                    val newMap = assetDetailsMap.toMutableMap()
                    newMap[query] =  emptyList()
                    searchResults.value = emptyList()
                    assetDetailsMap = newMap
                }
            } catch (e: Exception) {
                errorMessage = "Gagal mengambil detail aset: ${e.localizedMessage}"
            } finally {
                isLoading = false

//                isSearching = false
                onComplete?.invoke()
            }
        }
    }

    fun clearSearchResults() {
        searchResults.value = emptyList()
        totalAsset = historyTotalAsset
    }


    fun markAssetAsFound(qrCode: String, locationId: String) {
        val currentList = assetDetailsMap[locationId] ?: return
        val updatedList = currentList.map { asset ->
            if (asset.no_register == qrCode) {
                asset.copy(isFound = true)
            } else {
                asset
            }
        }
        assetDetailsMap = assetDetailsMap.toMutableMap().apply {
            put(locationId, updatedList)
        }
    }


    fun updateRfid(context: Context,rfid:String,asset: AssetDetail,onComplete: (() -> Unit)?){
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        viewModelScope.launch {

            try {
                val token = TokenDataStore.getToken(context) ?: ""
                val response = RetrofitClient.api.setReplaceTagProcess(SetReplaceTagProcessRequest(
                    old_rfid = asset.rfid.toString(),
                    new_rfid = rfid,
                    kd_barang = asset.kd_barang,
                    no_register = asset.no_register,
                    location_id = asset.location_id,
                    tgl_register = today,
                    dept_id = "0",
                    replace_notes = "Ganti Tag Lama"
                ),token = "Bearer $token")
                println("SAVE RESPONSE")
                println(response.errorBody())
                println(response.code())

                if (response.isSuccessful) {
                    resultReplaceRfid = response.body()
                    val message = response.body()?.metadata?.message ?: "Berhasil"
                    fetchAssetDetailsByCategory(
                        context,
                        asset.kd_kel_barang,true
                    )
//                    fetchGroups(context)
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                } else {
                    val errorBodyString = response.errorBody()?.string() // Hanya panggil sekali!
                    try {
                        val jsonObject = JSONObject(errorBodyString ?: "")
                        errorMessage = jsonObject.getJSONObject("metadata").getString("message")
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        Log.e("API_ERROR", "JSON error: ${jsonObject.toString(4)}") // pretty print
                    } catch (e: Exception) {
                        Log.e("API_ERROR", "Parse error: ${e.localizedMessage}")
                        Log.e("API_ERROR", "Raw error body: $errorBodyString")
                    }
                }

            } catch (e: Exception) {
                errorMessage = "Error: ${e.localizedMessage}"
            }finally {
                onComplete?.invoke()
            }
        }
    }

    fun saveRfid(context: Context,rfid:String,asset: AssetDetail,onComplete: (() -> Unit)?){
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        viewModelScope.launch {

            try {
                val token = TokenDataStore.getToken(context) ?: ""
                val response = RetrofitClient.api.setRegisterProcess(SetRegisterProcessRequest(
                    rfid = rfid,
                    kd_barang = asset.kd_barang,
                    no_register = asset.no_register,
                    location_id = asset.location_id,
                    tgl_register = today,
                    dept_id = "0"
                ),token = "Bearer $token")
                if (response.isSuccessful) {
                    resultSimpanRfid = response.body()
                    val message = response.body()?.metadata?.message ?: "Berhasil"
                    fetchAssetDetailsByCategory(
                        context,
                        asset.kd_kel_barang,true
                    )
//                    fetchGroups(context)
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                } else {
                    val errorBodyString = response.errorBody()?.string() // Hanya panggil sekali!
                    try {
                        val jsonObject = JSONObject(errorBodyString ?: "")
                        errorMessage = jsonObject.getJSONObject("metadata").getString("message")
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        Log.e("API_ERROR", "JSON error: ${jsonObject.toString(4)}") // pretty print
                    } catch (e: Exception) {
                        Log.e("API_ERROR", "Parse error: ${e.localizedMessage}")
                        Log.e("API_ERROR", "Raw error body: $errorBodyString")
                    }
                }

            } catch (e: Exception) {
                errorMessage = "Error: ${e.localizedMessage}"
            }finally {
                onComplete?.invoke()
            }
        }
    }

    fun insertNewGroup(context: Context, groupCode: String, groupName: String){
        viewModelScope.launch {

            try {
                val token = TokenDataStore.getToken(context) ?: ""
                val response = RetrofitClient.api.setStockOpnameGroup(SetGroupRequest(
                    stock_opname_group_code = groupCode,
                    stock_opname_group_name = groupName,
                ),token = "Bearer $token")
                println("SAVE Group")
                println(response.errorBody())
                println(response.code())

                if (response.isSuccessful) {
                    resultSimpanGroup = response.body()
                    val message = response.body()?.metadata?.message ?: "Berhasil"
                    fetchGroups(context)
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                } else {
                    val errorBodyString = response.errorBody()?.string() // Hanya panggil sekali!
                    try {
                        val jsonObject = JSONObject(errorBodyString ?: "")
                        errorMessage = jsonObject.getJSONObject("metadata").getString("message")
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        Log.e("API_ERROR", "JSON error: ${jsonObject.toString(4)}") // pretty print
                    } catch (e: Exception) {
                        Log.e("API_ERROR", "Parse error: ${e.localizedMessage}")
                        Log.e("API_ERROR", "Raw error body: $errorBodyString")
                    }
                }

            } catch (e: Exception) {
                errorMessage = "Error: ${e.localizedMessage}"
            }
        }
    }

    fun fetchStockOpnameItems(context: Context, locationId: String? = null,groupId: String? = null,dateStart: String? = null,dateEnd: String? = null) {
        viewModelScope.launch {
            isLoading = true
            try {
                val token = TokenDataStore.getToken(context) ?: ""


                println(locationId)
                val response = RetrofitClient.api.getDataStockOpnameMobile(
                    token = "Bearer $token",
                    locationId = locationId,
                    groupId = groupId,
                    dateStart =  if (dateStart != "Select Date") dateStart else null,
                    dateEnd = if (dateEnd != "Select Date") dateEnd else null
                )

                if (response.isSuccessful && response.body()?.metadata?.code == 200) {
                    val data = response.body()?.response?.data ?: emptyList()
                    stockOpnameItems = data
                    if(stockOpnameItems == emptyList<StockOpnameItem>()){
                        errorMessageStockOpname = "Data Stock Opname Tidak tersedia"
                    }
                } else {
                    errorMessageStockOpname = "Data Stock Opname Tidak tersedia"
                    stockOpnameItems = emptyList()
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Parse error: ${e.localizedMessage}")
            } finally {
                isLoading = false
            }
        }
    }


    var currentExpandedCode by mutableStateOf<String?>(null)

//    fun toggleExpanded(itemCode: String) {
//        currentExpandedCode = if (currentExpandedCode == itemCode) null else itemCode
//    }

    var currentPage by mutableStateOf(1)
    var totalPages by mutableStateOf(1)
    var isLoadMore by mutableStateOf(false)


    fun fetchStockOpnameItemDetail(context: Context, stockOpnameCode: String) {
        viewModelScope.launch {
//            isLoading = true
            try {
                val token = TokenDataStore.getToken(context) ?: ""
                val response = RetrofitClient.api.getDataStockOpnameMobileDetail(
                    token = "Bearer $token",
                    stockOpnameCode = stockOpnameCode
                )

                if (response.isSuccessful && response.body()?.metadata?.code == 200) {
                    val data = response.body()?.response?.data ?: emptyList()
                    _stockOpnameDetails[stockOpnameCode] = data
//                    if(stockOpnameItems == emptyList<StockOpnameItem>()){
//                        errorMessageStockOpname = "Data Stock Opname Tidak tersedia"
//                    }
                } else {
                    errorMessageStockOpname = "Data Stock Opname Tidak tersedia"
                    _stockOpnameDetails[stockOpnameCode] = emptyList()
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Parse error: ${e.localizedMessage}")
            } finally {
//                isLoading = false
            }
        }
    }


    fun fetchGroups(context: Context) {
        viewModelScope.launch {
            isLoading = true
            try {
                val token = TokenDataStore.getToken(context) ?: ""
                val response = RetrofitClient.api.getStockOpnameGroup("1","1000","Bearer $token")
                if (response.isSuccessful && response.body()?.metadata?.code == 200) {
                    val data = response.body()?.response?.data ?: emptyList()
//                    groups =listOf(StockOpnameGroup(
//                        stock_opname_group_code = "",
//                        stock_opname_group_name = "All Group",
//                    )) + data
                    groups = data
                } else {
                    errorMessage = "Gagal mendapatkan data Group Stock Opname"
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Parse error: ${e.localizedMessage}")
            } finally {
                println("JHEHE KELOAD")
                isLoading = false
            }
        }
    }

    fun selectGroup(group: StockOpnameGroup) {
        selectedGroup = group
    }




    private fun getKondisiId(kondisi: String): String {
        return when (kondisi) {
            "BAIK" -> "1"
            "SEDANG" -> "2"
            "BURUK" -> "3"
            else -> "1" // default jika tidak cocok
        }
    }

    fun saveInspection(context: Context, asset: AssetDetail?, condition: String, keterangan: String, isMaintenance: Boolean = false) {
//        val today = Instant.now().toString()
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault() // WIB, agar jam sesuai HP lokal
        val today = sdf.format(Date())
//        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        println("today: $today")

        viewModelScope.launch {

            try {
                val token = TokenDataStore.getToken(context) ?: ""
                var setinspection = SetInspectionProcessRequest(
                    tgl_inspeksi = today,
                    no_register = asset!!.no_register,
                    id_kondisi = condition,
                    ket_inspeksi =keterangan,
                    is_maintenance = isMaintenance
                )
                println(setinspection)
//
                val response = RetrofitClient.api.setInspectionProcess(setinspection,token = "Bearer $token", isMaintenance = isMaintenance)
                println("SAVE INSPEKSI")
                println(response.errorBody())
                println(response.code())

                if (response.isSuccessful) {
                    resultSimpanInspeksi = response.body()
                    val message = response.body()?.metadata?.message ?: "Berhasil"
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                } else {
                    val errorBodyString = response.errorBody()?.string() // Hanya panggil sekali!
                    try {
                        val jsonObject = JSONObject(errorBodyString ?: "")
                        errorMessage = jsonObject.getJSONObject("metadata").getString("message")
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        Log.e("API_ERROR", "JSON error: ${jsonObject.toString(4)}") // pretty print
                    } catch (e: Exception) {
                        Log.e("API_ERROR", "Parse error: ${e.localizedMessage}")
                        Log.e("API_ERROR", "Raw error body: $errorBodyString")
                    }
                }

            } catch (e: Exception) {
                errorMessage = "Error: ${e.localizedMessage}"
                errorMessage = "Error: ${e.localizedMessage}"
            }
        }
    }

    fun selectLocation(context: Context, location: LocationItem) {
        selectedLocation = location
        viewModelScope.launch {
            TokenDataStore.saveSelectedLocation(context, JSONObject().apply {
                put("location_id", location.location_id)
                put("location", location.location)
                put("full_location", location.full_location)
            })
        }
    }

    fun fetchAllLocationList(context : Context) {
        errorMessage = null
        if(allLocationList.isEmpty()){
            viewModelScope.launch {
                try {
                    val token = TokenDataStore.getToken(context) ?: ""
                    val response = RetrofitClient.api.getLocation("Bearer $token")

                    if (response.isSuccessful && response.body()?.metadata?.code == 200) {
                        val data = response.body()?.response?.data ?: emptyList()
                        allLocationList = data
                    } else {
                        errorMessage = "Gagal mendapatkan data Location"
                    }

                } catch (e: HttpException) {
                    errorMessage = "HTTP error: ${e.code()}"
                } catch (e: IOException) {
                    errorMessage = "Network error: ${e.localizedMessage}"
                } catch (e: Exception) {
                    errorMessage = "Unexpected error: ${e.localizedMessage}"
                } finally {
                    isLoading = false
                }
            }
        }
    }

    fun fetchAllDepartmentList(context : Context) {
        errorMessage = null
        if(allDepartmentList.isEmpty()){
            viewModelScope.launch {
                try {
                    val token = TokenDataStore.getToken(context) ?: ""
                    val response = RetrofitClient.api.getListDepartment("Bearer $token")

                    if (response.isSuccessful && response.body()?.metadata?.code == 200) {
                        val data = response.body()?.response?.data ?: emptyList()
                        allDepartmentList = data
                    } else {
                        errorMessage = "Gagal mendapatkan data Department"
                    }

                } catch (e: HttpException) {
                    errorMessage = "HTTP error: ${e.code()}"
                } catch (e: IOException) {
                    errorMessage = "Network error: ${e.localizedMessage}"
                } catch (e: Exception) {
                    errorMessage = "Unexpected error: ${e.localizedMessage}"
                } finally {
                    isLoading = false
                }
            }
        }
    }

    fun fetchRootLocations(context : Context) {
        viewModelScope.launch {
            try {
                val token = TokenDataStore.getToken(context) ?: ""
                val response = RetrofitClient.api.getLocation("Bearer $token")

                if (response.isSuccessful && response.body()?.metadata?.code == 200) {
                    val data = response.body()?.response?.data ?: emptyList()
                    val rootLocations = data.filter { it.parent == null }
                    locationList = rootLocations
                } else {
                    errorMessage = "Gagal mendapatkan data Location"
                }

            } catch (e: HttpException) {
                errorMessage = "HTTP error: ${e.code()}"
            } catch (e: IOException) {
                errorMessage = "Network error: ${e.localizedMessage}"
            } catch (e: Exception) {
                errorMessage = "Unexpected error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchCategoryAsset(context: Context) {
        if(categories.isEmpty()){
            viewModelScope.launch {
                isLoading = true
                errorMessage = null
                try {
                    val token = TokenDataStore.getToken(context) ?: ""
                    val response = RetrofitClient.api.getCategoryAsset("Bearer $token")
                    println(response.code())
                    println(response.errorBody())

                    if (response.isSuccessful && response.body()?.metadata?.code == 200) {
                        val data = response.body()?.response?.data ?: emptyList()
                        categories = data
                        totalAsset = data.sumOf { it.total_aset }
                        historyTotalAsset = totalAsset
                    } else {
                        errorMessage = "Gagal mendapatkan data kategori"
                    }

                } catch (e: HttpException) {
                    errorMessage = "HTTP error: ${e.code()}"
                } catch (e: IOException) {
                    errorMessage = "Network error: ${e.localizedMessage}"
                } catch (e: Exception) {
                    errorMessage = "Unexpected error: ${e.localizedMessage}"
                } finally {
                    isLoading = false
                }
            }
        }
    }

    val loadingGetImage = mutableStateMapOf<String,Boolean>()
    var assetFullDetailsMap by mutableStateOf<Map<String, List<AssetFullDetail>>>(emptyMap())

    fun getImageAsset(context:Context,noRegister: String){
        viewModelScope.launch {
            try {
                loadingGetImage[noRegister] = true
                val token = TokenDataStore.getToken(context) ?: ""
                val response = RetrofitClient.api.getImageAsset(
                    no_register = noRegister,
                    token = "Bearer $token"
                )

                if (response.body()?.metadata?.code == 200) {
                    val newMap = assetFullDetailsMap.toMutableMap()

                    newMap[noRegister] = response.body()?.response?.data ?: emptyList()
                    assetFullDetailsMap = newMap
                }
            } catch (e: Exception) {
                errorMessage = "Gagal mengambil detail aset: ${e.localizedMessage}"
            }
            finally {
                loadingGetImage[noRegister] = false
            }
        }
    }

    var loadingCategoryMap = mutableStateMapOf<String, Boolean>()



    // Method to set asset detail
//    fun setAssetDetail(asset: AssetDetail) {
//        assetDetail = asset
//    }


    fun fetchAssetDetailsByLocation(context: Context, location_id: String) {
        viewModelScope.launch {
            try {
                val token = TokenDataStore.getToken(context) ?: ""
                val response = RetrofitClient.api.getAssetsByLocation(
                    locationId = location_id,
                    token = "Bearer $token"
                )

                if (response.body()?.metadata?.code == 200) {
                    val newMap = assetDetailsMap.toMutableMap()
                    newMap[location_id] = response.body()?.response?.data ?: emptyList()
                    assetDetailsMap = newMap
                }
            } catch (e: Exception) {
                errorMessage = "Gagal mengambil detail aset: ${e.localizedMessage}"
            }
        }
    }


    fun insertMutation(context: Context, location: LocationItem,department: DepartmentItem, newUser: String, description: String, asset: List<AssetDetail>) {
        viewModelScope.launch {
            try {
                val token = TokenDataStore.getToken(context) ?: ""
                val assetRequest = asset.map { i ->
                    MoveAssetDataRequest(
                        asset_id = i.kd_barang,
                        asset_name = i.nama_barang,
                        no_register = i.no_register,
                        location = location.location,
                        old_location_id = i.location_id,
                        old_location = i.location,
                        dept = department.dept_name,
                        old_dept_id = i.dept_id.toString(),
                        old_dept = i.dept_name.toString(),
                        asset_holder = newUser
                    )
                }

                val relocationRequest = RelocationRequest(
                    tgl_keluar = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                    keterangan = description,
                    location_id = location.location_id,
                    dept_id = department.dept_id.toString(),
                    data_asset = assetRequest
                )
                print(relocationRequest)
                val response = RetrofitClient.api.setMutation(relocationRequest, token = "Bearer $token")
                print(response.body())
                if (response.isSuccessful) {
                    resultSimpanMutation = response.body()
                    val message = response.body()?.metadata?.message ?: "Berhasil"

                    // Update each asset in the list to have the new location data
                    asset.forEach { updatedAsset ->
                        // Create updated asset with new location information
                        val updatedAssetWithNewLocation = updatedAsset.copy(
                            location = location.location,
                            location_id = location.location_id,
                            asset_holder = newUser,
                            dept_id = department.dept_id.toString(),
                            dept_name = department.dept_name,
                        )

                        val key = updatedAsset.kd_kel_barang

                        // Update in assetDetailsMap
                        val newList = assetDetailsMap[key]?.map {
                            if (it.no_register == updatedAsset.no_register) updatedAssetWithNewLocation else it
                        } ?: listOf(updatedAssetWithNewLocation)

                        assetDetailsMap = assetDetailsMap.toMutableMap().apply {
                            this[key] = newList
                        }

                        // Update in rfidScanResult
                        rfidScanResult = rfidScanResult?.map {
                            if (it.no_register == updatedAsset.no_register) updatedAssetWithNewLocation else it
                        }
                    }

                    // Trigger a refresh of the details for the category
                    fetchAssetDetailsByCategory(context, asset.firstOrNull()?.kd_kel_barang ?: "", true)

                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                } else {
                    val errorBodyString = response.errorBody()?.string()
                    try {
                        val jsonObject = JSONObject(errorBodyString ?: "")
                        errorMessage = jsonObject.getJSONObject("metaData").getString("message")
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        Log.e("API_ERROR", "JSON error: ${jsonObject.toString(4)}")
                    } catch (e: Exception) {
                        Log.e("API_ERROR", "Parse error: ${e.localizedMessage}")
                        Log.e("API_ERROR", "Raw error body: $errorBodyString")
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.localizedMessage}"
            }
        }
    }

    fun insertRelocation(context: Context, location: LocationItem, department: DepartmentItem, newUser: String, description: String, asset: List<AssetDetail>) {
        viewModelScope.launch {
            try {
                val token = TokenDataStore.getToken(context) ?: ""
                val assetRequest = asset.map { i ->
                    MoveAssetDataRequest(
                        asset_id = i.kd_barang,
                        asset_name = i.nama_barang,
                        no_register = i.no_register,
                        location = location.location,
                        old_location_id = i.location_id,
                        old_location = i.location,
                        dept = department.dept_name,
                        old_dept_id = i.dept_id.toString(),
                        old_dept = i.dept_name.toString(),
                        asset_holder = newUser,
                    )
                }

                val relocationRequest = RelocationRequest(
                    tgl_keluar = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                    keterangan = description,
                    location_id = location.location_id,
                    dept_id = department.dept_id.toString(),
                    data_asset = assetRequest
                )
                Log.d("cek body", "body = ${relocationRequest}")
                val response = RetrofitClient.api.setRelocation(relocationRequest, token = "Bearer $token")
                if (response.isSuccessful) {
                    resultSimpanMutation = response.body()
                    val message = response.body()?.metadata?.message ?: "Berhasil"

                    // Update each asset in the list to have the new location data
                    asset.forEach { updatedAsset ->
                        // Create updated asset with new location information
                        val updatedAssetWithNewLocation = updatedAsset.copy(
                            location = location.location,
                            location_id = location.location_id,
                            asset_holder = newUser,
                            dept_id = department.dept_id.toString(),
                            dept_name = department.dept_name
                        )

                        val key = updatedAsset.kd_kel_barang

                        // Update in assetDetailsMap
                        val newList = assetDetailsMap[key]?.map {
                            if (it.no_register == updatedAsset.no_register) updatedAssetWithNewLocation else it
                        } ?: listOf(updatedAssetWithNewLocation)

                        assetDetailsMap = assetDetailsMap.toMutableMap().apply {
                            this[key] = newList
                        }

                        // Update in rfidScanResult
                        rfidScanResult = rfidScanResult?.map {
                            if (it.no_register == updatedAsset.no_register) updatedAssetWithNewLocation else it
                        }
                    }

                    // Trigger a refresh of the details for the category
                    fetchAssetDetailsByCategory(context, asset.firstOrNull()?.kd_kel_barang ?: "", true)

                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                } else {
                    val errorBodyString = response.errorBody()?.string()
                    try {
                        val jsonObject = JSONObject(errorBodyString ?: "")
                        errorMessage = jsonObject.getJSONObject("metaData").getString("message")
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        Log.e("API_ERROR", "JSON error: ${jsonObject.toString(4)}")
                    } catch (e: Exception) {
                        Log.e("API_ERROR", "Parse error: ${e.localizedMessage}")
                        Log.e("API_ERROR", "Raw error body: $errorBodyString")
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.localizedMessage}"
            }
        }
    }

    fun insertStockOpname(context: Context, groupCode: String, locationId: String,description: String,asset:  List<StockOpnameRequestItem>,data_foreign: List<StockOpnameForeignItem>){
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault() // WIB, agar jam sesuai HP lokal
        val today = sdf.format(Date())
//        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        println("today stockopname: $today")
        viewModelScope.launch {

            try {
                val token = TokenDataStore.getToken(context) ?: ""

                val stockOpnameRequest = StockOpnameRequest(
                    stock_opname_group_code=groupCode,
                    stock_opname_code= "",
                    stock_opname_date= today,
                    location_id= locationId,
                    stock_opname_description="Stock Opname pada tanggal " + SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                    created_at= today,
                    data=asset,
                    data_foreign=data_foreign
                )
                Log.d("Stock Opname Request",stockOpnameRequest.toString())

                val response = RetrofitClient.api.setStockOpname(stockOpnameRequest,token = "Bearer $token")
                if (response.isSuccessful) {
                    resultSimpanStockOpname = response.body()

                    Log.d("Stock Opname Response", response.body().toString())
                    val message = response.body()?.metadata?.message ?: "Berhasil"
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                } else {
                    val errorBodyString = response.errorBody()?.string() // Hanya panggil sekali!
                    try {
                        val jsonObject = JSONObject(errorBodyString ?: "")
                        errorMessage = jsonObject.getJSONObject("metaData").getString("message")
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        Log.e("API_ERROR", "JSON error: ${jsonObject.toString(4)}") // pretty print
                    } catch (e: Exception) {

                        try {
                            val json = JSONObject(errorBodyString)
                            val metadata = json.optJSONObject("metadata")
                            println(metadata)
                            var errorMsg = "Terjadi kesalahan"
                            errorMsg = metadata?.optString("message", errorMsg) ?: errorMsg
//                            AlertDialog.Builder(context)
//                                .setTitle("Gagal Menyimpan")
//                                .setMessage(errorMsg)
//                                .setPositiveButton("OK", null)
//                                .show()
                            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                        } catch (jsonError: Exception) {
                            Log.e("API_ERROR", "Gagal parsing JSON error: ${jsonError.localizedMessage}")
                        }

                        Log.e("API_ERROR", "Parse error: ${e.localizedMessage}")
                        Log.e("API_ERROR", "Raw error body: $errorBodyString")
                    }
                }

            } catch (e: Exception) {
                errorMessage = "Error: ${e.localizedMessage}"
            }
        }
    }

    val isLoadingSaveCorrective = mutableStateOf<Boolean>(false)
    fun saveCorrectiveMaintenance(
        context: Context,
        assetDetail: AssetDetail,
        priority: String,
        condition: String,
        description: String,
        imageUri: Uri,
        onComplete: (() -> Unit)?
    ) {
        viewModelScope.launch {
            isLoadingSaveCorrective.value = true
            try {
                val token = TokenDataStore.getToken(context) ?: ""

                // Convert URI to File
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val file = File(context.cacheDir, "maintenance_image_${System.currentTimeMillis()}.jpg")
                file.outputStream().use { outputStream ->
                    inputStream?.copyTo(outputStream)
                }

                // Create JSON data string
                val jsonData = """
                {
                    "maintenance_id": "",
                    "no_register": "${assetDetail.no_register}",
                    "priority": "$priority",
                    "condition": "$condition",
                    "description": "$description",
                    "type_request": "technical",
                    "status_maintenance": "awaiting_confirmation",
                    "request_date": "${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}"
                }
            """.trimIndent()

                // Create RequestBody for JSON data
                val jsonRequestBody = jsonData.toRequestBody("text/plain".toMediaType())

                // Create RequestBody for image file
                val imageRequestBody = file.asRequestBody("application/octet-stream".toMediaType())
                val imagePart = MultipartBody.Part.createFormData("data_img", file.name, imageRequestBody)

                println(jsonData)

                val response = RetrofitClient.api.setCorrectiveMaintenance(
                    dataJson = jsonRequestBody,
                    dataImg = imagePart,
                    token = "Bearer $token"
                )

                Log.d("MaintenanceAPI", "Request prepared:")
                Log.d("MaintenanceAPI", "- JSON data: $jsonData")
                Log.d("MaintenanceAPI", "- Image part name: ${file.name}")
                Log.d("MaintenanceAPI", "- Image part size: ${file.length()} bytes")
                Log.d("MaintenanceAPI", "- Token: Bearer ${token.take(10)}...")

                if (response.isSuccessful) {
                    val result = response.body()
                    println(result.toString())

                    val message = result?.metadata?.message ?: "Maintenance request saved successfully"
//                    withContext(Dispatchers.Main) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
//                    }
                } else {
                    val errorBodyString = response.errorBody()?.string()
                    try {
                        val jsonObject = JSONObject(errorBodyString ?: "")
                        val errorMessage = jsonObject.getJSONObject("metaData").getString("message")
//                        withContext(Dispatchers.Main) {
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
//                        }
                        Log.e("API_ERROR", "JSON error: ${jsonObject.toString(4)}")
                    } catch (e: Exception) {
//                        withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to save maintenance request", Toast.LENGTH_SHORT).show()
//                        }
                        Log.e("API_ERROR", "Parse error: ${e.localizedMessage}")
                        Log.e("API_ERROR", "Raw error body: $errorBodyString")
                    }
                }

                // Clean up temporary file
                file.delete()

            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
//                }
                Log.e("Corrective Maintenance", "Exception: ${e.localizedMessage}")
            }
            finally {
                isLoadingSaveCorrective.value = false

                onComplete?.invoke() ?: ""
            }
        }
    }

    var allLogMaintenanceList by mutableStateOf<List<LogMaintenanceItem>>(emptyList())
        private set

    fun fetchLogMaintenance(context: Context, no_register: String) {
        viewModelScope.launch {
            try {
                isLoading = true
                val token = TokenDataStore.getToken(context) ?: ""
                val response = RetrofitClient.api.getRptLogMaintenance(
                    no_register = no_register,
                    token = "Bearer $token"
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.metadata?.code == 200) {
                        val data = body.response?.data ?: emptyList()
                        println("Data berhasil diambil: $data")
                        allLogMaintenanceList = data
                    } else {
                        println("Metadata code bukan 200: ${body?.metadata?.code}")
                        errorMessage = "Response code tidak valid"
                    }
                } else {
                    val errMsg = response.errorBody()?.string()
                    println("Error body: $errMsg")
                    errorMessage = "Server error: $errMsg"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("Response gagal di-parse: ${e.message}")
                errorMessage = "Gagal mengambil log moving asset: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    var allLogMovingAssetList by mutableStateOf<List<LogMovingAssetItem>>(emptyList())
        private set

    fun fetchLogMovingAsset(context: Context, no_register: String) {
        viewModelScope.launch {
            try {
                println("cekk masukk")
                println("No Register: $no_register")
                isLoading = true
                val token = TokenDataStore.getToken(context) ?: ""
                val response = RetrofitClient.api.getRptLogMovingAsset(
                    no_register = no_register,
                    token = "Bearer $token"
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.metadata?.code == 200) {
                        val data = body.response?.data ?: emptyList()
                        println("Data berhasil diambil: $data")
                        allLogMovingAssetList = data
                    } else {
                        println("Metadata code bukan 200: ${body?.metadata?.code}")
                        errorMessage = "Response code tidak valid"
                    }
                } else {
                    val errMsg = response.errorBody()?.string()
                    println("Error body: $errMsg")
                    errorMessage = "Server error: $errMsg"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("Response gagal di-parse: ${e.message}")
                errorMessage = "Gagal mengambil log moving asset: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

}
