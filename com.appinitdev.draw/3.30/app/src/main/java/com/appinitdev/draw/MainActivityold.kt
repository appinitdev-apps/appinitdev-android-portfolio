

class MainActivityold : AppCompatActivity(), EasyPermissions.PermissionCallbacks,
    SurfaceHolder.Callback {
    private val RC_CAMERA_PERMISSION = 123
    private val RC_READ_STORAGE_PERMISSION = 124
    private val RC_IMAGE_PICK = 125
    private lateinit var camera: Camera
    private lateinit var surfaceView: SurfaceView
    private lateinit var imageOverlay: ImageView
    private lateinit var opacitySeekBar: SeekBar
    private var selectedImageUri: Uri? = null

    private var scaleGestureDetector: ScaleGestureDetector? = null
    private val matrix = Matrix()
    private val savedMatrix = Matrix()
    private var scaleFactor = 1.0f
    private val maxZoom = 3.0f
    private var distanceStart = 1f


    private var isFlashOn: Boolean = false
    private var isView: Boolean = false
    // Use an atomic boolean to initialize the Google Mobile Ads SDK and load ads once.

    private lateinit var binding: ActivityMainBinding
    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        surfaceView = binding.surfaceView
        imageOverlay = binding.imageOverlay

        val composeView = findViewById<ComposeView>(R.id.compose_view)

        composeView.setContent {
            AnimeImageApp(
                onImageSelected = { url ->
                    try {
                        loadImageWithOpacity(url)
                    }catch (e:Exception){e.printStackTrace()}
                // Cargamos y superponemos la imagen seleccionada
                }
            )  // Llamamos al composable que cargará las imágenes
        }
        // Solicitar permisos de cámara
        try {
            requestCameraPermissions()
        } catch (e: Exception) {
        }
        try {
            camera.release()
            surfaceView.holder.removeCallback(this)
            setupCamera()
            surfaceView.holder.addCallback(this)

            // Inicia la preview
            camera.startPreview()
        } catch (e: Exception) {
        }


        // Configurar el control del zoom
        scaleGestureDetector = ScaleGestureDetector(this, ScaleListener())


        opacitySeekBar = findViewById<SeekBar>(R.id.opacitySeekBar)
        opacitySeekBar.progress = 50

        findViewById<ImageView>(R.id.refresh).setOnClickListener {

            // Libera la cámara y el SurfaceView
            try {
                camera.release()
                surfaceView.holder.removeCallback(this)
                setupCamera()
                surfaceView.holder.addCallback(this)

                // Inicia la preview
                camera.startPreview()
            } catch (e: Exception) {
            }
        }

        val flashToggleButton: ImageView = findViewById(R.id.flashToggleButton)
        flashToggleButton.setOnClickListener {
            try {
                toggleFlash()
            } catch (e: Exception) {
            }

        }
        findViewById<ImageView>(R.id.lock).setOnClickListener{
            isView=!isView
            if (isView) {
                findViewById<FrameLayout>(R.id.opacitySeekBar).visibility= View.VISIBLE
                findViewById<ComposeView>(R.id.compose_view).visibility= View.VISIBLE

                findViewById<ImageView>(R.id.refresh).visibility= View.VISIBLE
                findViewById<ImageView>(R.id.flashToggleButton).visibility= View.VISIBLE
                findViewById<ImageView>(R.id.selectImageButton).visibility= View.VISIBLE
                findViewById<ImageView>(R.id.lock).setImageResource(R.drawable.lockclose)


            } else {
                findViewById<FrameLayout>(R.id.opacitySeekBar).visibility= View.GONE
                findViewById<ComposeView>(R.id.compose_view).visibility= View.GONE

                findViewById<ImageView>(R.id.refresh).visibility= View.GONE
                findViewById<ImageView>(R.id.flashToggleButton).visibility= View.GONE
                findViewById<ImageView>(R.id.selectImageButton).visibility= View.GONE
                findViewById<ImageView>(R.id.lock).setImageResource(R.drawable.lockopen)
            }

        }

        opacitySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Calcula la opacidad en función del valor del control deslizante (de 0 a 100)
                val opacity = progress / 100f
                findViewById<TextView>(R.id.porcentaje).text=progress.toString()+"%"
                // Aplica la opacidad a la imagen superpuesta
                imageOverlay.alpha = opacity
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Se llama cuando el usuario toca el control deslizante
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Se llama cuando el usuario suelta el control deslizante
            }
        })


        val selectImageButton = findViewById<ImageView>(R.id.selectImageButton)
        selectImageButton.setOnClickListener {
            // Al hacer clic en el botón, solicita permiso de lectura
            if (requestCameraPermissions())
                requestReadStoragePermission()

        }
    }

    // Escuchador para el gesto de pinch-to-zoom (zoom mediante gestos de pellizco)
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Delegar eventos de toque al ScaleGestureDetector
        scaleGestureDetector?.onTouchEvent(event)
        return true
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            var scaleFactor = detector.scaleFactor
            val newScale = scaleFactor * scaleFactor

            if (newScale > 1.0f && newScale < maxZoom) {
                scaleFactor *= newScale
                matrix.postScale(newScale, newScale, detector.focusX, detector.focusY)
                imageOverlay.imageMatrix = matrix
            }
            return true
        }
    }


    private fun requestCameraPermissions(): Boolean {
        val perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES

            )
        } else {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE


            )
        }

        if (EasyPermissions.hasPermissions(this, *perms)) {
            // Tienes los permisos, puedes proceder con la lógica de la cámara.
            try {
                setupCamera()
                return true
            } catch (e: Exception) {
            }

        } else {
            // Solicitar permisos al usuario
            EasyPermissions.requestPermissions(
                this,
                resources.getString(R.string.permision),
                RC_CAMERA_PERMISSION,
                *perms
            )
        }
        return false
    }

    fun setupCamera() {
        try {
            camera = Camera.open()
            camera.setDisplayOrientation(90)

            val holder = surfaceView.holder
            holder.addCallback(this)
            camera.setPreviewDisplay(surfaceView.holder)
        } catch (e: Exception) {
        }
    }

    private fun openCamera(holder: SurfaceHolder) {
        try {
            camera = Camera.open()
            camera.setDisplayOrientation(90) // Rotación a portrait
            camera.setPreviewDisplay(holder)
            camera.startPreview()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        try {
            camera.setPreviewDisplay(holder)
            camera.startPreview()
            openCamera(holder)
        } catch (e: Exception) {
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        // Se llama cuando se crea la superficie
    }


    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // Se llama cuando la superficie se destruye
        if (::camera.isInitialized) {
            try {
                camera.stopPreview()
                camera.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadImageWithOpacity( imageUrl: String) {
        val opacity = 128  // 50% de opacidad, 255 es totalmente opaco

        Picasso.get()
            .load(imageUrl)
            .transform(OpacityTransformation(opacity))  // Aplica la transformación con opacidad
            .into(imageOverlay)
    }

    class OpacityTransformation(private val opacity: Int) : Transformation {

        override fun transform(source: Bitmap): Bitmap? {
            // Crear un nuevo Bitmap con el mismo tamaño que la imagen original
            val result = source.config?.let { Bitmap.createBitmap(source.width, source.height, it) }
            val canvas = result?.let { Canvas(it) }

            // Crear un objeto Paint con la opacidad deseada
            val paint = Paint()
            paint.alpha = opacity

            // Dibujar la imagen sobre el canvas con el Paint que tiene la opacidad aplicada
            if (canvas != null) {
                canvas.drawBitmap(source, 0f, 0f, paint)
            }

            // Limpiar el recurso original
            if (source != result) {
                source.recycle()
            }

            return result
        }

        override fun key(): String {
            return "opacity$opacity"
        }
    }


    private fun requestReadStoragePermission() {
        val perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES

            )
        } else {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE


            )

        }

        if (EasyPermissions.hasPermissions(this, *perms)) {
            openImagePicker()

            try {
                setupCamera()

            } catch (e: Exception) {
            }

        } else {
            // Solicitar permisos al usuario
            EasyPermissions.requestPermissions(
                this,
                resources.getString(R.string.permision),
                RC_READ_STORAGE_PERMISSION,
                *perms
            )
        }
    }

    // Declara una propiedad para el ActivityResultLauncher
    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                // Realiza acciones con la URI de la imagen seleccionada
                selectedImageUri = uri
                // Cargar y superponer la imagen seleccionada en el ImageView

                loadAndOverlaySelectedImage()
                requestCameraPermissions()

            } else {
                // El usuario no seleccionó una imagen o hubo un error
            }
        }

    private fun openImagePicker() {
        /*  val imagePickerIntent = Intent(Intent.ACTION_PICK)
          imagePickerIntent.type = "image/*"
          startActivityForResult(imagePickerIntent, RC_IMAGE_PICK)*/

         */

        imagePicker.launch("image/*")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.data
            // Cargar y superponer la imagen seleccionada en el ImageView
            loadAndOverlaySelectedImage()


        }
    }
