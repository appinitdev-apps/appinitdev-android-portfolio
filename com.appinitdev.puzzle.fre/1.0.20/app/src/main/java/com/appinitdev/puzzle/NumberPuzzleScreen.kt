
enum class NumberPuzzleMode(val displayName: String, val puzzleId: Int) {
    NORMAL("Normal", 1),
    REVERSE("Inverso", 2),
    VERTICAL("Vertical", 3),
    SPIRAL("Espiral", 4),
    DIAGONAL("Diagonal", 5),
    PARES("Pares Primero", 6),
    IMPARES("Impares Primero", 7),
    MIRROR("Espejo", 8),
    SNAKE("Serpiente", 9),
    HORSESHOE("Herradura", 10),
    X_PATTERN("Multiplicador", 11),
    PIRAMIDE("Pirámide", 12),
    CLOCKWISE("Reloj", 13),
    SIMETRIA("Simetría", 14),
    RANDOM_TARGET("Objetivo Loco", 15)
}

val displayNames = listOf(
    (R.string.normal),
    (R.string.inverso),
    (R.string.vertical),
    (R.string.espiral),
    (R.string.diagonal),
    (R.string.pares_primero),
    (R.string.impares_primero),
    (R.string.espejo),
    (R.string.serpiente),
    (R.string.herradura),
    (R.string.multiplicador),
    (R.string.pir_mide),
    (R.string.reloj),
    (R.string.simetr_a),
    (R.string.objetivo_loco)
)

