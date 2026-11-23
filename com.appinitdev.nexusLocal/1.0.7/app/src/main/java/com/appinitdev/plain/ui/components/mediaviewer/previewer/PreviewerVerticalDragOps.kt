
 


internal suspend fun PreviewerVerticalDragState.viewerContainerShrinkDown() {
    stateCloseStart()
    viewerContainerState?.cancelOpenTransform()
    listOf(
        scope.async { viewerContainerState?.transformContentAlpha?.snapTo(0F) },
        scope.async { uiAlpha.animateTo(0F, DEFAULT_SOFT_ANIMATION_SPEC) },
        scope.async { animateContainerVisibleState = MutableTransitionState(false) }
    ).awaitAll()
    ticket.awaitNextTicket()
    stateCloseEnd()
    transformState?.setExitState()
}

 


internal suspend fun PreviewerVerticalDragState.dragDownClose() {
    transformState?.notifyEnterChanged()
    viewerContainerState?.showLoading = false
    ticket.awaitNextTicket()
    viewerContainerState?.copyViewerContainerStateToTransformState()
    viewerContainerState?.resetImmediately()
    transformSnapToViewer(false)
    ticket.awaitNextTicket()
    closeTransform()
    viewerContainerState?.showLoading = true
}
