data class SetRegisterProcessRequest(
    val rfid: String,
    val kd_barang: String,
    val no_register: String,
    val location_id: String,
    val tgl_register: String,
    val dept_id: String,
)

data class SetRegisterProcessResponse(
    val metadata: MetaDataSetRegisterProcessResponse
)

data class MetaDataSetRegisterProcessResponse(
    val code: Int,
    val message: String,
)