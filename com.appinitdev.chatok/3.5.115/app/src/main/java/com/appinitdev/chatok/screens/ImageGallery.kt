
@Composable
fun ImageGallery(navController: NavController, viewModel: ChatViewModel) {
    var mediaUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.getAllMedia(
            onResult = { urls ->
                mediaUrls = urls
                isLoading = false
            },
            onError = { exception ->
                errorMessage = exception.localizedMessage
                isLoading = false
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.blurry_gradient_haikei),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))

        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(40.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(imageVector = Icons.Filled.ArrowBackIosNew, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = stringResource(R.string.galeria_media),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF238CDD))
                }
            } else if (errorMessage != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.error_with_details, errorMessage ?: ""), color = Color.Red, textAlign = TextAlign.Center)
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C1E).copy(alpha = 0.8f))
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(mediaUrls) { mediaUrl ->
                            GalleryItem(
                                url = mediaUrl,
                                onDelete = {
                                    viewModel.deleteMedia(
                                        mediaUrl,
                                        onSuccess = { mediaUrls = mediaUrls - mediaUrl },
                                        onError = { errorMessage = it.localizedMessage }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

fun isVideo(url: String): Boolean {
    return url.contains(".mp4", ignoreCase = true) || 
           url.contains(".mov", ignoreCase = true) || 
           url.contains("video", ignoreCase = true)
}

@Composable
fun GalleryItem(url: String, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            if (isVideo(url)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(32.dp)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}
