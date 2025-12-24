package id.aseta.app.data.model


data class DataJenisDisposalResponse(
    val metadata: Metadata,
    val response: DataJenisDisposalData
)

data class DataJenisDisposalData(
    val count: Int,
    val data: List<DataJenisDisposalItem>
)

data class DataJenisDisposalItem(
    val kd_jenis_disposal: String,
    val namajenis: String
)
