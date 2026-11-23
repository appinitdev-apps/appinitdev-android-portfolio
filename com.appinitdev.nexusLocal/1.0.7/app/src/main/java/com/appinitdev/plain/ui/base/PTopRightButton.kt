@Composable
fun PTopRightButton(
    label: String,
    click: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = { click() },
        enabled = enabled,
        modifier = modifier.padding(horizontal = 8.dp),
    ) {
        Text(
            text = label,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