fun getNumberTargetState(n: Int, mode: NumberPuzzleMode): List<Int> {
    val totalPieces = n * n
    val blankId = totalPieces - 1
    val numbers = (0 until blankId).toList()

    return when (mode) {
        NumberPuzzleMode.NORMAL -> numbers + blankId
        NumberPuzzleMode.REVERSE -> numbers.reversed() + blankId
        NumberPuzzleMode.VERTICAL -> {
            val result = IntArray(totalPieces)
            result[totalPieces - 1] = blankId
            var current = 0
            for (col in 0 until n) {
                for (row in 0 until n) {
                    if (row == n - 1 && col == n - 1) continue
                    val index = row * n + col
                    result[index] = numbers[current++]
                }
            }
            result.toList()
        }
        NumberPuzzleMode.SPIRAL -> {
            val result = IntArray(totalPieces) { blankId }
            var left = 0; var right = n - 1
            var top = 0; var bottom = n - 1
            var current = 0
            while (left <= right && top <= bottom) {
                for (i in left..right) if (current < blankId) result[top * n + i] = numbers[current++]
                top++
                for (i in top..bottom) if (current < blankId) result[i * n + right] = numbers[current++]
                right--
                for (i in right downTo left) if (current < blankId) result[bottom * n + i] = numbers[current++]
                bottom--
                for (i in bottom downTo top) if (current < blankId) result[i * n + left] = numbers[current++]
                left++
            }
            result.toList()
        }
        NumberPuzzleMode.DIAGONAL -> {
            val result = IntArray(totalPieces) { blankId }
            var current = 0
            for (sum in 0 until (2 * n - 1)) {
                for (row in 0 until n) {
                    val col = sum - row
                    if (col in 0 until n) {
                        if (row == n - 1 && col == n - 1) continue
                        result[row * n + col] = numbers[current++]
                    }
                }
            }
            result.toList()
        }
        NumberPuzzleMode.PARES -> {
            val pares = numbers.filter { (it + 1) % 2 == 0 }
            val impares = numbers.filter { (it + 1) % 2 != 0 }
            (pares + impares) + blankId
        }
        NumberPuzzleMode.IMPARES -> {
            val impares = numbers.filter { (it + 1) % 2 != 0 }
            val pares = numbers.filter { (it + 1) % 2 == 0 }
            (impares + pares) + blankId
        }
        NumberPuzzleMode.MIRROR -> {
            val result = IntArray(totalPieces) { blankId }
            var current = 0
            for (row in 0 until n) {
                for (col in n - 1 downTo 0) {
                    if (row == n - 1 && col == n - 1) continue
                    result[row * n + col] = numbers[current++]
                }
            }
            result.toList()
        }
        NumberPuzzleMode.SNAKE -> {
            val result = IntArray(totalPieces) { blankId }
            var current = 0
            for (row in 0 until n) {
                if (row % 2 == 0) {
                    for (col in 0 until n) {
                        if (row == n - 1 && col == n - 1) continue
                        result[row * n + col] = numbers[current++]
                    }
                } else {
                    for (col in n - 1 downTo 0) {
                        if (row == n - 1 && col == n - 1) continue
                        result[row * n + col] = numbers[current++]
                    }
                }
            }
            result.toList()
        }
        NumberPuzzleMode.HORSESHOE -> {
            val result = IntArray(totalPieces) { blankId }
            val path = mutableListOf<Int>()
            for (i in 0 until n) path.add(i * n + 0)
            for (j in 1 until n) path.add((n - 1) * n + j)
            for (i in (n - 2) downTo 0) path.add(i * n + (n - 1))

            val remaining = (0 until totalPieces).filter { !path.contains(it) && it != blankId }
            val fullPath = path + remaining

            var current = 0
            fullPath.forEach { pos ->
                if (current < numbers.size && pos < totalPieces) {
                    result[pos] = numbers[current++]
                }
            }
            result.toList()
        }
        NumberPuzzleMode.X_PATTERN -> {
            val result = IntArray(totalPieces) { blankId }
            val xPositions = mutableListOf<Int>()
            val others = mutableListOf<Int>()

            for (r in 0 until n) {
                for (c in 0 until n) {
                    if (r == c || r + c == n - 1) xPositions.add(r * n + c)
                    else others.add(r * n + c)
                }
            }

            val allPos = xPositions + others
            var current = 0
            allPos.forEach { pos ->
                if (current < numbers.size && pos != totalPieces - 1) {
                    result[pos] = numbers[current++]
                }
            }
            result.toList()
        }
        NumberPuzzleMode.CLOCKWISE -> {
            val result = IntArray(totalPieces) { blankId }
            val path = mutableListOf<Int>()
            for (i in 0 until n) path.add(0 * n + i)
            for (i in 1 until n) path.add(i * n + (n - 1))
            for (i in (n - 2) downTo 0) path.add((n - 1) * n + i)
            for (i in (n - 2) downTo 1) path.add(i * n + 0)

            val remaining = (0 until totalPieces).filter { !path.contains(it) }
            val fullPath = path + remaining

            var current = 0
            fullPath.forEach { pos ->
                if (current < numbers.size && pos != totalPieces - 1) {
                    result[pos] = numbers[current++]
                }
            }
            result.toList()
        }
        NumberPuzzleMode.PIRAMIDE -> {
            val result = IntArray(totalPieces) { blankId }
            val centerX = n / 2
            val centerY = n / 2

            val positions = mutableListOf<Pair<Int, Int>>()
            for (r in 0 until n) {
                for (c in 0 until n) {
                    if (!(r == n - 1 && c == n - 1)) {
                        positions.add(r to c)
                    }
                }
            }

            val sortedPositions = positions.sortedBy { (r, c) ->
                abs(r - centerX) + abs(c - centerY)
            }

            var current = 0
            sortedPositions.forEach { (r, c) ->
                if (current < numbers.size) {
                    result[r * n + c] = numbers[current++]
                }
            }
            result.toList()
        }
        NumberPuzzleMode.RANDOM_TARGET -> {
            val shuffledNumbers = numbers.shuffled(Random(42))
            shuffledNumbers + blankId
        }
        NumberPuzzleMode.SIMETRIA -> {
            val result = IntArray(totalPieces) { blankId }
            val usedPos = BooleanArray(totalPieces)
            var current = 0

            for (i in 0 until totalPieces) {
                if (!usedPos[i] && i != totalPieces - 1) {
                    result[i] = numbers[current++]
                    usedPos[i] = true

                    val opposite = (totalPieces - 1) - i
                    if (opposite in 0 until totalPieces && !usedPos[opposite] && opposite != totalPieces - 1) {
                        if (current < numbers.size) {
                            result[opposite] = numbers[current++]
                            usedPos[opposite] = true
                        }
                    }
                }
            }
            result.toList()
        }
    }
}

