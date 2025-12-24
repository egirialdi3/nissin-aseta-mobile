package id.aseta.app.data.model

data class StockOpnameRequestResponse(
    val metadata: MetaDataStockOpnameResponse
)

data class MetaDataStockOpnameResponse(
    val code: Int,
    val message: String
)

data class StockOpnameRequest(
    val stock_opname_group_code: String,
    val stock_opname_code: String = "", // default empty string
    val stock_opname_date: String, // format: "yyyy-MM-dd'T'HH:mm:ss"
    val location_id: String,
    val stock_opname_description: String,
    val created_at: String, // format: "yyyy-MM-dd'T'HH:mm:ss"
    val data: List<StockOpnameRequestItem>,
    val data_foreign: List<StockOpnameForeignItem> = emptyList() // default empty list
)

data class StockOpnameRequestItem(
    val no_register: String,
    val kd_barang: String,
    val stock_opname_sort: Int
)

data class StockOpnameForeignItem(
    val no_register: String,
    val prev_location: String
)