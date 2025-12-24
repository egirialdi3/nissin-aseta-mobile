import id.aseta.app.data.model.AssetCategoryResponse
import id.aseta.app.data.model.AssetDetailResponse
import id.aseta.app.data.model.AssetFullDetailResponse
import id.aseta.app.data.model.AssetResponse
import id.aseta.app.data.model.ConditionResponse
import id.aseta.app.data.model.DataJenisDisposalResponse
import id.aseta.app.data.model.DepartmentResponse
import id.aseta.app.data.model.LocationItem
import id.aseta.app.data.model.LocationResponse
import id.aseta.app.data.model.LogMaintenanceResponse
import id.aseta.app.data.model.LogMovingAssetResponse
import id.aseta.app.data.model.Metadata
import id.aseta.app.data.model.RelocationRequest
import id.aseta.app.data.model.RelocationResponse
import id.aseta.app.data.model.StockOpnameGroup
import id.aseta.app.data.model.StockOpnameGroupResponse
import id.aseta.app.data.model.StockOpnameRequest
import id.aseta.app.data.model.StockOpnameRequestResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {
    @POST("auth/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/auth/refresh")
    suspend fun refresh(): Response<LoginResponse>

    @GET("aseta/api/sidebar/menu")
    suspend fun getMenu(
        @Header("Authorization") token: String
    ): Response<GetMenuResponse>

    @GET("aseta/api/asset/getAssetList")
    suspend fun getAssetByRegister(
        @Query("no_register") reg: String,
        @Header("Authorization") token: String
    ): Response<AssetResponse>

    @GET("aseta/api/asset/getCategoryAsset")
    suspend fun getCategoryAsset(
        @Header("Authorization") token: String
    ): Response<AssetCategoryResponse>

    @GET("aseta/api/asset/getAssetList")
    suspend fun getAssetsByCategory(
        @Query("kd_kel_barang") kdKelBarang: String,
        @Header("Authorization") token: String,
        @Query("page") page: String? = "1",
        @Query("limit") limit: String? = "10",
    ): Response<AssetDetailResponse>


    @GET("aseta/api/report/getRptRegister")
    suspend fun getImageAsset(
        @Query("search") no_register: String,
        @Header("Authorization") token: String
    ): Response<AssetFullDetailResponse>

    @GET("aseta/api/asset/getAssetList")
    suspend fun getAssetsByName(
        @Query("search") assetName: String,
        @Header("Authorization") token: String
    ): Response<AssetDetailResponse>

    @GET("aseta/api/asset/getAssetList")
    suspend fun getAssetsByLocation(
        @Query("location_id") locationId: String,
        @Header("Authorization") token: String
    ): Response<AssetDetailResponse>


    @GET("aseta/api/asset/getAssetList")
    suspend fun getAssetByRfid(
        @Query("search") rfid: String,
        @Header("Authorization") token: String
    ): Response<AssetDetailResponse>

    @GET("aseta/api/master/getListLocationFilter")
    suspend fun getLocation(
        @Header("Authorization") token: String
    ): Response<LocationResponse>

    @POST("aseta/api/asset/setInspectionProcess")
    suspend fun setInspectionProcess(
        @Body request: SetInspectionProcessRequest,
        @Header("Authorization") token: String,
        @Query("is_maintenance") isMaintenance: Boolean? = null
    ): Response<InspectionProcessResponse>


    @GET("aseta/api/asset/getDataStockOpnameGroup")
    suspend fun getStockOpnameGroup(
        @Query("page") page : String,
        @Query("limit") limit: String,
        @Header("Authorization") token: String
    ): Response<StockOpnameGroupResponse>

    @GET("aseta/api/asset/getDataStockOpnameMobile")
    suspend fun getDataStockOpnameMobile(
        @Header("Authorization") token: String,
        @Query("location_id") locationId: String? = null,
        @Query("stock_opname_group_code") groupId: String? = null,
        @Query("date_start") dateStart: String? = null,
        @Query("date_end") dateEnd: String? = null,
    ): Response<StockOpnameResponse>

    @GET("aseta/api/asset/getDataStockOpnameMobileDetail")
    suspend fun getDataStockOpnameMobileDetail(
        @Header("Authorization") token: String,
        @Query("stock_opname_code") stockOpnameCode: String? = null,
        @Query("page") page: String? = "1",
        @Query("limit") limit: String? = "10",
    ): Response<DetailStockOpnameResponse>

    @POST("aseta/api/asset/setStockOpnameGroup")
    suspend fun setStockOpnameGroup(
        @Body request: SetGroupRequest,
        @Header("Authorization") token: String
    ): Response<GroupResponse>

    @POST("aseta/api/asset/setRegisterProcess")
    suspend fun setRegisterProcess(
        @Body request: SetRegisterProcessRequest,
        @Header("Authorization") token: String
    ): Response<SetRegisterProcessResponse>


    @POST("aseta/api/asset/setReplaceTagProcess")
    suspend fun setReplaceTagProcess(
        @Body request: SetReplaceTagProcessRequest,
        @Header("Authorization") token: String
    ): Response<SetReplaceTagProcessRespone>


    @POST("aseta/api/relocation/setRelocation")
    suspend fun setRelocation(
        @Body request: RelocationRequest,
        @Header("Authorization") token: String
    ): Response<RelocationResponse>


    @POST("aseta/api/asset/setMutation")
    suspend fun setMutation(
        @Body request: RelocationRequest,
        @Header("Authorization") token: String
    ): Response<RelocationResponse>

    @POST("aseta/api/asset/setStockOpname")
    suspend fun setStockOpname(
        @Body request: StockOpnameRequest,
        @Header("Authorization") token: String
    ): Response<StockOpnameRequestResponse?>

    @GET("aseta/api/master/getListCondition") // Adjust this to the actual endpoint
    suspend fun getConditions(
        @Header("Authorization") token: String
    ): Response<ConditionResponse>


    @GET("aseta/api/disposal/getDataJenisDisposal")
    suspend fun getJenisDisposal(
        @Header("Authorization") token: String
    ): Response<DataJenisDisposalResponse>


    @Multipart
    @POST("aseta/api/maintenance/corrective/setMaint")
    suspend fun setCorrectiveMaintenance(
        @Part("data_json") dataJson: RequestBody,
        @Part dataImg: MultipartBody.Part,
        @Header("Authorization") token: String
    ): Response<StockOpnameRequestResponse>

    @GET("aseta/api/master/getListDepartment") // Adjust this to the actual endpoint
    suspend fun getListDepartment(
        @Header("Authorization") token: String
    ): Response<DepartmentResponse>

    @GET("aseta/api/report/getRptLogMaintenance")
    suspend fun getRptLogMaintenance(
        @Header("Authorization") token: String,
        @Query("page") page: String? = "1",
        @Query("limit") limit: String? = "10",
        @Query("no_register") no_register: String
    ): Response<LogMaintenanceResponse>

    @GET("aseta/api/report/getRptLogMovingAsset")
    suspend fun getRptLogMovingAsset(
        @Header("Authorization") token: String,
        @Query("page") page: String? = "1",
        @Query("limit") limit: String? = "10",
        @Query("no_register") no_register: String
    ): Response<LogMovingAssetResponse>
}