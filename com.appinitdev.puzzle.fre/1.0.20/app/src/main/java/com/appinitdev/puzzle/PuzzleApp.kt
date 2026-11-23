
data class Piece(
    val id: Int,
    val bitmap: Bitmap? = null,
    val isBlank: Boolean = false,
    val rotationDegrees: Float = 0f
)

@SuppressLint("SuspiciousIndentation")
@Composable
fun PuzzleApp(
    originalBitmap: Bitmap,
    puzzleId: Int,
    statsHistory: List<PuzzleStatEntity> = emptyList(),
    onVictory: (moves: Int, time: Int, n: Int) -> Unit,
    onCheckProgress: suspend (n: Int) -> PuzzleProgressEntity?,
    onSaveProgress: (n: Int, moves: Int, time: Int, pieces: List<Piece>) -> Unit,
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val appSettings = remember { loadAppSettings(context) }
    val skinStyle = remember(appSettings.boardSkin) { getBoardSkinStyle(appSettings.boardSkin) }

    var n by remember { mutableIntStateOf(3) }
    var showBackgroundHint by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    var isSolved by remember { mutableStateOf(false) }
    var isTimeOut by remember { mutableStateOf(false) }
    var isActive by remember { mutableStateOf(false) }
    var moveCount by remember { mutableIntStateOf(0) }
    var timeSeconds by remember { mutableIntStateOf(0) }
    var isAutoSolving by remember { mutableStateOf(false) }
    var hintPieceId by remember { mutableStateOf<Int?>(null) }
    val coroutineScope = rememberCoroutineScope()
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showAchievementsDialog by remember { mutableStateOf(false) }
    var hasStarted by remember { mutableStateOf(false) }
    var wasAssisted by remember { mutableStateOf(false) }
    var hasStartedMSM by remember { mutableStateOf(false) }
    var showPreview by remember { mutableStateOf(false) }

    // Innovative Game Modes States
    var isMemoryMode by remember { mutableStateOf(false) }
    var memoryPreviewTimer by remember { mutableIntStateOf(0) }
    var revealedIndex by remember { mutableStateOf<Int?>(null) }

    var isPadlockMode by remember { mutableStateOf(false) }
    var padlockCounters by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) }

    var isRotationMode by remember { mutableStateOf(false) }

    var secretMessage by remember { mutableStateOf("") }
    var showSecretMessageInput by remember { mutableStateOf(false) }
    var showSecretMessageModal by remember { mutableStateOf(false) }
    var showQRCodeDialog by remember { mutableStateOf(false) }

    val isTimeAttack = appSettings.isTimeAttackEnabled
    val timeLimit = appSettings.timeAttackLimitSeconds
    val displaySeconds = if (isTimeAttack) maxOf(0, timeLimit - timeSeconds) else timeSeconds

    // Replay System
    var moveHistory by remember { mutableStateOf(listOf<Int>()) }
    var initialPiecesState by remember { mutableStateOf<List<Piece>>(emptyList()) }
    var isReplaying by remember { mutableStateOf(false) }

    val squaredBitmap = remember(originalBitmap) {
        val size = minOf(originalBitmap.width, originalBitmap.height)
        val xOffset = (originalBitmap.width - size) / 2
        val yOffset = (originalBitmap.height - size) / 2
        Bitmap.createBitmap(originalBitmap, xOffset, yOffset, size, size)
    }

    var pieces by remember(n, squaredBitmap) {
        mutableStateOf(createPuzzlePieces(squaredBitmap, n))
    }

    val triggerSmartHint: () -> Unit = {
        coroutineScope.launch {
            val solutionSteps = withContext(Dispatchers.Default) {
                solvePuzzleIDAStar(pieces, n)
            }
            if (!solutionSteps.isNullOrEmpty()) {
                hintPieceId = solutionSteps.first()
                delay(3000)
                hintPieceId = null
            }
        }
    }

    val rotatePiece: (Int) -> Unit = { index ->
        val pieceToRotate = pieces[index]
        if (!pieceToRotate.isBlank) {
            val nextRotation = (pieceToRotate.rotationDegrees + 90f) % 360f
            val newList = pieces.toMutableList()
            newList[index] = pieceToRotate.copy(rotationDegrees = nextRotation)
            pieces = newList
            SocialAndHaptics.triggerMoveHaptic(haptic, context)
            isSolved = checkVictory(pieces)
            if (isSolved) {
                isActive = false
                SocialAndHaptics.triggerVictoryHaptic(haptic, context)
            }
        }
    }

    val movePiece: (Int) -> Unit = { index ->
        val pieceToMove = pieces[index]
        val lockCount = padlockCounters[pieceToMove.id] ?: 0

        if (lockCount <= 0) {
            val blankIndex = pieces.indexOfFirst { it.isBlank }
            val lineIndices = getLineToMoveIndices(index, blankIndex, n)

            if (lineIndices.isNotEmpty()) {
                if (!isActive) isActive = true

                SocialAndHaptics.triggerMoveHaptic(haptic, context)
                if (hintPieceId != null) hintPieceId = null

                if (isPadlockMode && padlockCounters.isNotEmpty()) {
                    padlockCounters = padlockCounters.mapValues { maxOf(0, it.value - 1) }
                }

                if (isMemoryMode) {
                    revealedIndex = index
                    coroutineScope.launch {
                        delay(1200)
                        if (revealedIndex == index) revealedIndex = null
                    }
                }

                val newList = pieces.toMutableList()
                for (stepIndex in lineIndices) {
                    val tempBlank = newList.indexOfFirst { it.isBlank }
                    val temp = newList[stepIndex]
                    newList[stepIndex] = newList[tempBlank]
                    newList[tempBlank] = temp
                    moveHistory = moveHistory + stepIndex
                    moveCount++
                }
                pieces = newList

                isSolved = checkVictory(pieces)
                if (isSolved) {
                    isActive = false
                    SocialAndHaptics.triggerVictoryHaptic(haptic, context)
                    if (secretMessage.isNotBlank()) {
                        showSecretMessageModal = true
                    }
                }
            } else if (isRotationMode) {
                rotatePiece(index)
            }
        } else {
            Toast.makeText(context, "🔒 Candado bloqueado: realiza $lockCount movimientos más", Toast.LENGTH_SHORT).show()
        }
    }

    val playReplay: () -> Unit = {
        coroutineScope.launch {
            if (moveHistory.isNotEmpty()) {
                isSolved = false
                isReplaying = true
                pieces = initialPiecesState
                delay(400)
                for (visualIndex in moveHistory) {
                    val blankIndex = pieces.indexOfFirst { it.isBlank }
                    if (isAdjacent(visualIndex, blankIndex, n)) {
                        val newList = pieces.toMutableList()
                        val temp = newList[visualIndex]
                        newList[visualIndex] = newList[blankIndex]
                        newList[blankIndex] = temp
                        pieces = newList
                        delay(220)
                    }
                }
                isReplaying = false
                hasStarted = false
            }
        }
    }

    var showBestRecordsDialog by remember { mutableStateOf(false) }

    val recordsByLevel = remember(statsHistory, n) {
        statsHistory.groupBy { it.n }.mapNotNull { (levelN, stats) ->
            val bestTime = stats.minByOrNull { it.time }
            val bestMoves = stats.minByOrNull { it.moves }
            if (bestTime != null && bestMoves != null) {
                Pair(levelN, Pair(bestTime, bestMoves))
            } else null
        }.sortedWith(
            compareByDescending<Pair<Int, Pair<PuzzleStatEntity, PuzzleStatEntity>>> { it.first == n }
                .thenBy { it.first }
        )
    }

    LaunchedEffect(n, squaredBitmap) {
        isActive = false
        val savedProgress = onCheckProgress(n)
        val basePieces = createPuzzlePieces(squaredBitmap, n)

        if (savedProgress != null) {
            val savedIds = savedProgress.state.split(",").mapNotNull { it.toIntOrNull() }
            if (savedIds.size == basePieces.size) {
                pieces = savedIds.mapNotNull { id -> basePieces.find { it.id == id } }
                moveCount = savedProgress.moves
                timeSeconds = savedProgress.time
            } else {
                pieces = basePieces
            }
        } else {
            pieces = basePieces
            moveCount = 0
            timeSeconds = 0
        }
    }

    LaunchedEffect(isMemoryMode, hasStarted) {
        if (isMemoryMode && hasStarted) {
            memoryPreviewTimer = 5
            while (memoryPreviewTimer > 0) {
                delay(1000)
                memoryPreviewTimer--
            }
        }
    }

    LaunchedEffect(isSolved) {
        if (isSolved && moveCount > 0 && !wasAssisted) {
            if (secretMessage.isNotBlank()) {
                showSecretMessageModal = true
            }
            onVictory(moveCount, timeSeconds, n)
            delay(1500)
            showHistoryDialog = true
        }
    }

    LaunchedEffect(isActive, isSolved, timeSeconds) {
        if (isActive && !isSolved) {
            if (isTimeAttack && displaySeconds <= 0) {
                isTimeOut = true
                isActive = false
                SoundEffectsManager.playGameOverSound()
            } else {
                delay(1000)
                timeSeconds++
            }
        }
    }

    if (showHistoryDialog) {
        val hasEnoughData = statsHistory.groupBy { it.n }.any { it.value.size >= 2 }
        if (hasEnoughData)
            StatsHistoryScreen(
                stats = statsHistory,
                onDismiss = { showHistoryDialog = false }
            )
    }

    if (showAchievementsDialog) {
        AchievementsDialog(
            stats = statsHistory,
            onDismiss = { showAchievementsDialog = false }
        )
    }

    if (showSecretMessageInput) {
        SecretMessageInputDialog(
            initialMessage = secretMessage,
            onDismiss = { showSecretMessageInput = false },
            onMessageSaved = { secretMessage = it }
        )
    }

    if (showSecretMessageModal) {
        SecretMessageRevelationDialog(
            message = secretMessage,
            onDismiss = { showSecretMessageModal = false }
        )
    }

    if (showQRCodeDialog) {
        QRCodeDialog(
            qrData = if (secretMessage.isNotBlank()) secretMessage else "¡Te desafío a resolver este Puzzle ${n}x${n}! Récord: $moveCount movs en ${timeSeconds}s.",
            title = "Código QR del Puzzle",
            onDismiss = { showQRCodeDialog = false }
        )
    }

    if (showBestRecordsDialog && recordsByLevel.isNotEmpty()) {
        Dialog(
            onDismissRequest = { showBestRecordsDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 12.dp,
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.sal_n_de_la_fama),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    androidx.compose.foundation.lazy.LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .padding(horizontal = 4.dp)
                    ) {
                        lazyListItems(recordsByLevel) { recordPair ->
                            val levelN = recordPair.first
                            val (bestTimeStat, bestMovesStat) = recordPair.second
                            val isCurrentLevel = levelN == n

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isCurrentLevel) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.fillMaxWidth(),
                                border = if (isCurrentLevel) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Surface(
                                        color = if (isCurrentLevel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        shape = CircleShape
                                    ) {
                                        Text(
                                            text = stringResource(R.string.nivel) + " ${levelN}x${levelN} ",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        RecordColumn(
                                            title = stringResource(R.string.m_s_r_pido),
                                            icon = Icons.Default.Timer,
                                            mainValue = formatTime(bestTimeStat.time),
                                            subValue = "${bestTimeStat.moves} movs",
                                            modifier = Modifier.weight(1f)
                                        )

                                        Box(
                                            modifier = Modifier
                                                .width(1.dp)
                                                .height(40.dp)
                                                .background(MaterialTheme.colorScheme.outlineVariant)
                                                .align(Alignment.CenterVertically)
                                        )

                                        RecordColumn(
                                            title = stringResource(R.string.mejor_precisi_n),
                                            icon = Icons.Default.Star,
                                            mainValue = "${bestMovesStat.moves} Movs",
                                            subValue = formatTime(bestMovesStat.time),
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.End
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { showBestRecordsDialog = false },
                        shape = CircleShape
                    ) {
                        Text(stringResource(R.string.a_seguir_jugando), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        if (showPreview) showPreview = false
                    }
                )
            }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        stringResource(R.string.movimientos),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                    Text(
                        "$moveCount",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                val minutes = displaySeconds / 60
                val seconds = displaySeconds % 60
                val timeString = "%02d:%02d".format(minutes, seconds)

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isTimeAttack) "Contrarreloj" else if (isMemoryMode && memoryPreviewTimer > 0) "Memoriza ($memoryPreviewTimer s)" else stringResource(R.string.tiempo),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isTimeAttack && displaySeconds <= 10) Color.Red else Color.Gray
                    )
                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isTimeAttack && displaySeconds <= 10) Color.Red else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Seleccion de Modos Especiales
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    FilterChip(
                        selected = isMemoryMode,
                        onClick = { isMemoryMode = !isMemoryMode },
                        label = { Text("Memoria") },
                        leadingIcon = { Icon(Icons.Default.Psychology, contentDescription = null) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    FilterChip(
                        selected = isPadlockMode,
                        onClick = { isPadlockMode = !isPadlockMode },
                        label = { Text("Candados") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    FilterChip(
                        selected = isRotationMode,
                        onClick = { isRotationMode = !isRotationMode },
                        label = { Text("Rotación") },
                        leadingIcon = { Icon(Icons.Default.RotateRight, contentDescription = null) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    FilterChip(
                        selected = secretMessage.isNotBlank(),
                        onClick = { showSecretMessageInput = true },
                        label = { Text(if (secretMessage.isBlank()) "Mensaje Oculto" else "Mensaje ✔") },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.tama_o_del_tablero),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    lazyListItems((3..8).toList()) { size ->
                        val isSelected = n == size

                        Surface(
                            modifier = Modifier
                                .size(width = 70.dp, height = 45.dp)
                                .clickable {
                                    if (n != size) {
                                        n = size
                                        moveCount = 0
                                        timeSeconds = 0
                                        isActive = false
                                        isSolved = false
                                        isTimeOut = false
                                        moveHistory = emptyList()
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            ),
                            shadowElevation = if (isSelected) 6.dp else 0.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${size}x${size}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
            ) {
                if (showBackgroundHint) {
                    Image(
                        bitmap = squaredBitmap.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0.5f)
                    )
                }

                val tileCornerShape = when (appSettings.tileStyle) {
                    "Soft Round" -> RoundedCornerShape(14.dp)
                    "Marco Foto" -> RoundedCornerShape(4.dp)
                    "Plano" -> RoundedCornerShape(0.dp)
                    else -> RoundedCornerShape(8.dp)
                }

                LazyVerticalGrid(
                    userScrollEnabled = false,
                    columns = GridCells.Fixed(n),
                    modifier = Modifier
                        .fillMaxSize()
                        .border(2.dp, skinStyle.borderColor)
                ) {
                    itemsIndexed(pieces, key = { _, piece -> piece.id }) { index, piece ->
                        val isHintPiece = hintPieceId != null && piece.id == hintPieceId
                        val isPieceCorrect = !piece.isBlank && piece.id == index && piece.rotationDegrees == 0f && appSettings.isHighlightCorrectEnabled

                        val lockCount = padlockCounters[piece.id] ?: 0
                        val isLocked = isPadlockMode && lockCount > 0

                        val isObscuredByMemory = isMemoryMode &&
                                memoryPreviewTimer == 0 &&
                                piece.id != index &&
                                revealedIndex != index &&
                                !piece.isBlank

                        val borderColor = when {
                            isHintPiece -> Color(0xFFFFC107)
                            isPieceCorrect -> Color(0xFF4CAF50)
                            else -> skinStyle.borderColor
                        }

                        val borderWidth = if (isHintPiece) 3.5.dp else if (isPieceCorrect) 2.dp else 0.5.dp

                        Box(
                            modifier = Modifier
                                .animateItem(
                                    placementSpec = spring(
                                        stiffness = Spring.StiffnessLow,
                                        visibilityThreshold = IntOffset.VisibilityThreshold
                                    )
                                )
                                .aspectRatio(1f)
                                .clip(tileCornerShape)
                                .border(borderWidth, borderColor, tileCornerShape)
                                .clickable {
                                    if (!isSolved && !isAutoSolving && !isReplaying && !isTimeOut && hasStarted) {
                                        movePiece(index)
                                        if (showPreview) showPreview = false
                                    } else {
                                        hasStartedMSM = true
                                    }
                                }
                                .pointerInput(index) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()

                                        if (isSolved || isAutoSolving || !hasStarted || isReplaying || isTimeOut) return@detectDragGestures
                                        if (showPreview) showPreview = false
                                        val threshold = 10f
                                        val isHorizontal = abs(dragAmount.x) > abs(dragAmount.y)
                                        val blankIndex = pieces.indexOfFirst { it.isBlank }
                                        var targetIndex = -1

                                        if (isHorizontal && abs(dragAmount.x) > threshold) {
                                            targetIndex =
                                                if (dragAmount.x > 0) index + 1 else index - 1
                                        } else if (!isHorizontal && abs(dragAmount.y) > threshold) {
                                            targetIndex =
                                                if (dragAmount.y > 0) index + n else index - n
                                        }

                                        if (targetIndex == blankIndex && isAdjacent(
                                                index,
                                                blankIndex,
                                                n
                                            )
                                        ) {
                                            movePiece(index)
                                        }
                                    }
                                }
                        ) {
                            if (!piece.isBlank) {
                                if (isObscuredByMemory) {
                                    Surface(
                                        modifier = Modifier.fillMaxSize(),
                                        color = skinStyle.containerColor,
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("?", style = MaterialTheme.typography.headlineMedium, color = skinStyle.textColor)
                                        }
                                    }
                                } else {
                                    Image(
                                        bitmap = piece.bitmap!!.asImageBitmap(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer { rotationZ = piece.rotationDegrees }
                                    )
                                }

                                if (appSettings.isAccessibilityPatternEnabled) {
                                    val patternSymbol = ACCESSIBILITY_PATTERNS[piece.id % ACCESSIBILITY_PATTERNS.size]
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.75f),
                                        shape = RoundedCornerShape(topStart = 6.dp, bottomEnd = 6.dp),
                                        modifier = Modifier.align(Alignment.TopStart)
                                    ) {
                                        Text(
                                            text = patternSymbol,
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                if (isLocked) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.55f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(24.dp))
                                            Text("$lockCount", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }

                                if (showHelp) {
                                    Text(
                                        "${piece.id}",
                                        color = Color.Red,
                                        fontSize = (30 - n * 2).sp,
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            if (showBackgroundHint) Color.Transparent
                                            else skinStyle.containerColor.copy(alpha = 0.3f)
                                        )
                                )
                            }
                        }
                    }
                }

                if (hasStartedMSM) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    ) {
                        Text(
                            stringResource(R.string.toca_mezclar_para_jugar),
                            color = Color.White,
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showPreview,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut(),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clickable(enabled = false) { }
                                .border(
                                    2.dp,
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(12.dp)
                                ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Image(
                                bitmap = originalBitmap.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            val solvedPieces = createPuzzlePieces(originalBitmap, n)
                            var currentPieces = solvedPieces
                            val totalMoves = n * n * 5

                            repeat(totalMoves) {
                                val blankIndex = currentPieces.indexOfFirst { it.isBlank }
                                val neighbors = getPossibleMoves(blankIndex, n)
                                val randomIndex = neighbors.random()

                                val newList = currentPieces.toMutableList()
                                val temp = newList[randomIndex]
                                newList[randomIndex] = newList[blankIndex]
                                newList[blankIndex] = temp
                                currentPieces = newList
                            }

                            if (isRotationMode) {
                                currentPieces = currentPieces.map { p ->
                                    if (p.isBlank) p
                                    else p.copy(rotationDegrees = listOf(0f, 90f, 180f, 270f).random())
                                }
                            }

                            pieces = currentPieces
                            initialPiecesState = currentPieces.toList()
                            moveHistory = emptyList()
                            moveCount = 0
                            timeSeconds = 0
                            isSolved = false
                            isTimeOut = false
                            hasStarted = true
                            hasStartedMSM = false
                            wasAssisted = false

                            if (isPadlockMode) {
                                val nonBlankIds = currentPieces.filter { !it.isBlank }.map { it.id }.shuffled()
                                padlockCounters = nonBlankIds.take(2).associateWith { 5 }
                            } else {
                                padlockCounters = emptyMap()
                            }

                            SoundEffectsManager.playShuffleSound()
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(42.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.mezclar), fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            pieces = createPuzzlePieces(squaredBitmap, n)
                            moveCount = 0
                            timeSeconds = 0
                            isSolved = false
                            isTimeOut = false
                            hasStarted = false
                            isActive = false
                            moveHistory = emptyList()
                            padlockCounters = emptyMap()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = hasStarted,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.resetear), style = MaterialTheme.typography.labelLarge)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ControlIconButton(
                        selected = hintPieceId != null,
                        onClick = { triggerSmartHint() },
                        icon = Icons.Default.Lightbulb,
                        activeColor = Color(0xFFFFC107)
                    )

                    ControlIconButton(
                        selected = showAchievementsDialog,
                        onClick = { showAchievementsDialog = true },
                        icon = Icons.Default.MilitaryTech,
                        activeColor = MaterialTheme.colorScheme.tertiary
                    )

                    ControlIconButton(
                        selected = showPreview,
                        onClick = { showPreview = !showPreview },
                        icon = if (showPreview) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        activeColor = MaterialTheme.colorScheme.primary
                    )

                    if (recordsByLevel.isNotEmpty()) {
                        ControlIconButton(
                            selected = showBestRecordsDialog,
                            onClick = { showBestRecordsDialog = true },
                            icon = Icons.Default.EmojiEvents,
                            activeColor = Color(0xFFFFD700)
                        )
                    }

                    ControlIconButton(
                        selected = showBackgroundHint,
                        onClick = { showBackgroundHint = !showBackgroundHint },
                        icon = Icons.Default.Image,
                        activeColor = MaterialTheme.colorScheme.secondary
                    )

                    ControlIconButton(
                        selected = showHelp,
                        onClick = { showHelp = !showHelp },
                        icon = Icons.Default.GridOn,
                        activeColor = MaterialTheme.colorScheme.tertiary
                    )

                    ControlIconButton(
                        selected = showQRCodeDialog,
                        onClick = { showQRCodeDialog = true },
                        icon = Icons.Default.QrCode2,
                        activeColor = MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = { onSaveProgress(n, moveCount, timeSeconds, pieces) },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.guardar))
                    }

                    if (n <= 4) {
                        Button(
                            enabled = !isSolved && !isAutoSolving && !isTimeOut,
                            onClick = {
                                wasAssisted = true
                                isAutoSolving = true
                                coroutineScope.launch {
                                    val solutionSteps = withContext(Dispatchers.Default) {
                                        solvePuzzleIDAStar(pieces, n)
                                    }

                                    if (solutionSteps == null) {
                                        isAutoSolving = false
                                    } else {
                                        for (targetPieceId in solutionSteps) {
                                            val currentVisualIndex = pieces.indexOfFirst { it.id == targetPieceId }
                                            if (currentVisualIndex != -1) {
                                                movePiece(currentVisualIndex)
                                                delay(if (n > 3) 150 else 300)
                                            }
                                        }
                                        isAutoSolving = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (isAutoSolving) stringResource(R.string.calculando) else stringResource(R.string.auto))
                        }
                    }

                    val hasEnoughData = statsHistory.any { it.n == n }
                    if (hasEnoughData) {
                        IconButton(
                            onClick = { showHistoryDialog = true },
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.secondaryContainer,
                                    CircleShape
                                )
                                .size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = "Estadísticas",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }

        if (isTimeOut) {
            Dialog(
                onDismissRequest = { isTimeOut = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 12.dp,
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .padding(vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(MaterialTheme.colorScheme.errorContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TimerOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "¡Tiempo Agotado!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.error
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Se acabó el tiempo del modo Contrarreloj. ¡Inténtalo de nuevo!",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                isTimeOut = false
                                timeSeconds = 0
                                moveCount = 0
                                pieces = createPuzzlePieces(squaredBitmap, n)
                                hasStarted = false
                                isActive = false
                            },
                            shape = CircleShape
                        ) {
                            Text("Reintentar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (isSolved && !wasAssisted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { isSolved = false }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.85f),
                        shape = MaterialTheme.shapes.large,
                        shadowElevation = 12.dp
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text(
                                stringResource(R.string.lo_lograste),
                                color = Color.White,
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                stringResource(R.string.puzzle_finalizado),
                                color = Color.Cyan,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        SocialAndHaptics.shareVictoryText(
                                            context,
                                            "Rompecabezas de Imagen ($n x $n)",
                                            moveCount,
                                            timeSeconds
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Compartir")
                                }

                                if (moveHistory.isNotEmpty()) {
                                    OutlinedButton(
                                        onClick = { playReplay() },
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, Color.White),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(if (isReplaying) "..." else "Replay")
                                    }
                                }
                            }
                        }
                    }
                }

                ConfettiKit(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(1f),
                    parties = getVictoryConfetti()
                )
            }
        }
    }
}

fun createPuzzlePieces(bitmap: Bitmap, n: Int): List<Piece> {
    val pieces = mutableListOf<Piece>()

    val size = Math.min(bitmap.width, bitmap.height)
    val xOffset = (bitmap.width - size) / 2
    val yOffset = (bitmap.height - size) / 2

    val squaredBitmap = Bitmap.createBitmap(bitmap, xOffset, yOffset, size, size)
    val pieceSize = size / n

    for (y in 0 until n) {
        for (x in 0 until n) {
            val id = y * n + x
            if (y == n - 1 && x == n - 1) {
                pieces.add(Piece(id = id, isBlank = true))
            } else {
                val b = Bitmap.createBitmap(
                    squaredBitmap,
                    x * pieceSize,
                    y * pieceSize,
                    pieceSize,
                    pieceSize
                )
                pieces.add(Piece(id = id, bitmap = b))
            }
        }
    }
    return pieces
}

fun isAdjacent(index: Int, blankIndex: Int, n: Int): Boolean {
    val row1 = index / n
    val col1 = index % n
    val row2 = blankIndex / n
    val col2 = blankIndex % n

    val rowDiff = Math.abs(row1 - row2)
    val colDiff = Math.abs(col1 - col2)

    return (rowDiff == 1 && col1 == col2) || (colDiff == 1 && row1 == row2)
}

fun checkVictory(pieces: List<Piece>): Boolean {
    for (i in pieces.indices) {
        if (pieces[i].id != i || pieces[i].rotationDegrees != 0f) {
            return false
        }
    }
    return true
}

fun getPossibleMoves(blankIndex: Int, n: Int): List<Int> {
    val moves = mutableListOf<Int>()
    val row = blankIndex / n
    val col = blankIndex % n

    if (row > 0) moves.add(blankIndex - n)
    if (row < n - 1) moves.add(blankIndex + n)
    if (col > 0) moves.add(blankIndex - 1)
    if (col < n - 1) moves.add(blankIndex + 1)

    return moves
}

fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): String {
    val maxDimension = 1200
    val scaledBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
        val aspect = bitmap.width.toFloat() / bitmap.height.toFloat()
        val (targetW, targetH) = if (aspect >= 1f) {
            maxDimension to (maxDimension / aspect).toInt()
        } else {
            (maxDimension * aspect).toInt() to maxDimension
        }
        Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
    } else {
        bitmap
    }

    val file = File(context.filesDir, "puzzle_${System.currentTimeMillis()}.jpg")
    FileOutputStream(file).use { out ->
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
    }
    return file.absolutePath
}

fun getLineToMoveIndices(clickedIndex: Int, blankIndex: Int, n: Int): List<Int> {
    val clickedRow = clickedIndex / n
    val clickedCol = clickedIndex % n
    val blankRow = blankIndex / n
    val blankCol = blankIndex % n

    val indicesToMove = mutableListOf<Int>()

    if (clickedRow == blankRow) {
        if (clickedCol < blankCol) {
            for (col in blankCol - 1 downTo clickedCol) {
                indicesToMove.add(clickedRow * n + col)
            }
        } else if (clickedCol > blankCol) {
            for (col in blankCol + 1..clickedCol) {
                indicesToMove.add(clickedRow * n + col)
            }
        }
    } else if (clickedCol == blankCol) {
        if (clickedRow < blankRow) {
            for (row in blankRow - 1 downTo clickedRow) {
                indicesToMove.add(row * n + clickedCol)
            }
        } else {
            for (row in blankRow + 1..clickedRow) {
                indicesToMove.add(row * n + clickedCol)
            }
        }
    }

    return indicesToMove
}

fun solvePuzzleIDAStar(pieces: List<Piece>, n: Int): List<Int>? {
    val initialState = pieces.map { it.id }
    val targetState = (0 until n * n).toList()

    var threshold = calculateManhattan(initialState, n)
    val path = mutableListOf<Int>()
    val pieceIdsPath = mutableListOf<Int>()

    while (true) {
        val result = search(initialState, 0, threshold, n, targetState, path, pieceIdsPath, -1)
        if (result == 0) return pieceIdsPath
        if (result == Int.MAX_VALUE) return null
        threshold = result
        if (threshold > 100) return null
    }
}

private fun search(
    state: List<Int>,
    g: Int,
    threshold: Int,
    n: Int,
    target: List<Int>,
    path: MutableList<Int>,
    pieceIdsPath: MutableList<Int>,
    lastMove: Int,
): Int {
    val h = calculateManhattan(state, n)
    val f = g + h
    if (f > threshold) return f
    if (state == target) return 0

    var min = Int.MAX_VALUE
    val blankIndex = state.indexOf(n * n - 1)

    for (nextIndex in getPossibleMoves(blankIndex, n)) {
        if (nextIndex == lastMove) continue

        val newState = state.toMutableList()
        val movedPieceId = newState[nextIndex]
        newState[blankIndex] = movedPieceId
        newState[nextIndex] = n * n - 1

        path.add(nextIndex)
        pieceIdsPath.add(movedPieceId)

        val t = search(newState, g + 1, threshold, n, target, path, pieceIdsPath, blankIndex)

        if (t == 0) return 0
        if (t < min) min = t

        path.removeAt(path.size - 1)
        pieceIdsPath.removeAt(pieceIdsPath.size - 1)
    }
    return min
}

fun getVictoryConfetti(): List<Party> {
    val colors = listOf(0xfce18a, 0xff726d, 0xb48def, 0xf4306d, 0x66ccff)

    val baseParty = Party(
        speed = 10f,
        maxSpeed = 40f,
        damping = 0.95f,
        colors = colors,
        emitter = Emitter(duration = 2000.milliseconds).max(100),
        size = listOf(Size.SMALL, Size.MEDIUM, Size.LARGE)
    )

    return listOf(
        baseParty.copy(
            angle = 315,
            spread = 50,
            position = Position.Relative(0.0, 0.7),
            emitter = Emitter(duration = 3000.milliseconds).perSecond(30)
        ),
        baseParty.copy(
            angle = 225,
            spread = 50,
            position = Position.Relative(1.0, 0.7),
            emitter = Emitter(duration = 3000.milliseconds).perSecond(30)
        ),
        baseParty.copy(
            angle = 90,
            spread = 360,
            position = Position.Relative(0.5, 0.0),
            speed = 0f,
            maxSpeed = 10f,
            emitter = Emitter(duration = 2000.milliseconds).max(50)
        )
    )
}

fun deletePuzzleFiles(context: Context, puzzles: List<PuzzleEntity>) {
    puzzles.forEach { puzzle ->
        val file = File(puzzle.imagePath)
        if (file.exists()) {
            file.delete()
        }
    }
}

@Composable
fun ControlIconButton(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    activeColor: Color
) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        shape = CircleShape,
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = if (selected) activeColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun RecordColumn(
    title: String,
    icon: ImageVector,
    mainValue: String,
    subValue: String,
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start
) {
    Column(modifier = modifier, horizontalAlignment = horizontalAlignment) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (horizontalAlignment == Alignment.Start) Icon(icon, null, Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
            Text(" $title ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (horizontalAlignment == Alignment.End) Icon(icon, null, Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
        }
        Text(text = mainValue, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
        Text(text = subValue, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
    }
}

fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}
