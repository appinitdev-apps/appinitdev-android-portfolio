@Composable
fun <T> StateFlow<T>.collectAsStateValue(context: CoroutineContext = Dispatchers.Default): T = collectAsState(context).value
@Composable
fun <T : R, R> Flow<T>.collectAsStateValue(
    initial: R,
    context: CoroutineContext = Dispatchers.Default,
): R = collectAsState(initial, context).value
