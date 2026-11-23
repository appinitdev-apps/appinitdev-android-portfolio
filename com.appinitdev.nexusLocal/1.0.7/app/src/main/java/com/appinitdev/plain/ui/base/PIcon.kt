
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PIcon(
    modifier: Modifier = Modifier,
    icon: Any,
    contentDescription: String?,
    tint: Color = LocalContentColor.current,
) {
    if (icon is ImageVector) {
        Icon(
            modifier = modifier,
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
        )
    } else if (icon is Painter) {
        Icon(
            modifier = modifier,
            painter = icon,
            contentDescription = contentDescription,
            tint = tint,
        )
    }
}
