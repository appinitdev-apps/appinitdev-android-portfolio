

@Composable
fun PhotoEditorScreen(imageUri: Uri, onBackClicked: () -> Unit, onSavedSuccessfully: (Uri) -> Unit) {
    val context = LocalContext.current
    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var selectedTexture by remember { mutableStateOf(FilterTools.textures[0]) }
    val allFiltersList = remember(context, selectedTexture) {
        val nativeFilters = FilterTools.getAllFilters(context, selectedTexture)

        // Generamos dinámicamente un filtro "Ninguno" para cada categoría existente
        val noneFilters = FilterTools.categories.map { category ->
            EditorFilter(
                name = "None",
                category = category.name,
                filterInstance = GPUImageFilter() // Filtro base que no hace nada
            )
        }

        // Colocamos los filtros "Ninguno" al inicio para que aparezcan primero
        noneFilters + nativeFilters
    }

    var selectedCategory by remember { mutableStateOf(FilterTools.categories[0].name) }
    var activeEditorFilter by remember(allFiltersList) { mutableStateOf(allFiltersList[0]) }
    var gpuImageViewInstance by remember { mutableStateOf<GPUImageView?>(null) }

    val filteredFilters = remember(selectedCategory, allFiltersList) {
        allFiltersList.filter { it.category == selectedCategory }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, flag)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val customTexture = TextureItem(
                name = "Personalizada",
                uriString = uri.toString()
            )
            selectedTexture = customTexture
        }
    }
    var photoName by remember { mutableStateOf("Imagen") }
    var baseThumbnail by remember { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(imageUri) {
        baseThumbnail = withContext(Dispatchers.IO) {
            try {
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(imageUri).use { BitmapFactory.decodeStream(it, null, options) }

                // Reducimos la resolución drásticamente (ej. máximo ~120px) para que procese instantáneo
                val targetSize = 120
                var scale = 1
                while (options.outWidth / scale / 2 >= targetSize && options.outHeight / scale / 2 >= targetSize) {
                    scale *= 2
                }

                val finalOptions = BitmapFactory.Options().apply { inSampleSize = scale }
                context.contentResolver.openInputStream(imageUri).use { BitmapFactory.decodeStream(it, null, finalOptions) }
            } catch (e: Exception) {
                null
            }
        }
    }

    LaunchedEffect(imageUri) {
        withContext(Dispatchers.IO) {
            val cursor = context.contentResolver.query(imageUri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    // Buscamos el índice de la columna del nombre del archivo
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        photoName = it.getString(nameIndex)
                    }
                }
            }
        }
    }

    LaunchedEffect(allFiltersList) {
        val matchingFilter = allFiltersList.find { it.name == activeEditorFilter.name }
        if (matchingFilter != null) {
            activeEditorFilter = matchingFilter
            gpuImageViewInstance?.requestRender()
        }
    }

    // CONTENEDOR PRINCIPAL: Permite superponer capas (Z-Index)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Fondo negro base
    ) {
        // --- CAPA 1: VISOR GPU (Ocupa el 100% de la pantalla, al fondo) ---
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                GPUImageView(ctx).apply {
                    setImage(imageUri)
                    gpuImageViewInstance = this
                }
            },
            update = { view ->
                view.filter = activeEditorFilter.filterInstance
                FilterTools.adjustFilter(view.filter, activeEditorFilter.parameters)
                view.requestRender()
            }
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.6f)) // Fondo semi-transparente sutil
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween, // Mantiene los grupos a los extremos opuestos
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- BLOQUE IZQUIERDO: Volver + Nombre de la foto ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                // Botón Volver estilizado y compacto
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onBackClicked() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = photoName,
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // --- BLOQUE DERECHO: Botón Guardar (Icono) Condicional ---
            // SOLO se muestra si el filtro activo NO es "Ninguno"
            if (activeEditorFilter.name != "None") {
                if (isSaving) {
                    // Mientras guarda, muestra un progreso circular del mismo tamaño que el icono
                    Box(
                        modifier = Modifier.size(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    }
                } else {
                    // Icono de Guardar interactivo y compacto
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clickable {
                                if (!isSaving) {
                                    isSaving = true
                                    scope.launch {
                                        try {
                                            val savedUri = withContext(Dispatchers.IO) {
                                                // 1. Carga la imagen original en alta resolución
                                                val inputStream = context.contentResolver.openInputStream(imageUri)
                                                    ?: throw Exception("No se pudo abrir la imagen original")
                                                
                                                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                                                inputStream.close()

                                                if (originalBitmap == null) throw Exception("Error al decodificar la imagen")

                                                // 2. CONFIGURACIÓN: Instancia fresca del filtro
                                                val freshFilters = FilterTools.getAllFilters(context, selectedTexture)
                                                val filterToApply = freshFilters.find { it.name == activeEditorFilter.name }?.filterInstance ?: GPUImageFilter()

                                                FilterTools.adjustFilter(filterToApply, activeEditorFilter.parameters)

                                                val gpuImage = GPUImage(context).apply {
                                                    setImage(originalBitmap)
                                                    setFilter(filterToApply)
                                                }

                                                val filteredBitmap = gpuImage.bitmapWithFilterApplied
                                                originalBitmap.recycle()

                                                if (filteredBitmap == null) throw Exception("La GPU no pudo procesar la imagen")

                                                // 3. Guardado en MediaStore
                                                val filename = "Filtro_${System.currentTimeMillis()}.jpg"
                                                val contentValues = ContentValues().apply {
                                                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                                                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                                        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/FiltersAID")
                                                        put(MediaStore.Images.Media.IS_PENDING, 1)
                                                    }
                                                }

                                                val resolver = context.contentResolver
                                                val outputUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                                                    ?: throw Exception("No se pudo crear el archivo en la galería")

                                                resolver.openOutputStream(outputUri)?.use { outputStream ->
                                                    if (!filteredBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)) {
                                                        throw Exception("Fallo al escribir la imagen")
                                                    }
                                                }

                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                                    contentValues.clear()
                                                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                                                    resolver.update(outputUri, contentValues, null, null)
                                                } else {
                                                    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                                                    mediaScanIntent.data = outputUri
                                                    context.sendBroadcast(mediaScanIntent)
                                                }

                                                filteredBitmap.recycle()
                                                outputUri
                                            }

                                            onSavedSuccessfully(savedUri)
                                            Toast.makeText(context, "Imagen guardada", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            Toast.makeText(
                                                context,
                                                "Error: ${e.localizedMessage}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } finally {
                                            isSaving = false
                                        }
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save, // Icono de palomita / check
                            contentDescription = "Guardar imagen",
                            tint = MaterialTheme.colorScheme.primary, // Color destacado de tu app
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            } else {
                // Truco de diseño: Si el botón está oculto, dejamos un Box vacío de 40.dp
                // para asegurar que el título mantenga su misma alineación y restricciones de espacio
                Spacer(modifier = Modifier.width(40.dp))
            }
        }
        // --- CAPA 2: PANEL DE CONTROL INFERIOR TRASLÚCIDO (Por encima de la imagen) ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter), // Lo ancla a la parte inferior del Box
            color = Color(0xFF1C1C1C).copy(alpha = 0.65f), // Opacidad del 85% para ver la foto detrás
            shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // PANEL MULTI-SLIDER DINÁMICO RECONFIGURABLE
                if (activeEditorFilter.parameters.isNotEmpty() && activeEditorFilter.category == selectedCategory) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        activeEditorFilter.parameters.forEachIndexed { index, param ->
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = param.label,
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "${param.currentValue.toInt()}%",
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Slider(
                                    value = param.currentValue,
                                    valueRange = 0f..100f,
                                    onValueChange = { newValue ->
                                        activeEditorFilter = activeEditorFilter.copy(
                                            parameters = activeEditorFilter.parameters.toMutableList()
                                                .apply {
                                                    this[index] = param.copy(currentValue = newValue)
                                                }
                                        )
                                        gpuImageViewInstance?.let { view ->
                                            FilterTools.adjustFilter(
                                                view.filter,
                                                activeEditorFilter.parameters
                                            )
                                            view.requestRender()
                                        }
                                    },

                                )
                            }
                        }
                    }
                }

                // --- SELECTOR DE TEXTURAS DINÁMICO (Categoría Fusión) ---
                if (selectedCategory == "Fusión") {
                    Text(
                        text = "Textura de fusión:",
                        color = Color.Gray,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                    )

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // --- BOTÓN 1: SELECCIONAR DE GALERÍA ---
                        item {
                            val isCustomActive = selectedTexture.uriString != null
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF252525))
                                    .border(
                                        width = if (isCustomActive) 2.dp else 1.dp,
                                        color = if (isCustomActive) MaterialTheme.colorScheme.primary else Color(0xFF444444),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        galleryLauncher.launch(
                                            PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCustomActive && selectedTexture.uriString != null) {
                                    Image(
                                        painter = coil.compose.rememberAsyncImagePainter(model = selectedTexture.uriString),
                                        contentDescription = "Galería",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Cargar de galería",
                                        tint = Color.White
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .align(Alignment.BottomCenter)
                                        .padding(vertical = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isCustomActive) "Galería" else "Subir",
                                        color = if (isCustomActive) MaterialTheme.colorScheme.primary else Color.LightGray,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        // --- LAS TEXTURAS NATIVAS RESTANTES ---
                        items(FilterTools.textures) { item ->
                            val isTextureSelected = selectedTexture == item && selectedTexture.uriString == null

                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E1E1E))
                                    .border(
                                        width = if (isTextureSelected) 2.dp else 1.dp,
                                        color = if (isTextureSelected) MaterialTheme.colorScheme.primary else Color(0xFF333333),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        selectedTexture = item
                                    },
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Image(
                                    painter = painterResource(id = item.resId ?: com.appinitdev.gallery.R.drawable.texture_01),
                                    contentDescription = item.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .padding(vertical = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item.name,
                                        color = if (isTextureSelected) MaterialTheme.colorScheme.primary else Color.White,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    Divider(color = Color(0xFF2B2B2B).copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 6.dp))
                }

                Divider(color = Color(0xFF2B2B2B).copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 6.dp))

                // --- SELECCIÓN DEL FILTRO ---
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filteredFilters) { item ->
                        val isSelected = item.name == activeEditorFilter.name ||
                                (item.name == "None" && activeEditorFilter.category != selectedCategory)

                        FilterPreviewItem(
                            item = item,
                            baseThumbnail = baseThumbnail,
                            isSelected = isSelected,
                            onClick = { activeEditorFilter = item }
                        )
                        //                        Column(
//                            horizontalAlignment = Alignment.CenterHorizontally,
//                            modifier = Modifier.width(80.dp).clickable { activeEditorFilter = item }
//                        ) {
//                            Box(
//                                modifier = Modifier
//                                    .size(56.dp)
//                                    .clip(CircleShape)
//                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF2A2A2A).copy(alpha = 0.9f))
//                                    .border(if (isSelected) 2.dp else 0.dp, Color.White, CircleShape),
//                                contentAlignment = Alignment.Center
//                            ) {
//                                Text(
//                                    text = item.name.take(2).uppercase(),
//                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.LightGray,
//                                    style = MaterialTheme.typography.titleSmall
//                                )
//                            }
//                            Spacer(modifier = Modifier.height(6.dp))
//                            Text(text = item.name, color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
//                        }
                    }
                }

                Divider(color = Color(0xFF2B2B2B).copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 6.dp))

                // --- PESTAÑAS DE CATEGORÍA ---
                TabRow(
                    selectedTabIndex = FilterTools.categories.map { it.name }.indexOf(selectedCategory),
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    FilterTools.categories.forEach { category ->
                        val isCatSelected = selectedCategory == category.name
                        Tab(
                            selected = isCatSelected,
                            onClick = { selectedCategory = category.name },
                            icon = { Icon(category.icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = if (isCatSelected) MaterialTheme.colorScheme.primary else Color.Gray) },
                            text = { Text(text = category.name, style = MaterialTheme.typography.labelMedium, color = if (isCatSelected) Color.White else Color.Gray) }
                        )
                    }
                }
            }
        }
    }
}




