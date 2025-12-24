import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.aseta.app.data.source.local.TokenDataStore
import kotlinx.coroutines.launch
import okio.IOException
import retrofit2.HttpException

class AuthViewModel : ViewModel() {
    var isLoggedIn by mutableStateOf(false)
    var token by mutableStateOf("")
    var errorMessage by mutableStateOf("")
    var isCheckingLogin by mutableStateOf(true)
    var dataUser: GetMenuItem? by mutableStateOf(null)
        private set



    fun getMenu(context: Context,token: String){
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getMenu("Bearer $token")

                if (response.isSuccessful && response.body()?.metadata?.code == 200) {
//                    response.body()?.response?.dataUser?.barcode = true
//                    response.body()?.response?.dataUser?.enterprise = true
                    dataUser = response.body()?.response?.dataUser
                    TokenDataStore.saveDataUser(context, dataUser)
                    isLoggedIn = true
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

            }
        }
    }

    fun login(context: Context, username: String, password: String) {
        viewModelScope.launch {

            try {

                val response = RetrofitClient.api.login(LoginRequest(username, password))
                if (response.isSuccessful) {
                    println(response.body())
                    token = response.body()?.data?.refreshToken ?: ""
                    getMenu(context,token)
                    TokenDataStore.saveToken(context, token)
                } else {
                    errorMessage = "Login gagal"
                    println(response)
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.localizedMessage}"
                println(errorMessage)
            }
        }
    }

    fun logout(context: Context) {
        viewModelScope.launch {
            TokenDataStore.clearToken(context)
            errorMessage = ""
            dataUser = null
            token = ""
            isLoggedIn = false
        }
    }

    fun checkSavedToken(context: Context) {
        viewModelScope.launch {
            val savedToken = TokenDataStore.getToken(context)
            if (!savedToken.isNullOrEmpty()) {
                token = savedToken
                isLoggedIn = true
            }
            isCheckingLogin = false
        }
    }
}
