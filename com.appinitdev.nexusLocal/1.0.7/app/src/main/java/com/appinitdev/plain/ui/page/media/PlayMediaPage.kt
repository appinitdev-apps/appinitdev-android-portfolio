
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayMediaPage(
    navController: NavHostController,
    path: String,
    audioPlaylistVM: AudioPlaylistViewModel,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    if (path.isAudioFast()) {
        LaunchedEffect(path) {
            Permissions.checkNotification(context, Res.string.audio_notification_prompt) {
                coMain {
                    val audio = withIO { DPlaylistAudio.fromPath(context, path) }
                    withIO {
                        audioPlaylistVM.playlistItems.value = listOf(audio)
                        audioPlaylistVM.selectedPath.value = path
                    }
                    AudioPlayer.play(context, audio)
                    navController.navigate(Routing.Audio) {
                        popUpTo(Routing.PlayMedia(path)) { inclusive = true }
                    }
                }
            }
        }
        Box(modifier = Modifier.fillMaxSize().background(Color.Black))
        return
    }

    MediaPreviewData.items = listOf(PreviewItem(id = path, path = path))

    val previewerState = rememberPreviewerState(
        scope = scope,
        pageCount = { MediaPreviewData.items.size },
    )

    var hasOpened by remember { mutableStateOf(false) }

    LaunchedEffect(previewerState.visible) {
        if (previewerState.visible) {
            hasOpened = true
        } else if (hasOpened && !previewerState.animating) {
            navController.navigateUp()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        LaunchedEffect(Unit) {
            previewerState.open(0)
        }
        MediaPreviewer(state = previewerState)
    }
}
