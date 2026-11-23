class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks,
    SurfaceHolder.Callback {
    private val RC_CAMERA_PERMISSION = 123
    private val RC_READ_STORAGE_PERMISSION = 124
    private val RC_IMAGE_PICK = 125
    private lateinit var camera: Camera
    private lateinit var surfaceView: SurfaceView
    private lateinit var imageOverlay: ImageView
    private lateinit var opacitySeekBar: SeekBar
    private var selectedImageUri: android.net.Uri? = null
    private var scaleGestureDetector: ScaleGestureDetector? = null
    private val matrix = Matrix()
    private val savedMatrix = Matrix()
    private var scaleFactor = 1.0f
    private val maxZoom = 3.0f
    private var distanceStart = 1f
    private var isFlashOn: Boolean = false
    private var isView: Boolean = false
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
                }
            )  
        }
        try {
            requestCameraPermissions()
        } catch (e: Exception) {
        }
        try {
            camera.release()
            surfaceView.holder.removeCallback(this)
            setupCamera()
            surfaceView.holder.addCallback(this)
            camera.startPreview()
        } catch (e: Exception) {
        }
        scaleGestureDetector = ScaleGestureDetector(this, ScaleListener())
        opacitySeekBar = findViewById<SeekBar>(R.id.opacitySeekBar)
        opacitySeekBar.progress = 50
        findViewById<ImageView>(R.id.refresh).setOnClickListener {
            try {
                camera.release()
                surfaceView.holder.removeCallback(this)
                setupCamera()
                surfaceView.holder.addCallback(this)
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
                findViewById<FrameLayout>(R.id.opacitySeekBarFrame).visibility= View.VISIBLE
                findViewById<ComposeView>(R.id.compose_view).visibility= View.VISIBLE
                findViewById<ImageView>(R.id.refresh).visibility= View.VISIBLE
                findViewById<ImageView>(R.id.flashToggleButton).visibility= View.VISIBLE
                findViewById<ImageView>(R.id.selectImageButton).visibility= View.VISIBLE
                findViewById<ImageView>(R.id.lock).setImageResource(R.drawable.lockclose)
            } else {
                findViewById<FrameLayout>(R.id.opacitySeekBarFrame).visibility= View.GONE
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
                val opacity = progress / 100f
                findViewById<TextView>(R.id.porcentaje).text=progress.toString()+"%"
                imageOverlay.alpha = opacity
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        val selectImageButton = findViewById<ImageView>(R.id.selectImageButton)
        selectImageButton.setOnClickListener {
            if (requestCameraPermissions())
                requestReadStoragePermission()
        }
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
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
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.READ_MEDIA_IMAGES
            )
        } else {
            arrayOf(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
        if (EasyPermissions.hasPermissions(this, *perms)) {
            try {
                setupCamera()
                return true
            } catch (e: Exception) {
            }
        } else {
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
            val params = camera.parameters
            val supportedSizes = params.supportedPreviewSizes
            val bestSize = supportedSizes.maxByOrNull { it.width * it.height }
            bestSize?.let {
                params.setPreviewSize(it.width, it.height)
            }
            if (params.supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                params.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
            }
            camera.parameters = params
            val holder = surfaceView.holder
            holder.addCallback(this)
            camera.setPreviewDisplay(surfaceView.holder)
        } catch (e: Exception) {
        }
    }
    private fun openCamera(holder: SurfaceHolder) {
        try {
            camera = Camera.open()
            camera.setDisplayOrientation(90) 
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
    }
    override fun surfaceDestroyed(holder: SurfaceHolder) {
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
        val opacity = 128  
        Picasso.get()
            .load(imageUrl)
            .transform(OpacityTransformation(opacity))  
            .into(imageOverlay)
    }
    class OpacityTransformation(private val opacity: Int) : Transformation {
        override fun transform(source: Bitmap): Bitmap? {
            val result = source.config?.let { Bitmap.createBitmap(source.width, source.height, it) }
            val canvas = result?.let { Canvas(it) }
            val paint = Paint()
            paint.alpha = opacity
            if (canvas != null) {
                canvas.drawBitmap(source, 0f, 0f, paint)
            }
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
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.READ_MEDIA_IMAGES
            )
        } else {
            arrayOf(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
        if (EasyPermissions.hasPermissions(this, *perms)) {
            openImagePicker()
            try {
                setupCamera()
            } catch (e: Exception) {
            }
        } else {
            EasyPermissions.requestPermissions(
                this,
                resources.getString(R.string.permision),
                RC_READ_STORAGE_PERMISSION,
                *perms
            )
        }
    }
    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri
                loadAndOverlaySelectedImage()
                requestCameraPermissions()
            } else {
            }
        }
    private fun openImagePicker() {
        imagePicker.launch("image/*")
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            loadAndOverlaySelectedImage()
        }
    }
    private fun loadAndOverlaySelectedImage() {
        if (selectedImageUri == null) return
        lifecycleScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                try {
                    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    contentResolver.openInputStream(selectedImageUri!!)?.use {
                        BitmapFactory.decodeStream(it, null, options)
                    }
                    val maxDimension = 1024
                    val scaleFactor = calculateInSampleSize(options, maxDimension, maxDimension)
                    val decodeOptions = BitmapFactory.Options().apply {
                        inSampleSize = scaleFactor
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                    }
                    val bmp = contentResolver.openInputStream(selectedImageUri!!)?.use {
                        BitmapFactory.decodeStream(it, null, decodeOptions)
                    }
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
            bitmap?.let {
                imageOverlay.setImageBitmap(it)
                imageOverlay.alpha = 0.5f
                opacitySeekBar.progress = 50
            }
        }
    }
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
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
        val perms = arrayOf(
            android.Manifest.permission.CAMERA
        )
        if (EasyPermissions.hasPermissions(this, *perms)) {
            setupCamera()
        }
    }
    override fun onPause() {
        super.onPause()
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
            camera.stopPreview() 
            camera.release()
        } catch (e: Exception) {
        }
    }
}
