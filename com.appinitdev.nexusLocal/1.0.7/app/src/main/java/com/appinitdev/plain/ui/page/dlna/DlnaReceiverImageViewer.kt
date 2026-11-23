
@Composable
fun DlnaReceiverImageViewer(onExit: () -> Unit) {
    val context = LocalContext.current
    val mediaUri by DlnaRendererState.mediaUri.collectAsState()
    val mediaTitle by DlnaRendererState.mediaTitle.collectAsState()

    var showControls by remember { mutableStateOf(true) }
    var isLoading by remember(mediaUri) { mutableStateOf(true) }
    val interactionSource = remember { MutableInteractionSource() }

    DisposableEffect(Unit) {
        context.findActivity().setFullScreen(true)
        onDispose { context.findActivity().setFullScreen(false) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(indication = null, interactionSource = interactionSource) {
                showControls = !showControls
            },
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context).data(mediaUri).build(),
            contentDescription = mediaTitle,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
            onLoading = { isLoading = true },
            onSuccess = { isLoading = false },
            onError = { isLoading = false },
        )

        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(48.dp),
            )
        }

        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopStart),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .statusBarsPadding()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onExit) {
                    Icon(
                        painter = painterResource(Res.drawable.arrow_left),
                        contentDescription = stringResource(Res.string.dlna_receiver_exit_player),
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Text(
                    text = mediaTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                )
            }
        }
    }
}

