//package com.appinitdev.tester.screen.CommunityAppsSection
//
//
//import android.content.Intent
//import android.net.Uri
//import android.widget.Toast
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.LazyListState
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.Chat
//import androidx.compose.material.icons.automirrored.filled.Launch
//import androidx.compose.material.icons.automirrored.filled.Send
//import androidx.compose.material.icons.filled.Add
//import androidx.compose.material.icons.filled.Apps
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.Build
//import androidx.compose.material.icons.filled.Chat
//import androidx.compose.material.icons.filled.Check
//import androidx.compose.material.icons.filled.CheckCircle
//import androidx.compose.material.icons.filled.Close
//import androidx.compose.material.icons.filled.FileDownload
//import androidx.compose.material.icons.filled.Launch
//import androidx.compose.material3.Badge
//import androidx.compose.material3.Button
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.HorizontalDivider
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.ListItem
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Tab
//import androidx.compose.material3.TabRow
//import androidx.compose.material3.Text
//import androidx.compose.material3.TopAppBar
//import androidx.compose.material3.TopAppBarDefaults
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableIntStateOf
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.stringResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//import com.appinitdev.tester.Funciones
//import com.appinitdev.tester.R
//import com.appinitdev.tester.model.AppModel
//import com.appinitdev.tester.screen.MessageBubble
//import com.appinitdev.tester.viewModel.AuthUiState
//import com.appinitdev.tester.viewModel.AuthViewModel
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.FirebaseUser
//import kotlin.collections.get
//
//@Composable
//fun CommunityAppsSection1(viewModel: AuthViewModel, navController: NavController, listState: LazyListState) {
//    val apps by viewModel.communityApps.collectAsState()
//    val user = FirebaseAuth.getInstance().currentUser
//    val uiState by viewModel.uiState.collectAsState()
//    val context = LocalContext.current
//    // Cargamos las apps al mostrar la sección
//
//    val mensajeExito = stringResource(R.string.paquete_de_30_puntos_0_25_rep_extra)
//
//    LaunchedEffect(uiState) {
//        if (uiState is AuthUiState.PurchaseSuccess) {
//            // 2. Usas el mensaje ya obtenido
//            Toast.makeText(
//                context,
//                mensajeExito,
//                Toast.LENGTH_LONG
//            ).show()
//
//            // 3. Limpias el estado en el ViewModel
//            viewModel.clearState()
//        }
//    }
//
//    LaunchedEffect(Unit) {
//        viewModel.loadCommunityApps()
//    }
//
//    val misChats = apps.filter { app ->
//        app.testers[user?.uid]?.validado == "Aceptado"
//    }
//
//    val explorarApps = apps.filter { app ->
//        app.testers[user?.uid]?.validado != "Aceptado"
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        Text(
//            text = stringResource(R.string.apps_para_testear),
//            style = MaterialTheme.typography.headlineSmall,
//            fontWeight = FontWeight.Bold
//        )
//        Text(
//            text = stringResource(R.string.ayuda_a_otros_desarrolladores_y_gana_puntos_las_apps_con_mayor_rep_son_desarrolladores_mas_activos_y_que_apoyan_a_otros),
//            style = MaterialTheme.typography.bodyMedium,
//            color = Color.Gray
//        )
//        Spacer(modifier = Modifier.height(16.dp))
//
//        LazyColumn(
//            state = listState,
//            modifier = Modifier.fillMaxSize(),
//            contentPadding = PaddingValues(8.dp),
//            verticalArrangement = Arrangement.spacedBy(12.dp)
//        ) {
//            // --- SECCIÓN: MIS CHATS ---
//            if (misChats.isNotEmpty()) {
//                item {
//                    Text(
//                        text = stringResource(R.string.mis_grupos_de_chat),
//                        style = MaterialTheme.typography.titleMedium,
//                        color = MaterialTheme.colorScheme.primary,
//                        fontWeight = FontWeight.Bold
//                    )
//                }
//                items(misChats) { app ->
//                    CommunityAppItem(app, isJoined = true, user) {
//                        navController.navigate("app_detail/${app.id}")
//                    }
//                    HorizontalDivider(
//                        modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 16.dp, bottom = 0.dp), // Margen a los lados
//                        thickness = 1.dp,                             // Grosor de la línea
//                        color = Color.Gray.copy(alpha = 0.5f)           // Color con transparencia
//                    )
//                }
//                item { Spacer(modifier = Modifier.height(8.dp)) }
//            }
//
//            // --- SECCIÓN: EXPLORAR ---
//            item {
//                Text(
//                    text = stringResource(R.string.explorar_nuevas_apps),
//                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.Bold
//                )
//            }
//
//            if (explorarApps.isEmpty() && misChats.isEmpty()) {
//                item {
//                    Box(
//                        Modifier
//                            .fillMaxWidth()
//                            .padding(20.dp), contentAlignment = Alignment.Center
//                    ) {
//                        CircularProgressIndicator()
//                    }
//                }
//            } else if (explorarApps.isEmpty()) {
//                item {
//                    Text(
//                        stringResource(R.string.no_hay_m_s_apps_disponibles),
//                        color = Color.Gray,
//                        fontSize = 14.sp
//                    )
//                }
//            } else {
//                items(explorarApps) { app ->
//                    CommunityAppItem(app, isJoined = false, user = user) {
//                        navController.navigate("app_detail/${app.id}")
//                    }
//                }
//            }
//            item {
//
//                Spacer(modifier = Modifier.height(80.dp))
//
//            }
//        }
//    }
//}
//
//@Composable
//fun CommunityAppItem(app: AppModel, isJoined: Boolean, user: FirebaseUser?, onClick: () -> Unit) {
//
//    val context = LocalContext.current
//    // Verificamos si la app está instalada usando tu función de Funciones.kt
//    val estaInstalada = remember(app.paquete) {
//        Funciones.isAppInstalled(context, app.paquete)
//    }
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable { onClick() },
//        elevation = CardDefaults.cardElevation(2.dp),
//        colors = CardDefaults.cardColors(
//            MaterialTheme.colorScheme.surface
//        )
//    ) {
//        Row(
//            modifier = Modifier.padding(1.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            // Icono dinámico según el estado
//            Surface(
//                modifier = Modifier.size(40.dp),
//                shape = RoundedCornerShape(8.dp),
//                color = if (isJoined) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
//            ) {
//                Icon(
//                    imageVector = if (isJoined) Icons.Default.Apps else Icons.Default.Build,
//                    contentDescription = null,
//                    modifier = Modifier.padding(8.dp),
//                    tint = if (isJoined) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
//                )
//            }
//
//            Spacer(modifier = Modifier.width(12.dp))
//
//            Column(modifier = Modifier.weight(1f)) {
//                Text(
//                    text = app.nombre.let { if (it.length > 15) "${it.take(15)}..." else it },
//                    fontWeight = FontWeight.Bold,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis
//                )
//                Text(
//                    text = if (isJoined) stringResource(R.string.chat_activo) else "${app.paquete}".take(
//                        15
//                    ),
//                    style = MaterialTheme.typography.bodySmall,
//                    color = if (isJoined) MaterialTheme.colorScheme.primary else Color.Gray
//                )
//                if (isJoined){
//                    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
//                    val miData = app.testers[currentUserId]
//                    val misDias = (miData?.dias ?: 0).toString()+" "+stringResource(R.string.d_ass).replace(":", "")
//
//                    Text(
//                        text ="$misDias ",
//                        style = MaterialTheme.typography.bodyMedium,
//
//                    )
//                }
//            }
//            Column(modifier = Modifier.weight(1f)) {
//                Text(
//                    "${app.testers.size.toString()} Testers",
//                    fontWeight = FontWeight.Bold,
//                    maxLines = 1
//                )
//                Surface(
//                    color = Color(0xFFE3F2FD),
//                    shape = RoundedCornerShape(4.dp)
//                ) {
//                    Row(
//                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
//                        horizontalArrangement = Arrangement.spacedBy(6.dp)
//                    ) {
//
//                        Text(
//                            text = "REP ${String.format("%.2f", app.reputacion)}",
//                            fontSize = 10.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = Color(0xFF1565C0)
//                        )
//
//                        if (app.testers[user?.uid]?.validado == "Solicitud") {
//                            Text(
//                                text = stringResource(R.string.enviado),
//                                fontSize = 10.sp,
//                                fontWeight = FontWeight.Bold,
//                                color = Color(0xFF4CAF50)
//                            )
//                        }
//                    }
//                }
//            }
//
//            Box(
//                modifier = Modifier.padding(horizontal = 8.dp),
//                contentAlignment = Alignment.Center
//            ) {
//                if (isJoined) {
//                    if (estaInstalada) {
//                        // Icono de "Ya instalada" (Verde)
//                        IconButton(
//                            onClick = {
//                                try {
//                                    val launchIntent =
//                                        context.packageManager.getLaunchIntentForPackage(app.paquete)
//
//                                    if (launchIntent != null) {
//                                        context.startActivity(launchIntent)
//                                    } else {
//
//                                    }
//                                } catch (_: Exception) {
//                                }
//                            }
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.CheckCircle,
//                                contentDescription = "Instalada",
//                                tint = Color(0xFF4CAF50),
//                                modifier = Modifier.size(24.dp)
//                            )
//                        }
//                    } else {
//                        // Icono de "Descargar/Instalar" (Gris o Azul)
//                        IconButton(
//                            onClick = {
//                                try {
//                                    // Intentamos abrir la URL de Android que tienes en tu AppModel
//                                    val intent =
//                                        Intent(Intent.ACTION_VIEW, Uri.parse(app.urlAndroid))
//                                    context.startActivity(intent)
//                                } catch (e: Exception) {
//                                    // Si falla (por ejemplo, link mal formado), abrimos la búsqueda directa en Play Store
//                                    val playStoreIntent = Intent(
//                                        Intent.ACTION_VIEW,
//                                        Uri.parse("market://details?id=${app.paquete}")
//                                    )
//                                    context.startActivity(playStoreIntent)
//                                }
//                            }
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.FileDownload,
//                                contentDescription = "Instalar",
//                                tint = MaterialTheme.colorScheme.primary,
//                                modifier = Modifier.size(24.dp)
//                            )
//                        }
//                    }
//                }
//            }
//
//
////            if (isJoined) {
////                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
////            }
//        }
//    }
//}