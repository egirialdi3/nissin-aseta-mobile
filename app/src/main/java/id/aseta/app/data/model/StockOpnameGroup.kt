package id.aseta.app.data.model

data class StockOpnameGroup(
    val stock_opname_group_code: String,
    val stock_opname_group_name: String
)

data class StockOpnameGroupResponse(
    val metadata: Metadata,
    val response: StockOpnameGroupData
)

data class StockOpnameGroupData(
    val countFullData: Int,
    val count: Int,
    val data: List<StockOpnameGroup>
)