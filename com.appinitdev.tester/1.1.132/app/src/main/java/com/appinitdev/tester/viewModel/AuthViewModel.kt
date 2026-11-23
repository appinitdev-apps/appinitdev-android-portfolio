// Reemplaza "com.appinitdev.tester" con el applicationId de tu build.gradle

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private fun getString(@StringRes resId: Int, vararg formatArgs: Any): String {
        return getApplication<Application>().getString(resId, *formatArgs)
    }

    private val repo: AuthRepository = AuthRepository()
    
    fun isUserLoggedIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile = _userProfile.asStateFlow()
    private val _fixedAppsIcons = MutableStateFlow<Map<String, String>>(emptyMap())
    val fixedAppsIcons: StateFlow<Map<String, String>> = _fixedAppsIcons.asStateFlow()
    // Estado para mostrar carga o errores en la UI
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState = _uiState.asStateFlow()
    private val _userApps = MutableStateFlow<List<AppModel>>(emptyList())
    val userApps = _userApps.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<Message>>(emptyList())
    val chatMessages = _chatMessages.asStateFlow()

    private val _communityApps = MutableStateFlow<List<AppModel>>(emptyList())
    val communityApps = _communityApps.asStateFlow()


    private val _currentApp = MutableStateFlow<AppModel?>(null)
    val currentApp = _currentApp.asStateFlow()

    private val _globalLinks = MutableStateFlow<List<ComunidadLink>>(emptyList())
    val globalLinks = _globalLinks.asStateFlow()


    private val _productionApps = MutableStateFlow<List<AppModel>>(emptyList())
    val productionApps = _productionApps.asStateFlow()

    private val _allUsers = MutableStateFlow<List<UserProfile>>(emptyList())
    val allUsers = _allUsers.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading

    private val _isUploadingMessages = MutableStateFlow(false)
    val isUploadingMessages: StateFlow<Boolean> = _isUploadingMessages

    private val _isUploadingCommunityApps = MutableStateFlow(false)
    val isUploadingCommunityApps: StateFlow<Boolean> = _isUploadingCommunityApps

    private val _notifications = MutableStateFlow<List<UserNotification>>(emptyList())
    val notifications = _notifications.asStateFlow()

    private val _feedbackReports14Days = MutableStateFlow<List<com.appinitdev.tester.model.FeedbackReport14Days>>(emptyList())
    val feedbackReports14Days = _feedbackReports14Days.asStateFlow()

    private val _bugReports = MutableStateFlow<List<BugReport>>(emptyList())
    val bugReports = _bugReports.asStateFlow()

    private val _pointsHistory = MutableStateFlow<List<PointsTransaction>>(emptyList())
    val pointsHistory = _pointsHistory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Agrega estas variables a tu AuthViewModel
    private lateinit var billingClient: BillingClient
    private val _puntosCompra = MutableStateFlow(0)


    private val _draftApp = MutableStateFlow(AppDraft())
    val draftApp = _draftApp.asStateFlow()

    fun updateDraft(update: AppDraft.() -> AppDraft) {
        _draftApp.value = _draftApp.value.update()
    }

    fun clearDraft() {
        _draftApp.value = AppDraft()
    }

    private val _featuredApps = MutableStateFlow<List<AppModel>>(emptyList())
    val featuredApps = _featuredApps.asStateFlow()



    private val _productionCount = MutableStateFlow(0)
    val productionCount: StateFlow<Int> = _productionCount

    private val _testersCount = MutableStateFlow(0)
    val testersCount: StateFlow<Int> = _testersCount

    private val _categoriasApps = MutableStateFlow(com.appinitdev.tester.model.CategoriasApps())
    val categoriasApps = _categoriasApps.asStateFlow()

    private val _appsNoInstaladas = MutableStateFlow<List<AppModel>>(emptyList())
    val appsNoInstaladas = _appsNoInstaladas.asStateFlow()

    private val _app20DaysPrompt = MutableStateFlow<AppModel?>(null)
    val app20DaysPrompt: StateFlow<AppModel?> = _app20DaysPrompt.asStateFlow()

    private val _app28DaysAlert = MutableStateFlow<AppModel?>(null)
    val app28DaysAlert: StateFlow<AppModel?> = _app28DaysAlert.asStateFlow()

    private val _dismissed20DaysAppIds = mutableSetOf<String>()

    fun dismiss20DaysPrompt(appId: String) {
        _dismissed20DaysAppIds.add(appId)
        if (_app20DaysPrompt.value?.id == appId) {
            _app20DaysPrompt.value = null
        }
    }

    fun dismiss28DaysAlert() {
        _app28DaysAlert.value = null
    }

    fun markAppAsProductionManual(appId: String) {
        _dismissed20DaysAppIds.add(appId)
        if (_app20DaysPrompt.value?.id == appId) {
            _app20DaysPrompt.value = null
        }
        viewModelScope.launch {
            repo.updateAppStatus(appId, "produccion").onSuccess {
                loadAndVerifyApps()
            }
        }
    }

    fun hideAppManual(appId: String) {
        _dismissed20DaysAppIds.add(appId)
        if (_app20DaysPrompt.value?.id == appId) {
            _app20DaysPrompt.value = null
        }
        viewModelScope.launch {
            repo.updateAppStatus(appId, "oculta").onSuccess {
                loadAndVerifyApps()
            }
        }
    }

    fun keepAppActive20Days(appId: String) {
        _dismissed20DaysAppIds.add(appId)
        if (_app20DaysPrompt.value?.id == appId) {
            _app20DaysPrompt.value = null
        }
        viewModelScope.launch {
            repo.updateApp(appId, mapOf("fechaActualizacion" to System.currentTimeMillis())).onSuccess {
                loadAndVerifyApps()
            }
        }
    }

    private var observeCategoriasJob: Job? = null
    fun startObservingCategorias(context: android.content.Context) {
        loadCommunityApps(context)
    }

    private fun verificarAppsInstaladas(context: android.content.Context, apps: List<AppModel>) {
        val noInstaladas = apps.filter { !com.appinitdev.tester.Funciones.isAppInstalled(context, it.paquete) }
        _appsNoInstaladas.value = noInstaladas
    }

    // En AuthViewModel.kt
// En AuthViewModel.kt
    fun login(email: String, pass: String) = viewModelScope.launch {
        _uiState.value = AuthUiState.Loading
        val result = repo.login(email, pass)

        if (result.isSuccess) {
//            if (repo.isEmailVerified()) {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

                // INTENTA OBTENER EL PERFIL
                val profile = repo.getUserProfile(uid)

                // ASIGNA EL VALOR AL STATEFLOW (Esto quita el cargando en la Home)
                _userProfile.value = profile

                if (profile == null || !profile.perfilCompletado) {
                    // Si no hay perfil, lo mandamos a crear uno
                    _uiState.value = AuthUiState.NeedsProfile
                } else {
                    // Si todo está bien, entramos al Home
                    _uiState.value = AuthUiState.Success
                }
//            } else {
//                repo.logout() // Asegúrate de tener este método en el repo
//                _uiState.value = AuthUiState.Error("Verifica tu cuenta para entrar.")
//            }

            try {
                registrarTokenFCM()
            } catch (_: Exception) {
            }
        } else {
            _uiState.value = AuthUiState.Error(result.exceptionOrNull()?.message)
        }
    }

    fun register(email: String, pass: String) = viewModelScope.launch {
        _uiState.value = AuthUiState.Loading
        val result = repo.register(email, pass)
        // Cambiamos Success por RegisterSuccess
        _uiState.value =
            if (result.isSuccess) AuthUiState.RegisterSuccess else AuthUiState.Error(result.exceptionOrNull()?.message)
    }

    fun clearState() {
        _uiState.value = AuthUiState.Idle
    }

    fun resetPassword(email: String) = viewModelScope.launch {
        _uiState.value = AuthUiState.Loading
        val result = repo.resetPassword(email)
        _uiState.value =
            if (result.isSuccess) AuthUiState.Success else AuthUiState.Error(result.exceptionOrNull()?.message)
    }


    fun fetchUserProfile() = viewModelScope.launch {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
        try {
            val profile = repo.getUserProfile(uid)
            _userProfile.value = profile
            if (profile != null) {
                recalcularYActualizarReputacion(profile)
            }
            checkAndAwardAchievements()
            // debugCheckAndHideProductionAppsAfter30Days()
            // debugCheckHiddenAppsMoveToProduction()
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error al cargar perfil: ${e.message}")
        }
    }

    private suspend fun recalcularYActualizarReputacion(perfil: UserProfile) {
        val maxAppsPosibles = DevAppsConfig.developerApps.size.toDouble()
        val scoreNivel = (perfil.nivel / maxAppsPosibles).coerceAtMost(1.0) * 5.0 * 0.30
        val scorePuntos = (Math.log10(perfil.puntos.toDouble() + 1.0) / Math.log10(501.0))
            .coerceAtMost(1.0) * 5.0 * 0.25
        val numVotos = perfil.calificaciones.size
        val confianza = (numVotos.toDouble() / (numVotos + 5)).coerceIn(0.0, 1.0)
        val scoreEstrellas = ((perfil.promedioEstrellas * confianza) + (3.0 * (1.0 - confianza))) * 0.45

        val nuevaReputacion = (scoreNivel + scorePuntos + scoreEstrellas + perfil.puntosExtra).coerceIn(1.0, 5.0)

        repo.updateAllUserAppsReputation(perfil.uid, nuevaReputacion, perfil.fcmToken)
        _userApps.update { apps -> apps.map { it.copy(reputacion = nuevaReputacion) } }
    }

    fun checkAndAwardAchievements() {
        val profile = _userProfile.value ?: return
        val currentAchievements = profile.achievements.toMutableList()
        var changed = false

        // 1. Pionero (Perfil completado)
        if (profile.perfilCompletado && !currentAchievements.contains("tester_pioneer")) {
            currentAchievements.add("tester_pioneer")
            changed = true
        }

        // 2. Racha (3+ días)
        if (profile.streaks >= 3 && !currentAchievements.contains("streak_fire")) {
            currentAchievements.add("streak_fire")
            changed = true
        }

        if (changed) {
            viewModelScope.launch {
                repo.updateUserProfile(profile.uid, mapOf("achievements" to currentAchievements))
                fetchUserProfile() // Recargar
            }
        }
    }

    // Dentro de la clase AuthViewModel
    fun saveProfile(profile: UserProfile, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repo.saveUserProfile(profile)
            if (result.isSuccess) {
                _userProfile.value = profile // ACTUALIZA EL ESTADO LOCAL AQUÍ
                _uiState.value = AuthUiState.Success
                onSuccess()
            } else {
                _uiState.value = AuthUiState.Error(null) // El error real vendrá del repo
            }
        }
    }

    private val _isVerifyingOcr = MutableStateFlow(false)
    val isVerifyingOcr = _isVerifyingOcr.asStateFlow()

    fun verificarResenaConOcr(context: Context, uri: Uri, appName: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _isVerifyingOcr.value = true
            val result = com.appinitdev.tester.util.ReviewOcrScanner.verifyReview(context, uri, appName)
            _isVerifyingOcr.value = false
            result.onSuccess {
                onResult(true, null)
            }.onFailure {
                onResult(false, it.message)
            }
        }
    }




    private var productionAppsJob: Job? = null
    fun loadProductionApps() {
        if (productionAppsJob?.isActive == true) return
        productionAppsJob = viewModelScope.launch {
            repo.getProductionAppsRealtime().collect { apps ->
                _productionApps.value = apps
            }
        }
    }

