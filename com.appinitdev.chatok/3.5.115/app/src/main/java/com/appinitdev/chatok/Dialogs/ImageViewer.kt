
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ImageViewer(userData: ChatUserData, hideDialog:()->Unit, message: Message) {
    val formatter = remember {
        SimpleDateFormat(("hh:mm a"), Locale.getDefault())
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
        val interactionSource = remember { MutableInteractionSource() }
        var show by remember {
            mutableStateOf(true)
        }
        val imageModel = remember(message.imgUrl, message.imgUri, message.msgId) {
            val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "ChatOk")
            val localJpg = File(directory, "IMG_${message.msgId}.jpg")
            val localGif = File(directory, "IMG_${message.msgId}.gif")
            when {
                localJpg.exists() -> localJpg
                localGif.exists() -> localGif
                message.imgUri.isNotEmpty() && (message.imgUri.startsWith("file:") || message.imgUri.startsWith("content:")) -> {
                    try {
                        val uri = Uri.parse(message.imgUri)
                        if (uri.scheme == "file") {
                            val file = File(uri.path ?: "")
                            if (file.exists()) uri else message.imgUrl
                        } else uri
                    } catch (e: Exception) { message.imgUrl }
                }
                else -> message.imgUrl
            }
        }
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
            AsyncImage(
                model = imageModel,
                contentDescription = "Profile picture",
                modifier = Modifier
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        show = !show
                    }
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    }
                    .transformable(state)
            )
            AnimatedVisibility(visible = show, enter = fadeIn(), exit = fadeOut()) {
                Row(modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
                    .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { hideDialog() }) {
                        Icon(imageVector = Icons.Filled.ArrowBackIosNew, contentDescription = null)
                    }
                    AsyncImage(
                        model = userData.ppurl,
                        contentDescription = "Profile picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(40.dp)
                    )
                    Column(modifier = Modifier.padding(start = 16.dp)){
                        Text(
                            text = userData.username.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text =if(message.time != null) {
                                
                                formatter.format(message.time.toDate())
                            } else "",
                            color = Color.LightGray,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Light)
                        )
                    }

                }
            }

        }

    }
}