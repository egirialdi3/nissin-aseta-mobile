data class SetInspectionProcessRequest(
    val tgl_inspeksi: String,
    val no_register: String,
    val id_kondisi: String,
    val ket_inspeksi: String,
    val is_maintenance: Boolean = false
)

data class InspectionProcessResponse(
    val metadata: MetaDataProcessResponse
)

data class MetaDataProcessResponse(
    val code: Int,
    val message: String,
)