data class SetReplaceTagProcessRequest(
    val old_rfid: String,
    val new_rfid: String,
    val kd_barang: String,
    val no_register: String,
    val location_id: String,
    val tgl_register: String,
    val dept_id: String,
    val replace_notes: String
)

data class SetReplaceTagProcessRespone(
    val metadata: MetadataSetReplaceTagProcessRespone
)

data class MetadataSetReplaceTagProcessRespone(
    val code: Int,
    val message: String,
)