
data class Series(
    val name: String,           
    val voltage: Double,        
    val charge: String,         
    val short: String,          
    val nameKey: String = name.lowercase()  
)

