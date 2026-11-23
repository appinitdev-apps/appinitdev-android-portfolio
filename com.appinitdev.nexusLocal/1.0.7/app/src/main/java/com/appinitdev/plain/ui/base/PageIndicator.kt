
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PageIndicator(pagerState: PagerState, color: Color = MaterialTheme.colorScheme.primary) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(pagerState.pageCount) { index ->
            Box(
                modifier = Modifier
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(if (pagerState.currentPage == index) color else color.copy(alpha = 0.4F))
                    .size(6.dp)
            )
        }
    }
}