data class StockOpnameResponse(
    val metadata: StockOpnameMetadata,
    val response: StockOpnameDataResponse
)

data class StockOpnameMetadata(
    val code: Int,
    val message: String
)

data class StockOpnameDataResponse(
    val count: Int,
    val data: List<StockOpnameItem>
)





data class StockOpnameItem(
    val stock_opname_group_code: String,
    val stock_opname_group_name: String,
    val stock_opname_code: String,
    val stock_opname_date: String,
    val userid: String,
    val location_id: String,
    val location: String,
    val stock_opname_description: String,
    val count_assets: Int,
//    val assets: StockAssetList
)

///// INI UNTUK STOCK OPNAME ITEM DETAIL
data class DetailStockOpnameResponse(
    val metadata: DetailStockOpnameMetadata,
    val response: DetailStockOpnameDataResponse
)

data class DetailStockOpnameMetadata(
    val code: Int,
    val message: String
)

data class DetailStockOpnameDataResponse(
    val countFull: Int,
    val count: Int,
    val data: List<DetailStockAssetItem>
)


data class DetailStockAssetItem(
    val no_register: String,
    val nama_barang: String,
    val rfid: String?,
    val kd_barang: String,
    val foreign: Boolean,
    val is_found: Boolean,
    val prev_location_id: String?,
    val prev_location: String?
)
