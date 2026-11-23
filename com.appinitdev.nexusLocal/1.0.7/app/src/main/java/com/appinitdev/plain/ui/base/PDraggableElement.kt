
@Composable
fun PDraggableElement(content: @Composable BoxScope.() -> Unit) {
    val offset = remember { mutableStateOf(IntOffset.Zero) }
    Box(
        modifier = Modifier
            .offset { offset.value }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val offsetChange = IntOffset(dragAmount.x.roundToInt(), dragAmount.y.roundToInt())
                    offset.value = offset.value.plus(offsetChange)
                }
            },
        content = content
    )
}

