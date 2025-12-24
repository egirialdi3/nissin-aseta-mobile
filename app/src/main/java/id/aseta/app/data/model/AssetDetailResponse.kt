package id.aseta.app.data.model

data class AssetDetailResponse(
    var metadata: Metadata,
    var response: AssetDetailResponseData
)

data class AssetDetailResponseData(
    var count: Int,
    var data: List<AssetDetail>,
    val countFullData: Int = 0,
)

data class AssetDetail(
    var urut: Int,
    var tgl_register: String,
    var no_register: String,
    var location_id: String,
    var location: String,
    var kd_barang: String,
    var nama_barang: String,
    var kd_kel_barang: String,
    var nama_kel_barang: String,
    var description: String,
    var userid: String,
    var nama: String,
    var is_aktif: Int,
    var isFound: Boolean = false,
    var isForeign: Boolean = false,
    var rfid: String?,
    var asset_holder: String?,
    var jarak: String? = "0",
    var dept_id: String? ="",
    var dept_name: String?=""

)
