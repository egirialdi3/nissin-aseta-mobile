package id.aseta.app.data.model

data class LogMaintenanceResponse(
    val metadata: Metadata,
    val response: LogMaintenanceData
)

data class LogMaintenanceData(
    val count: Int,
    val countFullData: Int,
    val data: List<LogMaintenanceItem>
)

data class LogMaintenanceItem(
    val no_register: String,
    val history_maint_date: String,
    val maintenance_id: String,
    val maintenance_title: String,
    val description: String,
    val history_status: String,
    val total_biaya: Int,
    val note: String
)