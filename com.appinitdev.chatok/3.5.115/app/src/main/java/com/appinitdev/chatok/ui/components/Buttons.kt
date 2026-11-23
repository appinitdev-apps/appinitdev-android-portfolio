
@Composable
fun MainGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    val brush = Brush.linearGradient(listOf(Color(0xFF238CDD), Color(0xFF1952C4)))
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "scale")

    Surface(
        onClick = onClick,
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .shadow(
                elevation = if (isPressed) 4.dp else 12.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color(0xFF238CDD),
                spotColor = Color(0xFF238CDD)
            ),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        enabled = enabled && !isLoading,
        interactionSource = interactionSource
    ) {
        Box(
            modifier = Modifier
                .background(if (enabled) brush else Brush.linearGradient(listOf(Color.Gray, Color.DarkGray)))
                .padding(horizontal = 24.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = text,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentColor: Color = Color.LightGray
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "scale")

    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .height(56.dp),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.25f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = contentColor.copy(alpha = 0.05f),
            contentColor = contentColor
        ),
        enabled = enabled,
        interactionSource = interactionSource
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun MainFAB(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GradientIconButton(
        icon = icon,
        onClick = onClick,
        modifier = modifier.size(60.dp),
        iconSize = 28.dp,
        elevation = 16.dp
    )
}

@Composable
fun SmallGradientIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GradientIconButton(
        icon = icon,
        onClick = onClick,
        modifier = modifier.size(46.dp),
        iconSize = 22.dp,
        elevation = 8.dp
    )
}

@Composable
private fun GradientIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: androidx.compose.ui.unit.Dp = 24.dp,
    elevation: androidx.compose.ui.unit.Dp = 8.dp
) {
    val brush = Brush.linearGradient(listOf(Color(0xFF238CDD), Color(0xFF1952C4)))
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.9f else 1f, label = "scale")

    Surface(
        onClick = onClick,
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .shadow(
                elevation = if (isPressed) elevation / 2 else elevation,
                shape = CircleShape,
                ambientColor = Color(0xFF238CDD),
                spotColor = Color(0xFF238CDD)
            ),
        shape = CircleShape,
        color = Color.Transparent,
        interactionSource = interactionSource
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}
