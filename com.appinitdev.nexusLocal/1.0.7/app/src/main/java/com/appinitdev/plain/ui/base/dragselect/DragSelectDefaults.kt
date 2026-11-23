object DragSelectDefaults {
    val autoScrollThreshold = 100f
    @Composable
    fun defaultIndication() = LocalIndication.current
    @Composable
    fun defaultHapticFeedback(): HapticFeedback = LocalHapticFeedback.current
} 