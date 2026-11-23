@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundMeterPage(navController: NavHostController) {
    val context = LocalContext.current
    var decibelValuesDialogVisible = remember { mutableStateOf(false) }
    val audioRecord = remember { mutableStateOf<AudioRecord?>(null) }
    val total = remember { mutableFloatStateOf(0f) }
    val count = remember { mutableIntStateOf(0) }
    val min = remember { mutableFloatStateOf(0f) }
    val avg = remember { mutableFloatStateOf(0f) }
    val max = remember { mutableFloatStateOf(0f) }
    val isRunning = remember { mutableStateOf(false) }
    val decibel = remember { mutableFloatStateOf(0f) }
    val decibelValueStrings = stringArrayResource(Res.array.decibel_values)
    val decibelValueString by remember(decibel.floatValue) {
        derivedStateOf { if (decibel.floatValue > 0) decibelValueStrings.getOrNull((decibel.floatValue / 10).toInt() - 1) ?: "" else "" }
    }
    SoundMeterRecorder(audioRecord, isRunning, decibel, total, count, min, avg, max)
    PScaffold(topBar = {
        PTopAppBar(navController = navController,
            title = stringResource(Res.string.sound_meter), actions = {
                PIconButton(icon = Res.drawable.info, contentDescription = stringResource(Res.string.decibel_values),
                    tint = MaterialTheme.colorScheme.onSurface) { decibelValuesDialogVisible.value = true }
            })
    }, content = { paddingValues ->
        LazyColumn(modifier = Modifier.padding(top = paddingValues.calculateTopPadding())) {
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 56.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(text = FormatHelper.formatFloat(abs(decibel.floatValue), digits = 1),
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp, fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        HorizontalSpace(dp = 16.dp)
                        Text(modifier = Modifier.padding(bottom = 12.dp), text = "dB",
                            style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp, vertical = 24.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = stringResource(Res.string.min)); Text(text = FormatHelper.formatFloat(min.floatValue, digits = 1))
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = stringResource(Res.string.avg)); Text(text = FormatHelper.formatFloat(avg.floatValue, digits = 1))
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = stringResource(Res.string.max)); Text(text = FormatHelper.formatFloat(max.floatValue, digits = 1))
                    }
                }
                Text(text = decibelValueString, color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth().height(96.dp).padding(16.dp), textAlign = TextAlign.Center)
                if (isRunning.value) {
                    PFilledButton(text = stringResource(Res.string.stop), modifier = Modifier.padding(horizontal = 16.dp), onClick = { isRunning.value = false })
                } else {
                    PFilledButton(text = stringResource(Res.string.start), modifier = Modifier.padding(horizontal = 16.dp), onClick = {
                        if (Permission.RECORD_AUDIO.can(context)) isRunning.value = true
                        else sendEvent(RequestPermissionsEvent(Permission.RECORD_AUDIO))
                    })
                }
                if (count.intValue > 0) {
                    VerticalSpace(dp = 40.dp)
                    PFilledButton(text = stringResource(Res.string.reset), type = ButtonType.DANGER, modifier = Modifier.padding(horizontal = 16.dp), onClick = {
                        total.floatValue = 0f; count.intValue = 0; decibel.floatValue = 0f
                        min.floatValue = 0f; max.floatValue = 0f; avg.floatValue = 0f
                    })
                }
                BottomSpace(paddingValues)
            }
        }
    })
    if (decibelValuesDialogVisible.value) {
        AlertDialog(containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { decibelValuesDialogVisible.value = false },
            confirmButton = { Button(onClick = { decibelValuesDialogVisible.value = false }) { Text(stringResource(Res.string.close)) } },
            title = { Text(text = stringResource(Res.string.decibel_values), style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    decibelValueStrings.forEach {
                        SelectionContainer { Text(text = it, style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface), modifier = Modifier.padding(bottom = 8.dp)) }
                    }
                }
            })
    }
}
