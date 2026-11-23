@Composable
fun CameraControlsPanelPro(
    showCamera: Boolean,
    onToggleCamera: () -> Unit,
    isFlashOn: Boolean,
    onToggleFlash: () -> Unit,
    imageUri: Uri?,
    imageAlpha: Float,
    onAlphaChange: (Float) -> Unit,
    currentColor: Color,
    onImageUriChange: (Uri) -> Unit,
    onTabSelected: () -> Unit,
    onRemoveBackground: () -> Unit,
    onSketch: (lineSize: Float, threshold: Float) -> Unit,
    onRestoreOriginal: () -> Unit,
    onCapture: () -> Unit = {},
    onRotateLeft: () -> Unit = {},
    onRotateRight: () -> Unit = {},
    onFlipHorizontal: () -> Unit = {},
    onFlipVertical: () -> Unit = {},
    currentFilter: String,
    onFilterChange: (String) -> Unit,
    isCollapsed: Boolean,
    onCollapseClick: () -> Unit = {},
    onChangeMode: () -> Unit = {},
    gridType: String = "NONE",
    onGridTypeChange: (String) -> Unit = {},
    gridSize: Float = 200f,
    onGridSizeChange: (Float) -> Unit = {},
    isRecordingTimeLapse: Boolean = false,
    onToggleTimeLapse: () -> Unit = {},
    showPip: Boolean = false,
    onTogglePip: () -> Unit = {},
    isWorldLockActive: Boolean = false,
    onToggleWorldLock: () -> Unit = {},
    onShowColorGuide: () -> Unit = {},
    onClearReferencePoints: () -> Unit = {},
    initialTab: Int = -1,
    pencilColorMode: String = "BLUE",
    onPencilColorModeChange: (String) -> Unit = {},
    posterizeLevels: Float = 10f,
    onPosterizeLevelsChange: (Float) -> Unit = {},
    gammaValue: Float = 2.0f,
    onGammaValueChange: (Float) -> Unit = {},
    sharpnessValue: Float = 2.0f,
    onSharpnessValueChange: (Float) -> Unit = {},
    grayscaleSaturation: Float = 0.0f,
    onGrayscaleSaturationChange: (Float) -> Unit = {},
    isRefPointsActive: Boolean = false,
    onToggleRefPoints: () -> Unit = {},
    referencePointsCount: Int = 0
) {
    var showAnimeDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(initialTab) }
    var sketchLineSize by remember { mutableFloatStateOf(1.0f) }
    var sketchThreshold by remember { mutableFloatStateOf(0.70f) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val activeColor = Color(0xFFFF4081)
    Box(modifier = Modifier.fillMaxSize()) {
        if (selectedTab != -1) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.0f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        selectedTab = -1
                    }
            )
        }
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .background(Color.Black.copy(alpha = 0.0f))
                .padding(vertical = 24.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(
                visible = selectedTab != -1,
                enter = expandHorizontally(expandFrom = Alignment.End),
                exit = shrinkHorizontally(shrinkTowards = Alignment.End)
            ) {
                Surface(
                    modifier = Modifier
                        .width(260.dp)
                        .wrapContentHeight()
                        .padding(end = 8.dp)
                        .border(
                            0.5.dp,
                            Color.White.copy(alpha = 0.2f),
                            RoundedCornerShape(24.dp)
                        ),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.35f),
                    contentColor = Color.Black,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier
                            .wrapContentHeight()
                            .background(Color.Black.copy(alpha = 0.0f))
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = when (selectedTab) {
                                0 -> stringResource(R.string.opacidad)
                                1 -> stringResource(R.string.imagen)
                                2 -> stringResource(R.string.c_mara)
                                3 -> stringResource(R.string.linterna)
                                4 -> stringResource(R.string.filtros)
                                else -> ""
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(modifier = Modifier.wrapContentHeight()) {
                            when (selectedTab) {
                                0 -> { 
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .padding(vertical = 12.dp)
                                            .background(Color.Black.copy(alpha = 0.0f))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color.Black.copy(alpha = 0.0f))
                                                .height(280.dp)
                                                .width(48.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Slider(
                                                value = imageAlpha,
                                                onValueChange = onAlphaChange,
                                                valueRange = 0f..1f,
                                                colors = SliderDefaults.colors(
                                                    thumbColor = activeColor,
                                                    activeTrackColor = activeColor
                                                ),
                                                modifier = Modifier
                                                    .requiredWidth(280.dp)
                                                    .rotate(-90f)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            "${(imageAlpha * 100).toInt()}%",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                1 -> { 
                                    Column(
                                        modifier = Modifier.verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        VerticalActionButton(
                                            icon = Icons.Filled.AddPhotoAlternate,
                                            text = stringResource(R.string.galer_a),
                                            onClick = { }
                                        ) {
                                            GalleryPickerPro(onImageSelected = onImageUriChange)
                                        }
                                        VerticalActionButton(
                                            icon = Icons.Filled.Search,
                                            text = stringResource(R.string.anime),
                                            activeColor = activeColor,
                                            onClick = { showAnimeDialog = true }
                                        )
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            TransformIconButton(
                                                Icons.Filled.RotateLeft,
                                                onRotateLeft
                                            )
                                            TransformIconButton(
                                                Icons.Filled.RotateRight,
                                                onRotateRight
                                            )
                                            TransformIconButton(Icons.Filled.Flip, onFlipHorizontal)
                                            TransformIconButton(
                                                Icons.Filled.Flip,
                                                onFlipVertical,
                                                rotate = 90f
                                            )
                                        }
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                        Text(
                                            text = stringResource(R.string.malla_de_referencia),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = activeColor
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            GridTypeButton(
                                                icon = Icons.Filled.GridOff,
                                                isSelected = gridType == "NONE",
                                                onClick = { onGridTypeChange("NONE") },
                                                modifier = Modifier.weight(1f)
                                            )
                                            GridTypeButton(
                                                icon = Icons.Filled.GridView,
                                                isSelected = gridType == "GRID",
                                                onClick = { onGridTypeChange("GRID") },
                                                modifier = Modifier.weight(1f)
                                            )
                                            GridTypeButton(
                                                icon = Icons.Filled.FilterNone,
                                                isSelected = gridType == "TRIANGLES",
                                                onClick = { onGridTypeChange("TRIANGLES") },
                                                modifier = Modifier.weight(1f)
                                            )
                                            GridTypeButton(
                                                icon = Icons.Filled.RadioButtonUnchecked,
                                                isSelected = gridType == "CIRCLES",
                                                onClick = { onGridTypeChange("CIRCLES") },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        Spacer(modifier = Modifier.padding(8.dp))
                                        if (gridType != "NONE") {
                                            Column {
                                                Text(
                                                    text = "${stringResource(R.string.densidad)}: ${gridSize.toInt()}",
                                                    fontSize = 11.sp
                                                )
                                                Slider(
                                                    value = gridSize,
                                                    onValueChange = onGridSizeChange,
                                                    valueRange = 50f..400f,
                                                    colors = SliderDefaults.colors(
                                                        thumbColor = activeColor,
                                                        activeTrackColor = activeColor
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                                2 -> { 
                                    Column(
                                        modifier = Modifier.verticalScroll(rememberScrollState()),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(20.dp)
                                    ) {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(20.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceEvenly
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    IconButton(
                                                        onClick = onToggleCamera,
                                                        modifier = Modifier
                                                            .size(56.dp)
                                                            .background(
                                                                if (showCamera) activeColor.copy(alpha = 0.1f) else Color(
                                                                    0xFFF5F5F5
                                                                ), CircleShape
                                                            )
                                                            .border(
                                                                1.dp,
                                                                if (showCamera) activeColor else Color.Transparent,
                                                                CircleShape
                                                            )
                                                    ) {
                                                        Icon(
                                                            imageVector = if (showCamera) Icons.Filled.CameraAlt else Icons.Filled.NoPhotography,
                                                            contentDescription = null,
                                                            tint = if (showCamera) activeColor else Color.Gray
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = if (showCamera) stringResource(R.string.apagar_c_mara) else stringResource(
                                                            R.string.encender_c_mara
                                                        ),
                                                        fontSize = 11.sp,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    FilledTonalIconButton(
                                                        onClick = onToggleFlash,
                                                        enabled = showCamera,
                                                        modifier = Modifier.size(56.dp),
                                                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                                                            containerColor = if (isFlashOn) activeColor else Color(
                                                                0xFFE8EAF6
                                                            ),
                                                            contentColor = if (isFlashOn) Color.White else activeColor
                                                        )
                                                    ) {
                                                        Icon(
                                                            if (isFlashOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                                                            null
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        stringResource(R.string.linterna),
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceEvenly
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    FilledTonalIconButton(
                                                        onClick = onCapture,
                                                        modifier = Modifier.size(56.dp),
                                                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                                                            containerColor = Color(0xFFE1F5FE),
                                                            contentColor = Color(0xFF03A9F4)
                                                        )
                                                    ) {
                                                        Icon(Icons.Filled.Camera, null)
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(stringResource(R.string.capturar), fontSize = 11.sp)
                                                }
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    FilledTonalIconButton(
                                                        onClick = onChangeMode,
                                                        modifier = Modifier.size(56.dp),
                                                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                                                            containerColor = Color(0xFFE8EAF6),
                                                            contentColor = activeColor
                                                        )
                                                    ) {
                                                        Icon(Icons.Filled.SwapHoriz, null)
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(stringResource(R.string.modo), fontSize = 11.sp)
                                                }
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    FilledTonalIconButton(
                                                        onClick = onToggleTimeLapse,
                                                        modifier = Modifier.size(56.dp),
                                                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                                                            containerColor = if (isRecordingTimeLapse) Color.Red.copy(alpha = 0.1f) else Color(0xFFF5F5F5),
                                                            contentColor = if (isRecordingTimeLapse) Color.Red else Color.Gray
                                                        )
                                                    ) {
                                                        Icon(if (isRecordingTimeLapse) Icons.Filled.StopCircle else Icons.Filled.Timelapse, null)
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(stringResource(R.string.time_lapse), fontSize = 11.sp)
                                                }
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceEvenly
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    FilledTonalIconButton(
                                                        onClick = onTogglePip,
                                                        modifier = Modifier.size(56.dp),
                                                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                                                            containerColor = if (showPip) activeColor.copy(alpha = 0.15f) else Color(0xFFF5F5F5),
                                                            contentColor = if (showPip) activeColor else Color.Gray
                                                        )
                                                    ) {
                                                        Icon(Icons.Filled.Layers, null)
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(stringResource(R.string.pip), fontSize = 11.sp)
                                                }
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    FilledTonalIconButton(
                                                        onClick = onToggleWorldLock,
                                                        modifier = Modifier.size(56.dp),
                                                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                                                            containerColor = if (isWorldLockActive) activeColor.copy(alpha = 0.15f) else Color(0xFFF5F5F5),
                                                            contentColor = if (isWorldLockActive) activeColor else Color.Gray
                                                        )
                                                    ) {
                                                        Icon(if (isWorldLockActive) Icons.Filled.Lock else Icons.Filled.LockOpen, null)
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(stringResource(R.string.ar_lock), fontSize = 11.sp)
                                                }
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Box(contentAlignment = Alignment.TopEnd) {
                                                        FilledTonalIconButton(
                                                            onClick = onToggleRefPoints,
                                                            modifier = Modifier
                                                                .size(56.dp)
                                                                .border(
                                                                    width = if (isRefPointsActive) 2.dp else 0.dp,
                                                                    color = if (isRefPointsActive) activeColor else Color.Transparent,
                                                                    shape = CircleShape
                                                                ),
                                                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                                                containerColor = if (isRefPointsActive) activeColor.copy(alpha = 0.2f) else Color(0xFFF5F5F5),
                                                                contentColor = if (isRefPointsActive) activeColor else Color.Gray
                                                            )
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Filled.PushPin,
                                                                contentDescription = null,
                                                                tint = if (isRefPointsActive) activeColor else Color.Gray
                                                            )
                                                        }
                                                        if (referencePointsCount > 0) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .offset(x = 4.dp, y = (-4).dp)
                                                                    .size(20.dp)
                                                                    .background(activeColor, CircleShape),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Text(
                                                                    text = "$referencePointsCount",
                                                                    color = Color.White,
                                                                    fontSize = 10.sp,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                            }
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = stringResource(R.string.ref_points),
                                                        fontSize = 11.sp,
                                                        fontWeight = if (isRefPointsActive) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isRefPointsActive) activeColor else Color.Unspecified
                                                    )
                                                    if (referencePointsCount > 0) {
                                                        Text(
                                                            text = stringResource(R.string.limpiar_puntos),
                                                            fontSize = 9.sp,
                                                            color = activeColor,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier
                                                                .padding(top = 2.dp)
                                                                .clickable { onClearReferencePoints() }
                                                        )
                                                    }
                                                }
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Button(
                                                        onClick = onShowColorGuide,
                                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                                                        colors = ButtonDefaults.buttonColors(containerColor = activeColor)
                                                    ) {
                                                        Icon(Icons.Filled.Palette, null)
                                                        Spacer(Modifier.width(8.dp))
                                                        Text(stringResource(R.string.guia_de_mezcla))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                4 -> { 
                                    Column(
                                        modifier = Modifier.verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        if (currentFilter != "NONE") {
                                            Button(
                                                onClick = {
                                                    onRestoreOriginal()
                                                    onFilterChange("NONE")
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(38.dp),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                                            ) {
                                                Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(6.dp))
                                                Text(stringResource(R.string.restaurar_foto), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        FilterModernCard(
                                            title = stringResource(R.string.boceto),
                                            subtitle = "Convierte a boceto lineal",
                                            icon = Icons.Filled.Brush,
                                            isSelected = currentFilter == "SKETCH",
                                            onClick = {
                                                if (currentFilter == "SKETCH") {
                                                    onRestoreOriginal()
                                                    onFilterChange("NONE")
                                                } else {
                                                    onSketch(sketchLineSize, sketchThreshold)
                                                    onFilterChange("SKETCH")
                                                }
                                            }
                                        )
                                        AnimatedVisibility(visible = currentFilter == "SKETCH") {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        Color.White.copy(alpha = 0.6f),
                                                        RoundedCornerShape(16.dp)
                                                    )
                                                    .border(1.dp, activeColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                                    .padding(12.dp)
                                            ) {
                                                Text(
                                                    stringResource(R.string.ajustar_boceto),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = activeColor
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    "${stringResource(R.string.grosor)}: ${"%.1f".format(sketchLineSize)}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Slider(
                                                    value = sketchLineSize,
                                                    onValueChange = { sketchLineSize = it },
                                                    onValueChangeFinished = { onSketch(sketchLineSize, sketchThreshold) },
                                                    valueRange = 0f..4f,
                                                    colors = SliderDefaults.colors(thumbColor = activeColor, activeTrackColor = activeColor)
                                                )
                                                Text(
                                                    "${stringResource(R.string.detalle)}: ${"%.2f".format(sketchThreshold)}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Slider(
                                                    value = sketchThreshold,
                                                    onValueChange = { sketchThreshold = it },
                                                    onValueChangeFinished = { onSketch(sketchLineSize, sketchThreshold) },
                                                    valueRange = 0.01f..1.0f,
                                                    colors = SliderDefaults.colors(thumbColor = activeColor, activeTrackColor = activeColor)
                                                )
                                            }
                                        }
                                        if (currentFilter != "SKETCH") {
                                            FilterModernCard(
                                                title = stringResource(R.string.quitar_fondo),
                                                subtitle = "Aislar objeto con IA",
                                                icon = Icons.Filled.AutoFixHigh,
                                                isSelected = false,
                                                onClick = {
                                                    onRemoveBackground()
                                                    selectedTab = -1
                                                }
                                            )
                                        }
                                        FilterModernCard(
                                            title = stringResource(R.string.pencil_color),
                                            subtitle = "Trazo en Azul, Rojo o Verde",
                                            icon = Icons.Filled.Edit,
                                            isSelected = currentFilter == "BLUE_PENCIL",
                                            onClick = {
                                                onFilterChange(if (currentFilter == "BLUE_PENCIL") "NONE" else "BLUE_PENCIL")
                                            }
                                        )
                                        AnimatedVisibility(visible = currentFilter == "BLUE_PENCIL") {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        Color.White.copy(alpha = 0.6f),
                                                        RoundedCornerShape(16.dp)
                                                    )
                                                    .border(1.dp, activeColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                                    .padding(12.dp)
                                            ) {
                                                Text(
                                                    text = "Color de Lápiz",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = activeColor
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceEvenly
                                                ) {
                                                    ColorPencilOption(
                                                        label = stringResource(R.string.azul),
                                                        color = Color(0xFF2196F3),
                                                        isSelected = pencilColorMode == "BLUE",
                                                        onClick = { onPencilColorModeChange("BLUE") }
                                                    )
                                                    ColorPencilOption(
                                                        label = stringResource(R.string.rojo),
                                                        color = Color(0xFFF44336),
                                                        isSelected = pencilColorMode == "RED",
                                                        onClick = { onPencilColorModeChange("RED") }
                                                    )
                                                    ColorPencilOption(
                                                        label = stringResource(R.string.verde),
                                                        color = Color(0xFF4CAF50),
                                                        isSelected = pencilColorMode == "GREEN",
                                                        onClick = { onPencilColorModeChange("GREEN") }
                                                    )
                                                }
                                            }
                                        }
                                        FilterModernCard(
                                            title = stringResource(R.string.posterizacion),
                                            subtitle = "Reduce gama de colores",
                                            icon = Icons.Filled.Palette,
                                            isSelected = currentFilter == "POSTERIZE",
                                            onClick = {
                                                onFilterChange(if (currentFilter == "POSTERIZE") "NONE" else "POSTERIZE")
                                            }
                                        )
                                        AnimatedVisibility(visible = currentFilter == "POSTERIZE") {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        Color.White.copy(alpha = 0.6f),
                                                        RoundedCornerShape(16.dp)
                                                    )
                                                    .border(1.dp, activeColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                                    .padding(12.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "Niveles de Color",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = activeColor
                                                    )
                                                    Text(
                                                        text = "${posterizeLevels.toInt()}",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.DarkGray
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Slider(
                                                    value = posterizeLevels,
                                                    onValueChange = { onPosterizeLevelsChange(it) },
                                                    valueRange = 2f..30f,
                                                    colors = SliderDefaults.colors(thumbColor = activeColor, activeTrackColor = activeColor)
                                                )
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceEvenly
                                                ) {
                                                    listOf(4f, 8f, 12f, 20f).forEach { preset ->
                                                        FilterPresetChip(
                                                            label = "${preset.toInt()}",
                                                            isSelected = posterizeLevels.toInt() == preset.toInt(),
                                                            onClick = { onPosterizeLevelsChange(preset) }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        FilterModernCard(
                                            title = stringResource(R.string.ajuste_de_gamma),
                                            subtitle = "Modifica curva de brillo",
                                            icon = Icons.Filled.BrightnessHigh,
                                            isSelected = currentFilter == "GAMMA",
                                            onClick = {
                                                onFilterChange(if (currentFilter == "GAMMA") "NONE" else "GAMMA")
                                            }
                                        )
                                        AnimatedVisibility(visible = currentFilter == "GAMMA") {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        Color.White.copy(alpha = 0.6f),
                                                        RoundedCornerShape(16.dp)
                                                    )
                                                    .border(1.dp, activeColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                                    .padding(12.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "Valor Gamma",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = activeColor
                                                    )
                                                    Text(
                                                        text = "%.2f".format(gammaValue),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.DarkGray
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Slider(
                                                    value = gammaValue,
                                                    onValueChange = { onGammaValueChange(it) },
                                                    valueRange = 0.2f..4.0f,
                                                    colors = SliderDefaults.colors(thumbColor = activeColor, activeTrackColor = activeColor)
                                                )
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceEvenly
                                                ) {
                                                    listOf(0.5f, 1.0f, 2.0f, 3.0f).forEach { preset ->
                                                        FilterPresetChip(
                                                            label = "%.1f".format(preset),
                                                            isSelected = kotlin.math.abs(gammaValue - preset) < 0.1f,
                                                            onClick = { onGammaValueChange(preset) }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        FilterModernCard(
                                            title = stringResource(R.string.sharpen),
                                            subtitle = "Resalta nitidez y bordes",
                                            icon = Icons.Filled.Details,
                                            isSelected = currentFilter == "SHARPEN",
                                            onClick = {
                                                onFilterChange(if (currentFilter == "SHARPEN") "NONE" else "SHARPEN")
                                            }
                                        )
                                        AnimatedVisibility(visible = currentFilter == "SHARPEN") {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        Color.White.copy(alpha = 0.6f),
                                                        RoundedCornerShape(16.dp)
                                                    )
                                                    .border(1.dp, activeColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                                    .padding(12.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "Intensidad de Nitidez",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = activeColor
                                                    )
                                                    Text(
                                                        text = "%.2f".format(sharpnessValue),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.DarkGray
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Slider(
                                                    value = sharpnessValue,
                                                    onValueChange = { onSharpnessValueChange(it) },
                                                    valueRange = -2.0f..4.0f,
                                                    colors = SliderDefaults.colors(thumbColor = activeColor, activeTrackColor = activeColor)
                                                )
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceEvenly
                                                ) {
                                                    listOf(0.5f, 1.5f, 2.5f, 4.0f).forEach { preset ->
                                                        FilterPresetChip(
                                                            label = "%.1f".format(preset),
                                                            isSelected = kotlin.math.abs(sharpnessValue - preset) < 0.1f,
                                                            onClick = { onSharpnessValueChange(preset) }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        FilterModernCard(
                                            title = stringResource(R.string.escala_de_grises),
                                            subtitle = "Efecto blanco y negro ajustable",
                                            icon = Icons.Filled.Contrast,
                                            isSelected = currentFilter == "GRAYSCALE",
                                            onClick = { onFilterChange(if (currentFilter == "GRAYSCALE") "NONE" else "GRAYSCALE") }
                                        )
                                        AnimatedVisibility(visible = currentFilter == "GRAYSCALE") {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        Color.White.copy(alpha = 0.6f),
                                                        RoundedCornerShape(16.dp)
                                                    )
                                                    .border(1.dp, activeColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                                    .padding(12.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "Intensidad B/N",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = activeColor
                                                    )
                                                    Text(
                                                        text = "${((1.0f - grayscaleSaturation) * 100).toInt()}%",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.DarkGray
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Slider(
                                                    value = grayscaleSaturation,
                                                    onValueChange = { onGrayscaleSaturationChange(it) },
                                                    valueRange = 0.0f..1.0f,
                                                    colors = SliderDefaults.colors(thumbColor = activeColor, activeTrackColor = activeColor)
                                                )
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceEvenly
                                                ) {
                                                    listOf(0.0f to "100%", 0.25f to "75%", 0.5f to "50%", 0.75f to "25%").forEach { (satValue, label) ->
                                                        FilterPresetChip(
                                                            label = label,
                                                            isSelected = kotlin.math.abs(grayscaleSaturation - satValue) < 0.05f,
                                                            onClick = { onGrayscaleSaturationChange(satValue) }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        FilterModernCard(
                                            title = stringResource(R.string.invertir),
                                            subtitle = "Invierte tonos de la imagen",
                                            icon = Icons.Filled.InvertColors,
                                            isSelected = currentFilter == "INVERT",
                                            onClick = { onFilterChange(if (currentFilter == "INVERT") "NONE" else "INVERT") }
                                        )
                                        FilterModernCard(
                                            title = stringResource(R.string.sepia),
                                            subtitle = "Tono vintage cálido",
                                            icon = Icons.Filled.Grain,
                                            isSelected = currentFilter == "SEPIA",
                                            onClick = { onFilterChange(if (currentFilter == "SEPIA") "NONE" else "SEPIA") }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Surface(
                modifier = Modifier
                    .width(64.dp)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(32.dp),
                color = Color.Black.copy(alpha = 0.6f),
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    val menuItems = listOf(
                        Icons.Filled.Opacity to 0,
                        Icons.Filled.ImageSearch to 1,
                        Icons.Filled.CameraAlt to 2,
                        Icons.Filled.FilterList to 4
                    )
                    menuItems.forEach { (icon, index) ->
                        val isSelected = selectedTab == index
                        IconButton(
                            onClick = {
                                selectedTab = if (isSelected) -1 else index
                                onTabSelected()
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    if (isSelected) activeColor else Color.Transparent,
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
        if (showAnimeDialog) {
            AnimeImageApp(
                onImageSelected = { url ->
                    try {
                        if (url.isNotBlank()) {
                            onImageUriChange(url.toUri())
                            scope.launch {
                                GalleryStorageManager.saveUrlToInternalStorage(
                                    context,
                                    url
                                )
                            }
                        }
                    } catch (e: Exception) {
                    }
                    showAnimeDialog = false
                },
                onDismissRequest = { showAnimeDialog = false }
            )
        }
    }
}
@Composable
fun VerticalActionButton(
    icon: ImageVector,
    text: String,
    activeColor: Color = Color.Gray,
    onClick: () -> Unit = {},
    content: @Composable (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF5F5F5))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (content != null) {
            content()
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = activeColor, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
@Composable
fun FilterModernCard(
    title: String,
    subtitle: String = "",
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val activeColor = Color(0xFFFF4081)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) activeColor.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.35f),
        border = BorderStroke(
            1.dp,
            if (isSelected) activeColor else Color.White.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (isSelected) activeColor else Color.White.copy(alpha = 0.4f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isSelected) activeColor else Color.Black.copy(alpha = 0.85f)
                )
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        fontSize = 10.sp,
                        color = if (isSelected) activeColor.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.5f)
                    )
                }
            }
            if (isSelected) {
                Icon(
                    Icons.Filled.CheckCircle,
                    null,
                    tint = activeColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
@Composable
fun ColorPencilOption(
    label: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = if (isSelected) color.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.5f),
        border = BorderStroke(
            1.5.dp,
            if (isSelected) color else Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) color else Color.DarkGray
            )
        }
    }
}
@Composable
fun FilterPresetChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val activeColor = Color(0xFFFF4081)
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        color = if (isSelected) activeColor else Color.White.copy(alpha = 0.6f),
        border = BorderStroke(
            1.dp,
            if (isSelected) activeColor else Color.LightGray.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else Color.DarkGray,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
@Composable
fun TransformIconButton(icon: ImageVector, onClick: () -> Unit, rotate: Float = 0f) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .background(Color(0xFFF5F5F5), CircleShape)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .size(20.dp)
                .rotate(rotate),
            tint = Color(0xFF333333)
        )
    }
}
@Composable
fun GridTypeButton(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = Color(0xFFFF4081)
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(40.dp)
            .background(
                if (isSelected) activeColor.copy(alpha = 0.15f) else Color(0xFFF5F5F5),
                RoundedCornerShape(8.dp)
            )
            .border(
                1.dp,
                if (isSelected) activeColor else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) activeColor else Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}
@Composable
fun GalleryPickerPro(onImageSelected: (Uri) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            onImageSelected(it)
            scope.launch { GalleryStorageManager.saveImageToInternalStorage(context, it) }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { launcher.launch("image/*") }, contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.AddPhotoAlternate,
                null,
                tint = Color.Gray,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.galer_a), fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}
