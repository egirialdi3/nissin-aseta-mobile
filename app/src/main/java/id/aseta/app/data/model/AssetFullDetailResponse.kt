package id.aseta.app.data.model

data class AssetFullDetailResponse(
    val metadata: Metadata,
    val response: AssetFullDetailResponseData
)

data class AssetFullDetailResponseData(
    val countFullData: Int,
    val count: Int,
    val data: List<AssetFullDetail>,
    val cekIsBooking: Boolean
)

data class AssetFullDetail(
    val urut: Int,
    val tgl_register: String,
    val register_date: String,
    val kd_barang: String,
    val no_register: String,
    val nama_barang: String,
    val description: String,
    val nama_kel_barang: String,
    val kd_kel_barang: String,
    val img_url: String,
    val nilai: Long,
    val keterangan: String,
    val kd_satuan: String,
    val satuan: String,
    val harga_standart: Long,
    val merk_default: String,
    val rfid: String?,
    val userid: String,
    val qty: Int,
    val location_id: String,
    val location: String,
    val full_location: String,
    val is_depresiasi: Boolean,
    val kd_vendor: String,
    val vendor: String,
    val dept_id: String,
    val dept_name: String,
    val smpl_status_asset_name: String,
    val smpl_asset_maint_date: String?, // Nullable, karena bisa null
    val id_kondisi: Int,
    val kondisi: String,
    val asset_holder: String,
    val asset_pic: String,
    val asset_holder_date: String, // Bisa jadi gunakan `LocalDateTime` jika parsing otomatis
    val depreciation_formula_no: Int,
    val depreciation_formula_name: String,
    val id_kel_harta: Int,
    val masa_manfaat: Int,
    val dep: Int,
    val images: List<String>,

    // Opsional untuk fitur tambahan
    var isFound: Boolean = false,
    var isForeign: Boolean = false
)