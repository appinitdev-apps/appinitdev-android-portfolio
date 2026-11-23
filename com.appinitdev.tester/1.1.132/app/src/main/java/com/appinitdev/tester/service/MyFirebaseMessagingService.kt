
class MyFirebaseMessagingService : FirebaseMessagingService() {

    // 1. Se ejecuta cuando llega una notificación y la app está abierta
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        handleIncomingMessage(remoteMessage)
    }

    // 2. Se ejecuta cuando el token cambia (instalación nueva, borrado de datos)
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // Necesitamos actualizar Firestore directamente aquí porque no tenemos acceso al ViewModel
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .update("fcmToken", token)
    }


    private fun handleIncomingMessage(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"]
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"]
        val data = remoteMessage.data

        if (!title.isNullOrBlank() && !body.isNullOrBlank()) {
            val appId = data["userId"] ?: data["appId"]
            val notificationIntent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("appId", appId)
            }

            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val CHANNEL_ID = "tester_alerts_v2"

            val iconUrl = remoteMessage.notification?.imageUrl?.toString()
                ?: data["iconUrl"]
                ?: data["image"]

            val appIconBitmap = android.graphics.BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
            val largeIconBitmap = getBitmapFromUrl(iconUrl) ?: appIconBitmap

            val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(largeIconBitmap)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(longArrayOf(0, 100, 50, 100, 50, 100))
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            createNotificationChannel(notificationManager)
            notificationManager.notify(Random.nextInt().hashCode(), notificationBuilder.build())
        }
    }

    private fun getBitmapFromUrl(urlString: String?): android.graphics.Bitmap? {
        if (urlString.isNullOrBlank()) return null
        return try {
            val url = java.net.URL(urlString)
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.doInput = true
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.connect()
            val inputStream = connection.inputStream
            android.graphics.BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            null
        }
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val CHANNEL_ID = "tester_alerts_v2"
            val channelName = "Alertas de Test"
            val channelDescription = "Default Channel for notifications"

            // Definir patrón de vibración

            // Establecer sonido
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            val channel = NotificationChannel(
                CHANNEL_ID,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = channelDescription
                enableVibration(true)
                setSound(soundUri, audioAttributes)

                setVibrationPattern(longArrayOf(0, 100, 50, 100, 50, 1000))
                vibrationPattern = longArrayOf(0, 100, 50, 100, 50, 100)
                enableLights(true)

            }
            notificationManager.createNotificationChannel(channel)
        }
    }


}