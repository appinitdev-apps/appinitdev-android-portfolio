
@Composable
fun VideoPreviewActions(context: Context, castViewModel: CastViewModel, m: PreviewItem, state: MediaPreviewerState) {
    val videoState = state.videoState
    if (!state.showActions || videoState.enablePip || videoState.isFullscreenMode) return
    val scope = rememberCoroutineScope()
    CastDialog(castViewModel)
    LaunchedEffect(Unit) { while (true) { scope.launch { state.videoState.updateTime() }; delay(1.seconds) } }

    Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 32.dp).navigationBarsPadding().alpha(state.uiAlpha.value)) {
        Column(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.End) {
            VideoButtons1(context, videoState)
            VideoButtons2(videoState, scope)
            if (castViewModel.castMode.value) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.align(Alignment.BottomCenter).clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.darkMask()).padding(horizontal = 20.dp, vertical = 8.dp)) {
                        POutlinedButton(text = stringResource(Res.string.cast), buttonSize = ButtonSize.SMALL, onClick = { castViewModel.cast(m.path) })
                        HorizontalSpace(dp = 20.dp)
                        POutlinedButton(text = stringResource(Res.string.exit_cast_mode), buttonSize = ButtonSize.SMALL, contentColor = Color.LightGray, onClick = { castViewModel.exitCastMode() })
                    }
                }
                return
            }
            Row(modifier = Modifier.clip(RoundedCornerShape(50)).align(Alignment.CenterHorizontally).background(MaterialTheme.colorScheme.darkMask()).padding(horizontal = 20.dp, vertical = 8.dp)) {
                ActionIconButton(icon = Res.drawable.share_2, contentDescription = stringResource(Res.string.share)) {
                    if (m.mediaId.isNotEmpty()) { ShareHelper.shareUris(context, listOf(VideoMediaStoreHelper.getItemUri(m.mediaId))) }
                    else if (m.path.isUrl()) { scope.launch { val tempFile = File.createTempFile("videoPreviewShare", "." + m.path.getFilenameExtension(), File(context.cacheDir, "/video_cache")); DialogHelper.showLoading(); val r = withIO { DownloadHelper.downloadToTempAsync(m.path, tempFile) }; DialogHelper.hideLoading(); if (r.success) ShareHelper.shareFile(context, File(r.path), m.getMimeType().ifEmpty { "video/*" }) else DialogHelper.showMessage(r.message) } }
                    else ShareHelper.shareFile(context, File(m.path), m.getMimeType().ifEmpty { "video/*" })
                }
                HorizontalSpace(dp = 20.dp)
                ActionIconButton(icon = Res.drawable.cast, contentDescription = stringResource(Res.string.cast)) { castViewModel.showCastDialog.value = true }
                if (m.data !is DVideo && m.data !is DFile) {
                    HorizontalSpace(dp = 20.dp)
                    ActionIconButton(icon = Res.drawable.save, contentDescription = stringResource(Res.string.save)) {
                        scope.launch {
                            if (m.path.isUrl()) { DialogHelper.showLoading(); val dir = PathHelper.getPlainPublicDir(Environment.DIRECTORY_MOVIES); val r = withIO { DownloadHelper.downloadAsync(m.path, dir.absolutePath) }; DialogHelper.hideLoading(); if (r.success) DialogHelper.showMessage(LocaleHelper.getStringFAsync(Res.string.video_save_to, "path", r.path)) else DialogHelper.showMessage(r.message) }
                            else { val newName = (m.data as? DMessageFile)?.fileName?.takeIf { it.isNotEmpty() } ?: ""; val r = withIO { FileHelper.copyFileToPublicDir(m.path, Environment.DIRECTORY_MOVIES, newName = newName) }; if (r.isNotEmpty()) DialogHelper.showMessage(LocaleHelper.getStringFAsync(Res.string.video_save_to, "path", r)) else DialogHelper.showMessage(LocaleHelper.getStringAsync(Res.string.video_save_to_failed)) }
                        }
                    }
                }
                HorizontalSpace(dp = 20.dp)
                ActionIconButton(icon = Res.drawable.ellipsis, contentDescription = stringResource(Res.string.more_info)) { state.showMediaInfo = true }
            }
        }
    }
}

@Composable
fun VideoButtons2(videoState: VideoState, scope: CoroutineScope) {
    val sliderProgress = if (videoState.totalTime <= 0L) 0f else (videoState.currentTime.toFloat() / videoState.totalTime.toFloat()).coerceIn(0f, 1f)
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
        IconButton(modifier = Modifier.size(40.dp), onClick = { videoState.togglePlay() }) {
            Image(modifier = Modifier.size(32.dp), painter = painterResource(if (videoState.isPlaying) Res.drawable.pause else Res.drawable.play_arrow), colorFilter = ColorFilter.tint(Color.White), contentDescription = stringResource(if (videoState.isPlaying) Res.string.pause else Res.string.play))
        }
        Text(modifier = Modifier.width(52.dp), text = videoState.currentTime.formatMinSec(), fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium, color = Color.White, textAlign = TextAlign.Center)
        Box(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
            PlayerSlider(modifier = Modifier.fillMaxWidth().height(20.dp), progress = sliderProgress, bufferedProgress = videoState.bufferedPercentage / 100f, onProgressChange = { videoState.seekTo((it * videoState.totalTime).toLong()) })
        }
        Text(modifier = Modifier.width(52.dp), text = videoState.totalTime.formatMinSec(), fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium, color = Color.White, textAlign = TextAlign.Center)
        IconButton(modifier = Modifier.size(40.dp), onClick = { videoState.isFullscreenMode = !videoState.isFullscreenMode }) {
            Icon(painter = painterResource(Res.drawable.maximize), tint = Color.White, contentDescription = stringResource(Res.string.fullscreen))
        }
    }
}
