data class Element(
    val element: String,      
    val short: String,        
    val number: Int,          
    val electro: Double,      
    val isotopes: Int,        
    val elementKey: String = element.lowercase()  
)
