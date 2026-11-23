
class HttpService : Service() {

    private var server: AndroidWebServer? = null

    companion object {
        private const val TAG = "HTTP_SERVICE_DEBUG"
        private const val CHANNEL_ID = "secure_http_network_channel"
        private const val NOTIFICATION_ID = 9027

        @Volatile var isRunning = false
            private set
        @Volatile var puertoActual = 8081
            internal set

        fun obtenerIpLocal(context: Context): String {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            val ipAddress = wifiInfo.ipAddress
            if (ipAddress == 0) return "0.0.0.0"

            val ip = if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                Integer.reverseBytes(ipAddress)
            } else {
                ipAddress
            }

            val ipByteArray = BigInteger.valueOf(ip.toLong()).toByteArray()
            return try {
                InetAddress.getByAddress(ipByteArray).hostAddress ?: "0.0.0.0"
            } catch (e: Exception) {
                "0.0.0.0"
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: Iniciando Servidor HTTP con Carga de Archivos")
        isRunning = true

        startForeground(NOTIFICATION_ID, buildNotification())

        Thread {
            try {
                server = AndroidWebServer(puertoActual)
                server?.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
                Log.i(TAG, "¡¡¡ SERVIDOR HTTP OPERATIVO CON SUBIDAS !!!")
            } catch (e: Exception) {
                Log.e(TAG, "Error arrancando NanoHTTPD", e)
                stopSelf()
            }
        }.start()

        return START_NOT_STICKY
    }
    private fun zipFolder(sourceFolder: File, zipFile: File) {
        java.util.zip.ZipOutputStream(zipFile.outputStream().buffered()).use { zos ->
            sourceFolder.walkTopDown().forEach { file ->
                if (file.isFile) {
                    
                    val relativePath = file.relativeTo(sourceFolder).path
                    val entry = java.util.zip.ZipEntry(relativePath)
                    zos.putNextEntry(entry)
                    file.inputStream().use { fis -> fis.copyTo(zos) }
                    zos.closeEntry()
                }
            }
        }
    }
    private inner class AndroidWebServer(port: Int) : NanoHTTPD(port) {
        private val rootDir = File("/storage/emulated/0")

        override fun serve(session: IHTTPSession): Response {
            try {
                val uriDecodificada = URLDecoder.decode(session.uri, "UTF-8")
                val targetFile = File(rootDir, uriDecodificada).canonicalFile

                
                if (!targetFile.path.startsWith(rootDir.canonicalPath)) {
                    return newFixedLengthResponse(Response.Status.FORBIDDEN, MIME_PLAINTEXT, "Acceso Denegado")
                }

                
                if (session.method == Method.POST) {
                    val files = HashMap<String, String>()

                    
                    session.parseBody(files)

                    
                    val tempPath = files["archivoSubido"]
                    val nombreOriginal = session.parms["archivoSubido"]

                    if (tempPath != null && nombreOriginal != null) {
                        val ficheroTemporal = File(tempPath)
                        
                        val ficheroDestino = File(targetFile, nombreOriginal)

                        
                        ficheroTemporal.copyTo(ficheroDestino, overwrite = true)

                        
                        val responseJson = newFixedLengthResponse(Response.Status.REDIRECT, MIME_HTML, "")
                        responseJson.addHeader("Location", session.uri)
                        return responseJson
                    }
                }

                
                if (!targetFile.exists()) {
                    return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "No encontrado")
                }

                if (targetFile.isDirectory) {
                    
                    if (session.parms["download"] == "true") {
                        try {
                            
                            val tempZip = File(cacheDir, "${targetFile.name}.zip")
                            zipFolder(targetFile, tempZip)

                            
                            val fileInputStream = object : java.io.FileInputStream(tempZip) {
                                override fun close() {
                                    super.close()
                                    tempZip.delete() 
                                }
                            }

                            val response = newChunkedResponse(Response.Status.OK, "application/zip", fileInputStream)
                            response.addHeader("Content-Disposition", "attachment; filename=\"${targetFile.name}.zip\"")
                            response.addHeader("Content-Length", tempZip.length().toString())
                            return response
                        } catch (e: Exception) {
                            Log.e(TAG, "Error al comprimir la carpeta", e)
                            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Error al crear el archivo ZIP")
                        }
                    }

                    val htmlResponse = this.generarPaginaWeb(targetFile, uriDecodificada)
                    return newFixedLengthResponse(Response.Status.OK, "text/html; charset=UTF-8", htmlResponse)
                } else if (targetFile.isFile) {
                    val fileInputStream = FileInputStream(targetFile)
                    val response = newChunkedResponse(Response.Status.OK, "application/octet-stream", fileInputStream)
                    response.addHeader("Content-Disposition", "attachment; filename=\"${targetFile.name}\"")
                    response.addHeader("Content-Length", targetFile.length().toString())
                    return response
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error procesando petición HTTP", e)
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Error Interno del Servidor")
            }

            return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not Found")
        }
        private fun generarPaginaWeb(folder: File, relativePath: String): String {
            val normalPath = if (relativePath.endsWith("/")) relativePath else "$relativePath/"

            val botonRegresar = if (relativePath != "/" && relativePath.isNotEmpty()) {
                val parentWebPath = File(relativePath).parent ?: "/"
                "<a href=\"$parentWebPath\" class=\"inline-flex items-center text-sm font-medium text-teal-400 hover:underline mb-4\">🔙 Volver a la carpeta anterior</a>"
            } else ""
            val pathSegments = normalPath.split("/").filter { it.isNotEmpty() }
            var currentPath = "/"
            val breadcrumbsBuilder = StringBuilder()

            
            breadcrumbsBuilder.append("<a href=\"/\" class=\"text-teal-400 hover:text-teal-300 hover:underline transition-colors\">/</a>")

            
            for (segment in pathSegments) {
                currentPath += "$segment/"
                breadcrumbsBuilder.append("<a href=\"$currentPath\" class=\"text-teal-400 hover:text-teal-300 hover:underline transition-colors\">$segment/</a>")
            }
            val breadcrumbsHtml = breadcrumbsBuilder.toString()
            val itemsBuilder = StringBuilder()
            val filesList = folder.listFiles()?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))

            filesList?.forEach { file ->
                val linkPath = if (normalPath == "/") "/${file.name}" else "$normalPath${file.name}"

                val icono: String
                val colorTexto: String
                val detalles: String
                val downloadLink: String

                if (file.isDirectory) {
                    val cantidadElementos = file.listFiles()?.size ?: 0
                    icono = "📁"
                    colorTexto = "text-yellow-400 font-semibold"
                    detalles = if (cantidadElementos == 1) "1 elemento" else "$cantidadElementos elementos"
                    
                    downloadLink = "$linkPath?download=true"
                } else {
                    val sizeKb = file.length() / 1024
                    val sizeStr = if (sizeKb > 1024) "${sizeKb / 1024} MB" else "$sizeKb KB"
                    icono = "📄"
                    colorTexto = "text-gray-300"
                    detalles = sizeStr
                    downloadLink = linkPath
                }

                itemsBuilder.append("""
            <div class="flex items-center justify-between p-4 hover:bg-gray-700/40 transition-colors border-b border-gray-700/50">
                <div class="flex items-center gap-3 overflow-hidden mr-4">
                    <span class="text-xl">$icono</span>
                    <a href="$linkPath" class="$colorTexto hover:text-teal-400 hover:underline truncate text-sm sm:text-base">${file.name}</a>
                </div>
                <div class="flex items-center gap-4 flex-shrink-0">
                    <span class="text-xs text-gray-400 whitespace-nowrap">$detalles</span>
                    <a href="$downloadLink" title="Descargar" class="text-teal-400 hover:text-teal-300 transition-all p-1 hover:bg-teal-500/10 rounded flex items-center justify-center">
                        📥
                    </a>
                </div>
            </div>
        """.trimIndent())
            }

            if (filesList.isNullOrEmpty()) {
                itemsBuilder.append("<div class='p-8 text-center text-gray-500 text-sm'>Esta carpeta está vacía</div>")
            }

            return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <script src="https://cdn.tailwindcss.com"></script>
            <title>${getString(R.string.app_name)}</title>
        </head>
        <body class="bg-gray-900 text-gray-100 antialiased min-h-screen">
            <div class="max-w-4xl mx-auto px-4 py-8">
                <header class="flex items-center justify-between border-b border-gray-800 pb-4 mb-6">
                    <h1 class="text-xl sm:text-2xl font-bold text-teal-400 flex items-center gap-2">
                        ${getString(R.string.app_name)}
                    </h1>
                    <span class="bg-teal-500/10 text-teal-400 px-3 py-1 rounded-full text-xs font-bold tracking-wide">ONLINE</span>
                </header>
                
              
                
                <form id="uploadForm" action="" method="POST" enctype="multipart/form-data">
                    <label id="dropZone" class="border-2 border-dashed border-gray-700 hover:border-teal-500 bg-gray-800/40 hover:bg-teal-500/5 p-8 rounded-xl mb-6 shadow-md flex flex-col items-center justify-center gap-2 cursor-pointer transition-all group">
                        <input type="file" id="fileInput" name="archivoSubido" required class="hidden">
                        <span class="text-3xl text-gray-400 group-hover:text-teal-400 transition-all group-hover:scale-110">📤</span>
                        <p class="text-sm text-gray-300 font-medium text-center">
                            Arrastra tu archivo aquí o <span class="text-teal-400 group-hover:underline">haz clic para buscar</span>
                        </p>
                        <p class="text-xs text-gray-500">El archivo se cargará automáticamente al soltarlo</p>
                    </label>
                </form>
                  <div class="bg-gray-800/40 border border-gray-800 p-3 rounded-lg mb-4 shadow-sm overflow-x-auto">
                    <p class="text-xs sm:text-sm font-mono whitespace-nowrap flex items-center gap-1">
                        $breadcrumbsHtml
                    </p>
                </div>
                $botonRegresar
                
                <div class="bg-gray-800 rounded-xl border border-gray-700 shadow-2xl overflow-hidden">
                    <div class="flex flex-col">
                        $itemsBuilder
                    </div>
                </div>
            </div>

            <script>
                const dropZone = document.getElementById('dropZone');
                const fileInput = document.getElementById('fileInput');
                const uploadForm = document.getElementById('uploadForm');

                // Prevenir comportamientos por defecto del navegador al arrastrar cosas
                ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
                    dropZone.addEventListener(eventName, e => {
                        e.preventDefault();
                        e.stopPropagation();
                    }, false);
                });

                // Efecto visual al arrastrar sobre el área
                ['dragenter', 'dragover'].forEach(eventName => {
                    dropZone.addEventListener(eventName, () => {
                        dropZone.classList.add('border-teal-500', 'bg-teal-500/10');
                        dropZone.classList.remove('border-gray-700', 'bg-gray-800/40');
                    }, false);
                });

                // Revertir efecto visual al salir del área
                ['dragleave', 'drop'].forEach(eventName => {
                    dropZone.addEventListener(eventName, () => {
                        dropZone.classList.remove('border-teal-500', 'bg-teal-500/10');
                        dropZone.classList.add('border-gray-700', 'bg-gray-800/40');
                    }, false);
                });

                // Capturar el archivo soltado y enviar el formulario de inmediato
                dropZone.addEventListener('drop', e => {
                    const dt = e.dataTransfer;
                    const files = dt.files;
                    if (files.length > 0) {
                        fileInput.files = files;
                        uploadForm.submit();
                    }
                });

                // Enviar automáticamente también si el usuario prefiere hacer clic y elegir el archivo
                fileInput.addEventListener('change', () => {
                    if (fileInput.files.length > 0) {
                        uploadForm.submit();
                    }
                });
            </script>
        </body>
        </html>
    """.trimIndent()
        }
       
































































































    }

    override fun onDestroy() {
        isRunning = false
        try {
            server?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Error al detener el servidor NanoHTTPD", e)
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        val systemTitle = getString(android.R.string.dialog_alert_title)
        val urlStr = "http://${obtenerIpLocal(this)}:$puertoActual"

        val notificationIntent = Intent(this, HttpServerActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(systemTitle)
            .setContentText(urlStr)
            .setSmallIcon(android.R.drawable.ic_menu_share)
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
                val channel = NotificationChannel(CHANNEL_ID, "HTTP LAN", NotificationManager.IMPORTANCE_LOW)
                manager?.createNotificationChannel(channel)
            }
        }
    }
}