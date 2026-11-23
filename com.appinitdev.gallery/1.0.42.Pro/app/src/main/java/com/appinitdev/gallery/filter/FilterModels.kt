
data class FilterParameter(
    val label: String,
    val initialPercentage: Float = 50f,
    var currentValue: Float = initialPercentage
)

data class EditorFilter(
    val name: String,
    val category: String,
    val filterInstance: GPUImageFilter,
    val parameters: List<FilterParameter> = emptyList()
)

data class FilterCategory(
    val name: String,
    val icon: ImageVector
)

data class TextureItem(
    val name: String,
    val resId: Int? = null,      
    val uriString: String? = null 
)
