
data class Festividad(
    val id: String,
    val nombre: String,
    val fecha: String,
    val mesNum: Int, // 1 a 12
    val municipio: String,
    val emoji: String,
    val descripcion: String,
    val recomendacion: String
) : Serializable