//    fun loadAndVerifyApps() = viewModelScope.launch {
//        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
//
//
//        val apps = repo.getUserApps(uid)
//
//        val updatedApps = apps.map { app ->
//            // Si está en test, verificamos si ya saltó a producción
//            if (app.status == "test") {
//                val isNowInProd = checkIfAppInProduction(app.paquete)
//                if (isNowInProd) {
//                    val iconUrl = fetchAppIcon(app.paquete)
//                    repo.updateAppStatus(app.id, "produccion", iconUrl)
//                    return@map app.copy(status = "produccion", icono = iconUrl)
//                }
//            }
//            app
//        }
//        _userApps.value = updatedApps
//        val numero = (1..3).random()
//        if (BuildConfig.DEBUG ) {
//            val others = repo.getAppsAll()
//            others.map { other ->
//                val iconUrl = fetchAppIcon(other.paquete)
//                Log.e("iconUrl", iconUrl.toString())
//                // if (other.status == "test") {
//                val isNowInProd = checkIfAppInProduction(other.paquete)
//                if (isNowInProd) {
//                    repo.updateAppStatus(other.id, "produccion", iconUrl)
//                }
//                //}
//            }
//        }
//    }

    private var userAppsJob: Job? = null
    fun loadAndVerifyApps() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        if (userAppsJob?.isActive == true) return
        userAppsJob = viewModelScope.launch {
            try {
                repo.getUserAppsRealtime(uid).collect { apps ->
                    _userApps.value = apps
                    val now = System.currentTimeMillis()

                    apps.forEach { app ->
                        if (app.status == "test") {
                            launch {
                                if (repo.checkIfAppInProduction(app.paquete)) {
                                    val iconUrl = repo.fetchAppIcon(app.paquete)
                                    repo.updateAppStatus(app.id, "produccion", iconUrl)
                                } else {
                                    val diasActiva = ((now - app.fechaCreacion) / (24 * 60 * 60 * 1000L)).toInt()
                                    val context = getApplication<Application>().applicationContext
                                    if (diasActiva >= 28) {
                                        repo.updateAppStatus(app.id, "oculta")
                                        if (com.appinitdev.tester.Funciones.shouldShowTestCompletionPromptEveryTwoDays(context, app.id)) {
                                            _app28DaysAlert.value = app
                                            com.appinitdev.tester.Funciones.saveTestCompletionPromptShown(context, app.id)
                                        }
                                    } else if (diasActiva >= 20 && !_dismissed20DaysAppIds.contains(app.id)) {
                                        if (_app20DaysPrompt.value == null && com.appinitdev.tester.Funciones.shouldShowTestCompletionPromptEveryTwoDays(context, app.id)) {
                                            _app20DaysPrompt.value = app
                                            com.appinitdev.tester.Funciones.saveTestCompletionPromptShown(context, app.id)
                                        }
                                    }
                                }
                            }
                        }
                        /* else if (app.status == "produccion" && com.appinitdev.tester.BuildConfig.DEBUG) {
                            val diasEnProduccion = ((now - app.fechaCreacion) / (24 * 60 * 60 * 1000L)).toInt()
                            if (diasEnProduccion >= 30) {
                                launch {
                                    repo.updateAppStatus(app.id, "oculta")
                                    Log.d("DebugHide", "App '${app.nombre}' lleva $diasEnProduccion días en producción -> Ocultada (DEBUG)")
                                }
                            }
                        } */
                    }
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error en loadAndVerifyApps: ${e.message}")
            }
        }
    }


    fun isDebuggable(context: Context): Boolean {
        return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }


    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun registerNewApp(
        nombre: String, 
        paquete: String, 
        urlWeb: String, 
        urlAndroid: String, 
        instrucciones: String,
        notas: String,
        urlGoogleGroup: String,
        categoria: String,
        contactoDirecto: String = "",
        comunidadUrl: String = "",
        cuentaPrueba: String = "",
        esDePago: Boolean = false,
        paisesAgregados: Boolean = false,
        metodoTest: String = "google_group",
        descripcion: String = "",
        iconoUri: android.net.Uri? = null
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val profile = _userProfile.value
        val appId = java.util.UUID.randomUUID().toString()

        _uiState.value = AuthUiState.Loading

        fun guardarEnFirestore(iconoUrl: String?) {
            val newApp = AppModel(
                id = appId,
                nombre = nombre,
                paquete = paquete,
                urlWeb = urlWeb,
                urlAndroid = urlAndroid,
                idPropietario = uid,
                instrucciones = instrucciones,
                notas = notas,
                descripcion = descripcion,
                status = "test",
                urlGoogleGroup = urlGoogleGroup,
                nombreDesarrollador = profile?.devName ?: "Devs",
                categoria = categoria,
                contactoDirecto = contactoDirecto,
                comunidadUrl = comunidadUrl,
                cuentaPrueba = cuentaPrueba,
                esDePago = esDePago,
                paisesAgregados = paisesAgregados,
                metodoTest = metodoTest,
                icono = iconoUrl,
                reputacion = profile?.reputacion ?: 0.0,
                fechaCreacion = System.currentTimeMillis(),
                fechaActualizacion = System.currentTimeMillis()
            )

            viewModelScope.launch {
                repo.createApp(newApp).onSuccess {
                    loadAndVerifyApps()
                    notifyDevelopersWithAppsInTest(newApp, profile?.devName ?: "Un desarrollador")
                    _uiState.value = AuthUiState.AppActionSuccess 
                }.onFailure {
                    _uiState.value = AuthUiState.Error(null)
                }
            }
        }

        if (iconoUri != null) {
            val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().reference
                .child("app_icons/$appId.jpg")
            storageRef.putFile(iconoUri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        guardarEnFirestore(downloadUri.toString())
                    }.addOnFailureListener {
                        guardarEnFirestore(null)
                    }
                }
                .addOnFailureListener {
                    guardarEnFirestore(null)
                }
        } else {
            guardarEnFirestore(null)
        }
    }

    private fun notifyDevelopersWithAppsInTest(newApp: AppModel, creatorName: String) {
        viewModelScope.launch {
            try {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val snapshot = db.collection("apps")
                    .whereEqualTo("status", "test")
                    .get()
                    .await()

                val activeApps = snapshot.toObjects(AppModel::class.java)
                val ownerIds = activeApps
                    .map { it.idPropietario }
                    .filter { it.isNotBlank() && it != newApp.idPropietario }
                    .distinct()

                Log.d("notifyDevs", "Notificando a ${ownerIds.size} desarrolladores con apps activas en test sobre ${newApp.nombre}")

                val title = getString(R.string.notificacion_nueva_app_titulo)
                val body = getString(R.string.notificacion_nueva_app_msg_formato, creatorName, newApp.nombre)

                ownerIds.forEach { ownerId ->
                    sendInAppAndPushNotification(
                        targetUserId = ownerId,
                        title = title,
                        body = body,
                        appId = newApp.id,
                        iconUrl = newApp.icono
                    )
                }
            } catch (e: Exception) {
                Log.e("notifyDevs", "Error notificando a desarrolladores: ${e.message}")
            }
        }
    }

    fun updateAppFullInfo(
        appId: String,
        nombre: String,
        paquete: String,
        urlWeb: String,
        urlAndroid: String,
        instrucciones: String,
        notas: String,
        urlGoogleGroup: String,
        categoria: String,
        contactoDirecto: String,
        comunidadUrl: String,
        cuentaPrueba: String,
        esDePago: Boolean,
        paisesAgregados: Boolean,
        metodoTest: String,
        descripcion: String,
        iconoUri: android.net.Uri? = null,
        iconoActual: String? = null
    ) {
        _uiState.value = AuthUiState.Loading

        fun actualizarEnFirestore(iconoUrl: String?) {
            viewModelScope.launch {
                val updateMap = mutableMapOf<String, Any?>(
                    "nombre" to nombre,
                    "paquete" to paquete,
                    "urlWeb" to urlWeb,
                    "urlAndroid" to urlAndroid,
                    "instrucciones" to instrucciones,
                    "urlGoogleGroup" to urlGoogleGroup,
                    "notas" to notas,
                    "descripcion" to descripcion,
                    "categoria" to categoria,
                    "contactoDirecto" to contactoDirecto,
                    "comunidadUrl" to comunidadUrl,
                    "cuentaPrueba" to cuentaPrueba,
                    "esDePago" to esDePago,
                    "paisesAgregados" to paisesAgregados,
                    "metodoTest" to metodoTest,
                    "fechaActualizacion" to System.currentTimeMillis()
                )
                if (iconoUrl != null) {
                    updateMap["icono"] = iconoUrl
                }

                repo.updateApp(appId, updateMap.filterValues { it != null }.mapValues { it.value!! }).onSuccess {
                    _uiState.value = AuthUiState.AppActionSuccess
                    loadAndVerifyApps()
                }
            }
        }

        if (iconoUri != null) {
            val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().reference
                .child("app_icons/$appId.jpg")
            storageRef.putFile(iconoUri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        actualizarEnFirestore(downloadUri.toString())
                    }.addOnFailureListener {
                        actualizarEnFirestore(iconoActual)
                    }
                }
                .addOnFailureListener {
                    actualizarEnFirestore(iconoActual)
                }
        } else {
            actualizarEnFirestore(iconoActual)
        }
    }

    /*
    /**
     * Función para buscar y actualizar automáticamente los íconos desde Play Store para todas las apps.
     * SOLO se ejecuta en modo DEBUG (com.appinitdev.tester.BuildConfig.DEBUG).
     */
    fun updateAllAppIconsFromPlayStoreDebug() {
        if (!com.appinitdev.tester.BuildConfig.DEBUG) {
            android.util.Log.d("DebugIcons", "Acción ignorada: No estamos en modo DEBUG.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                android.util.Log.d("DebugIcons", "Iniciando actualización masiva de íconos desde Play Store...")
                val apps = repo.getAllApps()
                var count = 0
                apps.forEach { app ->
                    if (app.paquete.isNotBlank()) {
                        val iconUrl = repo.fetchAppIcon(app.paquete)
                        if (!iconUrl.isNullOrBlank() && iconUrl != app.icono) {
                            repo.updateApp(app.id, mapOf("icono" to iconUrl))
                            count++
                            android.util.Log.d("DebugIcons", "Ícono actualizado para '${app.nombre}' (${app.paquete}): $iconUrl")
                        }
                    }
                }
                android.util.Log.d("DebugIcons", "Proceso completado. Se actualizaron $count íconos.")
                loadAndVerifyApps()
            } catch (e: Exception) {
                android.util.Log.e("DebugIcons", "Error al actualizar íconos: ${e.message}")
            }
        }
    }

    /**
     * Función de depuración (SOLO DEBUG): Si una app lleva 30 días o más en producción,
     * cambia su estado a "oculta".
     */
    fun debugCheckAndHideProductionAppsAfter30Days() {
        if (!com.appinitdev.tester.BuildConfig.DEBUG) {
            Log.d("DebugHide", "Acción ignorada: No estamos en modo DEBUG.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("DebugHide", "Iniciando verificación debug para ocultar apps en producción de >= 30 días...")
                val apps = repo.getAllApps()
                val ahora = System.currentTimeMillis()
                val treintaDiasMs = 30L * 24 * 60 * 60 * 1000L
                var ocultaCount = 0

                apps.forEach { app ->
                    if (app.status == "test") {
                        val antiguedadMs = agora(ahora, app.fechaCreacion)
                        if (antiguedadMs >= treintaDiasMs) {
                            repo.updateApp(app.id, mapOf("status" to "oculta"))
                            ocultaCount++
                            Log.d("DebugHide", "App '${app.nombre}' (${app.id}) en producción lleva ${antiguedadMs / (24 * 60 * 60 * 1000L)} días -> Estado cambiado a 'oculta'")
                        }
                    }
                }
                Log.d("DebugHide", "Proceso completado. Se ocultaron $ocultaCount apps en producción.")
            } catch (e: Exception) {
                Log.e("DebugHide", "Error al verificar apps de 30 días: ${e.message}")
            }
        }
    }

    /**
     * Función de depuración (SOLO DEBUG): Busca todas las aplicaciones con estado "oculta"
     * y verifica si ya se encuentran publicadas en producción en Play Store.
     * Si pasaron a producción, actualiza su estado a "produccion" y descarga su icono.
     */
    fun debugCheckHiddenAppsMoveToProduction() {
        if (!com.appinitdev.tester.BuildConfig.DEBUG) {
            Log.d("DebugRestore", "Acción ignorada: No estamos en modo DEBUG.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("DebugRestore", "Iniciando verificación debug de apps ocultas que hayan pasado a producción...")
                val apps = repo.getAllApps()
                var restauradasCount = 0

                apps.forEach { app ->
                    if (app.status == "oculta" && app.paquete.isNotBlank()) {
                        val isNowInProd = repo.checkIfAppInProduction(app.paquete)
                        if (isNowInProd) {
                            val iconUrl = repo.fetchAppIcon(app.paquete) ?: app.icono
                            val updates = mutableMapOf<String, Any>(
                                "status" to "produccion",
                                "fechaCreacion" to System.currentTimeMillis(), // Se reinicia la fecha para que empiece el conteo de 30 días en producción desde AHORA
                                "fechaActualizacion" to System.currentTimeMillis()
                            )
                            if (!iconUrl.isNullOrBlank()) {
                                updates["icono"] = iconUrl
                            }
                            repo.updateApp(app.id, updates)
                            restauradasCount++
                            Log.d("DebugRestore", "App '${app.nombre}' (${app.paquete}) estaba 'oculta' y se detectó en Play Store -> Estado cambiado a 'produccion'")
                        }
                    }
                }
                Log.d("DebugRestore", "Proceso completado. Se restauraron a producción $restauradasCount apps ocultas.")
            } catch (e: Exception) {
                Log.e("DebugRestore", "Error al verificar apps ocultas: ${e.message}")
            }
        }
    }

    private fun agora(ahora: Long, fechaCreacion: Long): Long = ahora - fechaCreacion
    */

    /**
     * Función de depuración (SOLO DEBUG): Se ejecuta como máximo 1 vez al día.
     * Busca todas las aplicaciones con estado "test", comprueba si ya están publicadas
     * en Google Play Store y, si es así, actualiza su estado a "produccion" y descarga su icono.
     */
    fun debugCheckTestAppsMovedToProductionToday(context: Context) {
        if (!com.appinitdev.tester.BuildConfig.DEBUG) return

        val prefs = context.getSharedPreferences("debug_prefs", Context.MODE_PRIVATE)
        val lastCheck = prefs.getLong("last_debug_test_to_prod_check", 0L)
        val calLast = Calendar.getInstance().apply { timeInMillis = lastCheck }
        val calNow = Calendar.getInstance()

        val isToday = lastCheck != 0L &&
                calLast.get(Calendar.YEAR) == calNow.get(Calendar.YEAR) &&
                calLast.get(Calendar.DAY_OF_YEAR) == calNow.get(Calendar.DAY_OF_YEAR)

        if (isToday) return

        prefs.edit().putLong("last_debug_test_to_prod_check", System.currentTimeMillis()).apply()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("DebugProdCheck", "Iniciando verificación debug (1 vez al día) de apps en 'test' para pasar a 'producción'...")
                val apps = repo.getAllApps()
                var movidasCount = 0

                apps.filter { it.status == "test" && it.paquete.isNotBlank() }.forEach { app ->
                    val isNowInProd = repo.checkIfAppInProduction(app.paquete)
                    if (isNowInProd) {
                        val iconUrl = repo.fetchAppIcon(app.paquete) ?: app.icono
                        repo.updateAppStatus(app.id, "produccion", iconUrl)
                        movidasCount++
                        Log.d("DebugProdCheck", "App '${app.nombre}' (${app.paquete}) estaba en 'test' y se detectó en Play Store -> Cambiada a 'produccion'")
                    }
                }
                Log.d("DebugProdCheck", "Verificación debug completada. Se pasaron a producción $movidasCount apps.")
            } catch (e: Exception) {
                Log.e("DebugProdCheck", "Error en verificación debug de apps a producción: ${e.message}")
            }
        }
    }

    fun resetApp(appId: String) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repo.updateApp(
                appId,
                mapOf(
                    "fechaCreacion" to now,
                    "fechaActualizacion" to now,
                    "status" to "test"
                )
            ).onSuccess {
                loadAndVerifyApps()
                _uiState.value = AuthUiState.AppActionSuccess
            }
        }
    }

    fun deleteApp(appId: String) {
        viewModelScope.launch {
            repo.deleteApp(appId).onSuccess {
                loadAndVerifyApps()
                _uiState.value = AuthUiState.AppActionSuccess
            }
        }
    }


    // Estados para la validación en tiempo real
    var isCheckingPackage by mutableStateOf(false)
        private set

    var packageExists by mutableStateOf(false)
        private set

    fun verifyPackageRealtime(paquete: String) {
        // Si el texto es muy corto o no tiene formato de paquete, no consultamos
        if (paquete.length < 5 || !paquete.contains(".")) {
            packageExists = false
            return
        }

        viewModelScope.launch {
            isCheckingPackage = true
            // Llamamos al repo
            packageExists = repo.checkIfPackageExists(paquete)
            isCheckingPackage = false
        }
    }

    // Función para limpiar los estados cuando se cierra el diálogo
    fun clearPackageCheck() {
        packageExists = false
        isCheckingPackage = false
    }

    fun toggleAppStatus(app: AppModel) {
        // Si ya está en producción, no permitimos el cambio manual
        if (app.status == "produccion") return

        // Alternamos entre test y oculta
        val newStatus = if (app.status == "test") "oculta" else "test"

        viewModelScope.launch {
            val result = repo.updateAppStatus(app.id, newStatus)
            if (result.isSuccess) {
                loadAndVerifyApps()
            }
        }
    }

    fun updateAppStatus(appId: String, newStatus: String) {
        viewModelScope.launch {
            repo.updateAppStatus(appId, newStatus).onSuccess {
                loadAndVerifyApps()
            }
        }
    }
    fun prepareForEdit(paqueteOriginal: String) {
        packageExists = false
        isCheckingPackage = false
        // Podrías guardar el paquete original para comparar
    }

