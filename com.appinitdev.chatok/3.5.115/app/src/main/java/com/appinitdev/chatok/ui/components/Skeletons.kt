
@Composable
fun SkeletonChatList() {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "skeleton_anim"
    )

    val shimmerColors = listOf(
        Color.White.copy(alpha = 0.05f),
        Color.White.copy(alpha = 0.15f),
        Color.White.copy(alpha = 0.05f),
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(10) {
            SkeletonChatItem(brush)
        }
    }
}

@Composable
fun SkeletonChatItem(brush: Brush) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(brush)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Spacer(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
        }
    }
}

@Composable
fun SkeletonMessageList() {
    val transition = rememberInfiniteTransition(label = "skeleton_msg")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "skeleton_msg_anim"
    )

    val shimmerColors = listOf(
        Color.White.copy(alpha = 0.05f),
        Color.White.copy(alpha = 0.15f),
        Color.White.copy(alpha = 0.05f),
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        reverseLayout = true,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(8) { index ->
            val alignment = if (index % 2 == 0) Alignment.End else Alignment.Start
            SkeletonMessageItem(brush, alignment)
        }
    }
}

@Composable
fun SkeletonMessageItem(brush: Brush, alignment: Alignment.Horizontal) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Spacer(
            modifier = Modifier
                .width(if (alignment == Alignment.End) 200.dp else 150.dp)
                .height(60.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(brush)
        )
    }
}