@Composable
fun FilterPreviewItem(
    item: EditorFilter,
    baseThumbnail: Bitmap?,
    isSelected: Boolean,
    onClick: () -> Unit
) {


    val context = LocalContext.current

    // produceState ejecuta el renderizado en un hilo alterno (Dispatchers.Default) de manera limpia
    val filteredBitmapState = produceState<Bitmap?>(initialValue = null, item, baseThumbnail) {
        if (baseThumbnail != null) {
            value = withContext(Dispatchers.Default) {
                try {
                    if (item.name == "None") {
                        baseThumbnail // Si es ninguno, no gasta CPU y devuelve la original limpia
                    } else {
                        // Usamos la versión estática de GPUImage para procesar Bitmaps sin inflar vistas
                        val gpuImage = GPUImage(context).apply {
                            setImage(baseThumbnail)
                            setFilter(item.filterInstance)
                        }
                        gpuImage.bitmapWithFilterApplied
                    }
                } catch (e: Exception) {
                    null // Si algo falla, retorna null para pintar el placeholder de texto
                }
            }
        }
    }

    val filteredBitmap = filteredBitmapState.value

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFF2A2A2A))
                .border(if (isSelected) 2.dp else 0.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (filteredBitmap != null) {
                // Renderiza la imagen ya filtrada de forma nativa y fluida
                Image(
                    bitmap = filteredBitmap.asImageBitmap(),
                    contentDescription = item.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Mientras el hilo de fondo trabaja, el usuario ve las letras iniciales (Cero congelamientos de UI)
                Text(
                    text = item.name.take(2).uppercase(),
                    color = Color.LightGray,
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = item.name,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}