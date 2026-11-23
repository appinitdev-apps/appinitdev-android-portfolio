
@Composable
fun VideoMetaRows(path: String) {

    var meta by remember {
        mutableStateOf<DVideoMeta?>(null)
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            meta = VideoHelper.getMeta(path)
        }
    }

    meta?.let { mt ->
        mt.takenAt?.let { takenAt ->
            PListItem(title = stringResource(Res.string.taken_at), value = takenAt.formatDateTime())
        }
        if (mt.title.isNotEmpty()) {
            PListItem(title = stringResource(Res.string.title), value = mt.title)
        }
        PListItem(title = stringResource(Res.string.duration), value = mt.duration.formatDuration())
        PListItem(title = stringResource(Res.string.bitrate), value =  mt.bitrate.formatBitrate())
    }
}