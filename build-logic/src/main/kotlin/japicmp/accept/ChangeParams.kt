package japicmp.accept

data class ChangeParams(
    val changeToReason: Map<ApiChange, String>,
    val changeFileName: String,
)
