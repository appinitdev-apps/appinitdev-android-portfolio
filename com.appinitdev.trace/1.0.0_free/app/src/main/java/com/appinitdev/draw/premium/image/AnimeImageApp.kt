
@Composable
fun AnimeImageApp(
    onImageSelected: (String) -> Unit,
    onDismissRequest: () -> Unit = {}
) {
    var images by remember { mutableStateOf<List<Image>>(emptyList()) }
    var page by remember { mutableIntStateOf(1) }
    var selectedCategory by remember { mutableStateOf("cosplay") }
    var isLoading by remember { mutableStateOf(false) }

    
    val cache = remember { mutableMapOf<String, List<Image>>() }

    val categories = listOf("cosplay", "girl", "woman",  "anime", "draw", "manga", "illustration", "art")

    val apiKey = ""

    LaunchedEffect(page, selectedCategory) {
        val cacheKey = ""
        if (cache.containsKey(cacheKey)) {
            images = cache[cacheKey] ?: emptyList()
            return@LaunchedEffect
        }

        isLoading = true
        try {
            Log.d("AnimeImageApp", "Searching Pixabay: query='$selectedCategory', page=$page")
            val response = RetrofitInstance.api.searchImages(
                apiKey = apiKey,
                query = selectedCategory,
                imageType = "all",
                perPage = 30,
                page = page
            )
            Log.d("AnimeImageApp", "Pixabay response received: hits count=${response.hits.size}")
            images = response.hits
            cache[cacheKey] = images 
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e("AnimeImageApp", "Pixabay HTTP Error code=${e.code()}, message=${e.message()}, body=$errorBody", e)
            if (page > 1) page = 1
        } catch (e: Exception) {
            Log.e("AnimeImageApp", "Pixabay Error: ${e.javaClass.simpleName} - ${e.message}", e)
            if (page > 1) page = 1
        } finally {
            isLoading = false
        }
    }

    ShowGalleryDialog(
        images = images,
        categories = categories,
        selectedCategory = selectedCategory,
        isLoading = isLoading,
        onCategorySelected = { category ->
            selectedCategory = category
            page = 1 
        },
        onSearchQuery = { query ->
            selectedCategory = query
            page = 1
        },
        onDismissRequest = onDismissRequest,
        onNextPage = {
            if (page < 15) page += 1
            else page = 1
        },
        onPrevPage = {
            if (page > 1) page -= 1
            else page = 15 
        },
        onImageClick = { url ->
            Log.d("AnimeImageApp", "Selected image: $url")
            onImageSelected(url)
            onDismissRequest()
        }
    )
}


