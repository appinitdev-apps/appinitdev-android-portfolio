@OptIn(ExperimentalFoundationApi::class)
class MediaPreviewerState(
    scope: CoroutineScope = MainScope(),
    pagerState: PagerState,
) : PreviewerVerticalDragState(scope, pagerState = pagerState) {
    val videoState = VideoState()
    companion object {
        fun getSaver(pagerState: PagerState): Saver<MediaPreviewerState, *> {
            return mapSaver(
                save = {
                    mapOf<String, Any>(
                        it.pagerState::currentPage.name to it.pagerState.currentPage,
                        it::animateContainerVisibleState.name to it.animateContainerVisibleState.currentState,
                        it::uiAlpha.name to it.uiAlpha.value,
                        it::visible.name to it.visible,
                    )
                },
                restore = {
                    val previewerState = MediaPreviewerState(pagerState = pagerState)
                    previewerState.animateContainerVisibleState =
                        MutableTransitionState(it[MediaPreviewerState::animateContainerVisibleState.name] as Boolean)
                    previewerState.uiAlpha = Animatable(it[MediaPreviewerState::uiAlpha.name] as Float)
                    previewerState.visible = it[MediaPreviewerState::visible.name] as Boolean
                    previewerState
                }
            )
        }
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun rememberPreviewerState(
    scope: CoroutineScope = rememberCoroutineScope(),
    verticalDragType: VerticalDragType = VerticalDragType.UpAndDown,
    @IntRange(from = 0) initialPage: Int = 0,
    pageCount: () -> Int = { MediaPreviewData.items.size },
    getKey: (Int) -> Any = { index ->
        MediaPreviewData.items.getOrNull(index)?.id ?: index
    },
): MediaPreviewerState {
    val pagerState = rememberPagerState(initialPage, pageCount = pageCount)
    val mediaPreviewerState = rememberSaveable(saver = MediaPreviewerState.getSaver(pagerState)) {
        MediaPreviewerState(pagerState = pagerState)
    }
    mediaPreviewerState.scope = scope
    mediaPreviewerState.getKey = getKey
    mediaPreviewerState.verticalDragType = verticalDragType
    return mediaPreviewerState
}