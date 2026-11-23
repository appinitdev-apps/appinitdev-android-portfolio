
@Keep
data class AppModel(
    val id: String = "",
    val nombre: String = "",
    val urlWeb: String = "",
    val urlAndroid: String = "",
    val urlGoogleGroup: String = "",
    val paquete: String = "", 
    val status: String = "test", 
    val idPropietario: String = "",
    val nombreDesarrollador: String? = "",
    val icono: String? = null,
    val testers: Map<String, TesterData> = emptyMap(),
    val testerIds: List<String> = emptyList(),
    val allParticipants: List<String> = emptyList(),
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaActualizacion: Long = System.currentTimeMillis(),
    val instrucciones: String = "",
    val notas: String = "",
    val descripcion: String = "", 
    val fcmToken:String="",
    val reputacion: Double = 0.0,
    val categoria: String = "Productividad", 
    val resenasProduccion: Map<String, ResenaProduccion> = emptyMap(),
    
    val isBoosted: Boolean = false,
    val boostExpires: Long = 0L,
    val bugReports: List<BugReport> = emptyList(),
    val surveys: List<Survey> = emptyList(),
    val checklist: List<ChecklistItem> = emptyList(),
    
    val contactoDirecto: String = "",
    val comunidadUrl: String = "",
    val cuentaPrueba: String = "",
    val esDePago: Boolean = false,
    val paisesAgregados: Boolean = false,
    val metodoTest: String = "google_group" 
){
    fun calcularReputacion(propietario: UserProfile?): Double {
        if (propietario == null) return 0.0

        
        val scoreNivel = propietario.nivel

        
        
        val scorePuntos = (propietario.puntos.toDouble() / 200.0 * 5.0).coerceAtMost(5.0)

        
        val scoreEstrellas = propietario.promedioEstrellas.toDouble()

        
        val resultado = (scoreNivel * 0.4) + (scorePuntos * 0.3) + (scoreEstrellas * 0.3) + propietario.puntosExtra
        return resultado.coerceAtMost(5.0)
    }
}

@Keep
data class TesterData(
    val idUser: String = "",
    val dias: Int = 0,
    val correoUser: String = "",
    val devName: String = "",
    val fcmToken:String="",
    val validado: String = "Solicitud",  
    val ultimaRevision: Long = 0L,
    val completado: Boolean = false
)


@Keep
data class ResenaProduccion(
    val userId: String = "",
    val userName: String? = "",
    val appName: String? = "",
    val fcmTokenTester:String?="",
    val fcmTokenApp:String?="",
    val evidenciaUrl: String = "", 
    val estado: String = "pendiente", 
    val fecha: Long = System.currentTimeMillis()
)

@Keep
data class CategoriasApps(
    val aceptadas: List<AppModel> = emptyList(),
    val explorar: List<AppModel> = emptyList(),
    val pendientes: List<AppModel> = emptyList(),
    val rechazadas: List<AppModel> = emptyList(),
){
    
    val total: Int get() = explorar.size + aceptadas.size + pendientes.size + rechazadas.size
}

object AppConstants {
    val CATEGORIAS = listOf(
        "Herramientas",
        "Productividad",
        "Social",
        "Educación",
        "Entretenimiento",
        "Finanzas",
        "Salud y Bienestar",
        "Juegos",
        "Compras",
        "Fotografía",
        "Música",
        "Noticias",
        "Personalización",
        "Comunicación",
        "Estilo de vida",
        "Otros"
    )
}

data class AppDraft(
    val nombre: String = "",
    val paquete: String = "",
    val categoria: String = AppConstants.CATEGORIAS[0],
    val paisesAgregados: Boolean = false,
    val metodoTest: String = "google_group",
    val urlGoogleGroup: String = "",
    val instrucciones: String = "",
    val contactoDirecto: String = "",
    val comunidadUrl: String = "",
    val notas: String = "",
    val descripcion: String = "",
    val cuentaPrueba: String = "",
    val esDePago: Boolean = false,
    val step: Int = 1
)
