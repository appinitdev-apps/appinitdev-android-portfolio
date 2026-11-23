@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun View(imageUrl: String, hideDialog: () ->Unit) {
    Column(modifier = Modifier
        .fillMaxSize()
        .blur(15.dp)){
        Column {
        }
    }
    Dialog(onDismissRequest = hideDialog,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    )       {
        var scale by remember {
            mutableStateOf(1f)
        }
        var offset by remember {
            mutableStateOf(Offset.Zero)
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
                model = imageUrl,
                contentDescription = "Profile picture",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    }
                    .transformable(state)
            )
        }
    }
}
