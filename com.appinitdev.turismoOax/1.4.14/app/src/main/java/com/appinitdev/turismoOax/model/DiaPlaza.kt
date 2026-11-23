
data class DiaPlaza(
    val id: String,
    val diaSemanaNum: Int, // 1 = Lunes, 2 = Martes, 3 = Miércoles, 4 = Jueves, 5 = Viernes, 6 = Sábado, 7 = Domingo
    val diaNombre: String,
    val municipio: String,
    val distanciaAprox: String,
    val platillosArtesanias: String,
    val descripcion: String,
    val latitud: Double,
    val longitud: Double
) : Serializable
