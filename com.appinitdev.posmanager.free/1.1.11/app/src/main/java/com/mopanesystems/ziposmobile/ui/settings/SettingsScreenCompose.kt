@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenCompose(
    onBack: (() -> Unit)? = null, 
    onNavigateToCategories: (() -> Unit)? = null,
    onNavigateToStore: (() -> Unit)? = null,
    onNavigateToExplorer: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val entryPoint = remember(context) {
        EntryPointAccessors.fromApplication(context.applicationContext, SettingsEntryPoint::class.java)
    }
    val databaseBackupHelper = remember(entryPoint) {
        entryPoint.databaseBackupHelper()
    }
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        uri?.let {
            try {
                databaseBackupHelper.exportDatabase(context, it)
                Toast.makeText(context,"Exported Database", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("Settings", "Export Error: ${e.message}")
                Toast.makeText(context,"Export Failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                databaseBackupHelper.importDatabase(context, it)
                Toast.makeText(context, "Importación Exitosa. Reiniciando...", Toast.LENGTH_LONG).show()
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    val activity = (context as? android.app.Activity)
                    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                    val mainIntent = Intent.makeRestartActivityTask(intent?.component)
                    context.startActivity(mainIntent)
                    activity?.finishAffinity()
                    System.exit(0)
                }, 1000) 
            } catch (e: Exception) {
                Log.e("Settings", "Import Error: ${e.message}")
                Toast.makeText(context,"Import Failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.configuraciones)) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Atrás")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
            ,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            TextButton(onClick = {
                val contextToUse = context
                val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Intent(Settings.ACTION_APP_LOCALE_SETTINGS)
                } else {
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                }
                intent.data = Uri.fromParts("package", contextToUse.packageName, null)
                try {
                    contextToUse.startActivity(intent)
                } catch (e: Exception) {
                    Log.e("SettingsScreen", "Error al abrir ajustes: $e")
                    Toast.makeText(contextToUse, "No se puede abrir configuración", Toast.LENGTH_SHORT).show()
                }
            }) {
                Row {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = "Language",
                        modifier = Modifier.width(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.language),
                        style = MaterialTheme.typography.titleMedium,
                        )
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp), 
                thickness = 1.dp,         
                color = Color.Gray       
            )
            TextButton(onClick = { onNavigateToCategories?.invoke() }) {
                Row {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = "Categories",
                        modifier = Modifier.width(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.administrar_categor_as),
                        style = MaterialTheme.typography.titleMedium,
                        )
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp), 
                thickness = 1.dp,         
                color = Color.Gray       
            )
            TextButton(onClick = { onNavigateToStore?.invoke() }) {
                Row {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = "Store",
                        modifier = Modifier.width(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.administrar_store),
                        style = MaterialTheme.typography.titleMedium,
                        )
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp), 
                thickness = 1.dp,         
                color = Color.Gray       
            )
            TextButton(onClick = { onNavigateToExplorer?.invoke() }) {
                Row {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = "Transacción",
                        modifier = Modifier.width(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.administrar_transacci_n),
                        style = MaterialTheme.typography.titleMedium,
                        )
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp), 
                thickness = 1.dp,         
                color = Color.Gray       
            )
            TextButton(onClick = {
                try {
                    exportLauncher.launch("backup_pos.db")
                } catch (e: Exception) {
                    Toast.makeText(context, "Fallo de Exportación: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }) {
                Row {
                    Icon(
                        imageVector = Icons.Default.ImportExport,
                        contentDescription = "Transacción",
                        modifier = Modifier.width(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.exportar_base_de_datos),
                        style = MaterialTheme.typography.titleMedium,
                        )
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp), 
                thickness = 1.dp,         
                color = Color.Gray       
            )
            TextButton(onClick = {
                importLauncher.launch(arrayOf("application/x-sqlite3", "application/octet-stream"))            }) {
                Row {
                    Icon(
                        imageVector = Icons.Default.ImportExport,
                        contentDescription = "Transacción",
                        modifier = Modifier.width(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.importar_base_de_datos),
                        style = MaterialTheme.typography.titleMedium,
                        )
                }
            }
        }
    }
}