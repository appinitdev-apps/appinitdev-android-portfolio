class FtpService : Service() {
    private var ftpServer: FtpServer? = null
    companion object {
        private const val TAG = "FTP_SERVICE_DEBUG" 
        private const val CHANNEL_ID = "secure_ftp_network_channel"
        private const val NOTIFICATION_ID = 9026
        @Volatile var isRunning = false
            private set
        @Volatile var usuarioActual = ""
            internal set
        @Volatile var contrasenaActual = ""
            internal set
        @Volatile var puertoActual = 8080
            internal set
        fun obtenerIpLocal(context: Context): String {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            val ipAddress = wifiInfo.ipAddress
            if (ipAddress == 0) {
                Log.w(TAG, "obtenerIpLocal: La dirección IP retornó 0 (No conectado a Wi-Fi)")
                return "0.0.0.0"
            }
            val ip = if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                Integer.reverseBytes(ipAddress)
            } else {
                ipAddress
            }
            val ipByteArray = BigInteger.valueOf(ip.toLong()).toByteArray()
            return try {
                val resolvedIp = InetAddress.getByAddress(ipByteArray).hostAddress ?: "0.0.0.0"
                Log.d(TAG, "obtenerIpLocal: IP detectada exitosamente -> $resolvedIp")
                resolvedIp
            } catch (e: Exception) {
                Log.e(TAG, "obtenerIpLocal: Error resolviendo IP de la interfaz", e)
                "0.0.0.0"
            }
        }
        private fun generarContrasenaAleatoria(longitud: Int = 8): String {
            val caracteres = "23456789abcdefghijkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ"
            return (1..longitud)
                .map { Random.nextInt(0, caracteres.length) }
                .map { caracteres[it] }
                .joinToString("")
        }
        fun prepararCredenciales() {
            Log.d(TAG, "prepararCredenciales: Iniciando generación síncrona de credenciales")
            usuarioActual = "usr${Random.nextInt(1000, 9999)}"
            contrasenaActual = generarContrasenaAleatoria(5)
            puertoActual = 8080
            Log.d(TAG, "prepararCredenciales: Credenciales listas -> Usuario: $usuarioActual, Puerto: $puertoActual")
        }
    }
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: Inicializando el servicio FtpService")
        createNotificationChannel()
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: Servicio llamado a iniciar")
        isRunning = true
        if (usuarioActual.isEmpty()) {
            Log.w(TAG, "onStartCommand: usuarioActual estaba vacío en memoria, generando de emergencia")
            prepararCredenciales()
        }
        Log.d(TAG, "onStartCommand: Iniciando Foreground Service con la notificación activa")
        startForeground(NOTIFICATION_ID, buildNotification())
        Thread {
            try {
                Log.d(TAG, "Hilo FTP: Configurando FtpServerFactory...")
                val serverFactory = FtpServerFactory()
                val listenerFactory = ListenerFactory().apply {
                    port = puertoActual
                    Log.d(TAG, "Hilo FTP: Puerto de comandos asignado a -> $port")
                    val dataConfigFactory = DataConnectionConfigurationFactory().apply {
                        passivePorts = "2122"
                    }
                    Log.d(TAG, "Hilo FTP: Puerto de datos Pasivo asignado a -> ${dataConfigFactory.passivePorts}")
                    setDataConnectionConfiguration(dataConfigFactory.createDataConnectionConfiguration())
                }
                serverFactory.addListener("default", listenerFactory.createListener())
                Log.d(TAG, "Hilo FTP: Listener por defecto añadido a la fábrica")
                val storageRoot = File("/storage")
                val user = BaseUser().apply {
                    name = usuarioActual
                    password = contrasenaActual
                    homeDirectory =  File("/storage/emulated/0").absolutePath 
                    authorities = listOf(WritePermission())
                }
                Log.d(TAG, "Hilo FTP: Configurando usuario '${user.name}' con directorio raíz: ${user.homeDirectory}")
                serverFactory.userManager.save(user)
                Log.d(TAG, "Hilo FTP: Usuario guardado exitosamente en el UserManager")
                ftpServer = serverFactory.createServer()
                Log.i(TAG, "Hilo FTP: Servidor instanciado. Intentando llamar a ftpServer.start()...")
                ftpServer?.start()
                Log.i(TAG, "¡¡¡ Hilo FTP: SERVIDOR INICIADO CORRECTAMENTE !!! Escuchando peticiones...")
            } catch (e: Exception) {
                Log.e(TAG, "CRÍTICO - Hilo FTP: Error al inicializar o arrancar el servidor FTP", e)
                stopSelf()
            }
        }.start()
        return START_NOT_STICKY
    }
    override fun onDestroy() {
        Log.w(TAG, "onDestroy: Deteniendo FtpService...")
        isRunning = false
        usuarioActual = ""
        contrasenaActual = ""
        Thread {
            try {
                if (ftpServer != null) {
                    Log.d(TAG, "Hilo Destrucción: Solicitando detención de ftpServer...")
                    ftpServer?.stop()
                    Log.i(TAG, "Hilo Destrucción: ftpServer detenido completamente")
                } else {
                    Log.d(TAG, "Hilo Destrucción: ftpServer ya era nulo")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Hilo Destrucción: Error al intentar detener ftpServer", e)
            }
        }.start()
        super.onDestroy()
    }
    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "onBind: Retornando null (Servicio no enlazado de tipo Bound)")
        return null
    }
    private fun buildNotification(): Notification {
        val systemTitle = getString(android.R.string.dialog_alert_title)
        val ipStr = "ftp://${obtenerIpLocal(this)}:$puertoActual"
        Log.d(TAG, "buildNotification: Construyendo notificación con texto -> $ipStr")
        val notificationIntent = Intent(this, FtpServerActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(systemTitle)
            .setContentText(ipStr)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(pendingIntent)
            .build()
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            if (manager?.getNotificationChannel(CHANNEL_ID) == null) {
                Log.d(TAG, "createNotificationChannel: Creando canal de notificaciones OREO+")
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "FTP LAN",
                    NotificationManager.IMPORTANCE_LOW
                )
                manager?.createNotificationChannel(channel)
            }
        }
    }
}