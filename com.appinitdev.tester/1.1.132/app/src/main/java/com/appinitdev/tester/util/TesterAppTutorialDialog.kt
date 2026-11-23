data class TutorialPage(
    val icon: ImageVector,
    val titleRes: Int,       
    val descriptionRes: Int  
)
val tutorialPages = listOf(
    TutorialPage(
        Icons.Default.VerifiedUser,
        R.string.regla_1_titulo,
        R.string.regla_1_desc
    ),
    TutorialPage(
        Icons.Default.Group,
        R.string.regla_2_titulo,
        R.string.regla_2_desc
    ),
    TutorialPage(
        Icons.Default.EmojiEvents,
        R.string.regla_3_titulo,
        R.string.regla_3_desc
    ),
    TutorialPage(
        Icons.Default.TrendingUp,
        R.string.regla_4_titulo,
        R.string.regla_4_desc
    )
)
@Composable
fun TesterAppTutorialDialog(
    onDismiss: () -> Unit,
    onSkip: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { tutorialPages.size })
    val coroutineScope = rememberCoroutineScope()
    Dialog(
        onDismissRequest = onSkip,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f) 
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.gu_a_de_la_app),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    TextButton(onClick = onSkip) {
                        Text(stringResource(R.string.saltar), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    val item = tutorialPages[page]
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(100.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(50.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = stringResource(id = item.titleRes),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(id = item.descriptionRes),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 24.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(tutorialPages.size) { iteration ->
                            val isSelected = pagerState.currentPage == iteration
                            val color by animateColorAsState(
                                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                label = "color_anim"
                            )
                            val width by animateDpAsState(
                                targetValue = if (isSelected) 24.dp else 8.dp,
                                label = "width_anim"
                            )
                            Box(
                                modifier = Modifier
                                    .size(width = width, height = 8.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                        }
                    }
                    Button(
                        onClick = {
                            if (pagerState.currentPage < tutorialPages.size - 1) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            } else {
                                onDismiss()
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (pagerState.currentPage == tutorialPages.size - 1) stringResource(R.string.empezar) else stringResource(
                            R.string.siguiente
                        ))
                    }
                }
            }
        }
    }
}