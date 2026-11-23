
class PushNotificationService: FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Token actualizado: $token")
        
        // Actualizar token en Firestore si el usuario está logueado
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.uid)
                .update("fcmToken", token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: getString(R.string.nueva_notificacion)
        val message = remoteMessage.notification?.body ?: remoteMessage.data["message"] ?: ""
        val from = remoteMessage.data["from"] ?: getString(R.string.sistema)
        val chatId = remoteMessage.data["chatId"]
        
        showNotification(title, message, chatId)
        if (message.isNotEmpty()) {
            saveNotificationLocally(from, message)
        }
    }

    @OptIn(UnstableApi::class)
    private fun saveNotificationLocally(from: String, message: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val entity = NotificationEntity(
            from = from,
            message = message,
            time = System.currentTimeMillis(),
            userId = currentUser.uid
        )
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                BaseApplication.database.chatDao().insertNotification(entity)
            } catch (e: Exception) {
                Log.e("PushNotificationService", "Error saving notification", e)
            }
        }
    }
    private fun showNotification(title: String, message: String, chatId: String? = null) {
        val channelId = "messages_channel_v1"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val pattern = longArrayOf(0, 100, 80, 100, 80, 100, 1000)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                getString(R.string.notificaciones),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableVibration(true)
                vibrationPattern = pattern
                setShowBadge(true)
                description = getString(R.string.notificaciones_desc)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (chatId != null) {
                putExtra("chatId", chatId)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(), // ID único para evitar colisiones
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setSound(soundUri)
            .setVibrate(pattern)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}