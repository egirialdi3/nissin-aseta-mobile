package id.aseta.app.data.model


data class RelocationResponse(
    val metadata: MetaDataRelocationResponse
)

data class MetaDataRelocationResponse(
    val code: Int,
    val message: String,
)

data class RelocationRequest(
    val tgl_keluar: String,
    val keterangan: String,
    val data_asset: List<MoveAssetDataRequest>,
    val location_id: String,
    val dept_id: String
)

data class MoveAssetDataRequest(
    val asset_id: String,
    val asset_name: String,
    val no_register: String,
    val location: String = "", // default empty string
    val old_location_id: String,
    val old_location: String,
    val dept: String = "", // default empty string
    val old_dept_id: String,
    val old_dept: String = "", // default empty string
    val asset_holder: String
)
