@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun StoryPreview(
    uri: Uri?, 
    isSeller: Boolean,
    hideDialog: () -> Unit, 
    upload: (Uri?, Boolean, Int) -> Unit
) {
    var croppedUri: Uri? by remember {
        mutableStateOf(null)
    }
    var isLocked by remember { mutableStateOf(false) }
    var price by remember { mutableStateOf("10") }
    val cropImage = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            croppedUri = result.uriContent
        } else {
            val exception = result.error
        }
    }
    Dialog(onDismissRequest = hideDialog,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    )       {
        Column(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f))) {
        }
        var scale by remember {
            mutableStateOf(1f)
        }
        var offset by remember {
            mutableStateOf(Offset.Zero)
        }
        val brush = Brush.linearGradient(listOf(
            Color(0xFF238CDD),
            Color(0xFF1952C4)
        ))
        val brush2 = Brush.linearGradient(listOf(
            Color(0xFFA02424),
            Color(0xFFC43B56)
        ))
        BoxWithConstraints {
            val state = rememberTransformableState{ zoomChange, panChange, rotationChange ->
                scale = (scale*zoomChange).coerceIn(1f,5f)
                val extWidth = (scale - 1)*constraints.maxWidth
                val extHeight = (scale -1) * constraints.maxHeight
                val maxX = extWidth/2
                val maxY = extHeight/2
                offset = Offset(
                    x=(offset.x + scale * panChange.x).coerceIn(-maxX,maxX),
                    y=(offset.y + scale * panChange.y).coerceIn(-maxY,maxY)
                )
            }
            Column(modifier = Modifier
                .fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                AsyncImage(
                    model = if(croppedUri!=null) croppedUri else uri,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize(0.9f)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
                        }
                        .transformable(state)
                )
                Row(modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    if (isSeller) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = isLocked,
                                    onCheckedChange = { isLocked = it },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF238CDD))
                                )
                                Text(stringResource(R.string.bloquear_contenido), color = Color.White, fontSize = 12.sp)
                            }
                            if (isLocked) {
                                androidx.compose.material3.OutlinedTextField(
                                    value = price,
                                    onValueChange = { if(it.all { c -> c.isDigit() }) price = it },
                                    modifier = Modifier.padding(start = 8.dp).size(width = 80.dp, height = 50.dp),
                                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 12.sp),
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                                )
                            }
                        }
                    }
                    Button(onClick = hideDialog, modifier = Modifier.background(brush2, CircleShape), colors = ButtonDefaults.buttonColors(
                        Color.Transparent) ) {
                        Text(text = stringResource(R.string.cancelar), color = Color.White)
                    }
                    Button(onClick = { upload(croppedUri, isLocked, price.toIntOrNull() ?: 0) }, modifier = Modifier.background(brush, CircleShape), colors = ButtonDefaults.buttonColors(
                        Color.Transparent) ) {
                        Text(text = stringResource(R.string.subir), color = Color.White)
                    }
                }
            }
            IconButton(modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
                onClick = {
                    val cropOptions = CropImageContractOptions(uri, CropImageOptions(activityBackgroundColor = Color(0xFF000000).toArgb()))
                    cropImage.launch(cropOptions)
                }) {
                Icon(imageVector = Icons.Rounded.CropRotate, contentDescription = null)
            }
        }
    }
}