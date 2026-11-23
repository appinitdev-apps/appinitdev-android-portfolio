@Composable
fun PFilledButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    type: ButtonType = ButtonType.PRIMARY,
    buttonSize: ButtonSize = ButtonSize.MEDIUM,
    isLoading: Boolean = false,
    enabled: Boolean = true,
) {
    val containerColor = when (type) {
        ButtonType.PRIMARY -> MaterialTheme.colorScheme.primary
        ButtonType.DANGER -> MaterialTheme.colorScheme.red
        ButtonType.TERTIARY -> MaterialTheme.colorScheme.tertiary
    }
    val contentColor = when (type) {
        ButtonType.PRIMARY -> MaterialTheme.colorScheme.onPrimary
        ButtonType.DANGER -> Color.White
        ButtonType.TERTIARY -> MaterialTheme.colorScheme.onTertiary
    }
    val padding = if (buttonSize == ButtonSize.SMALL) PaddingValues(horizontal = 12.dp) else ButtonDefaults.ContentPadding
    Button(
        onClick = onClick,
        modifier = modifier
            .then(if (buttonSize == ButtonSize.SMALL) Modifier else Modifier.fillMaxWidth())
            .height(buttonSize.height),
        shape = RoundedCornerShape(buttonSize.cornerRadius),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 2.dp,
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = if (isLoading) containerColor.copy(alpha = 0.8f) else containerColor.copy(alpha = 0.12f),
            disabledContentColor = if (isLoading) contentColor else contentColor.copy(alpha = 0.38f),
        ),
        contentPadding = padding,
        enabled = enabled && !isLoading,
    ) {
        AnimatedContent(
            targetState = isLoading,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
            label = "filled_btn_${type.name.lowercase()}",
        ) { loading ->
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(if (buttonSize == ButtonSize.SMALL) 18.dp else 24.dp),
                    strokeWidth = if (buttonSize == ButtonSize.SMALL) 2.dp else 3.dp,
                    color = contentColor,
                )
            } else {
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    if (icon != null) {
                        Icon(
                            painter = icon,
                            contentDescription = null,
                            modifier = Modifier.size(if (buttonSize == ButtonSize.SMALL) 16.dp else 20.dp),
                            tint = contentColor,
                        )
                        HorizontalSpace(8.dp)
                    }
                    Text(
                        text = text,
                        style = when (buttonSize) {
                            ButtonSize.SMALL -> MaterialTheme.typography.labelMedium
                            ButtonSize.MEDIUM -> MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            ButtonSize.LARGE -> MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        },
                    )
                }
            }
        }
    }
}
