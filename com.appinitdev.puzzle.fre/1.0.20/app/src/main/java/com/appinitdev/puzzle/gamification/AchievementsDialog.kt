data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val isUnlocked: Boolean,
    val currentProgress: Int,
    val maxProgress: Int,
    val progressText: String,
    val icon: ImageVector
)
fun calculateAchievements(stats: List<PuzzleStatEntity>): List<Achievement> {
    val totalWins = stats.size
    val minTime = stats.minOfOrNull { it.time } ?: Int.MAX_VALUE
    val minMoves = stats.minOfOrNull { it.moves } ?: Int.MAX_VALUE
    val maxGrid = stats.maxOfOrNull { it.n } ?: 0
    val uniquePuzzles = stats.map { it.puzzleId }.distinct().size
    val uniqueNumberModes = stats.filter { it.puzzleId in 1..15 }.map { it.puzzleId }.distinct().size
    return listOf(
        Achievement(
            id = "first_win",
            title = "Primer Paso",
            description = "Resuelve tu primer rompecabezas",
            isUnlocked = totalWins >= 1,
            currentProgress = totalWins.coerceAtMost(1),
            maxProgress = 1,
            progressText = "${totalWins.coerceAtMost(1)} / 1",
            icon = Icons.Default.EmojiEvents
        ),
        Achievement(
            id = "speed_demon",
            title = "Rayo Veloz",
            description = "Resuelve un puzzle en menos de 30 segundos",
            isUnlocked = minTime <= 30,
            currentProgress = if (minTime <= 30) 1 else 0,
            maxProgress = 1,
            progressText = if (minTime == Int.MAX_VALUE) "--" else "${minTime}s",
            icon = Icons.Default.FlashOn
        ),
        Achievement(
            id = "precision_master",
            title = "Mente Maestra",
            description = "Completa un puzzle en menos de 20 movimientos",
            isUnlocked = minMoves <= 20,
            currentProgress = if (minMoves <= 20) 1 else 0,
            maxProgress = 1,
            progressText = if (minMoves == Int.MAX_VALUE) "--" else "$minMoves movs",
            icon = Icons.Default.Star
        ),
        Achievement(
            id = "grid_expert",
            title = "Desafío Avanzado",
            description = "Resuelve un tablero de 4x4 o superior",
            isUnlocked = maxGrid >= 4,
            currentProgress = maxGrid.coerceAtMost(4),
            maxProgress = 4,
            progressText = if (maxGrid > 0) "${maxGrid}x${maxGrid}" else "--",
            icon = Icons.Default.GridOn
        ),
        Achievement(
            id = "grid_master",
            title = "Gigante de Tableros",
            description = "Resuelve un tablero de 5x5 o superior",
            isUnlocked = maxGrid >= 5,
            currentProgress = maxGrid.coerceAtMost(5),
            maxProgress = 5,
            progressText = if (maxGrid > 0) "${maxGrid}x${maxGrid}" else "--",
            icon = Icons.Default.Psychology
        ),
        Achievement(
            id = "puzzle_collector",
            title = "Coleccionista",
            description = "Juega y completa al menos 5 puzzles diferentes",
            isUnlocked = uniquePuzzles >= 5,
            currentProgress = uniquePuzzles.coerceAtMost(5),
            maxProgress = 5,
            progressText = "${uniquePuzzles.coerceAtMost(5)} / 5",
            icon = Icons.Default.MilitaryTech
        ),
        Achievement(
            id = "time_strategist",
            title = "Estratega del Tiempo",
            description = "Completa un puzzle en menos de 15 segundos",
            isUnlocked = minTime <= 15,
            currentProgress = if (minTime <= 15) 1 else 0,
            maxProgress = 1,
            progressText = if (minTime == Int.MAX_VALUE) "--" else "${minTime}s",
            icon = Icons.Default.Timer
        ),
        Achievement(
            id = "perfectionist",
            title = "Perfeccionista",
            description = "Resuelve un puzzle con menos de 15 movimientos",
            isUnlocked = minMoves <= 15,
            currentProgress = if (minMoves <= 15) 1 else 0,
            maxProgress = 1,
            progressText = if (minMoves == Int.MAX_VALUE) "--" else "$minMoves movs",
            icon = Icons.Default.AutoAwesome
        ),
        Achievement(
            id = "number_explorer",
            title = "Explorador de Números",
            description = "Completa al menos 3 modos numéricos distintos",
            isUnlocked = uniqueNumberModes >= 3,
            currentProgress = uniqueNumberModes.coerceAtMost(3),
            maxProgress = 3,
            progressText = "${uniqueNumberModes.coerceAtMost(3)} / 3",
            icon = Icons.Default.Numbers
        ),
        Achievement(
            id = "veteran",
            title = "Maratón",
            description = "Acumula 10 victorias en total",
            isUnlocked = totalWins >= 10,
            currentProgress = totalWins.coerceAtMost(10),
            maxProgress = 10,
            progressText = "${totalWins.coerceAtMost(10)} / 10",
            icon = Icons.Default.WorkspacePremium
        ),
        Achievement(
            id = "legend",
            title = "Leyenda Viva",
            description = "Acumula 25 victorias en total",
            isUnlocked = totalWins >= 25,
            currentProgress = totalWins.coerceAtMost(25),
            maxProgress = 25,
            progressText = "${totalWins.coerceAtMost(25)} / 25",
            icon = Icons.Default.EmojiEvents
        ),
        Achievement(
            id = "grandmaster",
            title = "Maestro Supremo",
            description = "Alcanza 50 victorias totales en la aplicación",
            isUnlocked = totalWins >= 50,
            currentProgress = totalWins.coerceAtMost(50),
            maxProgress = 50,
            progressText = "${totalWins.coerceAtMost(50)} / 50",
            icon = Icons.Default.WorkspacePremium
        )
    )
}
@Composable
fun AchievementsDialog(
    stats: List<PuzzleStatEntity>,
    onDismiss: () -> Unit
) {
    val achievements = remember(stats) { calculateAchievements(stats) }
    val unlockedCount = achievements.count { it.isUnlocked }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 10.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MilitaryTech,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.sal_n_de_la_fama),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$unlockedCount de ${achievements.size} Logros Desbloqueados",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxHeight(0.65f)
                ) {
                    items(achievements, key = { it.id }) { achievement ->
                        AchievementCard(achievement)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    shape = CircleShape,
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text(stringResource(R.string.cerrar), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
@Composable
fun AchievementCard(achievement: Achievement) {
    val containerColor = if (achievement.isUnlocked) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    }
    val iconTint = if (achievement.isUnlocked) {
        Color(0xFFFFD700)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    }
    val border = if (achievement.isUnlocked) {
        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
    } else null
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(18.dp),
        border = border,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (achievement.isUnlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = achievement.icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = achievement.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (achievement.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (achievement.isUnlocked) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Desbloqueado",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val progressRatio = (achievement.currentProgress.toFloat() / achievement.maxProgress.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { if (achievement.isUnlocked) 1f else progressRatio },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(CircleShape),
                        color = if (achievement.isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Text(
                        text = achievement.progressText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
