
@Composable
internal fun SleepTimerSelectionContent(
    selectedTimeMinutes: Int,
    onSelectMinutes: (Int) -> Unit,
) {
    val timeOptions = listOf(5, 10, 15, 30, 45, 60, 90, 120)
    Spacer(modifier = Modifier.height(24.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        timeOptions.take(4).forEach { minutes ->
            TimeOptionChip(minutes = minutes, isSelected = selectedTimeMinutes == minutes, onClick = { onSelectMinutes(minutes) })
        }
    }
    Spacer(modifier = Modifier.height(20.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        timeOptions.takeLast(4).forEach { minutes ->
            TimeOptionChip(minutes = minutes, isSelected = selectedTimeMinutes == minutes, onClick = { onSelectMinutes(minutes) })
        }
    }
}

@Composable
private fun TimeOptionChip(minutes: Int, isSelected: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), label = "scale")
    Box(modifier = Modifier.size(64.dp).clip(CircleShape).scale(scale).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Card(modifier = Modifier.fillMaxSize(), shape = CircleShape,
            colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface),
            border = BorderStroke(width = 1.dp, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 0.dp)) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "$minutes",
                    style = if (isSelected) MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.titleMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}
