
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FtpServerScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    
    var isServerRunning by remember { mutableStateOf(FtpService.isRunning) }
    var ipAddress by remember { mutableStateOf(FtpService.obtenerIpLocal(context)) }
    var currentUsername by remember { mutableStateOf(FtpService.usuarioActual) }
    var currentPassword by remember { mutableStateOf(FtpService.contrasenaActual) }
    var currentPort by remember { mutableStateOf(FtpService.puertoActual) }

    
    var hasStoragePermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else {
                true
            }
        )
    }

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    val storageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            hasStoragePermission = Environment.isExternalStorageManager()
        }
    }

    
    val statusColor by animateColorAsState(
        targetValue = when {
            !hasStoragePermission || !hasNotificationPermission -> MaterialTheme.colorScheme.tertiary
            isServerRunning -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outline
        },
        label = "SecurityColorAnimation"
    )



    val scaleFactor by animateFloatAsState(
        targetValue = if (isServerRunning) 1.02f else 1f,
        label = "SecurityButtonScale"
    )


    
    val infiniteTransition = rememberInfiniteTransition(label = "IconAnimation")
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isServerRunning) 1.2f else 1f, 
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "IconScale"
    )
    
    val iconAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = if (isServerRunning) 1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "IconAlpha"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Service FTP", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { ipAddress = FtpService.obtenerIpLocal(context) }) {
                        Icon(Icons.Default.Sync, contentDescription = null)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                !hasStoragePermission || !hasNotificationPermission -> Icons.Default.NewReleases
                                isServerRunning -> Icons.Default.Security 
                                else -> Icons.Default.Lock
                            },
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(56.dp).graphicsLayer {
                                
                                if (isServerRunning) {
                                    scaleX = iconScale
                                    scaleY = iconScale
                                    alpha = iconAlpha
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                InputChip(
                    selected = isServerRunning,
                    onClick = {},
                    label = {
                        Text(
                            text = if (isServerRunning) "FTP ACTIVE" else "FTP STOP",
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    },
                    avatar = {
                        Icon(
                            imageVector = if (isServerRunning) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }

            
            if (isServerRunning) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lan, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "ftp://$ipAddress:$currentPort",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("URL", "ftp://$ipAddress:$currentPort"))
                                },
                                colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(currentUsername, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            }

                            Box(modifier = Modifier.size(1.dp, 18.dp).background(MaterialTheme.colorScheme.outlineVariant))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(currentPassword, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }

            
            Button(
                onClick = {
                    if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        return@Button
                    }
                    if (!hasStoragePermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        storageLauncher.launch(intent)
                        return@Button
                    }

                    val intent = Intent(context, FtpService::class.java)
                    if (!isServerRunning) {
                        
                        FtpService.prepararCredenciales()

                        
                        context.startForegroundService(intent)

                        
                        isServerRunning = true
                        currentUsername = FtpService.usuarioActual
                        currentPassword = FtpService.contrasenaActual
                        currentPort = FtpService.puertoActual
                        ipAddress = FtpService.obtenerIpLocal(context)
                    } else {
                        context.stopService(intent)
                        isServerRunning = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .scale(scaleFactor),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        !hasStoragePermission || !hasNotificationPermission -> MaterialTheme.colorScheme.tertiaryContainer
                        isServerRunning -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.primaryContainer
                    },
                    contentColor = when {
                        !hasStoragePermission || !hasNotificationPermission -> MaterialTheme.colorScheme.onTertiaryContainer
                        isServerRunning -> MaterialTheme.colorScheme.onErrorContainer
                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                    }
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = when {
                            !hasStoragePermission || !hasNotificationPermission -> Icons.Default.GppMaybe
                            isServerRunning -> Icons.Default.LockOpen
                            else -> Icons.Default.Lock
                        },
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = when {
                            !hasStoragePermission || !hasNotificationPermission -> "GRANT PERMISSIONS"
                            isServerRunning -> "DISCONNECT"
                            else -> "CONNECT"
                        },
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}