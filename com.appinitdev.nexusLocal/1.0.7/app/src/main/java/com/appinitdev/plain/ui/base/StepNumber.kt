

@Composable
fun StepNumber(num: Int) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    Box(
        modifier = Modifier
            .size(28.dp)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(28.dp)) {
            drawCircle(primaryColor, radius = size.minDimension / 2)
        }
        Text(
            text = num.toString(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = onPrimaryColor
        )
    }
}