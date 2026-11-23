@Composable
fun ImagePreviewActions(
    context: Context, castViewModel: CastViewModel,
    m: PreviewItem, state: MediaPreviewerState,
) {
    val scope = rememberCoroutineScope()
    CastDialog(castViewModel)
    Box(
        modifier = Modifier.fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 32.dp)
            .navigationBarsPadding()
            .alpha(state.uiAlpha.value),
    ) {
        if (!state.showActions) return
        if (castViewModel.castMode.value) {
            Row(
                modifier = Modifier.align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.darkMask())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
            ) {
                POutlinedButton(text = stringResource(Res.string.cast), buttonSize = ButtonSize.SMALL, onClick = { castViewModel.cast(m.path) })
                HorizontalSpace(dp = 20.dp)
                POutlinedButton(text = stringResource(Res.string.exit_cast_mode), buttonSize = ButtonSize.SMALL, contentColor = Color.LightGray, onClick = { castViewModel.exitCastMode() })
            }
            return
        }
        Row(
            modifier = Modifier.align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.darkMask())
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            ActionIconButton(icon = Res.drawable.share_2, contentDescription = stringResource(Res.string.share)) {
                scope.launch { sharePreviewImage(context, m) }
            }
            HorizontalSpace(dp = 20.dp)
            ActionIconButton(icon = Res.drawable.cast, contentDescription = stringResource(Res.string.cast)) {
                castViewModel.showCastDialog.value = true
            }
            HorizontalSpace(dp = 20.dp)
            ActionIconButton(icon = Res.drawable.rotate_cw_square, contentDescription = stringResource(Res.string.rotate)) {
                scope.launch {
                    state.viewerContainerState?.viewerState?.let {
                        it.rotation.animateTo(it.rotation.value + 90, SpringSpec())
                    }
                }
            }
            if (m.data !is DImage && m.data !is DFile) {
                HorizontalSpace(dp = 20.dp)
                ActionIconButton(icon = Res.drawable.save, contentDescription = stringResource(Res.string.save)) {
                    scope.launch { savePreviewImage(context, m) }
                }
            }
            HorizontalSpace(dp = 20.dp)
            ActionIconButton(icon = Res.drawable.ellipsis, contentDescription = stringResource(Res.string.more_info)) {
                state.showMediaInfo = true
            }
        }
    }
}
@Composable
fun ActionIconButton(icon: DrawableResource, contentDescription: String, click: () -> Unit) {
    Box(
        modifier = Modifier.size(32.dp).clip(CircleShape)
            .background(MaterialTheme.colorScheme.lightMask())
            .clickable { click() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(18.dp),
            painter = painterResource(icon),
            contentDescription = contentDescription,
            tint = Color.White,
        )
    }
}
