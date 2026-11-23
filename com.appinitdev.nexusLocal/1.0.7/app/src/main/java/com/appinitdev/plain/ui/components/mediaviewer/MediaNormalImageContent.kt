
@Composable
fun MediaNormalImageContent(
    imageModifier: Modifier,
    painter: Painter?,
    model: PreviewItem,
    uSize: IntSize,
) {
    if (painter != null) {
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = imageModifier.size(
                LocalDensity.current.run { uSize.width.toDp() },
                LocalDensity.current.run { uSize.height.toDp() }
            ),
        )
    } else {
        if (model.path.endsWith(".svg", true)) {
            AsyncImage(
                model = model.path,
                contentDescription = model.path,
                contentScale = ContentScale.Fit,
                modifier = imageModifier
                    .background(Color.White)
                    .size(
                        LocalDensity.current.run { uSize.width.toDp() },
                        LocalDensity.current.run { uSize.height.toDp() }
                    ),
            )
        } else {
            AsyncImage(
                model = model.path,
                contentDescription = model.path,
                contentScale = ContentScale.Fit,
                modifier = imageModifier.fillMaxSize(),
            )
        }
    }
}