//    fun requestJoinApp(app: AppModel, context: Context) {
//        val user = FirebaseAuth.getInstance().currentUser ?: return
//        val profile = _userProfile.value ?: return
//
//        val newTester = TesterData(
//            idUser = user.uid,
//            correoUser = profile.contacto ?: user.email ?: "",
//            devName = profile.devName,
//            validado = "Solicitud",
//            fcmToken = profile.fcmToken
//        )
//        try {
//
//            val titulo = context.getString(R.string.nuevo_tester)
//            val cuerpo =
//                "${profile.devName} ${context.getString(R.string.solicito_permiso_para_testear)} ${app.nombre}"
//
//            sendNotification(
//                app.nombre,
//                titulo, cuerpo, app.fcmToken, viewModelScope
//            )
//        } catch (_: Exception) {
//        }
//        viewModelScope.launch {
//            repo.applyAsTester(app.id, newTester).onSuccess {
//               // loadAndVerifyApps()
//                actualizarAppLocalmente(app.id, newTester)
//            }
//        }
//    }
suspend fun getUserFcmToken(userId: String): String? {
    if (userId.isBlank()) return null
    return try {
        val userDoc = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .await()
        val token = userDoc.getString("fcmToken")
        if (token.isNullOrBlank()) {
            Log.w("getUserFcmToken", "")
        } else {
            Log.d("getUserFcmToken", "")
        }
        token
    } catch (e: Exception) {
        Log.e("getUserFcmToken", "")
        null
    }
}

