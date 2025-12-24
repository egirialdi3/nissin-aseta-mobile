package id.aseta.app.data.model

data class AssetCategoryResponse(
    val metadata: Metadata,
    val response: AssetCategoryData
)


data class AssetCategoryData(
    val count: Int,
    val data: List<AssetCategory>
)

data class AssetCategory(
    val urut: Int,
    val kd_kel_barang: String,
    val nama_kel_barang: String,
    val total_aset: Int
)
