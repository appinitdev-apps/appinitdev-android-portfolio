
@Composable
fun ImageGallery(images: List<Image>, onImageClick: (String) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // Dos columnas
        modifier = Modifier.fillMaxHeight(0.90f),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(images) { image ->
            AsyncImage(
                model = image.webformatURL,
                contentDescription = image.tags,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp).clickable {
                        // Aquí pasas la URL cuando se hace clic
                        onImageClick(image.webformatURL)
                    },

                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun ShowGalleryDialog(
    images: List<Image>,
    onDismissRequest: () -> Unit,
    onNextPage: () -> Unit,
    onPrevPage: () -> Unit,
    onImageClick: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f) // Ajusta el tamaño de la ventana emergente
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Mostrar las imágenes
                ImageGallery(images, onImageClick)

                // Espaciado para empujar los botones hacia el fondo
                Spacer(modifier = Modifier.weight(1f))

                // Fila de botones para navegar entre las páginas
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Botón para ir a la página anterior
                    Button(
                        onClick = onPrevPage,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp) // Espaciado entre los botones
                    ) {
                        Text("<")
                    }

                    // Botón para ir a la siguiente página
                    Button(
                        onClick = onNextPage,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp) // Espaciado entre los botones
                    ) {
                        Text(">")
                    }
                }
            }
        }
    }
}
