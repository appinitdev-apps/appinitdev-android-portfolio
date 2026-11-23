@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawingModeScreen(
    onModeSelected: (startWithCameraOn: Boolean, useGridByDefault: Boolean, useSketchByDefault: Boolean) -> Unit,
    onBack: () -> Unit
) {
    var selectedModeIndex by remember { mutableIntStateOf(0) } 
    var modalData by remember { mutableStateOf<ModeModalData?>(null) }
    val activeColor = Color(0xFFFF4081)
    modalData?.let { data ->
        AlertDialog(
            onDismissRequest = { modalData = null },
            confirmButton = {
                TextButton(onClick = { modalData = null }) {
                    Text(
                        text = stringResource(R.string.cerrar),
                        color = activeColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = data.imageResId),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = data.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1A1C1E),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = data.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(28.dp)
        )
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.atras)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.seleccionar_el_modo_de_dibujo),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1A1C1E),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModernModeCard(
                    title = stringResource(R.string.dibujar_con_la_c_mara),
                    description = stringResource(R.string.utiliza_un_tr_pode_un_vaso_o_una_pila_de_libros_para_sujetar_el_tel_fono_mientras_dibujas),
                    imageResId = R.drawable.m1,
                    isSelected = selectedModeIndex == 0,
                    activeColor = activeColor,
                    onClick = { selectedModeIndex = 0 },
                    onImageClick = {
                        modalData = ModeModalData(
                            title = it.title,
                            description = it.description,
                            imageResId = it.imageResId
                        )
                    }
                )
                ModernModeCard(
                    title = stringResource(R.string.metodo_de_cuadricula),
                    description = stringResource(R.string.descripcion_cuadricula),
                    imageResId = R.drawable.m3, 
                    isSelected = selectedModeIndex == 2,
                    activeColor = activeColor,
                    onClick = { selectedModeIndex = 2 },
                    onImageClick = {
                        modalData = ModeModalData(
                            title = it.title,
                            description = it.description,
                            imageResId = it.imageResId
                        )
                    }
                )
                ModernModeCard(
                    title = stringResource(R.string.dibujar_con_pantalla),
                    description = stringResource(R.string.coloca_un_papel_sobre_la_pantalla_de_tu_tel_fono_y_sigue_las_l_neas_para_trazar_tu_imagen_con_facilidad),
                    imageResId = R.drawable.m2,
                    isSelected = selectedModeIndex == 1,
                    activeColor = activeColor,
                    onClick = { selectedModeIndex = 1 },
                    onImageClick = {
                        modalData = ModeModalData(
                            title = it.title,
                            description = it.description,
                            imageResId = it.imageResId
                        )
                    }
                )
                ModernModeCard(
                    title = stringResource(R.string.modo_boceto),
                    description = stringResource(R.string.descripcion_boceto),
                    imageResId = R.drawable.m4,
                    isSelected = selectedModeIndex == 3,
                    activeColor = activeColor,
                    onClick = { selectedModeIndex = 3 },
                    onImageClick = {
                        modalData = ModeModalData(
                            title = it.title,
                            description = it.description,
                            imageResId = it.imageResId
                        )
                    }
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { 
                    when(selectedModeIndex) {
                        0 -> onModeSelected(true, false, false)
                        1 -> onModeSelected(false, false, false)
                        2 -> onModeSelected(true, true, false)
                        3 -> onModeSelected(true, false, true)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .shadow(8.dp, RoundedCornerShape(30.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = activeColor),
                shape = RoundedCornerShape(30.dp)
            ) {
                Text(
                    text = stringResource(R.string.continuar).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
data class ModeModalData(
    val title: String,
    val description: String,
    val imageResId: Int
)
@Composable
fun ModernModeCard(
    title: String,
    description: String,
    imageResId: Int,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    onImageClick: (ModeModalData) -> Unit
) {
    val backgroundColor = if (isSelected) activeColor.copy(alpha = 0.04f) else Color.White
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable { onClick() }
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) activeColor else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        color = backgroundColor,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF0F0F0))
                    .clickable { 
                        onImageClick(ModeModalData(title, description, imageResId))
                    }
            ) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(activeColor.copy(alpha = 0.15f))
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) activeColor else Color(0xFF1A1C1E)
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = activeColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black,
                    lineHeight = 18.sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