fun requestJoinApp(app: AppModel, context: Context) {
    val user = FirebaseAuth.getInstance().currentUser ?: return
    val profile = _userProfile.value ?: return

    val isGoogleGroup = app.metodoTest == "google_group" || app.urlGoogleGroup.isNotBlank()
    val initialStatus = if (isGoogleGroup) "Aceptado" else "Solicitud"

    val newTester = TesterData(
        idUser = user.uid,
        correoUser = profile.contacto.ifBlank { user.email ?: "" },
        devName = profile.devName,
        validado = initialStatus,
        fcmToken = profile.fcmToken
    )

    // 🔥 1. ACTUALIZACIÓN OPTIMISTA (UI Instantánea)
    actualizarAppLocalmente(app.id, newTester)

    viewModelScope.launch {
        try {
            val targetToken = getUserFcmToken(app.idPropietario)

            if (!targetToken.isNullOrBlank()) {
                val titulo = context.getString(R.string.nuevo_tester)
                val cuerpo = if (isGoogleGroup) {
                    "${profile.devName} ${context.getString(R.string.se_unio_auto_tester)} ${app.nombre}"
                } else {
                    "${profile.devName} ${context.getString(R.string.solicito_permiso_para_testear)} ${app.nombre}"
                }
                sendNotification(app.nombre, titulo, cuerpo, targetToken, viewModelScope, iconUrl = app.icono)
            } else {
                Log.w("requestJoinApp", "No se encontró FCM token para el propietario ${app.idPropietario}")
            }
        } catch (e: Exception) {
            Log.e("requestJoinApp", "Error enviando notificación: ${e.message}")
        }

        // 2. Guardamos en Firebase en segundo plano
        repo.applyAsTester(app.id, newTester).onFailure {
            loadCommunityApps()
        }
    }
}

fun leaveAppAsTester(app: AppModel, context: Context) {
    val user = FirebaseAuth.getInstance().currentUser ?: return
    val userId = user.uid

    removerTesterLocalmente(app.id, userId)

    viewModelScope.launch {
        try {
            val targetToken = getUserFcmToken(app.idPropietario)

            if (!targetToken.isNullOrBlank()) {
                val profile = _userProfile.value
                val devName = profile?.devName ?: "Un tester"
                val titulo = context.getString(R.string.tester_retirado_titulo)
                val cuerpo = "$devName ${context.getString(R.string.tester_se_ha_retirado)} ${app.nombre}"
                sendNotification(app.nombre, titulo, cuerpo, targetToken, viewModelScope, iconUrl = app.icono)
            } else {
                Log.w("leaveAppAsTester", "No se encontró FCM token para el propietario ${app.idPropietario}")
            }
        } catch (e: Exception) {
            Log.e("leaveAppAsTester", "Error enviando notificación: ${e.message}")
        }

        repo.leaveAppAsTester(app.id, userId).onFailure {
            loadCommunityApps()
        }
    }
}

private fun removerTesterLocalmente(appId: String, testerId: String) {
    _communityApps.update { listaActual ->
        listaActual.map { app ->
            if (app.id == appId) {
                val nuevosTesters = app.testers.toMutableMap()
                nuevosTesters.remove(testerId)
                val nuevosTesterIds = app.testerIds.filter { it != testerId }
                val nuevosParticipants = app.allParticipants.filter { it != testerId }
                app.copy(
                    testers = nuevosTesters,
                    testerIds = nuevosTesterIds,
                    allParticipants = nuevosParticipants
                )
            } else {
                app
            }
        }
    }

    _userApps.update { listaActual ->
        listaActual.map { app ->
            if (app.id == appId) {
                val nuevosTesters = app.testers.toMutableMap()
                nuevosTesters.remove(testerId)
                val nuevosTesterIds = app.testerIds.filter { it != testerId }
                val nuevosParticipants = app.allParticipants.filter { it != testerId }
                app.copy(
                    testers = nuevosTesters,
                    testerIds = nuevosTesterIds,
                    allParticipants = nuevosParticipants
                )
            } else {
                app
            }
        }
    }

    val uid = FirebaseAuth.getInstance().currentUser?.uid
    _categoriasApps.value = com.appinitdev.tester.Funciones.clasificarApps(_communityApps.value, uid)
}

fun responseToTester(app: AppModel, testerId: String, accept: Boolean, context: Context) {
    val status = if (accept) "Aceptado" else "Rechazado"
    val mensajeNotificacion = if (accept) {
        "${app.nombre} " + context.getString(R.string.ha_aceptado_tu_solicitud)
    } else {
        "${app.nombre} " + context.getString(R.string.ha_rechazado_tu_solicitud)
    }

    // Actualización optimista local
    val currentTester = app.testers[testerId]
    if (currentTester != null) {
        actualizarAppLocalmente(app.id, currentTester.copy(validado = status))
    }

    viewModelScope.launch {
        try {
            val targetToken = getUserFcmToken(testerId)

            if (!targetToken.isNullOrBlank()) {
                sendNotification(
                    app.nombre,
                    context.getString(R.string.respuesta_de_solicitud),
                    mensajeNotificacion,
                    targetToken,
                    viewModelScope,
                    iconUrl = app.icono
                )
            } else {
                Log.w("responseToTester", "No se encontró FCM token para el tester $testerId")
            }
        } catch (e: Exception) {
            Log.e("responseToTester", "Error enviando notificación a tester: ${e.message}")
        }

        repo.updateTesterStatus(app.id, testerId, status).onSuccess {
            loadAndVerifyApps()
            loadCommunityApps()
        }
    }
}

    private var chatJob: Job? = null

    fun listenToMessages(appId: String) {
        stopListeningToMessages()

        chatJob = viewModelScope.launch {
            _isUploadingMessages.value = true

            // Iniciamos un temporizador de seguridad
            val timeoutJob = launch {
                delay(8000)
                _isUploadingMessages.value = false
            }

            try {
                _chatMessages.value = emptyList()

                repo.getMessages(appId).collect { mensajes ->
                    timeoutJob.cancel()
                    _chatMessages.value = mensajes
                    _isUploadingMessages.value = false
                }
            } catch (e: Exception) {
                _isUploadingMessages.value = false
            }
        }
    }

    fun stopListeningToMessages() {
        chatJob?.cancel()
        chatJob = null
        _chatMessages.value = emptyList()
        _isUploadingMessages.value = false
    }
