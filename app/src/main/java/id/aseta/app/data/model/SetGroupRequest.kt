data class SetGroupRequest(
    val stock_opname_group_code: String,
    val stock_opname_group_name: String,
)

data class GroupResponse(
    val metadata: MetaDataGroupResponse
)

data class MetaDataGroupResponse(
    val code: Int,
    val message: String,
)