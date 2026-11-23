
@Composable
fun Customization(viewModel: ChatViewModel, state: AppState) {
    val pref = state.userData?.pref
    var anim by remember { mutableStateOf(pref?.anim) }
    var sliderPosition by remember { mutableFloatStateOf(pref?.fontSize.toString().toFloat()) }
    var bsliderPosition by remember { mutableFloatStateOf(pref?.back.toString().toFloat()) }
    var dsliderPosition by remember { mutableFloatStateOf(pref?.doodles.toString().toFloat()) }
    var customImg by remember { mutableStateOf(pref?.customImg.toString()) }
    var imgUri by remember {
        mutableStateOf<Uri?>(null)
    }
    var isUploading by remember {
        mutableStateOf(false)
    }
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()){
        imgUri = it
    }
    var bitmap by remember {mutableStateOf<Bitmap?>(null)}
    val size = sliderPosition.sp
    val brush = Brush.linearGradient(listOf(
        Color(0xFF238CDD),
        Color(0xFF1952C4)
    ))
    val brush2 = Brush.linearGradient(listOf(
        Color(0xFF2A4783),
        Color(0xFF2F6086)
    ))
    val context = LocalContext.current
    val comp by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.upload))
    val comp1 by rememberLottieComposition(LottieCompositionSpec.RawRes(
        when(anim){
            1 -> R.raw.animation1
            2 -> R.raw.animation2
            3 -> R.raw.animation3
            else -> {
                R.raw.animation4
            }
        }))
    Image(modifier = Modifier.fillMaxSize(),
        painter = painterResource(R.drawable.blurry_gradient_haikei),
        contentDescription = null,
        contentScale = ContentScale.Crop,
    )
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(top = 50.dp)
        .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.personalizaci_n),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp,)
        )
        Box(
            modifier = Modifier
                .shadow(5.dp)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.inversePrimary,
                    RoundedCornerShape(16.dp)
                )
                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(16.dp))
                .height(350.dp)
        ) {
            if (customImg.length == 0) {
                Image(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(bsliderPosition)
                        .clip(RoundedCornerShape(16.dp)),
                    painter = painterResource(R.drawable.blurry_gradient_haikei),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )
                Image(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(
                            dsliderPosition
                        )
                        .clip(RoundedCornerShape(16.dp)),
                    painter = painterResource(R.drawable.social_media_doodle_seamless_pattern_vector_27700734),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )
            }
            if (customImg.isNotBlank()) {
                AsyncImage(
                    model = customImg, contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(bsliderPosition)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            imgUri?.let {
                customImg = "1"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val src = ImageDecoder.createSource(LocalContext.current.contentResolver, it)
                    bitmap = ImageDecoder.decodeBitmap(src)
                }
                AsyncImage(
                    model = it,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(bsliderPosition)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Text(
                text = stringResource(R.string.previsualizaci_n), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            )
            Column(
                modifier = Modifier
                    .padding(bottom = 10.dp)
                    .fillMaxSize(), verticalArrangement = Arrangement.Bottom
            ) {
                MessageItem1(stringResource(R.string.ejemplo_mensaje), alignment = Alignment.CenterStart, brush2, size)
                MessageItem1(stringResource(R.string.ejemplo_mensaje), alignment = Alignment.CenterEnd, brush, size)
                LottieAnimation(
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .size(80.dp)
                        .alpha(0.8f),
                    composition = comp1,
                    iterations = LottieConstants.IterateForever
                )
            }
            Text(
                text = stringResource(R.string.previsualizaci_n2), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            )
            if (isUploading) {
                LottieAnimation(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .scale(0.5f),
                    composition = comp,
                    iterations = LottieConstants.IterateForever
                )
            }
        }
    Spacer(modifier = Modifier.height(20.dp))
    LazyColumn(modifier = Modifier.weight(1f)) {
        item {
            Row(
                modifier = Modifier
                    .background(
                        Color.White.copy(alpha = 0.2f),
                        RoundedCornerShape(12.dp)
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                ) {
                    Text(
                        text = stringResource(R.string.tama_o_de_fuente),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                    Slider(
                        value = sliderPosition,
                        onValueChange = { sliderPosition = it },
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.secondary,
                            activeTrackColor = MaterialTheme.colorScheme.secondary,
                            inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        steps = 4,
                        valueRange = 14f..24f
                    )
                }
            }
        }
        item {
            Row(
                modifier = Modifier
                    .padding(top = 15.dp)
                    .background(
                        Color.White.copy(alpha = 0.2f),
                        RoundedCornerShape(12.dp)
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                ) {
                    Text(
                        text = stringResource(R.string.indicador_de_escritura),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround) {
                        RadioButton(selected = anim==1, onClick = { anim=1 })
                        RadioButton(selected = anim==2, onClick = { anim=2 })
                        RadioButton(selected = anim==3, onClick = { anim=3 })
                        RadioButton(selected = anim==4, onClick = { anim=4 })
                    }
                }
            }
        }
        item {
            Row(
                modifier = Modifier
                    .padding(top = 15.dp)
                    .background(
                        Color.White.copy(alpha = 0.2f),
                        RoundedCornerShape(12.dp)
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                ) {
                    Text(
                        text = stringResource(R.string.fondo),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                    Slider(
                        value = bsliderPosition,
                        onValueChange = { bsliderPosition = it },
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.secondary,
                            activeTrackColor = MaterialTheme.colorScheme.secondary,
                            inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer
                        ),

                        )
                }
            }
        }
        item {
            Row(
                modifier = Modifier
                    .padding(top = 15.dp)
                    .background(
                        Color.White.copy(alpha = 0.2f),
                        RoundedCornerShape(12.dp)
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                ) {
                    Text(
                        text = stringResource(R.string.garabatos),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                    Slider(
                        enabled = customImg.isEmpty(),
                        value = dsliderPosition,
                        onValueChange = { dsliderPosition = it },
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.secondary,
                            activeTrackColor = MaterialTheme.colorScheme.secondary,
                            inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        valueRange = 0f..0.5f
                    )
                }
            }
        }
        item {
            Row(
                modifier = Modifier
                    .padding(top = 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .clickable {
                            launcher.launch("image/*")
                        }
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        )
                        .weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.explorar_im_genes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(25.dp)
                    )
                }
                if (customImg.isNotBlank()) {
                    Spacer(modifier = Modifier.width(15.dp))
                    Column(
                        modifier = Modifier
                            .clickable {
                                customImg = ""
                                imgUri = null
                            }
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                RoundedCornerShape(12.dp)
                            )
                            .weight(1f),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.quitar_imagen),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(25.dp)
                        )
                    }
                }

            }
        }
    }
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = {
            if(imgUri!=null){
                isUploading=true
                viewModel.uploadMedia(uri = imgUri!!, folder = "photos"){
                    state.userData?.let { user ->
                        viewModel.updatePref(user,
                            user.pref.copy(
                                anim = anim ?: 3,
                                fontSize = sliderPosition,
                                back = bsliderPosition,
                                doodles = dsliderPosition,
                                customImg=it
                            )
                        )
                    }
                    isUploading=false
                    imgUri=null
                }
            }
              else {
                state.userData?.let { user ->
                    viewModel.updatePref(user,
                        user.pref.copy(
                            anim = anim ?: 3,
                            fontSize = sliderPosition,
                            back = bsliderPosition,
                            doodles = dsliderPosition,
                            customImg=customImg
                        )
                    )
                }
            }
                Toast.makeText(context, context.getString(R.string.preferencias_guardadas), Toast.LENGTH_SHORT).show()
                        },
            modifier = Modifier
                .padding(bottom = 50.dp)
                .background(brush, CircleShape)
                .fillMaxWidth(0.7f)
                .height(50.dp), colors = ButtonDefaults.buttonColors(Color.Transparent), shape = CircleShape
        ) {
            Text(
                text = stringResource(R.string.guardar_preferencias),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
@Composable
fun MessageItem1(
    text: String,
    alignment: Alignment,
    brush: Brush,
    size: TextUnit,
){
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 3.dp,
                bottom = 3.dp,
                start = 10.dp,
                end = 10.dp
            ),
        contentAlignment = alignment
    ) {
        Column(verticalArrangement = Arrangement.Bottom) {
            Column(
                modifier = Modifier
                    .shadow(2.dp, RoundedCornerShape(16.dp))
                    .widthIn(max = 270.dp)
                    .background(brush, RoundedCornerShape(16.dp)),
                horizontalAlignment = Alignment.End
            ) {
                if(alignment == Alignment.CenterEnd){
                    Row(
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(top = 4.dp, start = 4.dp, end = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.4f)),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.ejemplo_usuario),
                                modifier = Modifier
                                    .padding(horizontal = 6.dp)
                                    .padding(top = 4.dp),
                                fontSize = size.times(0.85),
                                color = Color(0xFF3CAAFF),
                                fontWeight = FontWeight.Black
                            )
                        Text(
                            text = stringResource(R.string.ejemplo_mensaje),
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .padding(bottom = 4.dp),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = size.times(0.8)
                            ),
                            color = Color.White,
                        )
                    }
                    }
                }

                Text(
                    text = text,
                    modifier = Modifier.padding(top = 5.dp, start = 10.dp, end = 10.dp),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = size),
                    color = Color.White
                )
                Text(
                    text = stringResource(R.string.ejemplo_hora),
                    modifier = Modifier.padding(
                        end = 8.dp,
                        bottom = 5.dp,
                        start = 8.dp,
                        top = 2.dp
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                )


            }
        }
    }
}