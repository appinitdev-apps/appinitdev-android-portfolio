
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomEmojiPicker(
    onEmojiSelected: (String) -> Unit,
    onGifSelected: (String) -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(450.dp)
            .padding(16.dp)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = Color(0xFF238CDD),
            divider = {}
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text(stringResource(R.string.emojis), modifier = Modifier.padding(16.dp), color = Color.White)
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Gif, contentDescription = null, tint = Color.White)
                    Text(stringResource(R.string.gifs), modifier = Modifier.padding(16.dp), color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 0) {
            EmojiGrid(onEmojiSelected)
        } else {
            GifPicker(onGifSelected)
        }
    }
}

@Composable
fun EmojiGrid(onEmojiSelected: (String) -> Unit) {
    val emojis = remember {
        listOf(
            "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇", 
            "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😙", "😚", 
            "😋", "😛", "😝", "😜", "🤪", "🤨", "🧐", "🤓", "😎", "🤩", 
            "🥳", "😏", "😒", "😞", "😔", "😟", "😕", "🙁", "☹️", "😣", 
            "😖", "😫", "😩", "🥺", "😢", "😭", "😤", "😠", "😡", "🤬", 
            "🤯", "😳", "🥵", "🥶", "😱", "😨", "😰", "😥", "😓", "🤗", 
            "🤔", "🤭", "🤫", "🤥", "😶", "😐", "😑", "😬", "🙄", "😯", 
            "😦", "😧", "😮", "😲", "🥱", "😴", "🤤", "😪", "😵", "🤐", 
            "🥴", "🤢", "🤮", "🤧", "❤️", "🧡", "💛", "💚", "💙", "💜",
            "🔥", "✨", "🌟", "⭐", "🌈", "☀️", "☁️", "❄️", "🌊", "👍"
        )
    }
    
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 48.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(emojis) { emoji ->
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onEmojiSelected(emoji) },
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 28.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GifPicker(onGifSelected: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    var gifs by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val client = remember { OkHttpClient() }
    val gson = remember { Gson() }

    LaunchedEffect(query) {
        isLoading = true
        if (query.length < 2 && query.isNotEmpty()) {
            delay(300)
        } else {
            delay(500)
        }

        withContext(Dispatchers.IO) {
            try {
                // USANDO API KEY PROPORCIONADA
                val apiKey = ""
                val url = if (query.isEmpty()) {
                    "https://api.giphy.com/v1/gifs/trending?api_key=$apiKey&limit=20"
                } else {
                    "https://api.giphy.com/v1/gifs/search?api_key=$apiKey&q=$query&limit=20"
                }

                val request = Request.Builder().url(url).build()
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        val json = gson.fromJson(body, GiphyResponse::class.java)
                        gifs = json.data.map { it.images.fixed_height.url }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    Column {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.buscar_gifs), color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF238CDD),
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading && gifs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF238CDD))
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(gifs) { gifUrl ->
                    AsyncImage(
                        model = gifUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onGifSelected(gifUrl) },
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

// Modelos para Giphy
data class GiphyResponse(val data: List<GiphyData>)
data class GiphyData(val images: GiphyImages)
data class GiphyImages(val fixed_height: GiphyImage)
data class GiphyImage(val url: String)
