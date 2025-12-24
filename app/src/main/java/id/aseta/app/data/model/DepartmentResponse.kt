package id.aseta.app.data.model


data class DepartmentResponse(
    val metadata: Metadata,
    val response: DepartmentData
)

data class DepartmentData(
    val countFullData: Int,
    val count: Int,
    val data: List<DepartmentItem>
)

data class DepartmentItem(
    val dept_id: String,
    val dept_name: String
)
