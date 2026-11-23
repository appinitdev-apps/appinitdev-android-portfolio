//package com.appinitdev.videotophoto
//
//import android.content.ContentValues
//import android.content.Context
//import android.graphics.Bitmap
//import android.media.MediaMetadataRetriever
//import android.net.Uri
//import android.os.Build
//import android.os.Bundle
//import android.os.Environment
//import android.provider.MediaStore
//import android.widget.Toast
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.compose.setContent
//import androidx.activity.result.ActivityResultLauncher
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyRow
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.asImageBitmap
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.media3.common.MediaItem
//import androidx.media3.exoplayer.ExoPlayer
//import androidx.media3.ui.PlayerView
//import coil.compose.AsyncImage
//import com.canhub.cropper.CropImageContract
//import com.canhub.cropper.CropImageContractOptions
//import com.canhub.cropper.CropImageOptions
//import com.canhub.cropper.CropImageView
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import java.io.File
//
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            MaterialTheme {
//                Surface(modifier = Modifier.fillMaxSize()) {
//                    VideoFrameCatcherApp()
//                }
//            }
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun VideoFrameCatcherApp() {
//    val context = LocalContext.current
//    val scope = rememberCoroutineScope()
//    val db = remember { AppDatabase.getDatabase(context) }
//
//    val cropImageLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
//        if (result.isSuccessful) {
//            result.uriContent?.let { uriCortada ->
//                scope.launch {
//                    // Llamamos a saveImageToInternalStorage que es como se llama tu función abajo
//                    val pathFinal = saveImageToInternalStorage(context, uriCortada)
//                    db.captureDao().insertCapture(CaptureEntity(imagePath = pathFinal, videoName = "Video"))
//                    Toast.makeText(context, "¡Recorte guardado!", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }
//    var videoUri by remember { mutableStateOf<Uri?>(null) }
//    var surroundingFrames by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
//    var showSheet by remember { mutableStateOf(false) }
//
////    // 1. Configurador de Recorte (Cropper)
////    val cropImageLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
////        if (result.isSuccessful) {
////            result.uriContent?.let { uri ->
////                scope.launch {
////                    // Aquí podrías implementar una edición extra o guardar directo
////                    Toast.makeText(context, "Imagen recortada lista", Toast.LENGTH_SHORT).show()
////                }
////            }
////        }
////    }
//
//    val exoPlayer = remember {
//        ExoPlayer.Builder(context).build().apply { playWhenReady = false }
//    }
//
//    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
//        videoUri = uri
//        uri?.let {
//            exoPlayer.setMediaItem(MediaItem.fromUri(it))
//            exoPlayer.prepare()
//        }
//    }
//
//    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
//        if (videoUri != null) {
//            // VIDEO A PANTALLA COMPLETA
//            AndroidView(
//                factory = { ctx ->
//                    PlayerView(ctx).apply {
//                        player = exoPlayer
//                        useController = true // Puedes ponerlo en false para una UI más limpia
//                    }
//                },
//                modifier = Modifier.fillMaxSize()
//            )
//
//            // BOTÓN DE CAPTURA FLOTANTE
//            Button(
//                onClick = {
//                    val pos = exoPlayer.currentPosition
//                    scope.launch {
//                        surroundingFrames = extractSurroundingFrames(context, videoUri!!, pos)
//                        showSheet = true
//                    }
//                },
//                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 48.dp)
//            ) {
//                Text("Capturar Fotograma")
//            }
//        } else {
//            // Pantalla inicial si no hay video
//            Button(
//                onClick = { videoPicker.launch("video/*") },
//                modifier = Modifier.align(Alignment.Center)
//            ) {
//                Text("Seleccionar Video")
//            }
//        }
//        val sheetState = rememberModalBottomSheetState(
//            skipPartiallyExpanded = false // Esto hace que se abra directamente en pantalla completa
//        )
//        // VENTANA EMERGENTE (BottomSheet) PARA ELEGIR FOTOGRAMA
//        if (showSheet) {
//            ModalBottomSheet(
//                sheetState = sheetState,
//                onDismissRequest = { showSheet = false },
//                containerColor = MaterialTheme.colorScheme.surfaceVariant
//            ) {
//                Column(modifier = Modifier.padding(16.dp).navigationBarsPadding()) {
//                    Text("Selecciona el mejor fotograma", style = MaterialTheme.typography.titleMedium)
//                    Spacer(modifier = Modifier.height(16.dp))
//                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                        items(surroundingFrames) { bitmap ->
//                            Image(
//                                bitmap = bitmap.asImageBitmap(),
//                                contentDescription = null,
//                                modifier = Modifier
//                                    .height(250.dp)
//                                   // .height(150.dp)
//                                  //  .aspectRatio(16f / 9f)
//                                    .clickable {
//                                        showSheet = false
//                                        launchCropper(context, bitmap, cropImageLauncher)
//                                    },
//                               // contentScale = ContentScale.Crop
//                            )
//                        }
//                    }
//                    Spacer(modifier = Modifier.height(40.dp))
//                }
//            }
//        }
//    }
//}
//
//// LÓGICA DE EXTRACCIÓN (5 antes, 5 después)
//suspend fun extractSurroundingFrames(context: Context, uri: Uri, centerMs: Long): List<Bitmap> {
//    return withContext(Dispatchers.IO) {
//        val retriever = MediaMetadataRetriever()
//        val list = mutableListOf<Bitmap>()
//        try {
//            retriever.setDataSource(context, uri)
//            // Intervalo de 100ms entre frames para ver diferencia real
//            for (i in -5..5) {
//                val time = (centerMs + (i * 500)).coerceAtLeast(0)
//                retriever.getFrameAtTime(time * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)?.let {
//                    list.add(it)
//                }
//            }
//        } catch (e: Exception) { e.printStackTrace() }
//        finally { retriever.release() }
//        list
//    }
//}
//
//
//suspend fun saveImageToInternalStorage(context: Context, uri: Uri): String {
//    return withContext(Dispatchers.IO) {
//        // Creamos un nombre único para la captura
//        val filename = "Capture_${System.currentTimeMillis()}.jpg"
//        val file = File(context.filesDir, filename)
//
//        context.contentResolver.openInputStream(uri)?.use { input ->
//            file.outputStream().use { output ->
//                input.copyTo(output)
//            }
//        }
//        file.absolutePath // Esta es la ruta que guardaremos en la BD
//    }
//}
//
//// 2. Modifica la función de lanzamiento para que use los parámetros correctos
//fun launchCropper(
//    context: Context,
//    bitmap: Bitmap,
//    launcher: ActivityResultLauncher<CropImageContractOptions>
//) {
//    val tempFile = File(context.cacheDir, "temp_capture.jpg")
//    tempFile.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
//    val uri = Uri.fromFile(tempFile)
//
//    val options = CropImageContractOptions(uri, CropImageOptions().apply {
//        // --- CONFIGURACIÓN CRÍTICA ---
//        guidelines = CropImageView.Guidelines.ON
//        showProgressBar = true
//
//        // Forzamos el color del icono del menú para que contraste (Negro o Blanco)
//        activityMenuIconColor = android.graphics.Color.BLACK
//
//        // Título de la actividad para saber qué estamos haciendo
//        activityTitle = "Recortar Captura"
//
//        // Habilitar botones de edición
//        allowRotation = true
//        allowFlipping = true
//
//        // Asegúrate de NO estar bloqueando el guardado accidentalmente
//        // ------------------------------
//    })
//
//    launcher.launch(options)
//}