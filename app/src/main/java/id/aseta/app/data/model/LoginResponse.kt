
data class LoginResponse(
    val message: String,
    val data: Data
)

data class Data(
    val accessToken: String,
    val refreshToken: String
)