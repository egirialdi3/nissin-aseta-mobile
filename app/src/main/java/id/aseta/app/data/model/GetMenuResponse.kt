import id.aseta.app.data.model.Metadata

data class GetMenuResponse(
    val metadata: Metadata,
    val response: GetMenuResponseData
)
data class GetMenuResponseData(
    val count: Int,
    val dataUser: GetMenuItem
)

data class GetMenuItem(
    val user_names: String,
    val nama: String,
    val nama_prsh: String,
    var barcode: Boolean,
    val alamat: String,
    val no_hp: String,
    val super_admin: Boolean,
    val cabang_id : String,
    val customer_id: String,
    val nama_cabang: String,
    var enterprise: Boolean,
)