fun checkNumberVictory(pieces: List<Piece>, targetState: List<Int>): Boolean {
    for (i in pieces.indices) {
        if (pieces[i].id != targetState[i]) {
            return false
        }
    }
    return true
}

@SuppressLint("SuspiciousIndentation")
@Composable
fun NumberPuzzleScreen(puzzleDao: PuzzleDao) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val appSettings = remember { loadAppSettings(context) }
    val skinStyle = remember(appSettings.boardSkin) { getBoardSkinStyle(appSettings.boardSkin) }

    var n by remember { mutableIntStateOf(3) }
    var currentMode by remember { mutableStateOf(NumberPuzzleMode.NORMAL) }
    val coroutineScope = rememberCoroutineScope()
    var wasStatSaved by remember { mutableStateOf(false) }

    var hasStarted by remember { mutableStateOf(false) }
    var hasStartedMSM by remember { mutableStateOf(false) }
    var isSolved by remember { mutableStateOf(false) }
    var isTimeOut by remember { mutableStateOf(false) }
    var isActive by remember { mutableStateOf(false) }
    var moveCount by remember { mutableIntStateOf(0) }
    var timeSeconds by remember { mutableIntStateOf(0) }
    var hintPieceId by remember { mutableStateOf<Int?>(null) }
    val statsHistory by puzzleDao.getStatsForPuzzle(currentMode.puzzleId).collectAsState(initial = emptyList())
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showAchievementsDialog by remember { mutableStateOf(false) }
    var showPreview by remember { mutableStateOf(false) }
    var showQRCodeDialog by remember { mutableStateOf(false) }
    var secretMessage by remember { mutableStateOf("") }
    var showSecretMessageInput by remember { mutableStateOf(false) }
    var showSecretMessageModal by remember { mutableStateOf(false) }

    val isTimeAttack = appSettings.isTimeAttackEnabled
    val timeLimit = appSettings.timeAttackLimitSeconds
    val displaySeconds = if (isTimeAttack) maxOf(0, timeLimit - timeSeconds) else timeSeconds

    
    var moveHistory by remember { mutableStateOf(listOf<Int>()) }
    var initialPiecesState by remember { mutableStateOf<List<Piece>>(emptyList()) }
    var isReplaying by remember { mutableStateOf(false) }

    val targetState = remember(n, currentMode) { getNumberTargetState(n, currentMode) }

    var pieces by remember(n, currentMode) {
        val blankId = n * n - 1
        mutableStateOf(targetState.map { id -> Piece(id = id, isBlank = (id == blankId)) })
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

    val movePiece: (Int) -> Unit = { index ->
        val blankIndex = pieces.indexOfFirst { it.isBlank }
        val lineIndices = getLineToMoveIndices(index, blankIndex, n)

        if (lineIndices.isNotEmpty()) {
            if (!isActive) isActive = true

            SocialAndHaptics.triggerMoveHaptic(haptic, context)
            if (hintPieceId != null) hintPieceId = null

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

            isSolved = checkNumberVictory(pieces, targetState)
            if (isSolved) {
                isActive = false
                SocialAndHaptics.triggerVictoryHaptic(haptic, context)
                if (secretMessage.isNotBlank()) {
                    showSecretMessageModal = true
                }
            }
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

    LaunchedEffect(isSolved) {
        if (isSolved && !wasStatSaved) {
            wasStatSaved = true
            isActive = false
            if (secretMessage.isNotBlank()) {
                showSecretMessageModal = true
            }
            coroutineScope.launch(Dispatchers.IO) {
                puzzleDao.insertStat(
                    PuzzleStatEntity(
                        puzzleId = currentMode.puzzleId,
                        n = n,
                        moves = moveCount,
                        time = timeSeconds
                    )
                )
            }
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

    Box(modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectTapGestures(
                onTap = {
                    if (showPreview) showPreview = false
                }
            )
        }) {
        if (showHistoryDialog) {
            val hasEnoughData = statsHistory.any { it.n == n }
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
                qrData = "PUZZLE_NUM_${currentMode.displayName}_${n}x${n}_MOVS_${moveCount}_TIME_${timeSeconds}s",
                title = "Código QR del Desafío Numérico",
                subtitle = "Escanea este código QR para compartir tu tiempo y puntuación.",
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

                        LazyColumn(
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
                                                text = stringResource(R.string.nivel)+" ${levelN}x${levelN} ",
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

                                            Box(modifier = Modifier
                                                .width(1.dp)
                                                .height(40.dp)
                                                .background(MaterialTheme.colorScheme.outlineVariant)
                                                .align(Alignment.CenterVertically))

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {
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

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isTimeAttack) "Contrarreloj" else stringResource(R.string.tiempo),
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

                Row(
                    modifier = Modifier.weight(1.2f),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ControlIconButton(
                        selected = showAchievementsDialog,
                        onClick = { showAchievementsDialog = true },
                        icon = Icons.Default.MilitaryTech,
                        activeColor = MaterialTheme.colorScheme.tertiary
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    if (recordsByLevel.isNotEmpty()) {
                        ControlIconButton(
                            selected = showBestRecordsDialog,
                            onClick = { showBestRecordsDialog = true },
                            icon = Icons.Default.EmojiEvents,
                            activeColor = Color(0xFFFFD700)
                        )
                    }

                    val hasEnoughData = statsHistory.any { it.n == n }
                    if (hasEnoughData) {
                        Spacer(modifier = Modifier.width(6.dp))
                        ControlIconButton(
                            selected = showHistoryDialog,
                            onClick = { showHistoryDialog = true },
                            icon = Icons.Default.BarChart,
                            activeColor = Color(0xFFFFD700)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.tama_o_del_tablero),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 8.dp)
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
                                        wasStatSaved = false
                                        moveHistory = emptyList()
                                        val blankId = n * n - 1
                                        pieces = getNumberTargetState(n, currentMode).map { id ->
                                            Piece(id = id, isBlank = (id == blankId))
                                        }
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

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                FilterChip(
                    selected = secretMessage.isNotBlank(),
                    onClick = { showSecretMessageInput = true },
                    label = { Text(if (secretMessage.isBlank()) "Ocultar Mensaje Secreto 💌" else "Mensaje Personalizado ✔") },
                    shape = RoundedCornerShape(12.dp)
                )
            }

            DifficultySelect(
                currentMode = currentMode,
                onModeSelected = { newMode ->
                    currentMode = newMode
                    moveCount = 0
                    timeSeconds = 0
                    isActive = false
                    isSolved = false
                    isTimeOut = false
                    moveHistory = emptyList()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier
                .weight(1f)
                .aspectRatio(1f)) {

                LazyVerticalGrid(
                    userScrollEnabled = false,
                    columns = GridCells.Fixed(n),
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.LightGray)
                        .border(2.dp, skinStyle.borderColor)
                ) {
                    itemsIndexed(pieces, key = { _, piece -> piece.id }) { index, piece ->
                        val isHintPiece = hintPieceId != null && piece.id == hintPieceId
                        val isPieceCorrect = !piece.isBlank && piece.id == targetState[index] && appSettings.isHighlightCorrectEnabled

                        val borderColor = when {
                            isHintPiece -> Color(0xFFFFC107)
                            isPieceCorrect -> Color(0xFF4CAF50)
                            else -> skinStyle.borderColor
                        }

                        val borderWidth = if (isHintPiece) 3.5.dp else if (isPieceCorrect) 2.dp else 0.dp

                        Box(
                            modifier = Modifier
                                .animateItem(
                                    placementSpec = spring(
                                        stiffness = Spring.StiffnessLow,
                                        visibilityThreshold = IntOffset.VisibilityThreshold
                                    )
                                )
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .border(
                                    borderWidth,
                                    borderColor,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    if (!isSolved && !isReplaying && !isTimeOut && hasStarted) movePiece(index) else {
                                        hasStartedMSM = true
                                    }
                                }
                                .pointerInput(index) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        if (isSolved || isReplaying || !hasStarted || isTimeOut) return@detectDragGestures
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
                                Surface(
                                    modifier = Modifier.fillMaxSize(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isHintPiece) Color(0xFFFFF8E1) else skinStyle.containerColor,
                                    shadowElevation = 4.dp
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = (piece.id + 1).toString(),
                                            color = if (isHintPiece) Color(0xFFF57F17) else skinStyle.textColor,
                                            fontSize = (50 - n * 3).sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            } else {
                                Box(modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Transparent))
                            }
                        }
                    }
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = showPreview,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut(),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .zIndex(10f)
                ) {
                    NumberPreview(n = n, targetState = targetState)
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
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        val blankId = n * n - 1
                        var currentPieces = targetState.map { id -> Piece(id = id, isBlank = (id == blankId)) }
                        val totalMoves = n * n * 20
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
                        pieces = currentPieces
                        initialPiecesState = currentPieces.toList()
                        moveHistory = emptyList()
                        moveCount = 0
                        timeSeconds = 0
                        isSolved = false
                        isTimeOut = false
                        isActive = false
                        hasStarted = true
                        hasStartedMSM = false
                        wasStatSaved = false
                        SoundEffectsManager.playShuffleSound()
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .height(42.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(Icons.Default.Shuffle, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.mezclar), fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                }

                OutlinedButton(
                    onClick = {
                        val blankId = n * n - 1
                        pieces = targetState.map { id -> Piece(id = id, isBlank = (id == blankId)) }
                        moveCount = 0
                        timeSeconds = 0
                        isSolved = false
                        isTimeOut = false
                        isActive = false
                        hasStarted = false
                        hasStartedMSM = false
                        wasStatSaved = false
                        moveHistory = emptyList()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text(stringResource(R.string.resetear), style = MaterialTheme.typography.labelLarge)
                }

                Surface(
                    onClick = { showPreview = !showPreview },
                    shape = RoundedCornerShape(16.dp),
                    color = if (showPreview) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (showPreview) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Previsualizar",
                            tint = if (showPreview) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    onClick = { showQRCodeDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = "Compartir QR",
                            tint = MaterialTheme.colorScheme.primary
                        )
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
                                val blankId = n * n - 1
                                pieces = getNumberTargetState(n, currentMode).map { id ->
                                    Piece(id = id, isBlank = (id == blankId))
                                }
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

        if (isSolved) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { isSolved = false }
            ) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + scaleIn(initialScale = 0.5f),
                    exit = fadeOut() + scaleOut(targetScale = 1.2f)
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
                                    style = MaterialTheme.typography.displaySmall
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
                                                currentMode.displayName,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DifficultySelect(
    currentMode: NumberPuzzleMode,
    onModeSelected: (NumberPuzzleMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = stringResource(R.string.seleccionar_dificultad),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = currentMode.displayName,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                NumberPuzzleMode.entries.forEach { mode ->
                    val name = displayNames[mode.puzzleId - 1]
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(name),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        onClick = {
                            if (currentMode != mode) {
                                onModeSelected(mode)
                            }
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}

@Composable
fun NumberPreview(n: Int, targetState: List<Int>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(n),
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            userScrollEnabled = false
        ) {
            lazyGridItems(targetState) { id ->
                val isBlank = id == (n * n - 1)
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(1.dp)
                        .background(
                            if (isBlank) Color.Transparent
                            else MaterialTheme.colorScheme.secondaryContainer,
                            RoundedCornerShape(4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (!isBlank) {
                        Text(
                            text = (id + 1).toString(),
                            fontSize = (20 - n).sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}
