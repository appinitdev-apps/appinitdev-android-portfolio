
enum class ToastType {
    INFO,
    SUCCESS,
    WARNING,
    ERROR
}

@Composable
fun PToast(
    message: String,
    type: ToastType = ToastType.INFO,
    onDismiss: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 32.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it },
            exit = fadeOut(tween(200)) + slideOutVertically(tween(200)) { it }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        onDismiss()
                    },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (DarkTheme.isDarkTheme(LocalDarkTheme.current)) Color(0xCC3A3A3C) else Color(0xCC1C1C1E)
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val iconRes = when (type) {
                        ToastType.INFO -> Res.drawable.info
                        ToastType.SUCCESS -> Res.drawable.circle_check
                        ToastType.WARNING -> Res.drawable.circle_alert
                        ToastType.ERROR -> Res.drawable.circle_x
                    }
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(20.dp)
                    )
                    Text(
                        text = message,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
} 