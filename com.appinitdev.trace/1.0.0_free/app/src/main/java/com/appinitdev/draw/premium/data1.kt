//package com.appinitdev.draw.premium
//
//import android.Manifest
//import android.content.Context
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import android.net.Uri
//import android.os.Build
//import android.os.Environment
//import android.widget.Toast
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.annotation.RequiresApi
//import androidx.camera.core.*
//import androidx.camera.lifecycle.ProcessCameraProvider
//import androidx.camera.view.PreviewView
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.gestures.detectDragGestures
//import androidx.compose.foundation.gestures.detectTapGestures
//import androidx.compose.foundation.gestures.detectTransformGestures
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.*
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.*
//import androidx.compose.ui.graphics.drawscope.Stroke
//import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
//import androidx.compose.ui.graphics.graphicsLayer
//import androidx.compose.ui.input.pointer.pointerInput
//import androidx.compose.ui.layout.onSizeChanged
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.platform.LocalLifecycleOwner
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.viewinterop.AndroidView
//import coil.compose.rememberAsyncImagePainter
//import kotlinx.coroutines.*
//import java.io.File
//import java.io.FileOutputStream
//import kotlin.random.Random
//import androidx.core.net.toUri
//import com.appinitdev.draw.premium.panel.CameraControlsPanel
//import com.appinitdev.draw.premium.panel.CameraControlsPanelPro
//
//// ==================== ENUMS ====================
//
//// ==================== DATA CLASS ====================
//
//// ==================== COMPOSABLE PRINCIPAL ====================
//@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
//@Composable
//fun CameraPaintScreen1() {
//    val context = LocalContext.current
//    val scope = rememberCoroutineScope()
//
//    // Estados principales
//    var showMenu by remember { mutableStateOf(true) }
//    var permissionsGranted by remember { mutableStateOf(false) }
//    var showCamera by remember { mutableStateOf(true) }
//    var isFlashOn by remember { mutableStateOf(false) }
//    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }
//
//    var isLocked by remember { mutableStateOf(false) }
//    var imageUri by remember { mutableStateOf(Uri.parse("android.resource://${context.packageName}/${R.drawable.logo}")) }
//    var imageAlpha by remember { mutableStateOf(0.5f) }
//    var currentColor by remember { mutableStateOf(Color.Black) }
//    var strokeWidth by remember { mutableStateOf(5f) }
//    var drawEnabled by remember { mutableStateOf(false) }
//
//    var currentPath by remember { mutableStateOf(Path()) }
//    var showImage by remember { mutableStateOf(true) }
//    var showCanva by remember { mutableStateOf(true) }
//    var cameraControl: CameraControl? by remember { mutableStateOf(null) }
//    var drawTrigger by remember { mutableStateOf(0) }
//
//    var lastPoint by remember { mutableStateOf<Offset?>(null) }
//
//    // Transformación de imagen y canvas
//    var imageOffset by remember { mutableStateOf(Offset.Zero) }
//    var imageScale by remember { mutableStateOf(2.5f) }
//    var imageRotation by remember { mutableStateOf(0f) }
//    var canvasWidth by remember { mutableStateOf(1080) }
//    var canvasHeight by remember { mutableStateOf(1920) }
//    var showColorPicker by remember { mutableStateOf(false) }
//
//    // ==================== PERMISOS ====================
//    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
//        permissionsGranted = perms[Manifest.permission.CAMERA] == true
//        if (!permissionsGranted) Toast.makeText(context, "Se requieren permisos de cámara", Toast.LENGTH_SHORT).show()
//    }
//    LaunchedEffect(Unit) { permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA)) }
//
//    Box(modifier = Modifier.fillMaxSize()) {
//
//        // ==================== 1️⃣ Cámara ====================
//        if (permissionsGranted && showCamera) {
//            CameraPreviewComposable(
//                modifier = Modifier.fillMaxSize(),
//                showCamera = showCamera,
//                onCameraReady = { control -> cameraControl = control },
//                onPreviewReady = { previewView -> previewViewRef = previewView }
//            )
//        }
//
//        // ==================== 2️⃣ Imagen + Canvas ====================
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(if (showCamera) Color.Transparent else Color.White)
//                .pointerInput(drawEnabled, isLocked) {
//                    if (!drawEnabled && !isLocked) {
//                        detectTransformGestures { _, pan, zoom, rotation ->
//                            imageOffset += pan
//                            imageScale = (imageScale * zoom).coerceIn(0.5f, 15f)
//                            imageRotation += rotation
//                        }
//                    }
//                }
//        ) {
//            // Imagen de fondo
//            if (showImage)
//                imageUri?.let {
//                    Image(
//                        painter = rememberAsyncImagePainter(it),
//                        contentDescription = null,
//                        modifier = Modifier
//                            .align(Alignment.Center)
//                            .graphicsLayer(
//                                translationX = imageOffset.x,
//                                translationY = imageOffset.y,
//                                scaleX = imageScale,
//                                scaleY = imageScale,
//                                rotationZ = imageRotation,
//                                alpha = imageAlpha
//                            )
//                    )
//                }
//
//        }
//
//        // ==================== 3️⃣ Botón menú ====================
//        IconButton(
//            onClick = { showMenu = true },
//            modifier = Modifier
//                .align(Alignment.TopEnd)
//                .padding(16.dp)
//                .size(72.dp)
//        ) {
//            Icon(Icons.Filled.Menu, contentDescription = "Abrir menú", modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
//        }
//
//
//        // ==================== 🔒 Botón Flotante del Candado ====================
//        IconButton(
//            onClick = {
//                isLocked = !isLocked
//                Toast.makeText(context, if (isLocked) "Imagen bloqueada" else "Imagen liberada", Toast.LENGTH_SHORT).show()
//            },
//            modifier = Modifier
//                .align(Alignment.TopEnd)
//                .padding(top = 80.dp, end = 24.dp) // Posicionado estratégicamente abajo del menú principal
//                .size(48.dp)
//                .clip(CircleShape)
//                .background(Color.Black.copy(alpha = 0.5f)) // Fondo translúcido oscuro de tus capturas
//        ) {
//            Icon(
//                imageVector = if (isLocked) Icons.Filled.Lock else Icons.Filled.LockOpen,
//                contentDescription = "Bloquear imagen",
//                tint = if (isLocked) Color(0xFFFF4081) else Color.White, // Rosa fucsia si está bloqueado, blanco si no
//                modifier = Modifier.size(24.dp)
//            )
//        }
//
//        // ==================== 3️⃣ Botón menú ====================
//        IconButton(
//            onClick = { showMenu = true },
//            modifier = Modifier
//                .align(Alignment.TopEnd)
//                .padding(16.dp)
//                .size(72.dp)
//        ) {
//            Icon(Icons.Filled.Menu, contentDescription = "Abrir menú", modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
//        }
//
//        // ==================== 4️⃣ Panel de controles ====================
//        if (showMenu) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(Color.Black.copy(alpha = 0.4f))
//                    .pointerInput(Unit) { detectTapGestures { showMenu = false } },
//                contentAlignment = Alignment.Center
//            ) {
//                Box(
//                    modifier = Modifier
//                        .wrapContentSize()
//                        .pointerInput(Unit) { detectTapGestures {} }
//                ) {
//                    CameraControlsPanelPro(
//                        showCamera = showCamera,
//                        onToggleCamera = { showCamera = !showCamera },
//                        isFlashOn = isFlashOn,
//                        onToggleFlash = { cameraControl?.enableTorch(!isFlashOn); isFlashOn = !isFlashOn },
//                        imageUri = imageUri,
//                        imageAlpha = imageAlpha,
//                        onAlphaChange = { imageAlpha = it },
//                        currentColor = currentColor,
//                        onImageUriChange = { imageUri = it },
//                        onCollapseClick = { showMenu = false } // Mapeado directo a la flecha hacia abajo (v)
//                    )
//                }
//            }
//        }
//    }
//}
//
//// ==================== FUNCIONES AUXILIARES ====================
////fun Path.getLastPoint(): Offset? {
////    val measure = android.graphics.PathMeasure(asAndroidPath(), false)
////    val coords = FloatArray(2)
////    if (measure.length > 0) {
////        measure.getPosTan(measure.length, coords, null)
////        return Offset(coords[0], coords[1])
////    }
////    return null
////}
//
//
//
//// ==================== COMPOSABLE DE CÁMARA ====================
//@Composable
//fun CameraPreviewComposable(modifier: Modifier = Modifier, showCamera: Boolean, onCameraReady: (CameraControl) -> Unit, onPreviewReady: (PreviewView) -> Unit) {
//    val context = LocalContext.current
//    val lifecycleOwner = LocalLifecycleOwner.current
//    val previewView = remember { PreviewView(context).apply { implementationMode = PreviewView.ImplementationMode.COMPATIBLE } }
//
//    LaunchedEffect(Unit) { onPreviewReady(previewView) }
//
//    LaunchedEffect(showCamera) {
//        if (showCamera) try {
//            val provider = ProcessCameraProvider.getInstance(context).get()
//            val preview = Preview.Builder().build().apply { setSurfaceProvider(previewView.surfaceProvider) }
//            val selector = CameraSelector.DEFAULT_BACK_CAMERA
//            provider.unbindAll()
//            val camera = provider.bindToLifecycle(lifecycleOwner, selector, preview)
//            onCameraReady(camera.cameraControl)
//        } catch (e: Exception) { Toast.makeText(context, "Error cámara: ${e.message}", Toast.LENGTH_LONG).show() }
//    }
//    AndroidView(factory = { previewView }, modifier = modifier)
//}
//
