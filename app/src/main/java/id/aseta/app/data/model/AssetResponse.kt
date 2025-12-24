package id.aseta.app.data.model

data class AssetResponse(
    val metadata: Metadata,
    val response: ResponseData
)

data class Metadata(val code: Int, val message: String)
data class ResponseData(val count: Int, val data: List<AssetDetail>)
data class AssetItem(
    val no_register: String,
    val nama_barang: String,
    val nama: String,
    val location: String
)
