object Funciones {
    fun shouldRunToday(context: Context): Boolean {
        val prefs = context.getSharedPreferences("daily_check", Context.MODE_PRIVATE)
        val lastCheck = prefs.getLong("last_check_date", 0L)

        if (lastCheck == 0L) return true

        val cal1 = Calendar.getInstance().apply { timeInMillis = lastCheck }
        val cal2 = Calendar.getInstance()
        Log.e("Calxcv", "${cal1.get(Calendar.DAY_OF_YEAR)}, ${cal2.get(Calendar.DAY_OF_YEAR)}")

        return cal1.get(Calendar.YEAR) != cal2.get(Calendar.YEAR) ||
                cal1.get(Calendar.DAY_OF_YEAR) != cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun saveTodayCheck(context: Context) {
        val prefs = context.getSharedPreferences("daily_check", Context.MODE_PRIVATE)
        prefs.edit()
            .putLong("last_check_date", System.currentTimeMillis())
            .apply()
    }

    fun shouldShowInstallAppsPromptToday(context: Context): Boolean {
        val prefs = context.getSharedPreferences("prompt_prefs", Context.MODE_PRIVATE)
        val lastShown = prefs.getLong("last_install_apps_prompt_date", 0L)
        if (lastShown == 0L) return true

        val calLast = Calendar.getInstance().apply { timeInMillis = lastShown }
        val calNow = Calendar.getInstance()

        return calLast.get(Calendar.YEAR) != calNow.get(Calendar.YEAR) ||
                calLast.get(Calendar.DAY_OF_YEAR) != calNow.get(Calendar.DAY_OF_YEAR)
    }

    fun saveInstallAppsPromptShownToday(context: Context) {
        val prefs = context.getSharedPreferences("prompt_prefs", Context.MODE_PRIVATE)
        prefs.edit().putLong("last_install_apps_prompt_date", System.currentTimeMillis()).apply()
    }

    fun shouldShowTestCompletionPromptEveryTwoDays(context: Context, appId: String): Boolean {
        val prefs = context.getSharedPreferences("prompt_prefs", Context.MODE_PRIVATE)
        val lastShown = prefs.getLong("last_test_completion_prompt_$appId", 0L)
        if (lastShown == 0L) return true

        val twoDaysInMillis = 2 * 24 * 60 * 60 * 1000L
        return (System.currentTimeMillis() - lastShown) >= twoDaysInMillis
    }

    fun saveTestCompletionPromptShown(context: Context, appId: String) {
        val prefs = context.getSharedPreferences("prompt_prefs", Context.MODE_PRIVATE)
        prefs.edit().putLong("last_test_completion_prompt_$appId", System.currentTimeMillis()).apply()
    }


    fun daysBetween(from: Long, to: Long): Int {
        val millisPerDay = 24 * 60 * 60 * 1000L
        return ((to - from) / millisPerDay).toInt().coerceAtLeast(0)
    }

    fun isAppInstalled(context: Context, packageName: String): Boolean {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        return intent != null

    }

    fun sendNotification(app: String, title: String, msg: String, token: String? = "", scope: CoroutineScope, iconUrl: String? = null) {
        if (token.isNullOrBlank()) {
            Log.w("sendNotification", "No token provided, skipping notification: $title")
            return
        }

        val messageObj = JSONObject().apply {
            put("token", token)
            put("notification", JSONObject().apply {
                put("title", title)
                put("body", msg)
                if (!iconUrl.isNullOrBlank()) {
                    put("image", iconUrl)
                }
            })
            put("android", JSONObject().apply {
                put("priority", "high")
                put("notification", JSONObject().apply {
                    put("channel_id", "tester_alerts_v2")
                })
            })
            put("data", JSONObject().apply {
                put("userId", app)
                if (!iconUrl.isNullOrBlank()) {
                    put("iconUrl", iconUrl)
                }
            })
        }
        val jsonObj = JSONObject().apply {
            put("message", messageObj)
        }
        Log.d("sendNotification", "Sending notification to token ${token.take(10)}... | Title: $title")
        callApi(jsonObj, scope)
    }


    fun notificationsEnabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun openAppNotificationSettings(context: Context) {
        val intent = Intent().apply {
            action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
        context.startActivity(intent)
    }


    fun callApi(jsonObj: JSONObject, scope: CoroutineScope) {
        val url = "https://fcm.googleapis.com/v1/projects/appinitdev-android/messages:send"
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jsonObj.toString().toRequestBody(mediaType)

        // 1. Usamos el scope del ViewModel o uno controlado, NO GlobalScope
        scope.launch(Dispatchers.IO) {
            try {
                val jsonString = """
            {
              "type": "service_account",
              "project_id": "appinitdev-android",
              "private_key_id": "",
              "private_key": "",
              "client_email": "firebase-adminsdk-49tfb@appinitdev-android.iam.gserviceaccount.com",
              "client_id": "109080808800224325648",
              "auth_uri": "https://accounts.google.com/o/oauth2/auth",
              "token_uri": "",
              "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
              "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-49tfb%40appinitdev-android.iam.gserviceaccount.com",
              "universe_domain": "googleapis.com"
            }""" // Tu JSON de credenciales

                val googleCredentials = GoogleCredentials.fromStream(jsonString.byteInputStream())
                    .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))

                // 2. refreshIfExpired() es propenso a SocketException si la red falla
                googleCredentials.refreshIfExpired()
                val accessToken = googleCredentials.accessToken.tokenValue

                // 3. Evitar crear un OkHttpClient por cada mensaje (consume muchos sockets)
                val client = OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)
                    .build()

                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .header("Authorization", "")
                    .build()

                // Ejecución síncrona dentro de Dispatchers.IO para mejor control de errores
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    Log.i("chatoknoti", "Notification sent: ${response.body?.string()}")
                } else {
                    Log.e("chatoknoti", "Error code: ${response.code} - ${response.message}")
                }
                response.close() // Siempre cerrar la respuesta

            } catch (e: java.net.SocketException) {
                Log.e("chatoknoti", "Socket cerrado inesperadamente: ${e.message}")
            } catch (e: IOException) {
                Log.e("chatoknoti", "Error de red/IO: ${e.message}")
            } catch (e: Exception) {
                Log.e("chatoknoti", "Error desconocido: ${e.message}")
            }
        }
        // Configurar el endpoint V1
        /*   val url = "https://fcm.googleapis.com/v1/projects/appinitdev-android/messages:send"
           val mediaType = "application/json; charset=utf-8".toMediaType()
           val body = jsonObj.toString().toRequestBody(mediaType)

           // Ejecutar la obtención del token en un hilo de fondo
           GlobalScope.launch(Dispatchers.IO) {
               // Asegúrate de tener el JSON de credenciales correcto
               val jsonString = """
               {
                 "type": "service_account",
                 "project_id": "appinitdev-android",
                 "private_key_id": "",
                 "private_key": "",
                 "client_email": "firebase-adminsdk-49tfb@appinitdev-android.iam.gserviceaccount.com",
                 "client_id": "109080808800224325648",
                 "auth_uri": "https://accounts.google.com/o/oauth2/auth",
                 "token_uri": "",
                 "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
                 "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-49tfb%40appinitdev-android.iam.gserviceaccount.com",
                 "universe_domain": "googleapis.com"
               }"""

               val googleCredentials = GoogleCredentials.fromStream(jsonString.byteInputStream())
                   .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
               googleCredentials.refreshIfExpired() // Esto se ejecuta en IO
               val accessToken = googleCredentials.accessToken.tokenValue

               val client = OkHttpClient()
               val request = Request.Builder()
                   .url(url)
                   .post(body)
                   .header("Authorization", "")
                   .build()

               client.newCall(request).enqueue(object : Callback {
                   override fun onFailure(call: Call, e: IOException) {
                       println("Request failed: ${e.message}")
                       Log.i("chatoknoti", "Request failed: ${e.message}")
                       e.printStackTrace()
                   }

                   override fun onResponse(call: Call, response: Response) {
                       val responseBody = response.body?.string()
                       if (response.isSuccessful) {
                           println("Notification sent successfully: $responseBody")
                           Log.i("chatoknoti", "Notification sent successfully: $responseBody")
                       } else {
                           println("Failed to send notification. Response code: ${response.code}")
                           Log.i(
                               "chatoknoti",
                               "Failed to send notification. Response code: ${response.code}"
                           )

                       }
                   }
               })
           }*/
    }


    fun registrarTokenFCM() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                // Guardar en Firestore
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .update("fcmToken", token)
            }
        }
    }

    fun isValidUrl(url: String): Boolean {
        val urlPattern = android.util.Patterns.WEB_URL
        return url.isNotBlank() && urlPattern.matcher(url).matches()
    }



    fun formatLongToDate(time: Long): String {
        return try {
            // Validamos que el tiempo sea una fecha coherente (mayor a 0)
            if (time <= 0) return ""

            val date = Date(time)
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            format.format(date)
        } catch (e: Exception) {
            // Si algo falla (error de formato, fecha nula, etc.), retorna vacío
            ""
        }
    }

    fun clasificarApps(apps: List<AppModel>, userId: String?, scope: CoroutineScope? = null): CategoriasApps {
        if (userId == null) return CategoriasApps(explorar = apps)

        val aceptadas = mutableListOf<AppModel>()
        val pendientes = mutableListOf<AppModel>()
        val rechazadas = mutableListOf<AppModel>()
        val explorar = mutableListOf<AppModel>()
        apps.forEach { app ->

            // Si soy el dueño, la app va directamente a mis grupos de chat (aceptadas)
            if (app.idPropietario == userId) {
                aceptadas.add(app)
                return@forEach
            }

            val testerData = app.testers[userId]
            val estado = testerData?.validado

            when {
                estado == "Aceptado" -> aceptadas.add(app)
                estado == "Solicitud" -> pendientes.add(app)
                estado == "Rechazado" -> rechazadas.add(app)

                // Si no hay estado (no ha solicitado), va a explorar
                else -> {
                    explorar.add(app)
                }
            }
        }

        Log.d("DEBUG_APPS", "=== CLASIFICACIÓN DE APPS ===")
        Log.d("DEBUG_APPS", "Total recibidas de Firestore: ${apps.size}")
        Log.d("DEBUG_APPS", "Usuario actual UID: $userId")
        Log.d("DEBUG_APPS", "Mis Chats / Aceptadas (propias + aceptadas): ${aceptadas.size}")
        Log.d("DEBUG_APPS", "Solicitudes pendientes: ${pendientes.size}")
        Log.d("DEBUG_APPS", "Rechazadas: ${rechazadas.size}")
        Log.d("DEBUG_APPS", "Explorar nuevas Apps: ${explorar.size}")
        explorar.forEachIndexed { i, app ->
            Log.d("DEBUG_APPS", "  [Explorar #$i] Nombre: ${app.nombre} | Paquete: ${app.paquete} | Status: ${app.status} | Owner: ${app.idPropietario}")
        }
        Log.d("DEBUG_APPS", "================================")

        return CategoriasApps(
            explorar = explorar,
            aceptadas = aceptadas,
            pendientes = pendientes,
            rechazadas = rechazadas
        )
    }


}