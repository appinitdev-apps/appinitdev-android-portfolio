
data class DiaPlaza(
    val id: String,
    val diaSemanaNum: Int, 
    val diaNombre: String,
    val municipio: String,
    val distanciaAprox: String,
    val platillosArtesanias: String,
    val descripcion: String,
    val latitud: Double,
    val longitud: Double
) : Serializable
