@Composable
fun PhotoEditorScreen(imageUri: Uri, onBackClicked: () -> Unit, onSavedSuccessfully: (Uri) -> Unit) {
    val context = LocalContext.current
    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var selectedTexture by remember { mutableStateOf(FilterTools.textures[0]) }
    val allFiltersList = remember(context, selectedTexture) {
        val nativeFilters = FilterTools.getAllFilters(context, selectedTexture)
        val noneFilters = FilterTools.categories.map { category ->
            EditorFilter(
                name = "None",
                category = category.name,
                filterInstance = GPUImageFilter() 
            )
        }
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) 
    ) {
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
                .background(Color.Black.copy(alpha = 0.6f)) 
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween, 
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
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
            if (activeEditorFilter.name != "None") {
                if (isSaving) {
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
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clickable {
                                if (!isSaving) {
                                    isSaving = true
                                    scope.launch {
                                        try {
                                            val savedUri = withContext(Dispatchers.IO) {
                                                val inputStream = context.contentResolver.openInputStream(imageUri)
                                                    ?: throw Exception("No se pudo abrir la imagen original")
                                                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                                                inputStream.close()
                                                if (originalBitmap == null) throw Exception("Error al decodificar la imagen")
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
                            imageVector = Icons.Default.Save, 
                            contentDescription = "Guardar imagen",
                            tint = MaterialTheme.colorScheme.primary, 
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.width(40.dp))
            }
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter), 
            color = Color(0xFF1C1C1C).copy(alpha = 0.65f), 
            shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                    }
                }
                Divider(color = Color(0xFF2B2B2B).copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 6.dp))
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
    val filteredBitmapState = produceState<Bitmap?>(initialValue = null, item, baseThumbnail) {
        if (baseThumbnail != null) {
            value = withContext(Dispatchers.Default) {
                try {
                    if (item.name == "None") {
                        baseThumbnail 
                    } else {
                        val gpuImage = GPUImage(context).apply {
                            setImage(baseThumbnail)
                            setFilter(item.filterInstance)
                        }
                        gpuImage.bitmapWithFilterApplied
                    }
                } catch (e: Exception) {
                    null 
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
                Image(
                    bitmap = filteredBitmap.asImageBitmap(),
                    contentDescription = item.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
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