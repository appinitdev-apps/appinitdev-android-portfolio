@Composable
fun POutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    type: ButtonType = ButtonType.PRIMARY,
    buttonSize: ButtonSize = ButtonSize.MEDIUM,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    block: Boolean = false,
    contentColor: Color? = null,
) {
    val resolvedColor = contentColor ?: when (type) {
        ButtonType.PRIMARY -> MaterialTheme.colorScheme.primary
        ButtonType.TERTIARY -> MaterialTheme.colorScheme.tertiary
        ButtonType.DANGER -> MaterialTheme.colorScheme.red
    }
    val borderColor = resolvedColor.copy(alpha = 0.5f)
    val padding = if (buttonSize == ButtonSize.SMALL) PaddingValues(horizontal = 12.dp) else ButtonDefaults.ContentPadding
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .then(if (block || buttonSize != ButtonSize.SMALL) Modifier.fillMaxWidth() else Modifier)
            .height(buttonSize.height),
        shape = RoundedCornerShape(buttonSize.cornerRadius),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = resolvedColor),
        border = BorderStroke(1.dp, borderColor),
        contentPadding = padding,
        enabled = enabled,
    ) {
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(if (buttonSize == ButtonSize.SMALL) 14.dp else 16.dp), strokeWidth = 2.dp, color = resolvedColor)
                HorizontalSpace(8.dp)
            } else if (icon != null) {
                Icon(painter = icon, contentDescription = null, modifier = Modifier.size(if (buttonSize == ButtonSize.SMALL) 16.dp else 20.dp), tint = resolvedColor)
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