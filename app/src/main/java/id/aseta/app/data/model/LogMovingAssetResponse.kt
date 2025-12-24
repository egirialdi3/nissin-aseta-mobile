package id.aseta.app.data.model

data class LogMovingAssetResponse(
    val metadata: Metadata,
    val response: LogMovingAssetData? = null // ✅ wajib nullable
)

data class LogMovingAssetData(
    val count: Int? = 0,
    val countFullData: Int? = 0,
    val data: List<LogMovingAssetItem>? = emptyList()
)

data class LogMovingAssetItem(
    val asset_holder_date: String? = "",
    val asset_holder_sort: String? = "",
    val no_register: String? = "",
    val previous_pic: String? = "",
    val asset_pic: String? = "",
    val previous_holder: String? = "",
    val asset_holder: String? = "",
    val note: String? = "",
    val cabang_id: String? = null,
    val nama_cabang: String? = null
)
