
@Composable
fun ImageGallery(images: List<Image>, onImageClick: (String) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), 
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
                .fillMaxHeight(0.9f) 
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                
                ImageGallery(images, onImageClick)

                
                Spacer(modifier = Modifier.weight(1f))

                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    
                    Button(
                        onClick = onPrevPage,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp) 
                    ) {
                        Text("<")
                    }

                    
                    Button(
                        onClick = onNextPage,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp) 
                    ) {
                        Text(">")
                    }
                }
            }
        }
    }
}
