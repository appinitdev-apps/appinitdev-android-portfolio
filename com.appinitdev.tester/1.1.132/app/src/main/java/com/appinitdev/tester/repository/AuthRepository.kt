

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Login
    suspend fun login(email: String, pass: String): Result<String> {
        return try {
            auth.signInWithEmailAndPassword(email, pass).await()
            Result.success("Login Exitoso")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Registro
    suspend fun register(email: String, pass: String): Result<String> {
        return try {
            // Ahora 'auth' ya será reconocido aquí
            val result = auth.createUserWithEmailAndPassword(email, pass).await()

            result.user?.sendEmailVerification()?.await()
            auth.signOut()

            Result.success("Registro exitoso. Verifica tu correo.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isEmailVerified(): Boolean {
        val user = auth.currentUser
        return try {
            user?.reload()?.await() // Actualiza el estado desde el servidor
            user?.isEmailVerified ?: false
        } catch (e: Exception) {
            false
        }
    }

    // Recuperar Contraseña
    suspend fun resetPassword(email: String): Result<String> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success("Correo de recuperación enviado")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(uid: String): UserProfile? {
        return try {
            val snapshot = db.collection("users").document(uid).get().await()
            snapshot.toObject(UserProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            db.collection("users").document(profile.uid).set(profile).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(uid: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            db.collection("users").document(uid).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun rateUser(targetUid: String, rating: Int): Result<Unit> {
        return try {
            val watcherUid = auth.currentUser?.uid ?: throw Exception("No logueado")
            val userRef = db.collection("users").document(targetUid)

            // Actualizamos solo el campo 'calificaciones' dentro del mapa
            // Esto añade el UID del que califica o actualiza su nota si ya existía
            userRef.update("calificaciones.$watcherUid", rating).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkIfPackageExists(paquete: String): Boolean {
        return try {
            val snapshot = db.collection("apps")
                .whereEqualTo("paquete", paquete)
                .limit(1) // Solo necesitamos saber si hay al menos una
                .get()
                .await()

            !snapshot.isEmpty // Devuelve true si el paquete YA EXISTE
        } catch (e: Exception) {
            false // En caso de error de conexión, asumimos que no existe o manejamos el error
        }
    }
    // Añade esto a AuthRepository.kt
    suspend fun createApp(app: AppModel): Result<Unit> {
        return try {
            // Validar si el paquete ya existe
            val query = db.collection("apps")
                .whereEqualTo("paquete", app.paquete)
                .get().await()

            if (!query.isEmpty) throw Exception("El ID de paquete ya está registrado")

            db.collection("apps").document(app.id).set(app).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserApps(uid: String): List<AppModel> {
        return try {
            db.collection("apps")
                .whereEqualTo("idPropietario", uid)
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get().await()
                .toObjects(AppModel::class.java)

        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getUserAppsRealtime(uid: String): Flow<List<AppModel>> = callbackFlow {
        val listener = db.collection("apps")
            .whereEqualTo("idPropietario", uid)
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val apps = snapshot?.toObjects(AppModel::class.java) ?: emptyList()

                // Ordenar localmente por estatus: 1. test, 2. produccion, 3. oculta
                val sortedApps = apps.sortedWith(
                    compareBy<AppModel> {
                        when (it.status) {
                            "test" -> 0
                            "produccion" -> 2
                            "oculta" -> 1
                            else -> 3
                        }
                    }.thenByDescending { it.fechaCreacion }
                )

                trySend(sortedApps)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getAppsByStatus(status: String): List<AppModel> {
        return try {
            db.collection("apps")
                .whereEqualTo("status", status)
                .get().await()
                .toObjects(AppModel::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateAppTesters(appId: String, testerId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            val formattedUpdates = updates.mapKeys { "testers.$testerId.${it.key}" }
            db.collection("apps").document(appId).update(formattedUpdates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAppStatus(appId: String, newStatus: String, iconUrl: String? = null): Result<Unit> {
        return try {
            val nuevaFecha = System.currentTimeMillis()

            // Creamos un mapa con los datos básicos
            val updates = mutableMapOf<String, Any>(
                "status" to newStatus,
                "fechaActualizacion" to nuevaFecha
            )

            // SI el icono no es nulo, lo agregamos al mapa de actualización
            if (iconUrl != null) {
                updates["icono"] = iconUrl
            }

            // Enviamos todo el mapa a Firestore en una sola operación
            db.collection("apps").document(appId)
                .update(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Para que un usuario se postule como tester
    suspend fun applyAsTester(appId: String, testerData: TesterData): Result<Unit> {
        return try {
            val uid = testerData.idUser
            val updates = mutableMapOf<String, Any>(
                "testers.$uid" to testerData,
                "allParticipants" to com.google.firebase.firestore.FieldValue.arrayUnion(uid)
            )

            if (testerData.validado == "Aceptado") {
                updates["testerIds"] = com.google.firebase.firestore.FieldValue.arrayUnion(uid)
            }

            db.collection("apps").document(appId)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Para que un usuario deje de ser tester de una app
    suspend fun leaveAppAsTester(appId: String, testerId: String): Result<Unit> {
        return try {
            val updates = mapOf<String, Any>(
                "testers.$testerId" to com.google.firebase.firestore.FieldValue.delete(),
                "testerIds" to com.google.firebase.firestore.FieldValue.arrayRemove(testerId),
                "allParticipants" to com.google.firebase.firestore.FieldValue.arrayRemove(testerId)
            )

            db.collection("apps").document(appId)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTesterStatus(
        appId: String,
        testerId: String,
        newStatus: String,
    ): Result<Unit> {
        return try {
            val appRef = db.collection("apps").document(appId)
            val updates = mutableMapOf<String, Any>(
                "testers.$testerId.validado" to newStatus
            )

            when (newStatus) {
                "Aceptado" -> {
                    updates["testerIds"] =
                        com.google.firebase.firestore.FieldValue.arrayUnion(testerId)
                }
                else -> {
                    // Para Rechazado, Solicitud, removido, etc.
                    updates["testerIds"] =
                        com.google.firebase.firestore.FieldValue.arrayRemove(testerId)
                }
            }
            appRef.update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    fun getMessages(appId: String): Flow<List<Message>> = callbackFlow {
        val listener = db.collection("apps").document(appId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val msgs = snapshot?.toObjects(Message::class.java) ?: emptyList()
                trySend(msgs)
            }
        awaitClose { listener.remove() }
    }

    suspend fun sendMessage(appId: String, message: Message): Result<Unit> {
        return try {
            db.collection("apps").document(appId)
                .collection("messages").add(message).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


//    suspend fun getAllTestApps(): List<AppModel> {
//        return try {
//            val currentUid = auth.currentUser?.uid ?: ""
//            val snapshot = db.collection("apps")
//                .whereEqualTo("status", "test")
//                .orderBy("reputacion", Query.Direction.DESCENDING)
//                .get().await()
//
//            snapshot.toObjects(AppModel::class.java)//Adrian.filter { it.idPropietario != currentUid }
//        } catch (e: Exception) {
//            emptyList()
//        }
//    }

    fun getAllTestAppsRealtime(): Flow<Pair<List<AppModel>, Boolean>> = callbackFlow {
        val listener = db.collection("apps")
            .whereEqualTo("status", "test")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("DEBUG_APPS", "Error en Firestore listener: ${error.message}", error)
                    close(error)
                    return@addSnapshotListener
                }
                val isFromCache = snapshot?.metadata?.isFromCache ?: false
                val apps = (snapshot?.toObjects(AppModel::class.java) ?: emptyList())
                    .sortedByDescending { it.fechaActualizacion.takeIf { f -> f > 0 } ?: it.fechaCreacion }
                Log.d("DEBUG_APPS", "Firestore Listener -> Total apps obtenidas con status=test: ${apps.size} | esDeCaché: $isFromCache")
                trySend(Pair(apps, isFromCache))
            }
        awaitClose { listener.remove() }
    }

    suspend fun getAllTestApps(): List<AppModel> = withContext(Dispatchers.IO) {
    try {
        val snapshot = db.collection("apps")
            .whereEqualTo("status", "test")
            .orderBy("reputacion", Query.Direction.DESCENDING)
            .get().await()

        snapshot.toObjects(AppModel::class.java)
    } catch (e: Exception) {
        emptyList()
    }
}

    suspend fun getUserProfileById(userId: String): UserProfile? {
        return try {
            val snapshot = db.collection("users").document(userId).get().await()
            snapshot.toObject(UserProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun rateTester(testerId: String, rating: Int): Result<Unit> {
        return try {
            val myUid = auth.currentUser?.uid ?: return Result.failure(Exception("No auth"))
            val userRef = db.collection("users").document(testerId)

            db.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val currentProfile = snapshot.toObject(UserProfile::class.java)
                    ?: throw Exception("Perfil no encontrado")

                val newRatings = currentProfile.calificaciones.toMutableMap()
                newRatings[myUid] = rating

                transaction.update(userRef, "calificaciones", newRatings)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAppsWhereIMTesterValidated(): List<AppModel> {
        return try {
            val uid = auth.currentUser?.uid ?: return emptyList()

            val snapshot = db.collection("apps")
                .whereEqualTo("status", "test")
                .whereArrayContains("allParticipants", uid)
                //.whereArrayContains("testerIds", uid)
                .get()
                .await()

            snapshot.toObjects(AppModel::class.java)
                .filter {
//                    it.testers[uid]?.validado == "Aceptado" &&
                            it.testers[uid]?.idUser == uid
                }

        } catch (e: Exception) {
            emptyList()
        }
    }

    // Agregar un link a una subcolección dentro de la App
    fun getGlobalLinks(): Flow<List<ComunidadLink>> = callbackFlow {
        val collection = db.collection("links_comunidad")
        val subscription = collection.addSnapshotListener { snapshot, _ ->
            val links = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(ComunidadLink::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            trySend(links)
        }
        awaitClose { subscription.remove() }
    }

    suspend fun addLink(link: ComunidadLink): Result<Unit> {
        return try {
            val collection = db.collection("links_comunidad")
            collection.add(link).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteLink(linkId: String): Result<Unit> {
        return try {
            val collection = db.collection("links_comunidad")
            collection.document(linkId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- NUEVAS FUNCIONALIDADES ---

    suspend fun submitBugReport(bugReport: BugReport): Result<Unit> {
        return try {
            db.collection("bug_reports").document(bugReport.id).set(bugReport).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBugReports(appId: String): List<BugReport> {
        return try {
            db.collection("bug_reports")
                .whereEqualTo("appId", appId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get().await()
                .toObjects(BugReport::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun submitSurveyResponse(response: SurveyResponse): Result<Unit> {
        return try {
            db.collection("survey_responses").document(response.id).set(response).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserNotifications(userId: String): Flow<List<UserNotification>> = callbackFlow {
        val listener = db.collection("notifications")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val notifications = snapshot?.toObjects(UserNotification::class.java) ?: emptyList()
                trySend(notifications)
            }
        awaitClose { listener.remove() }
    }

    suspend fun markNotificationAsRead(notificationId: String): Result<Unit> {
        return try {
            db.collection("notifications").document(notificationId)
                .update("isRead", true).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAppBoost(appId: String, isBoosted: Boolean, expires: Long): Result<Unit> {
        return try {
            db.collection("apps").document(appId)
                .update(mapOf("isBoosted" to isBoosted, "boostExpires" to expires)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateChecklist(appId: String, checklist: List<ChecklistItem>): Result<Unit> {
        return try {
            db.collection("apps").document(appId)
                .update("checklist", checklist).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateStreaksAndLastActive(userId: String, streaks: Int, lastActive: Long): Result<Unit> {
        return try {
            db.collection("users").document(userId)
                .update(mapOf("streaks" to streaks, "lastActive" to lastActive)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addPointsTransaction(transaction: PointsTransaction): Result<Unit> {
        return try {
            db.collection("points_history").document(transaction.id).set(transaction).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPointsHistory(userId: String): List<PointsTransaction> {
        return try {
            db.collection("points_history")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get().await()
                .toObjects(PointsTransaction::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }


// En AuthRepository.kt

    suspend fun updateTesterDaysAndPoints(
        appId: String,
        userId: String,
        nuevosDias: Int,
        hoy: Long,
        otorgarPunto: Boolean
    ): Result<Unit> {
        return try {
            val appRef = db.collection("apps").document(appId)
            val userRef = db.collection("users").document(userId)

            // Actualizar los días en la aplicación
            appRef.update(
                mapOf(
                    "testers.$userId.dias" to nuevosDias,
                    "testers.$userId.ultimaRevision" to hoy
                )
            ).await()

            // Si ganoPunto es true, incrementamos el contador en el perfil del usuario
            if (otorgarPunto) {
                userRef.update(
                    "puntos",
                    com.google.firebase.firestore.FieldValue.increment(1)
                ).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun updateAllUserAppsReputation(userId: String, nuevaReputacion: Double, fcmToken:String): Result<Unit> {
        return try {
            val db = FirebaseFirestore.getInstance()
            val appsSnapshot = db.collection("apps")
                .whereEqualTo("idPropietario", userId)
                .whereEqualTo("status", "test")
                .get()
                .await()

            val batch = db.batch()

            val userRef = db.collection("users").document(userId)
            batch.update(userRef, mapOf(
                "reputacion" to nuevaReputacion,
                "fcmToken" to fcmToken
            ))
            if (!appsSnapshot.isEmpty) {

                for (document in appsSnapshot.documents) {
                    // 2. Preparamos las actualizaciones en un mapa para mayor claridad
                    val updates = mapOf(
                        "reputacion" to nuevaReputacion,
                        "fcmToken" to fcmToken // Se actualiza el token recibido por parámetro
                    )

                    // 3. Agregamos la actualización al batch
                    batch.update(document.reference, updates)
                }

                // 4. Ejecutamos todas las operaciones de forma atómica

            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateApp(appId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            db.collection("apps").document(appId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteApp(appId: String): Result<Unit> {
        return try {
            db.collection("apps").document(appId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProductionAppsCount(): Int {
        return try {
            val query = db.collection("apps")
                .whereEqualTo("status", "produccion")
            val snapshot = query.count().get(com.google.firebase.firestore.AggregateSource.SERVER).await()
            snapshot.count.toInt()
        } catch (e: Exception) {
            0
        }
    }

    suspend fun getTestersCount(): Int {
        return try {
            val query = db.collection("users")
            val snapshot = query.count().get(com.google.firebase.firestore.AggregateSource.SERVER).await()
            snapshot.count.toInt()
        } catch (e: Exception) {
            0
        }
    }

    suspend fun incrementUserPoints(userId: String, amount: Long): Result<Unit> {
        return try {
            db.collection("users").document(userId)
                .update("puntos", com.google.firebase.firestore.FieldValue.increment(amount))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // En AuthRepository.kt

    suspend fun updateFcmToken(token: String): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("No user logged in")

            // Actualizamos solo el campo 'fcmToken'
            db.collection("users").document(uid)
                .update("fcmToken", token)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // En AuthRepository.kt

    // En AuthRepository.kt
    fun getProductionAppsRealtime(): Flow<List<AppModel>> = callbackFlow {
        val listener = db.collection("apps")
            .whereEqualTo("status", "produccion")
            .orderBy("reputacion", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val apps = snapshot?.toObjects(AppModel::class.java) ?: emptyList()
                trySend(apps)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getAllApps(): List<AppModel> = withContext(Dispatchers.IO) {
        try {
            db.collection("apps").get().await().toObjects(AppModel::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getProductionApps(): List<AppModel> = withContext(Dispatchers.IO) {
        try {
            val snapshot = db.collection("apps")
                .whereEqualTo("status", "produccion") // Filtro por producción
                .orderBy("reputacion", Query.Direction.DESCENDING)
                .get().await()

            snapshot.toObjects(AppModel::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
    fun getFeaturedProductionAppsRealtime(): Flow<List<AppModel>> = callbackFlow {
        val priorityOwnerId = "TLaAvv2YGObm93iLGM8xqLZOETk2"
        val listener = db.collection("apps")
            .whereEqualTo("status", "produccion")
            .whereEqualTo("idPropietario", priorityOwnerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val apps = snapshot?.toObjects(AppModel::class.java) ?: emptyList()
                trySend(apps)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getFeaturedProductionApps(): List<AppModel> = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        val priorityOwnerId = "TLaAvv2YGObm93iLGM8xqLZOETk2"

        return@withContext try {
            // CONSULTA 1: Solo las apps del dueño prioritario
            val prioritySnapshot = db.collection("apps")
                .whereEqualTo("status", "produccion")
                .whereEqualTo("idPropietario", priorityOwnerId)
                .get()
                .await()

            prioritySnapshot.toObjects(AppModel::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun checkIfAppInProduction(packageName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://play.google.com/store/apps/details?id=$packageName")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.connect()
            connection.responseCode == 200
        } catch (e: Exception) {
            false
        }
    }

    suspend fun fetchAppIcon(packageName: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = "https://play.google.com/store/apps/details?id=$packageName&hl=en&gl=US"
            val document = org.jsoup.Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .get()

            val imgElement = document.select("img[itemprop=image]").first()
            val rawUrl = imgElement?.attr("srcset")?.split(" ")?.firstOrNull()
                ?: imgElement?.attr("src")

            rawUrl?.replace(Regex("=s\\d+.*$"), "=s512")
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAllUsersListRaw(): List<UserProfile> {
        return try {
            db.collection("users")
                .get()
                .await()
                .toObjects(UserProfile::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // 1. Estado: Aceptado (Mis Chats)
    fun getAppsAcceptedRealtime(): Flow<List<AppModel>> = callbackFlow {
        val uid = auth.currentUser?.uid ?: return@callbackFlow
        val listener = db.collection("apps")
            .whereEqualTo("status", "test")
            .whereEqualTo("testers.$uid.validado", "Aceptado")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val apps = snapshot?.toObjects(AppModel::class.java) ?: emptyList()
                trySend(apps.sortedByDescending { it.reputacion })
            }
        awaitClose { listener.remove() }
    }

    suspend fun getAppsAccepted(): List<AppModel> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = db.collection("apps")
                .whereEqualTo("status", "test")
                .whereEqualTo("testers.$uid.validado", "Aceptado")
                .orderBy("reputacion", Query.Direction.DESCENDING)
                .get().await()
            snapshot.toObjects(AppModel::class.java)
        } catch (e: Exception) { emptyList() }
    }

    // 2. Estado: Solicitud (Pendientes)
    fun getAppsPendingRealtime(): Flow<List<AppModel>> = callbackFlow {
        val uid = auth.currentUser?.uid ?: return@callbackFlow
        val listener = db.collection("apps")
            .whereEqualTo("status", "test")
            .whereEqualTo("testers.$uid.validado", "Solicitud")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val apps = snapshot?.toObjects(AppModel::class.java) ?: emptyList()
                trySend(apps)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getAppsPending(): List<AppModel> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = db.collection("apps")
                .whereEqualTo("status", "test")
                .whereEqualTo("testers.$uid.validado", "Solicitud")
                .get().await()
            snapshot.toObjects(AppModel::class.java)
        } catch (e: Exception) { emptyList() }
    }

    // 3. Estado: Rechazado
    suspend fun getAppsRejected(): List<AppModel> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = db.collection("apps")
                .whereEqualTo("status", "test")
                .whereEqualTo("testers.$uid.validado", "Rechazado")
                .get().await()
            snapshot.toObjects(AppModel::class.java)
        } catch (e: Exception) { emptyList() }
    }

    // 4. Estado: Explorar
    fun getAppsToExploreRealtime(): Flow<List<AppModel>> = callbackFlow {
        val uid = auth.currentUser?.uid ?: return@callbackFlow
        val listener = db.collection("apps")
            .whereEqualTo("status", "test")
            .whereNotIn("testerIds", listOf(uid))
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val apps = snapshot?.toObjects(AppModel::class.java) ?: emptyList()
                // Filtramos localmente solo para no ver nuestras propias apps y ordenamos
                val filtered = apps.filter { it.idPropietario != uid }
                    .sortedByDescending { it.reputacion }
                trySend(filtered)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getAppsToExplore(): List<AppModel> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = db.collection("apps")
                .whereEqualTo("status", "test")
                //.whereNotEqualTo("idPropietario", uid)
                .whereNotIn("testerIds", listOf(uid))
                .orderBy("reputacion", Query.Direction.DESCENDING)
                //.whereNotEqualTo("idPropietario", uid)
                .get().await()

            // Filtramos localmente solo para no ver nuestras propias apps
            snapshot.toObjects(AppModel::class.java).filter { it.idPropietario != uid }
        } catch (e: Exception) { emptyList() }
    }

    // --- NOTIFICACIONES IN-APP ---
    fun getUserNotificationsRealtime(userId: String): Flow<List<com.appinitdev.tester.model.UserNotification>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = db.collection("users").document(userId)
            .collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val notifs = snapshot?.toObjects(com.appinitdev.tester.model.UserNotification::class.java) ?: emptyList()
                trySend(notifs)
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveInAppNotification(userId: String, notification: com.appinitdev.tester.model.UserNotification): Result<Unit> {
        return try {
            val docId = if (notification.id.isBlank()) java.util.UUID.randomUUID().toString() else notification.id
            db.collection("users").document(userId)
                .collection("notifications")
                .document(docId)
                .set(notification.copy(id = docId))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markNotificationAsRead(userId: String, notificationId: String): Result<Unit> {
        return try {
            db.collection("users").document(userId)
                .collection("notifications")
                .document(notificationId)
                .update("isRead", true)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAllNotificationsAsRead(userId: String): Result<Unit> {
        return try {
            val snapshot = db.collection("users").document(userId)
                .collection("notifications")
                .whereEqualTo("isRead", false)
                .get()
                .await()
            val batch = db.batch()
            snapshot.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearAllNotifications(userId: String): Result<Unit> {
        return try {
            val snapshot = db.collection("users").document(userId)
                .collection("notifications")
                .get()
                .await()
            val batch = db.batch()
            snapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- REPORTES DE FEEDBACK DE 14 DÍAS ---
    suspend fun submit14DayFeedback(appId: String, feedback: com.appinitdev.tester.model.FeedbackReport14Days): Result<Unit> {
        return try {
            val docId = if (feedback.id.isBlank()) java.util.UUID.randomUUID().toString() else feedback.id
            db.collection("apps").document(appId)
                .collection("feedback_reports")
                .document(docId)
                .set(feedback.copy(id = docId))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun get14DayFeedbacks(appId: String): List<com.appinitdev.tester.model.FeedbackReport14Days> {
        return try {
            val snapshot = db.collection("apps").document(appId)
                .collection("feedback_reports")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.toObjects(com.appinitdev.tester.model.FeedbackReport14Days::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

}