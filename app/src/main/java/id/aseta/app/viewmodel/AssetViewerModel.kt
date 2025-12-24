package id.aseta.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.aseta.app.data.source.local.TokenDataStore
import id.aseta.app.data.model.AssetDetail
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class AssetViewerViewModel : ViewModel() {

    var assetData: AssetDetail? = null
        private set

    var isLoading: Boolean = false
        private set

    var errorMessage: String? = null
        private set

    fun fetchAssetByQRCode(context: Context, qrValue: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                val token = TokenDataStore.getToken(context) ?: ""
                println("TOKEN: Bearer $token")
                println(token)
                println(qrValue)

                val response = RetrofitClient.api.getAssetByRegister(
                    reg = qrValue,
                    token = "Bearer $token"
                )
                println(response.headers())
                println(response.code())

                println("Response: ${response.body()}")
                println(response.errorBody())
                val errorBody = response.errorBody()?.string()
                println(errorBody)
                if (response.body()?.metadata?.code == 200 && !response.body()?.response?.data.isNullOrEmpty()) {
                    assetData = response.body()?.response?.data?.get(0)
                } else {
                    errorMessage = "Asset tidak ditemukan"
                }
            } catch (e: HttpException) {
                errorMessage = "HTTP error: ${e.code()}"
            } catch (e: IOException) {
                errorMessage = "Network error: ${e.localizedMessage}"
            } catch (e: Exception) {
                errorMessage = "Unexpected error: ${e.localizedMessage}"
            } finally {
                isLoading = false
                onComplete()
            }
        }
    }
}