//    fun listenToMessages(appId: String) {
//        viewModelScope.launch {
//            _isUploadingMessages.value = true
//            try {
//
//                repo.getMessages(appId).collect {
//                    _chatMessages.value = it
//                    if (_isUploadingMessages.value) {
//                        _isUploadingMessages.value = false
//                    }
//                }
//
//            } catch (e: Exception) {
//
//                Log.e("AuthViewModel", "Error cargando apps: ${e.message}")
//
//            } finally {
//
//                _isUploadingMessages.value = false // Ocultar cargando (siempre se ejecuta)
//
//            }
//        }
//    }

    fun sendMessage(app: AppModel, text: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val profile = _userProfile.value
        val msg = Message(
            senderId = user.uid,
            senderName = profile?.devName ?: "Tester",
            text = text,
            nameDev = profile?.devName
                ?.ifBlank { user.uid }
                ?: user.uid,
            timestamp = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repo.sendMessage(app.id, msg)

            if (user.uid != app.idPropietario) {
                try {
                    val targetToken = getUserFcmToken(app.idPropietario)
                    if (!targetToken.isNullOrBlank()) {
                        sendNotification(
                            app.nombre,
                            "TesterApp:${app.nombre} ",
                            "${profile?.devName ?: "Tester"}: $text",
                            targetToken,
                            viewModelScope,
                            iconUrl = app.icono
                        )
                    } else {
                        Log.w("sendMessage", "No se encontró FCM token para el propietario ${app.idPropietario}")
                    }
                } catch (e: Exception) {
                    Log.e("sendMessage", "Error enviando notificación: ${e.message}")
                }
            }
        }
    }


    private var communityAppsJob: Job? = null
    fun loadCommunityApps(context: android.content.Context? = null) {
        if (communityAppsJob?.isActive == true) return
        communityAppsJob = viewModelScope.launch {
            if (_communityApps.value.isEmpty()) {
                _isUploadingCommunityApps.value = true
            }
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                // Timeout de seguridad en caso de estar offline o conexión lenta
                val timeoutJob = launch {
                    delay(3000)
                    _isUploadingCommunityApps.value = false
                }

                repo.getAllTestAppsRealtime().collect { (apps, isFromCache) ->
                    _communityApps.value = apps
                    val categorizadas = com.appinitdev.tester.Funciones.clasificarApps(apps, uid)
                    _categoriasApps.value = categorizadas
                    context?.let { verificarAppsInstaladas(it, categorizadas.aceptadas) }

                    // Se quita el estado de carga al recibir los datos del servidor o si la caché ya contiene elementos
                    if (!isFromCache || apps.isNotEmpty()) {
                        timeoutJob.cancel()
                        _isUploadingCommunityApps.value = false
                    }
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error cargando apps: ${e.message}")
                _isUploadingCommunityApps.value = false
            }
        }
    }



    private fun actualizarAppLocalmente(appId: String, newTester: TesterData) {
        // 🔥 Usamos .update para evitar choques de lectura/escritura en el Flow
        _communityApps.update { listaActual ->
            listaActual.map { app ->
                if (app.id == appId) {
                    val nuevosTesters = app.testers.toMutableMap()
                    nuevosTesters[newTester.idUser] = newTester
                    app.copy(testers = nuevosTesters)
                } else {
                    app
                }
            }
        }

        _userApps.update { listaActual ->
            listaActual.map { app ->
                if (app.id == appId) {
                    val nuevosTesters = app.testers.toMutableMap()
                    nuevosTesters[newTester.idUser] = newTester
                    app.copy(testers = nuevosTesters)
                } else {
                    app
                }
            }
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        _categoriasApps.value = com.appinitdev.tester.Funciones.clasificarApps(_communityApps.value, uid)
    }




    suspend fun getProfileById(userId: String): UserProfile? {
        return repo.getUserProfileById(userId)
    }

    // --- Dentro de class AuthViewModel : ViewModel() ---

    /**
     * Obtiene el perfil completo de un usuario desde la colección "users"
     */


    fun rateTester(testerId: String, rating: Int) {
        viewModelScope.launch {
            val result = repo.rateTester(testerId, rating)
            result.onSuccess {
                Log.d("AuthViewModel", "Calificación enviada")
            }.onFailure { e ->
                Log.e("AuthViewModel", "Error al calificar: ${e.message}")
            }
        }
    }
    fun daysBetween(start: Long, end: Long): Long {
        val diff = end - start
        return diff / (24 * 60 * 60 * 1000) // Convierte milisegundos a días
    }

    suspend fun verificarAppsDiarias(context: Context) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        viewModelScope.launch {
            val perfil = repo.getUserProfile(userId) ?: return@launch
            val maxAppsPosibles = DevAppsConfig.developerApps.size.toDouble()

            // 1. Cálculo de Nivel
            val scoreNivel = (perfil.nivel / maxAppsPosibles).coerceAtMost(1.0) * 5.0 * 0.30

            // 2. Cálculo de Puntos
            val scorePuntos = (Math.log10(perfil.puntos.toDouble() + 1.0) / Math.log10(501.0))
                .coerceAtMost(1.0) * 5.0 * 0.25

            // 3. Cálculo de Estrellas (Confianza Bayesiana)
            val numVotos = perfil.calificaciones.size
            val confianza = (numVotos.toDouble() / (numVotos + 5)).coerceIn(0.0, 1.0)
            val scoreEstrellas = ((perfil.promedioEstrellas * confianza) + (3.0 * (1.0 - confianza))) * 0.45

            val nuevaReputacion = (scoreNivel + scorePuntos + scoreEstrellas + perfil.puntosExtra).coerceIn(1.0, 5.0)
            
            repo.updateAllUserAppsReputation(userId, nuevaReputacion, perfil.fcmToken)
            _userApps.value = _userApps.value.map { it.copy(reputacion = nuevaReputacion) }
        }

        val apps = repo.getAppsWhereIMTesterValidated()
        if (apps.isEmpty()) return

        val hoy = startOfToday()

        apps.forEach { app ->
            val tester = app.testers[userId] ?: return@forEach
            if (tester.validado != "Aceptado") return@forEach

            val yaCompleto = tester.completado
            val instalada = isAppInstalled(context, app.paquete)
            val ultimaRev = tester.ultimaRevision
            val diasPasados = if (ultimaRev == 0L) 1L else daysBetween(ultimaRev, hoy)

            val nuevosDias = when {
                yaCompleto -> 14
                !instalada -> 0
                diasPasados <= 0L -> tester.dias
                else -> (tester.dias + diasPasados.toInt()).coerceAtMost(14)
            }

            val acabaDeTerminar = !yaCompleto && nuevosDias >= 14
            val ganaPuntosProgreso = !yaCompleto && instalada && diasPasados > 0 && nuevosDias < 14

            val actualizaciones = mutableMapOf<String, Any>(
                "testers.$userId.dias" to nuevosDias,
                "testers.$userId.ultimaRevision" to hoy
            )

            if (acabaDeTerminar) {
                actualizaciones["testers.$userId.completado"] = true
            }

            viewModelScope.launch {
                repo.updateApp(app.id, actualizaciones).onSuccess {
                    if (acabaDeTerminar) {
                        val diff = (14 - tester.dias).coerceAtLeast(0)
                        if (diff > 0) otorgarPuntoPorPrueba(userId, diff.toLong())
                    } else if (ganaPuntosProgreso) {
                        otorgarPuntoPorPrueba(userId, diasPasados.toLong())
                    }
                }
            }
        }
        saveTodayCheck(context)
    }

    suspend fun migrarAppsParaAgregarDesarrollador() {
        val db = FirebaseFirestore.getInstance()

        try {
            Log.d("Migracion", "Iniciando migración de apps...")

            // 1. Obtenemos TODAS las apps de la colección
            val appsSnapshot = db.collection("apps").get().await()

            for (appDoc in appsSnapshot) {
                val appId = appDoc.id
                val idPropietario = appDoc.getString("idPropietario")

                // Verificamos si la app ya tiene el campo para no hacer trabajo doble
                val tieneNombre = appDoc.contains("nombreDesarrollador")

                if (idPropietario != null && !tieneNombre) {
                    // 2. Buscamos el perfil del dueño en la colección de usuarios
                    // Nota: Asegúrate de que tu colección de usuarios se llame "users" o cámbialo aquí
                    val userSnapshot = db.collection("users").document(idPropietario).get().await()

                    // Extraemos el nombre o ponemos uno por defecto si no existe
                    val devName = userSnapshot.getString("devName") ?: "Desconocido"

                    // 3. Actualizamos la app con el nuevo campo
                    db.collection("apps").document(appId)
                        .update("nombreDesarrollador", devName)
                        .await()

                    Log.d("Migracion", "App $appId actualizada con devName: $devName")
                }
            }

            Log.d("Migracion", "¡Migración completada con éxito!")

        } catch (e: Exception) {
            Log.e("Migracion", "Error durante la migración: ${e.message}")
        }
    }
    fun migrarAppsExistentes() {
        val db = FirebaseFirestore.getInstance()

        // 1. Obtenemos todas las apps que están en fase de test
        db.collection("apps")
            .whereEqualTo("status", "test")
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = db.batch()
                var contador = 0

                for (document in snapshot.documents) {
                    val app = document.toObject(AppModel::class.java)
                    if (app != null) {
                        // Extraemos todos los UIDs que ya están en el mapa de testers
                        val uidsEnTesters = app.testers.keys.toList()

                        if (uidsEnTesters.isNotEmpty()) {
                            val docRef = db.collection("apps").document(document.id)

                            // Actualizamos el nuevo campo 'allParticipants' con todos los IDs
                            // Usamos arrayUnion para no sobreescribir si ya hubiera alguno
                            batch.update(docRef, "allParticipants", uidsEnTesters)
                            contador++
                        }
                    }
                }

                // 2. Ejecutamos los cambios de forma atómica
                if (contador > 0) {
                    batch.commit()
                        .addOnSuccessListener {
                            Log.d("Migracionsd", "✔ Se actualizaron $contador aplicaciones con éxito.")
                        }
                        .addOnFailureListener { e ->
                            Log.e("Migracionsd", "✘ Error al ejecutar batch: ${e.message}")
                        }
                } else {
                    Log.d("Migracionsd", "No se encontraron apps para migrar.")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Migracion", "Error al obtener documentos: ${e.message}")
            }
    }
    /*
    fun debugRecalculateAllReputations() {
        viewModelScope.launch {
            Log.d("DEBUG_REPUTACION", "Iniciando recalculo global...")

            // 1. Obtenemos a TODOS los usuarios (Solo para debug)
            val allUsers = repo.getAllUsersListRaw()

            allUsers.forEach { perfil ->
                val userId = perfil.uid

                // --- TUS REGLAS DE CÁLCULO ---
                val maxAppsPosibles = 13.0 // Según tu DevAppsConfig

                // 1. Cálculo de Nivel
                val scoreNivel = (perfil.nivel / maxAppsPosibles).coerceAtMost(1.0) * 5.0 * 0.30

                // 2. Cálculo de Puntos
                val scorePuntos = (Math.log10(perfil.puntos.toDouble() + 1.0) / Math.log10(501.0))
                    .coerceAtMost(1.0) * 5.0 * 0.25

                // 3. Cálculo de Estrellas (Bayesiano)
                val numVotos = perfil.calificaciones.size
                val confianza = (numVotos.toDouble() / (numVotos + 5)).coerceIn(0.0, 1.0)
                val scoreEstrellas = ((perfil.promedioEstrellas * confianza) + (3.0 * (1.0 - confianza))) * 0.45

                // RESULTADO FINAL
                val nuevaReputacion = (scoreNivel + scorePuntos + scoreEstrellas + perfil.puntosExtra)
                    .coerceIn(1.0, 5.0)
                // -----------------------------

                // 2. Actualizamos en Firebase (Usando la función atómica que ya tienes)
                repo.updateAllUserAppsReputation(userId, nuevaReputacion, perfil.fcmToken)

                Log.d("DEBUG_REPUTACION", "Usuario: ${perfil.devName} -> Nueva Rep: $nuevaReputacion")
            }

            Log.d("DEBUG_REPUTACION", "Recalculo finalizado con éxito.")
        }
    }
    */
    // En AuthRepository.kt

    fun startOfToday(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun otorgarPuntoPorPrueba(userId: String, valor: Long = 1L) {
        viewModelScope.launch {
            repo.incrementUserPoints(userId, valor).onSuccess {
                Log.d("Puntos", "¡Usuario $userId ganó $valor punto(s)!")
                fetchUserProfile()
            }.onFailure { e ->
                Log.e("Puntos", "Error al otorgar puntos: ${e.message}")
            }
        }
    }


    // Añade esto dentro del init { ... } de tu AuthViewModel o llámalo manualmente
    fun startObservingGlobalLinks() {
        viewModelScope.launch {
            try {
                repo.getGlobalLinks().collect { links ->
                    _globalLinks.value = links
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error observing links: ${e.message}")
            }
        }
    }

    fun addGlobalLink(titulo: String, url: String, tag: String) {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            val nuevoLink = ComunidadLink(
                titulo = titulo,
                url = url,
                tag = tag,
                ownerId = uid
            )
            repo.addLink(nuevoLink)
        }
    }

    fun deleteGlobalLink(linkId: String) {
        viewModelScope.launch {
            repo.deleteLink(linkId)
        }
    }

    fun updateAppInfo(
        appId: String,
        nombre: String,
        paquete: String,       // Nuevo parámetro
        urlWeb: String,        // Nuevo parámetro
        urlAndroid: String,    // Nuevo parámetro
        instrucciones: String,
        notas: String,
        urlGoogleGroup: String,
        categoria: String
    ) {
        viewModelScope.launch {
            repo.updateApp(
                appId,
                mapOf(
                    "nombre" to nombre,
                    "paquete" to paquete,       // Se agrega al mapa
                    "urlWeb" to urlWeb,         // Se agrega al mapa
                    "urlAndroid" to urlAndroid, // Se agrega al mapa
                    "instrucciones" to instrucciones,
                    "urlGoogleGroup" to urlGoogleGroup,
                    "notas" to notas,
                    "categoria" to categoria
                )
            ).onSuccess {
                _uiState.value = AuthUiState.AppActionSuccess
                loadAndVerifyApps() // Recarga la lista para ver los cambios
            }.onFailure {
                // Opcional: manejar error con un estado de UI
            }
        }
    }

    // En AuthViewModel.kt

    fun checkAndSaveToken() {
        com.google.firebase.messaging.FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                viewModelScope.launch {
                    // Solo actualizamos si tenemos un usuario logueado
                    if (isUserLoggedIn()) {
                        repo.updateFcmToken(token)
                        Log.d("FCM", "Token actualizado en perfil: $token")
                    }
                }
            }
    }


    fun actualizarNivelPorInstalacionesFijas(context: Context) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            // Contamos basándonos en la lista de objetos DevAppItem
            val conteo = DevAppsConfig.developerApps.count {
                isAppInstalled(context, it.paquete)
            }
            val nuevoNivel = if (conteo == 0) 1.0 else conteo.toDouble()

            FirebaseFirestore.getInstance().collection("users").document(userId)
                .update("nivel", nuevoNivel)
                .await()
            fetchUserProfile()
        }
    }

    fun registrarTokenFCM() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result ?: return@addOnCompleteListener
                viewModelScope.launch {
                    repo.updateFcmToken(token)
                }
            }
        }
    }

    fun notifyNewAppToRandomTesters(context: Context) {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()

                // 1. Buscamos usuarios que tengan token de FCM
                // Limitamos a un número mayor (ej. 50) para poder elegir 20 al azar localmente
                val snapshot = db.collection("users")
                    .whereNotEqualTo("fcmToken", null)
                    .get()
                    .await()

                val allUsers = snapshot.documents
                if (allUsers.isEmpty()) return@launch

                // 2. Seleccionamos hasta 20 usuarios de forma aleatoria
                val randomUsers = allUsers

                val titulo =
                    context.getString(R.string.bienvenido_a_testerapp) // O un string nuevo como "Nueva App disponible"
                val cuerpo =
                    "${context.getString(R.string.prueba_otras_apps_y_sube_las_tuyas)}"

                // 3. Enviamos las notificaciones una por una
                randomUsers.forEach { userDoc ->
                    val token = userDoc.getString("fcmToken")
                    if (!token.isNullOrBlank()) {
                        try {
                            // Usamos tu función existente sendNotification
                            sendNotification(
                                "TesterApp",
                                titulo,
                                cuerpo,
                                token,
                                viewModelScope
                            )
                        } catch (e: Exception) {
                            // Validación: Si no se puede enviar a un usuario específico, se ignora
                            Log.e("Notification", "Error enviando a ${userDoc.id}: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignora errores generales del proceso
                Log.e("Notification", "Error general en proceso de notificación: ${e.message}")
            }
        }
    }

    fun loadRanking() {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                // Traemos solo usuarios que hayan completado su perfil
                val snapshot = db.collection("users")
                    .whereEqualTo("perfilCompletado", true)
                    .orderBy("reputacion", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(15)
                    .get()
                    .await()

                val usersList = snapshot.toObjects(UserProfile::class.java)
                Log.d("Ranking", "Usuarios encontrados: ${usersList.size}")
                _allUsers.value = usersList
            } catch (e: Exception) {
                Log.e("RankingError", "Error al cargar ranking: ${e.message}")
            }
        }
    }
    fun loadFixedAppsIcons(packages: List<String>) = viewModelScope.launch {
        packages.forEach { packageName ->
            if (!_fixedAppsIcons.value.containsKey(packageName)) {
                launch {
                    val icon = repo.fetchAppIcon(packageName)
                    if (icon != null) {
                        _fixedAppsIcons.update { it + (packageName to icon) }
                    }
                }
            }
        }
    }

    // En AuthViewModel.kt
    fun uploadProfilePicture(uri: android.net.Uri, onSuccess: (String) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().reference
            .child("profile_pictures/$uid.jpg")

        _uiState.value = AuthUiState.Loading

        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    onSuccess(downloadUri.toString())
                    _uiState.value = AuthUiState.Success
                }
            }
            .addOnFailureListener {
                _uiState.value = AuthUiState.Error("Error al subir imagen")
            }
    }

    // 1. EL TESTER SUBE LA EVIDENCIA
    fun subirEvidenciaResena(
        appId: String,
        uri: android.net.Uri,
        userName: String,
        appName: String?,
        token: String?,
        tokenApp: String?,
        onSuccess: () -> Unit
    ) {

        _isUploading.value = true
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        _uiState.value = AuthUiState.Loading

        // Referencia: production_reviews/appId/userId.jpg
        val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().reference
            .child("production_reviews/$appId/$userId.jpg")

        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val url = downloadUri.toString()

                    val nuevaResena = ResenaProduccion(
                        userId = userId,
                        userName = userName,
                        evidenciaUrl = url,
                        estado = "pendiente",
                        appName=appName,
                        fcmTokenTester = token,
                        fcmTokenApp = tokenApp
                    )

                    // Actualizamos Firestore: app.resenasProduccion.userId = Objeto
                    FirebaseFirestore.getInstance().collection("apps").document(appId)
                        .update("resenasProduccion.$userId", nuevaResena)
                        .addOnSuccessListener {
                            _uiState.value = AuthUiState.Success
                            loadProductionApps() // Recargar lista para actualizar UI
                            _isUploading.value = false
                            _featuredApps.value = _featuredApps.value.map { app ->
                                if (app.id == appId) {
                                    // Convertimos el mapa actual a mutable y reemplazamos solo la reseña del usuario
                                    val mapaActualizado = app.resenasProduccion.toMutableMap()
                                    mapaActualizado[userId] = nuevaResena // Guardamos el objeto completo, no solo el estado
                                    app.copy(resenasProduccion = mapaActualizado)
                                } else app
                            }

                            _uiState.value = AuthUiState.Success
                            onSuccess()
                            viewModelScope.launch {
                                try {
                                    val app = _communityApps.value.find { it.id == appId }
                                        ?: _productionApps.value.find { it.id == appId }
                                    var ownerId = app?.idPropietario ?: ""
                                    if (ownerId.isBlank()) {
                                        val appDoc = FirebaseFirestore.getInstance().collection("apps").document(appId).get().await()
                                        ownerId = appDoc.getString("idPropietario") ?: ""
                                    }
                                    if (ownerId.isNotBlank()) {
                                        val appTitle = appName ?: "App"
                                        sendInAppAndPushNotification(
                                            targetUserId = ownerId,
                                            title = getString(R.string.nueva_resena_titulo, appTitle),
                                            body = getString(R.string.nueva_resena_msg, userName),
                                            appId = appId,
                                            iconUrl = app?.icono
                                        )
                                    }
                                } catch (e: Exception) {
                                    Log.e("subirEvidenciaResena", "Error notificando al propietario: ${e.message}")
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            _isUploading.value = false
                            _uiState.value = AuthUiState.Error(e.message)
                        }
                }
            }
            .addOnFailureListener {
                _uiState.value = AuthUiState.Error(null)
            }
    }

    fun aprobarResenaProduccion(appId: String, testerId: String, resenaProduccion: ResenaProduccion) {
        val db = FirebaseFirestore.getInstance()
        val appRef = db.collection("apps").document(appId)
        val userRef = db.collection("users").document(testerId)

        viewModelScope.launch {
            try {
                db.runTransaction { transaction ->
                    val snapshot = transaction.get(appRef)
                    val resenas = snapshot.get("resenasProduccion") as? Map<String, Any> ?: emptyMap()
                    val datosResena = resenas[testerId] as? Map<String, Any>
                    val estadoActual = datosResena?.get("estado") as? String

                    if (estadoActual != "pendiente") {
                        throw Exception("Reseña ya procesada")
                    }

                    transaction.update(appRef, "resenasProduccion.$testerId.estado", "aprobada")
                    transaction.update(userRef, "puntos", com.google.firebase.firestore.FieldValue.increment(10))
                }.await()

                val appTitle = resenaProduccion.appName ?: "App"
                sendInAppAndPushNotification(
                    targetUserId = testerId,
                    title = getString(R.string.resena_aprobada_titulo),
                    body = getString(R.string.resena_aprobada_msg, appTitle),
                    appId = appId
                )

                loadAndVerifyApps()
            } catch (e: Exception) {
                Log.e("Reviews", "Transacción fallida o abortada: ${e.message}")
            }
        }
    }
