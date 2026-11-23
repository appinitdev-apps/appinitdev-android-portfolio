
@Composable
fun CameraControlsPanel(
    showCamera: Boolean,
    onToggleCamera: () -> Unit,
    isFlashOn: Boolean,
    onToggleFlash: () -> Unit,
    imageUri: Uri?,
    imageAlpha: Float,
    onAlphaChange: (Float) -> Unit,
    currentColor: Color,
    onImageUriChange: (Uri) -> Unit,

    ) {
    var showAnimeDialog by remember { mutableStateOf(false) }



    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(
                alpha = 0.8f
            )
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .padding(16.dp)
            .shadow(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                stringResource(R.string.c_mara_y_flash),
                style = MaterialTheme.typography.labelMedium
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onToggleCamera,
                    modifier = Modifier.size(72.dp) 
                ) {
                    Icon(
                        imageVector = Icons.Filled.Camera,
                        contentDescription = if (showCamera) "Desactivar cámara" else "Activar cámara",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp) 
                    )
                }
                if (showCamera) {

                    IconButton(
                        onClick = onToggleFlash,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Icon(
                            imageVector = if (isFlashOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                            contentDescription = if (isFlashOn) "Flash ON" else "Flash OFF",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                
                GalleryPicker { uri -> onImageUriChange(uri) }

                IconButton(onClick = {
                    showAnimeDialog = true
                }, modifier = Modifier.size(72.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Search, 
                        contentDescription = "Buscar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )


            
            imageUri?.let {
                Text(
                    stringResource(R.string.opacidad_de_imagen),
                    style = MaterialTheme.typography.labelMedium
                )
                Slider(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    value = imageAlpha,
                    onValueChange = onAlphaChange,
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = currentColor,
                        activeTrackColor = currentColor
                    )
                )

            }

            if (showAnimeDialog) {
                AnimeImageApp(
                    onImageSelected = { url ->
                        try {
                            if (url.isNotBlank())
                                onImageUriChange(url.toUri())
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        showAnimeDialog = false 
                    },
                    onDismissRequest = { showAnimeDialog = false }
                )
            }
        }
    }
}


@Composable
fun GalleryPicker(onImageSelected: (Uri) -> Unit) {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { onImageSelected(it) } }
    IconButton(onClick = { launcher.launch("image/*") }, modifier = Modifier.size(72.dp)) {
        Icon(
            imageVector = Icons.Filled.AddPhotoAlternate, 
            contentDescription = "Seleccionar imagen",
            modifier = Modifier.size(32.dp), 
            tint = MaterialTheme.colorScheme.primary 
        )
    }
}