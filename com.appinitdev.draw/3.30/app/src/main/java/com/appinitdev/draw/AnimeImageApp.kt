@Composable
fun AnimeImageApp(onImageSelected: (String) -> Unit) {
    val images = remember { mutableStateOf<List<Image>>(emptyList()) }
    val openDialog = remember { mutableStateOf(false) }
    val selectedImage = remember { mutableStateOf<Image?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val page = remember { mutableStateOf(1) }
    var showDialog by remember { mutableStateOf(false) }
    fun loadImages(pageNumber: Int) {
        coroutineScope.launch {
            try {
            val response = RetrofitInstance.api.searchImages(
                apiKey = "",
                query = "anime",
                page = pageNumber
            )
            images.value = response.hits
            } catch (e: HttpException) {
                page.value=1
            } catch (e: Exception) {
                page.value=1
            }
        }
    }
    LaunchedEffect(page.value) {
        loadImages(page.value)
    }
    Box(
        modifier = Modifier
            .background(Color(0x4B000000)) 
            .size(50.dp) 
    ) {
        IconButton(onClick = { showDialog = true }) {
            Icon(
                imageVector = Icons.Filled.Search, 
                contentDescription = "Buscar",
                tint = Color.White 
            )
        }
    }
    if (showDialog) {
        ShowGalleryDialog(
            images = images.value,
            onDismissRequest = { showDialog = false },
            onNextPage = {
                page.value += 1
                if (page.value <= 15)
                    loadImages(page.value)  
                else page.value = 1
            },
            onPrevPage = {
                if (page.value > 1) {  
                    page.value -= 1
                    loadImages(page.value)  
                }else {
                    page.value =16
                    loadImages(page.value)
                }
            },
            onImageClick = { url ->
                Log.e("koll", url)
                onImageSelected(url)
                showDialog = false
            }
        )
    }
}
