//package id.aseta.app.viewmodel
//
//import android.content.Context
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateMapOf
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.setValue
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import dagger.hilt.android.lifecycle.HiltViewModel
//import id.aseta.app.data.model.AssetCategory
//import id.aseta.app.data.model.AssetDetail
//import id.aseta.app.data.model.AssetFullDetail
//import id.aseta.app.data.source.local.TokenDataStore
//import kotlinx.coroutines.launch
//import okio.IOException
//import retrofit2.HttpException
//import javax.inject.Inject
//
///**
// * ViewModel for Asset viewer functionality migrated from AssetCategoryViewModel
// * Focuses only on the features needed for viewing and managing asset details
// */
//@HiltViewModel
//class AssetViewerViewModel @Inject constructor(
//) : ViewModel() {
//
//
//    val searchResults = mutableStateOf<List<AssetDetail>>(emptyList())
//    var totalAsset by mutableStateOf(0)
//    var historyTotalAsset by mutableStateOf<Int>(0)
//    var categories by mutableStateOf<List<AssetCategory>>(emptyList())
//        private set
//    var expandedCategory by mutableStateOf<String?>(null)
//    var assetDetail by   mutableStateOf<AssetDetail?>(null)
//    var loadingCategoryMap = mutableStateMapOf<String, Boolean>()
//    var assetFullDetailsMap by mutableStateOf<Map<String, List<AssetFullDetail>>>(emptyMap())
//    val loadingGetImage = mutableStateMapOf<String,Boolean>()
//
//
//
//    var isLoading by mutableStateOf(false)
//        private set
//
//    var errorMessage by mutableStateOf<String?>(null)
//        private set
//
//    var assetDetailsMap by mutableStateOf<Map<String, List<AssetDetail>>>(emptyMap())
//
//    fun clearSearchResults() {
//        searchResults.value = emptyList()
//        totalAsset = historyTotalAsset
//    }
//
//
//    // func : Search Asset By Name
//    fun searchAssetByName(context: Context, query: String,onComplete: (() -> Unit)? = null) {
//        viewModelScope.launch {
//            try {
//                isLoading = true
//                val token = TokenDataStore.getToken(context) ?: ""
//                val response = RetrofitClient.api.getAssetsByName(
//                    assetName = query,
//                    token = "Bearer $token"
//                )
//
//                if (response.body()?.metadata?.code == 200) {
//                    val newMap = assetDetailsMap.toMutableMap()
//                    newMap[query] = response.body()?.response?.data ?: emptyList()
//                    searchResults.value = response.body()?.response?.data ?: emptyList()
//                    assetDetailsMap = newMap
//                    totalAsset = response.body()?.response?.data?.size ?: totalAsset
//                }else{
//                    clearSearchResults()
//                    val newMap = assetDetailsMap.toMutableMap()
//                    newMap[query] =  emptyList()
//                    searchResults.value = emptyList()
//                    assetDetailsMap = newMap
//                }
//            } catch (e: Exception) {
//                errorMessage = "Failed to load Detail Asset: ${e.localizedMessage}"
//            } finally {
//                isLoading = false
//                onComplete?.invoke()
//            }
//        }
//    }
//
//    fun fetchCategoryAsset(context: Context) {
//        if(categories.isEmpty()){
//            viewModelScope.launch {
//                isLoading = true
//                errorMessage = null
//                try {
//                    val token = TokenDataStore.getToken(context) ?: ""
//                    val response = RetrofitClient.api.getCategoryAsset("Bearer $token")
//                    println(response.code())
//                    println(response.errorBody())
//
//                    if (response.isSuccessful && response.body()?.metadata?.code == 200) {
//                        val data = response.body()?.response?.data ?: emptyList()
//                        categories = data
//                        totalAsset = data.sumOf { it.total_aset }
//                        historyTotalAsset = totalAsset
//                    } else {
//                        errorMessage = "Gagal mendapatkan data kategori"
//                    }
//
//                } catch (e: HttpException) {
//                    errorMessage = "HTTP error: ${e.code()}"
//                } catch (e: IOException) {
//                    errorMessage = "Network error: ${e.localizedMessage}"
//                } catch (e: Exception) {
//                    errorMessage = "Unexpected error: ${e.localizedMessage}"
//                } finally {
//                    isLoading = false
//                }
//            }
//        }
//    }
//
//    fun fetchAssetDetailsByCategory(
//        context: Context,
//        kdKelBarang: String,
//        isRefresh: Boolean = false,
//        onComplete: (() -> Unit)? = null
//    ) {
//        if (assetDetailsMap.containsKey(kdKelBarang) && !isRefresh) {
//            onComplete?.invoke()
//            return
//        }
//
//        viewModelScope.launch {
//            loadingCategoryMap[kdKelBarang] = true
//
//            try {
//                val token = TokenDataStore.getToken(context) ?: ""
//                val response = RetrofitClient.api.getAssetsByCategory(
//                    kdKelBarang = kdKelBarang,
//                    token = "Bearer $token"
//                )
//
//                if (response.body()?.metadata?.code == 200) {
//                    val newData = response.body()?.response?.data.orEmpty()
//                    assetDetailsMap = assetDetailsMap.toMutableMap().apply {
//                        this[kdKelBarang] = newData
//                    }
//
//                    println(assetDetailsMap[kdKelBarang])
//                } else {
//                    errorMessage = response.body()?.metadata?.message ?: "Failed to load Asset Data."
//                }
//            } catch (e: Exception) {
//                errorMessage = "Failed to load Asset Data with error message: ${e.localizedMessage}"
//            } finally {
//                loadingCategoryMap[kdKelBarang] = false
//                onComplete?.invoke()
//            }
//        }
//    }
//
//    fun getImageAsset(context:Context,noRegister: String){
//        viewModelScope.launch {
//            try {
//                loadingGetImage[noRegister] = true
//                val token = TokenDataStore.getToken(context) ?: ""
//                val response = RetrofitClient.api.getImageAsset(
//                    no_register = noRegister,
//                    token = "Bearer $token"
//                )
//
//                if (response.body()?.metadata?.code == 200) {
//                    val newMap = assetFullDetailsMap.toMutableMap()
//
//                    newMap[noRegister] = response.body()?.response?.data ?: emptyList()
//                    assetFullDetailsMap = newMap
//                }
//            } catch (e: Exception) {
//                errorMessage = "Gagal mengambil detail aset: ${e.localizedMessage}"
//            }
//            finally {
//                loadingGetImage[noRegister] = false
//            }
//        }
//    }
//
//    fun fetchAssetByQRCode(context: Context, qrValue: String, onComplete: () -> Unit) {
//        viewModelScope.launch {
//            isLoading = true
//            errorMessage = null
//
//            try {
//                val token = TokenDataStore.getToken(context) ?: ""
//
//                val response = RetrofitClient.api.getAssetByRegister(
//                    reg = qrValue,
//                    token = "Bearer $token"
//                )
//                if (response.body()?.metadata?.code == 200 && !response.body()?.response?.data.isNullOrEmpty()) {
//                    assetDetail = response.body()?.response?.data?.get(0)
//                } else {
//                    errorMessage = "Asset tidak ditemukan"
//                }
//            } catch (e: HttpException) {
//                errorMessage = "HTTP error: ${e.code()}"
//            } catch (e: java.io.IOException) {
//                errorMessage = "Network error: ${e.localizedMessage}"
//            } catch (e: Exception) {
//                errorMessage = "Unexpected error: ${e.localizedMessage}"
//            } finally {
//                isLoading = false
//                onComplete()
//            }
//        }
//    }
//}
