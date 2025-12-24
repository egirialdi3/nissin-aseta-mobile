package id.aseta.app.data.model


data class ConditionResponse(
    val metadata: Metadata,
    val response: ConditionData
)

data class ConditionData(
    val countFullData: Int,
    val count: Int,
    val data: List<ConditionItem>
)

data class ConditionItem(
    val id_kondisi: Int,
    val kondisi: String
)
