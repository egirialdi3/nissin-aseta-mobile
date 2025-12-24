package id.aseta.app.data.model

data class LocationResponse(
    val metadata: Metadata,
    val response: LocationResponseData
)
data class LocationResponseData(
    val count: Int,
    val data: List<LocationItem>
)

data class LocationItem(
    val location_id: String,
    val location: String,
    val parent: String?,
    val group: Int?,
    val level: Int?,
    val sort: Int?,
    val area: String?,
    val detail: Boolean?,
    val process_area: Int?,
    val full_location: String,
    var children: List<LocationItem> = emptyList()

)