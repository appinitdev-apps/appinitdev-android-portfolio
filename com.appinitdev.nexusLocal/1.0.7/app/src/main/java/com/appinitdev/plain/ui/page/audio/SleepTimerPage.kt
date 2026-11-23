
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTimerPage(onDismissRequest: () -> Unit) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var timerActive by remember { mutableStateOf(false) }
    var remainingTimeMs by remember { mutableLongStateOf(0L) }
    var selectedTimeMinutes by remember { mutableIntStateOf(15) }
    var timerJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(Unit) {
        val futureTime = TempData.audioSleepTimerFutureTime
        if (futureTime > 0) {
            timerActive = true
            val remaining = futureTime - SystemClock.elapsedRealtime()
            if (remaining > 0) remainingTimeMs = remaining
        }
        sheetState.expand()
    }

    LaunchedEffect(timerActive) {
        if (timerActive && remainingTimeMs > 0) {
            timerJob?.cancel()
            timerJob = scope.launch {
                while (true) {
                    delay(1000)
                    remainingTimeMs = maxOf(0, TempData.audioSleepTimerFutureTime - SystemClock.elapsedRealtime())
                    if (remainingTimeMs <= 0) { timerActive = false; break }
                }
            }
        }
    }

    PModalBottomSheet(onDismissRequest = onDismissRequest, sheetState = sheetState) {
        Box(modifier = Modifier.fillMaxWidth().height(500.dp).padding(bottom = 32.dp)) {
            Column(modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Top) {
                Text(text = stringResource(Res.string.sleep_timer),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(vertical = 16.dp))
                Spacer(modifier = Modifier.height(8.dp))

                if (timerActive) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().weight(1f)) {
                        SleepTimerActiveContent(remainingTimeMs, selectedTimeMinutes)
                        Spacer(modifier = Modifier.weight(1f))
                        PFilledButton(text = stringResource(Res.string.cancel_timer), onClick = {
                            scope.launch {
                                withIO { TempData.audioSleepTimerFutureTime = 0 }
                                timerJob?.cancel(); timerActive = false; remainingTimeMs = 0
                                sendEvent(CancelSleepTimerEvent())
                            }
                        }, type = ButtonType.DANGER)
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().weight(1f)) {
                        SleepTimerSelectionContent(selectedTimeMinutes) { selectedTimeMinutes = it }
                        Spacer(modifier = Modifier.weight(1f))
                        PFilledButton(text = stringResource(Res.string.start_timer), onClick = {
                            scope.launch {
                                withIO {
                                    val durationMs = selectedTimeMinutes * 60 * 1000L
                                    TempData.audioSleepTimerFutureTime = SystemClock.elapsedRealtime() + durationMs
                                    remainingTimeMs = durationMs
                                }
                                timerActive = true
                                sendEvent(SleepTimerEvent(selectedTimeMinutes * 60 * 1000L))
                            }
                        })
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}
