enum class AlertType {
    WARNING,
    ERROR,
}
@Composable
fun PAlert(
    description: String,
    type: AlertType,
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    val accentColor = if (type == AlertType.WARNING) MaterialTheme.colorScheme.orange else MaterialTheme.colorScheme.red
    val containerColor = if (type == AlertType.WARNING) {
        MaterialTheme.colorScheme.orange.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.red.copy(alpha = 0.12f)
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    .background(accentColor),
            )
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (actions != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        actions.invoke(this)
                    }
                }
            }
        }
    }
    VerticalSpace(dp = 12.dp)
}