//    fun aprobarResenaProduccion(appId: String, testerId: String, resenaProduccion: ResenaProduccion) {
//        val db = FirebaseFirestore.getInstance()
//        val appRef = db.collection("apps").document(appId)
//        val userRef = db.collection("users").document(testerId)
//
//        db.runTransaction { transaction ->
//            // A. Cambiar estado a "aprobada" en la App
//            transaction.update(appRef, "resenasProduccion.$testerId.estado", "aprobada")
//
//            // B. Dar +10 puntos al usuario (Tester)
//            // Usamos increment para evitar condiciones de carrera
//            transaction.update(userRef, "puntos", com.google.firebase.firestore.FieldValue.increment(10))
//            if (resenaProduccion.fcmTokenTester.isNullOrEmpty()) {
//                sendNotification(
//                    app = resenaProduccion.appName?:"App",
//                    title = "¡Reseña Aprobada!",
//                    msg = "Has ganado 10 puntos en "+"$resenaProduccion.appName",
//                    token = resenaProduccion.fcmTokenTester,
//                    scope = viewModelScope
//                )
//            }
//        }.addOnSuccessListener {
//            Log.d("Reviews", "Reseña aprobada y 10 puntos entregados")
//            // Recargamos si estamos viendo la pantalla de detalle
//            loadAndVerifyApps()
//        }.addOnFailureListener { e ->
//            Log.e("Reviews", "Error en transacción", e)
//        }
//    }




    fun insertarAppsEstaticas() {
        val db = FirebaseFirestore.getInstance()
        val ownerId = "TLaAvv2YGObm93iLGM8xqLZOETk2"
        val tokenFijo = ""

        // Lista de tus aplicaciones (Nombre, Paquete)
        val appsEstaticas = listOf(
            Pair("Color Palette", "com.appinitdev.color.free"),
            Pair("Canvas", "com.appinitdev.canvas.free"),
            Pair("Notepad", "com.appinitdev.notes.premium"),
            Pair("ChatOk", "com.appinitdev.chatok"),
            Pair("ElementalHub", "com.appinitdev.periodictable"),
            Pair("POSManager", "com.appinitdev.posmanager.free"),
            Pair("Music Player & Equalizer", "com.appinitdev.music"),
            Pair("ImageTools", "com.appinitdev.imagetools")
        )

        appsEstaticas.forEach { (nombre, paquete) ->
            val appId = db.collection("apps").document().id // Genera ID único para Firestore

            val appData = AppModel(
                id = appId,
                nombre = nombre,
                paquete = paquete,
                idPropietario = ownerId,
                status = "produccion", // Para que aparezcan en la zona de recompensas
                fechaCreacion = System.currentTimeMillis(),
                fcmToken = tokenFijo, // El token que me pasaste
                urlAndroid = "https://play.google.com/store/apps/details?id=$paquete",
                urlWeb = "https://play.google.com/apps/testing/$paquete",
                instrucciones = "Descarga la app, deja una reseña positiva de 5 estrellas y sube la captura aquí.",

                )

            db.collection("apps").document(appId).set(appData)
                .addOnSuccessListener {
                    Log.d("InsertBatch", "App insertada: $nombre")
                }
                .addOnFailureListener { e ->
                    Log.e("InsertBatch", "Error insertando $nombre: ${e.message}")
                }
        }
    }



    fun lanzarFlujoPago(activity: Activity, productId: String) {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList != null) {

                // 1. Usamos métodos nativos de Java para máxima compatibilidad
                val productDetails = if (productDetailsList.productDetailsList.size > 0) {
                    productDetailsList.productDetailsList.get(0)
                } else {
                    null
                }

                if (productDetails != null) {
                    val productDetailsParamsList = listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .build()
                    )

                    val flowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(productDetailsParamsList)
                        .build()

                    // 2. Lanzamos la ventana de pago
                    billingClient.launchBillingFlow(activity, flowParams)
                } else {
                    Log.e("Billing", "No se encontró el producto en la tienda: $productId")
                }
            } else {
                Log.e("Billing", "Error de Google Play: ${billingResult.debugMessage}")
            }
        }
    }

    fun iniciarBilling(context: Context) {
        billingClient = BillingClient.newBuilder(context)
            .setListener { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    for (purchase in purchases) {
                        gestionarCompraConsumible(purchase)
                    }
                }
            }
            // Así se corrige el error:
            .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                // Conexión lista para procesar compras
            }
            override fun onBillingServiceDisconnected() {
                // Reintentar conexión si es necesario
            }
        })
    }

    private fun gestionarCompraConsumible(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {

            // 1. Parámetros para consumir la compra
            val consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

            // 2. Ejecutar el consumo en Google Play
            billingClient.consumeAsync(consumeParams) { billingResult, _ ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // 3. Solo si Google confirma el consumo, damos los puntos en Firebase
                    otorgarPuntosPorCompra(30, purchase.orderId ?: "compra_${System.currentTimeMillis()}")
                    _uiState.value = AuthUiState.PurchaseSuccess
                }
            }
        }
    }


    private fun otorgarPuntosPorCompra(puntos: Int, orderId: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(uid)

            db.runTransaction { transaction ->
                // Sumar puntos de forma segura
                val valorExtra = puntos * 0.25
                transaction.update(userRef, "puntos", FieldValue.increment(puntos.toLong()))
                transaction.update(userRef, "puntosExtra", FieldValue.increment(valorExtra))
                // Registrar ID de orden para auditoría
                val registroRef = db.collection("compras").document(orderId)
                transaction.set(registroRef, mapOf(
                    "userId" to uid,
                    "puntos" to puntos,
                    "puntosExtra" to valorExtra,
                    "fecha" to System.currentTimeMillis()
                ))
            }.addOnSuccessListener {
                fetchUserProfile() // Actualizar UI
            }
        }
    }



    private var featuredAppsJob: Job? = null
    fun loadFeaturedApps() {
        if (featuredAppsJob?.isActive == true) return
        featuredAppsJob = viewModelScope.launch {
            repo.getFeaturedProductionAppsRealtime().collect { apps ->
                _featuredApps.value = apps
            }
        }
    }

    fun loadCommunityStats() {
        viewModelScope.launch {
            try {
                // Ejecutamos en paralelo para mayor velocidad
                val appsJob = async { repo.getProductionAppsCount() }
                val testersJob = async { repo.getTestersCount() }

                _productionCount.value = appsJob.await()
                _testersCount.value = testersJob.await()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error al cargar stats: ${e.message}")
            }
        }
    }

