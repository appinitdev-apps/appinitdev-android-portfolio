
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileExplorerScreen(
    onPdfClick: ((File) -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    context: Context,
    startPath: String = "POSManager"
) {
    var currentPath by remember { mutableStateOf(startPath) }
    var files by remember { mutableStateOf(listFilesAndDirs(context, currentPath)) }

    val selectedFiles = remember { mutableStateListOf<File>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.administrar_transacciones)) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBackIosNew, contentDescription = stringResource(R.string.atr_s))
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "$currentPath",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(files) { file ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                when {
                                    file.isDirectory -> {
                                        currentPath = File(currentPath, file.name).path
                                        files = listFilesAndDirs(context, currentPath)
                                    }

                                    file.extension.equals("pdf", ignoreCase = true) -> {
                                        onPdfClick?.invoke(file)
                                    }
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (file.isDirectory) "📁 ${file.name}" else "📄 ${file.name}",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )

                        Checkbox(
                            checked = selectedFiles.contains(file),
                            onCheckedChange = { checked ->
                                if (checked) selectedFiles.add(file)
                                else selectedFiles.remove(file)
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (selectedFiles.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            selectedFiles.forEach {
                                if (it.isDirectory) it.deleteRecursively() else it.delete()
                            }
                            Toast.makeText(context,
                                context.getString(R.string.deleted), Toast.LENGTH_SHORT).show()

                            selectedFiles.clear()

                            files = listFilesAndDirs(context, currentPath)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.eliminarf))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.eliminarf))
                    }

                    TextButton(
                        onClick = {
                            val downloadDir =
                                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                            selectedFiles.forEach { file ->
                                if (file.isDirectory) {
                                    copyDirectory(file, File(downloadDir, file.name))
                                    Toast.makeText(context,
                                        context.getString(R.string.copied_directory), Toast.LENGTH_SHORT).show()
                                } else {
                                    file.copyTo(File(downloadDir, file.name), overwrite = true)
                                    Toast.makeText(context,
                                        context.getString(R.string.copied_file), Toast.LENGTH_SHORT).show()

                                }
                            }
                            selectedFiles.clear()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = stringResource(R.string.descargar))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.descargar))
                    }
                }

                if (currentPath != startPath) {
                    TextButton(
                        onClick = {
                            val parentFile = File(currentPath).parentFile
                            if (parentFile != null) {
                                currentPath = parentFile.path
                                files = listFilesAndDirs(context, currentPath)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = stringResource(R.string.volver))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.volver))
                    }
                }
            }
        }
    }
}

fun listFilesAndDirs(context: Context, relativePath: String): List<File> {
    val baseDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
    val dir = File(baseDir, relativePath)
    if (!dir.exists() || !dir.isDirectory) return emptyList()
    return dir.listFiles()?.toList()?.sortedBy { it.name } ?: emptyList()
}

fun copyDirectory(source: File, destination: File) {
    if (!destination.exists()) {
        destination.mkdirs()
    }
    source.listFiles()?.forEach { file ->
        val destFile = File(destination, file.name)
        if (file.isDirectory) {
            copyDirectory(file, destFile)
        } else {
            file.copyTo(destFile, overwrite = true)
        }
    }
}

/*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileExplorerScreen( onPdfClick: ((File) -> Unit)? = null, onBack: (() -> Unit)? = null,context: Context, startPath: String = "POSManager") {
    var currentPath by remember { mutableStateOf(startPath) }
    var files by remember { mutableStateOf(listFilesAndDirs(context, currentPath)) }

    val selectedFiles = remember { mutableStateListOf<File>() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Adminstrar transaciones") },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.Default.ArrowBackIosNew,
                                contentDescription = "Atrás"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "$currentPath",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(files) { file ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                when {
                                    file.isDirectory -> {
                                        currentPath = File(currentPath, file.name).path
                                        files = listFilesAndDirs(context, currentPath)
                                    }
                                    file.extension.equals("pdf", ignoreCase = true) -> {
                                        onPdfClick?.invoke(file) // DISPARA EL DIÁLOGO
                                    }
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (file.isDirectory) "📁" else "📄",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.width(32.dp) // ancho fijo
                        )

                        // Columna 2: Nombre del archivo
                        Text(
                            text = file.name,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                        )

                        // Columna 3: Checkbox (solo PDF)
                        if (file.isFile && file.extension.equals("pdf", ignoreCase = true)) {
                            Checkbox(
                                checked = selectedFiles.contains(file),
                                onCheckedChange = { checked ->
                                    if (checked) selectedFiles.add(file)
                                    else selectedFiles.remove(file)
                                }
                            )
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        thickness = 1.dp,   // grosor
                        color = Color.Gray // color de la línea
                    )
                    Spacer(Modifier.height(4.dp))

                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (selectedFiles.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            selectedFiles.forEach { it.delete() }
                            selectedFiles.clear()
                            files = listFilesAndDirs(context, currentPath)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                        Spacer(Modifier.width(4.dp))
                        Text("Eliminar")
                    }
                }

                if (currentPath != startPath) {
                    TextButton(
                        onClick = {
                            val parentFile = File(currentPath).parentFile
                            if (parentFile != null) {
                                currentPath = parentFile.path
                                files = listFilesAndDirs(context, currentPath)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Volver")
                        Spacer(Modifier.width(4.dp))
                        Text("Volver")
                    }
                }
            }

        }

    }
}

fun listFilesAndDirs(context: Context, relativePath: String): List<File> {
    val baseDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
    val dir = File(baseDir, relativePath)
    if (!dir.exists() || !dir.isDirectory) return emptyList()
    return dir.listFiles()?.toList()?.sortedBy { it.name } ?: emptyList()
}
*/