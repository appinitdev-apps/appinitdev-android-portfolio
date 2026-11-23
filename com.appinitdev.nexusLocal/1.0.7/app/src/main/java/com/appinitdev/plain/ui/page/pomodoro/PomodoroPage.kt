
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroPage(navController: NavHostController, pomodoroVM: PomodoroViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    if (pomodoroVM.showSettings.value) {
        PomodoroSettingsDialog(settings = pomodoroVM.settings.value, onSettingsChange = { newSettings ->
            scope.launch {
                pomodoroVM.settings.value = newSettings
                withIO { PomodoroSettingsPreference.putAsync(newSettings) }
                if (!pomodoroVM.isRunning.value) pomodoroVM.updateTimeForCurrentState()
                sendEvent(WebSocketEvent(EventType.POMODORO_SETTINGS_UPDATE, JsonHelper.jsonEncode(newSettings)))
            }
        }, onDismiss = { pomodoroVM.showSettings.value = false })
    }

    PScaffold(topBar = {
        PTopAppBar(navController = navController, title = "", actions = {
            PIconButton(icon = Res.drawable.settings, contentDescription = stringResource(Res.string.settings), tint = MaterialTheme.colorScheme.onSurface) { pomodoroVM.showSettings.value = true }
        })
    }) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(top = paddingValues.calculateTopPadding()).fillMaxSize().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Text(text = pomodoroVM.currentState.value.getText(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                }
                VerticalSpace(dp = 8.dp)
                Text(text = stringResource(Res.string.round_counter, pomodoroVM.currentRound.intValue, pomodoroVM.settings.value.pomodorosBeforeLongBreak),
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f))
                VerticalSpace(dp = 32.dp)
            }
            item {
                PomodoroTimerSection(pomodoroVM, scope)
                VerticalSpace(dp = 16.dp)
            }
            item {
                Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = stringResource(Res.string.today_completed), style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
                    VerticalSpace(dp = 16.dp)
                    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                        val completedCount = pomodoroVM.completedCount.intValue
                        val displayCount = if (completedCount <= 4) 4 else completedCount
                        repeat(displayCount) { index ->
                            Text(text = "\ud83c\udf45", fontSize = 24.sp,
                                color = if (index < completedCount) Color.Unspecified else Color.Gray.copy(alpha = 0.3f),
                                modifier = Modifier.padding(horizontal = 4.dp))
                        }
                    }
                    VerticalSpace(dp = 16.dp)
                    Text(text = pluralStringResource(Res.plurals.n_pomodoros, pomodoroVM.completedCount.intValue, pomodoroVM.completedCount.intValue),
                        style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
                BottomSpace(paddingValues)
            }
        }
    }
}