//    fun actualizarEstadoResena(appId: String, testerId: String, nuevoEstado: String) {
//        viewModelScope.launch {
//            // 1. Iniciamos el estado de carga
//            _uiState.value = AuthUiState.Loading
//
//            try {
//                val db = FirebaseFirestore.getInstance()
//
//                // Actualizamos en Firebase
//                db.collection("apps").document(appId)
//                    .update("resenasProduccion.$testerId.estado", nuevoEstado)
//                    .await() // Esperamos a que termine la red
//
//                // 2. IMPORTANTE: Si tienes un listener activo (snapshot),
//                // la UI se actualizará sola. Si no, deberías recargar la app aquí.
//
//                _uiState.value = AuthUiState.Success // O AuthUiState.Idle
//            } catch (e: Exception) {
//                Log.e("AuthViewModel", "Error: ${e.message}")
//                _uiState.value = AuthUiState.Error(e.message ?: "Error desconocido")
//            } finally {
//                // 3. ESTO QUITA EL CÍRCULO DE CARGA PASE LO QUE PASE
//                _uiState.value = AuthUiState.Idle
//            }
//        }
//    }

    fun actualizarEstadoResena(appId: String, testerId: String, nuevoEstado: String) {
        viewModelScope.launch {
           // _uiState.value = AuthUiState.Loading // Empieza carga
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("apps").document(appId)
                    .update("resenasProduccion.$testerId.estado", nuevoEstado)
                    .await()

                // 🔥 LA CLAVE: Actualizar la lista local manualmente para que Compose lo vea
                _userApps.value = _userApps.value.map { currentApp ->
                    if (currentApp.id == appId) {
                        val mapaEditado = currentApp.resenasProduccion.toMutableMap().apply {
                            // Buscamos la reseña existente y solo le cambiamos el estado
                            this[testerId] = this[testerId]?.copy(estado = nuevoEstado)
                                ?: ResenaProduccion(estado = nuevoEstado)
                        }
                        currentApp.copy(resenasProduccion = mapaEditado)
                    } else {
                        currentApp
                    }
                }
                _featuredApps.value = _featuredApps.value.map { currentApp ->
                    if (currentApp.id == appId) {
                        val mapaEditado = currentApp.resenasProduccion.toMutableMap().apply {
                            this[testerId] = this[testerId]?.copy(estado = nuevoEstado) ?: ResenaProduccion(estado = nuevoEstado)
                        }
                        currentApp.copy(resenasProduccion = mapaEditado)
                    } else currentApp
                }

                _uiState.value = AuthUiState.Success
                _uiState.value = AuthUiState.Idle // Detiene el cargando
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Error")
            } finally {
                _uiState.value = AuthUiState.Idle // Asegura que el circulo desaparezca
            }
        }
    }

    fun clearChat() {
        stopListeningToMessages()
    }

    fun clearBugReports() {
        _bugReports.value = emptyList()
    }

    fun clearAppDetailState() {
        stopListeningToMessages()
        clearBugReports()
    }

    // --- NUEVAS FUNCIONALIDADES ---

    fun startObservingNotifications() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                repo.getUserNotificationsRealtime(uid).collect {
                    _notifications.value = it
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error observing notifications: ${e.message}")
            }
        }
    }

    fun markNotificationAsRead(id: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        _notifications.update { list ->
            list.map { if (it.id == id) it.copy(isRead = true) else it }
        }
        viewModelScope.launch {
            repo.markNotificationAsRead(uid, id)
        }
    }

    fun markAllNotificationsAsRead() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        _notifications.update { list ->
            list.map { it.copy(isRead = true) }
        }
        viewModelScope.launch {
            repo.markAllNotificationsAsRead(uid)
        }
    }

    fun clearAllNotifications() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        _notifications.value = emptyList()
        viewModelScope.launch {
            repo.clearAllNotifications(uid)
        }
    }

    fun sendInAppAndPushNotification(
        targetUserId: String,
        title: String,
        body: String,
        appId: String? = null,
        iconUrl: String? = null
    ) {
        if (targetUserId.isBlank()) return
        viewModelScope.launch {
            try {
                val userNotif = UserNotification(
                    id = java.util.UUID.randomUUID().toString(),
                    userId = targetUserId,
                    title = title,
                    body = body,
                    appId = appId,
                    isRead = false,
                    timestamp = System.currentTimeMillis()
                )
                repo.saveInAppNotification(targetUserId, userNotif)

                val targetToken = getUserFcmToken(targetUserId)
                if (!targetToken.isNullOrBlank()) {
                    sendNotification(appId ?: "TesterApp", title, body, targetToken, viewModelScope, iconUrl = iconUrl)
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error sending in-app & push notification: ${e.message}")
            }
        }
    }

    fun submit14DayFeedback(
        appId: String,
        ratingEstabilidad: Float,
        ratingDiseno: Float,
        comentarios: String,
        sugerencias: String,
        onSuccess: () -> Unit
    ) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val profile = _userProfile.value
        val feedback = com.appinitdev.tester.model.FeedbackReport14Days(
            id = java.util.UUID.randomUUID().toString(),
            appId = appId,
            userId = user.uid,
            userName = profile?.devName ?: "Tester",
            ratingEstabilidad = ratingEstabilidad,
            ratingDiseno = ratingDiseno,
            modeloDispositivo = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}",
            versionAndroid = "Android ${android.os.Build.VERSION.RELEASE} (SDK ${android.os.Build.VERSION.SDK_INT})",
            comentarios = comentarios,
            sugerencias = sugerencias,
            timestamp = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repo.submit14DayFeedback(appId, feedback).onSuccess {
                onSuccess()
            }
        }
    }

    fun load14DayFeedbacks(appId: String) {
        _feedbackReports14Days.value = emptyList()
        viewModelScope.launch {
            _feedbackReports14Days.value = repo.get14DayFeedbacks(appId)
        }
    }

    fun submitBugReport(appId: String, deviceInfo: String, steps: String, description: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val profile = _userProfile.value ?: return
        val bug = BugReport(
            id = java.util.UUID.randomUUID().toString(),
            appId = appId,
            userId = uid,
            userName = profile.devName,
            deviceInfo = deviceInfo,
            stepsToReproduce = steps,
            description = description
        )
        viewModelScope.launch {
            repo.submitBugReport(bug).onSuccess {
                _uiState.value = AuthUiState.AppActionSuccess
            }
        }
    }

    fun loadBugReports(appId: String) {
        _bugReports.value = emptyList()
        viewModelScope.launch {
            _bugReports.value = repo.getBugReports(appId)
        }
    }

    fun updateBugStatus(bugId: String, appId: String, newStatus: String, testerUserId: String?) {
        viewModelScope.launch {
            val updates = mapOf("status" to newStatus)
            FirebaseFirestore.getInstance().collection("bug_reports").document(bugId).update(updates).await()
            
            if (!testerUserId.isNullOrBlank()) {
                sendInAppAndPushNotification(
                    targetUserId = testerUserId,
                    title = getString(R.string.bug_actualizado_titulo),
                    body = getString(R.string.bug_actualizado_msg, newStatus),
                    appId = appId
                )
            }
            loadBugReports(appId)
        }
    }

    fun checkStreaks() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val profile = _userProfile.value ?: return
        val hoy = startOfToday()
        val ayer = hoy - (24 * 60 * 60 * 1000L)

        viewModelScope.launch {
            if (profile.lastActive < hoy) {
                val newStreaks = if (profile.lastActive >= ayer) profile.streaks + 1 else 1
                repo.updateStreaksAndLastActive(uid, newStreaks, System.currentTimeMillis())
                fetchUserProfile()
            }
        }
    }

    fun updateChecklist(appId: String, items: List<ChecklistItem>) {
        viewModelScope.launch {
            repo.updateChecklist(appId, items).onSuccess {
                loadAndVerifyApps()
            }
        }
    }

    fun boostApp(appId: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val profile = _userProfile.value ?: return
        if (profile.puntos >= 50) { 
            viewModelScope.launch {
                addPointsWithHistory(uid, -50, "App Boost: $appId")
                val expires = System.currentTimeMillis() + (3 * 24 * 60 * 60 * 1000L)
                repo.updateAppBoost(appId, true, expires).onSuccess {
                    loadAndVerifyApps()
                }
            }
        }
    }

    private suspend fun addPointsWithHistory(userId: String, amount: Int, reason: String) {
        repo.incrementUserPoints(userId, amount.toLong()).onSuccess {
            val transaction = PointsTransaction(
                id = java.util.UUID.randomUUID().toString(),
                userId = userId,
                amount = amount,
                reason = reason
            )
            repo.addPointsTransaction(transaction)
            fetchUserProfile()
        }
    }

    fun loadPointsHistory() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            _pointsHistory.value = repo.getPointsHistory(uid)
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun applyReferralCode(code: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val profile = _userProfile.value ?: return
        if (profile.referredBy != null) return // Solo se puede referir una vez

        viewModelScope.launch {
            val allUsers = repo.getAllUsersListRaw()
            val referrer = allUsers.find { it.referralCode == code }
            if (referrer != null && referrer.uid != uid) {
                // Dar puntos a ambos
                repo.incrementUserPoints(referrer.uid, 20)
                repo.incrementUserPoints(uid, 10)
                FirebaseFirestore.getInstance().collection("users").document(uid).update("referredBy", referrer.uid).await()
                fetchUserProfile()
            }
        }
    }

    fun generateReferralCode() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val profile = _userProfile.value ?: return
        if (profile.referralCode.isNotBlank()) return

        val newCode = profile.devName.take(3).uppercase() + (1000..9999).random()
        viewModelScope.launch {
            FirebaseFirestore.getInstance().collection("users").document(uid).update("referralCode", newCode).await()
            fetchUserProfile()
        }
    }
}

// Estados sellados para manejar la UI
sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    object RegisterSuccess : AuthUiState()
    object NeedsProfile : AuthUiState()
    object PurchaseSuccess : AuthUiState()
    data class Error(val msg: String?) : AuthUiState()
    object AppActionSuccess : AuthUiState()
}
