@Composable
internal fun SelectedOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.lightMask())
            .aspectRatio(1f)
    )
}
@Composable
internal fun CastModeOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.darkMask())
            .aspectRatio(1f)
    ) {
        Icon(
            modifier = Modifier
                .align(Alignment.Center)
                .size(48.dp),
            painter = painterResource(Res.drawable.cast),
            contentDescription = null,
            tint = Color.LightGray
        )
    }
}
@Composable
internal fun SelectionCheckbox(
    selected: Boolean,
    id: String,
    dragSelectState: DragSelectState,
) {
    Checkbox(
        modifier = Modifier,
        checked = selected,
        onCheckedChange = {
            dragSelectState.select(id)
        },
    )
}
@Composable
internal fun SizeLabel(text: String) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.darkMask()),
    ) {
        Text(
            modifier = Modifier
                .padding(horizontal = 4.dp, vertical = 2.dp),
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Normal),
        )
    }
}
