
@Keep
data class AppModel(
    val id: String = "",
    val nombre: String = "",
    val urlWeb: String = "",
    val urlAndroid: String = "",
    val urlGoogleGroup: String = "",
    val paquete: String = "", // ID único (ej: com.ejemplo.app)
    val status: String = "test", // "test", "produccion", "oculta"
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
    val descripcion: String = "", // Opcional: De qué trata la app
    val fcmToken:String="",
    val reputacion: Double = 0.0,
    val categoria: String = "Productividad", // Nueva: Educación, Juegos, Herramientas, etc.
    val resenasProduccion: Map<String, ResenaProduccion> = emptyMap(),
    // Nuevas funcionalidades
    val isBoosted: Boolean = false,
    val boostExpires: Long = 0L,
    val bugReports: List<BugReport> = emptyList(),
    val surveys: List<Survey> = emptyList(),
    val checklist: List<ChecklistItem> = emptyList(),
    // Nuevas funcionalidades 2.0
    val contactoDirecto: String = "",
    val comunidadUrl: String = "",
    val cuentaPrueba: String = "",
    val esDePago: Boolean = false,
    val paisesAgregados: Boolean = false,
    val metodoTest: String = "google_group" // "google_group" o "email"
){
    fun calcularReputacion(propietario: UserProfile?): Double {
        if (propietario == null) return 0.0

        // 1. Nivel (Ya viene en escala 1-5)
        val scoreNivel = propietario.nivel

        // 2. Puntos (Normalizamos 0-200 a escala 1-5)
        // (puntos / 200) * 5. Limitamos a un máximo de 5.
        val scorePuntos = (propietario.puntos.toDouble() / 200.0 * 5.0).coerceAtMost(5.0)

        // 3. Estrellas (Ya viene en escala 1-5)
        val scoreEstrellas = propietario.promedioEstrellas.toDouble()

        // Aplicamos pesos: (Nivel * 0.4) + (Puntos * 0.3) + (Estrellas * 0.3)
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
    val validado: String = "Solicitud",  //Solicitud/Aceptado, Rechazado
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
    val evidenciaUrl: String = "", // URL de la imagen en Storage
    val estado: String = "pendiente", // "pendiente", "aprobada", "rechazada"
    val fecha: Long = System.currentTimeMillis()
)

@Keep
data class CategoriasApps(
    val aceptadas: List<AppModel> = emptyList(),
    val explorar: List<AppModel> = emptyList(),
    val pendientes: List<AppModel> = emptyList(),
    val rechazadas: List<AppModel> = emptyList(),
){
    // Esta propiedad se calcula automáticamente
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
