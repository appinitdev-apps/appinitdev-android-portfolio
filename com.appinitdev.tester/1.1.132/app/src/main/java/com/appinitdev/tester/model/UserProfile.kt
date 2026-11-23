
@Keep
data class UserProfile(
    val uid: String = "",
    val devName: String = "",
    val bio: String = "",
    val contacto: String = "",
    val redes: String = "",
    val enlaces: String = "",
    val web: String = "",
    
    val puntos: Int = 0,
    val nivel: Double = 1.0,
    val puntosExtra: Double = 0.0,
    val calificaciones: Map<String, Int> = emptyMap(),
    val perfilCompletado: Boolean = false,
    val fcmToken: String = "",
    
    val streaks: Int = 0,
    val lastActive: Long = 0L,
    val achievements: List<String> = emptyList(),
    val isPro: Boolean = false,
    val referralCode: String = "",
    val referredBy: String? = null
){
    val promedioEstrellas: Float
        get() = if (calificaciones.isEmpty()) 0f else calificaciones.values.average().toFloat()
    val reputacion: Double
        get() {
            
            val scoreNivel = nivel.coerceIn(1.0, 5.0)

            
            val scorePuntos = (puntos.toDouble() / 200.0 * 5.0).coerceIn(0.0, 5.0)

            
            val scoreEstrellas = promedioEstrellas.toDouble()

            
            return (scoreNivel * 0.4) + (scorePuntos * 0.3) + (scoreEstrellas * 0.3)
        }


}
