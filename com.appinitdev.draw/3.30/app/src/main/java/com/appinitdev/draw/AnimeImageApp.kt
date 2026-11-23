
@Composable
fun AnimeImageApp(onImageSelected: (String) -> Unit) {
    val images = remember { mutableStateOf<List<Image>>(emptyList()) }
    val openDialog = remember { mutableStateOf(false) }
    val selectedImage = remember { mutableStateOf<Image?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val page = remember { mutableStateOf(1) }
    var showDialog by remember { mutableStateOf(false) }

    /*LaunchedEffect(Unit) {
        coroutineScope.launch {
            val response = RetrofitInstance.api.searchImages(
                apiKey = "",
                query = "anime",
                page = 5
            )
            images.value = response.hits
        }
    }*/
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
            .background(Color(0x4B000000)) // Fondo con color ligeramente transparente
            .size(50.dp) // Tamaño del botón (ajústalo a tu necesidad)
    ) {
        IconButton(onClick = { showDialog = true }) {
            Icon(
                imageVector = Icons.Filled.Search, // El ícono de lupa de material design
                contentDescription = "Buscar",
                tint = Color.White // Color del ícono
            )
        }
    }

    // Muestra la galería de imágenes en forma de lista

    if (showDialog) {
        ShowGalleryDialog(
            images = images.value,
            onDismissRequest = { showDialog = false },
            onNextPage = {
                page.value += 1
                if (page.value <= 15)
                    loadImages(page.value)  // Cargar la siguiente página
                else page.value = 1
            },
            onPrevPage = {
                if (page.value > 1) {  // Evitar que la página sea menor que 1
                    page.value -= 1
                    loadImages(page.value)  // Cargar la página anterior
                }else {
                    page.value =16
                    loadImages(page.value)

                }
            },
            onImageClick = { url ->
                // Aquí puedes manejar la URL de la imagen clicada, como mostrarla o hacer otra cosa
                Log.e("koll", url)
                onImageSelected(url)
                showDialog = false
            }


        )
    }


}