/*
    private fun loadAndOverlaySelectedImage() {
        if (selectedImageUri != null) {
            try {
                // Libera la cámara y el SurfaceView
                val originalBitmap =
                    MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImageUri)

                // Comprueba si la imagen es más ancha que alta
                if (originalBitmap.width > originalBitmap.height) {
                    val matrix = Matrix()
                    matrix.postRotate(90f) // Gira la imagen 90 grados para mostrarla verticalmente
                    val rotatedBitmap = Bitmap.createBitmap(
                        originalBitmap,
                        0,
                        0,
                        originalBitmap.width,
                        originalBitmap.height,
                        matrix,
                        true
                    )
                    imageOverlay.setImageBitmap(rotatedBitmap)
                } else {
                    imageOverlay.setImageBitmap(originalBitmap)
                }


                // Configura la transparencia según sea necesario
                imageOverlay.alpha = 0.5f // Ajusta la transparencia según sea necesario
                setupCamera()
                opacitySeekBar.progress = 50
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }


 */ /*
private fun loadAndOverlaySelectedImage() {
    if (selectedImageUri != null) {
        try {
            // Opciones para decodificar y redimensionar la imagen
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            // Obtiene las dimensiones de la imagen sin cargarla en memoria
            val inputStream = contentResolver.openInputStream(selectedImageUri!!)
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            // Calcula el factor de escala para reducir el tamaño de la imagen
            val maxDimension = 1024 // Cambia según tus necesidades (1024px es un buen punto de inicio)
            val scaleFactor = calculateInSampleSize(options, maxDimension, maxDimension)

            // Decodifica la imagen con el factor de escala calculado
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = scaleFactor
                inPreferredConfig = Bitmap.Config.ARGB_8888 // Configuración optimizada para la memoria
            }
            val resizedBitmap = contentResolver.openInputStream(selectedImageUri!!)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            }

            resizedBitmap?.let { bitmap ->
                // Comprueba si la imagen es más ancha que alta
                val finalBitmap = if (bitmap.width > bitmap.height) {
                    val matrix = Matrix()
                    matrix.postRotate(90f) // Gira la imagen 90 grados para mostrarla verticalmente
                    Bitmap.createBitmap(
                        bitmap,
                        0,
                        0,
                        bitmap.width,
                        bitmap.height,
                        matrix,
                        true
                    )
                } else {
                    bitmap
                }

                // Establece la imagen redimensionada y procesada en el overlay
                imageOverlay.setImageBitmap(finalBitmap)
                imageOverlay.alpha = 0.5f // Ajusta la transparencia según sea necesario
            }

            setupCamera()
            opacitySeekBar.progress = 50
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
*/

    private fun loadAndOverlaySelectedImage() {
        if (selectedImageUri == null) return

        lifecycleScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                try {
                    // Lee solo dimensiones
                    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    contentResolver.openInputStream(selectedImageUri!!)?.use {
                        BitmapFactory.decodeStream(it, null, options)
                    }

                    // Calcula factor de escala
                    val maxDimension = 1024
                    val scaleFactor = calculateInSampleSize(options, maxDimension, maxDimension)

                    // Decodifica reducido
                    val decodeOptions = BitmapFactory.Options().apply {
                        inSampleSize = scaleFactor
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                    }
                    val bmp = contentResolver.openInputStream(selectedImageUri!!)?.use {
                        BitmapFactory.decodeStream(it, null, decodeOptions)
                    }

                    // Rota si es necesario
                    bmp?.let { original ->
                        if (original.width > original.height) {
                            val matrix = Matrix().apply { postRotate(90f) }
                            Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)
                        } else {
                            original
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }

            // Actualiza la UI solo cuando ya esté lista
            bitmap?.let {
                imageOverlay.setImageBitmap(it)
                imageOverlay.alpha = 0.5f
                opacitySeekBar.progress = 50
            }
        }
    }

    /**
     * Calcula el valor inSampleSize para redimensionar la imagen.
     */
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calcula el mayor valor de inSampleSize que sea potencia de 2 y mantenga la imagen más grande que las dimensiones requeridas
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }


    private fun toggleFlash() {
        if (::camera.isInitialized) {
            if (isFlashOn) {
                turnOffFlash()
                Log.i("i", "off")
            } else {
                turnOnFlash()
                Log.i("i", "on")
            }
        }
    }

    private fun turnOnFlash() {
        if (::camera.isInitialized) {
            val parameters = camera.parameters
            parameters.flashMode = Camera.Parameters.FLASH_MODE_TORCH
            camera.parameters = parameters
            isFlashOn = true
            findViewById<ImageView>(R.id.flashToggleButton).setImageResource(R.drawable.flashon)
        }
    }

    private fun turnOffFlash() {
        if (::camera.isInitialized) {
            val parameters = camera.parameters
            parameters.flashMode = Camera.Parameters.FLASH_MODE_OFF
            camera.parameters = parameters
            isFlashOn = false
            findViewById<ImageView>(R.id.flashToggleButton).setImageResource(R.drawable.flasoff)

        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        if (requestCode == RC_CAMERA_PERMISSION) {
            setupCamera()
        } else if (requestCode == RC_READ_STORAGE_PERMISSION) {
            openImagePicker()
            try {
                setupCamera()

            } catch (e: Exception) {
            }
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (requestCode == RC_CAMERA_PERMISSION || requestCode == RC_READ_STORAGE_PERMISSION) {
            if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
                AppSettingsDialog.Builder(this).setRationale(getString(R.string.perm)).build()
                    .show()
            }
        }
    }

    private fun showSettingsSnackbar() {
        Snackbar.make(
            findViewById(android.R.id.content),
            getString(R.string.msm),
            Snackbar.LENGTH_LONG
        ).setAction(getString(R.string.config)) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        }.show()
    }

    override fun onResume() {
        super.onResume()
        // Reconfigura la cámara cuando la actividad se reanuda
        val perms = arrayOf(
            Manifest.permission.CAMERA
        )
        if (EasyPermissions.hasPermissions(this, *perms)) {
            // Tienes los permisos, puedes proceder con la lógica de la cámara.
            setupCamera()
        }
    }

    override fun onPause() {
        super.onPause()
        // Libera los recursos de la cámara cuando la actividad se pausa
        if (::camera.isInitialized) {

            try {
                turnOffFlash()
                camera.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }


            try {
                camera.stopPreview()
                camera.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            camera.stopPreview() // Detiene la vista previa de la cámara
            camera.release()
        } catch (e: Exception) {
        }
    }



}
