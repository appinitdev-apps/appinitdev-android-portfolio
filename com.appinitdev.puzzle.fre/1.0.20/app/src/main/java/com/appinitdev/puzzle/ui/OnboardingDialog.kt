data class OnboardingStep(
    val title: String,
    val description: String,
    val icon: ImageVector
)
@Composable
fun OnboardingDialog(
    onDismiss: () -> Unit
) {
    val steps = remember {
        listOf(
            OnboardingStep(
                title = "¡Bienvenido a Puzzle Master!",
                description = "Desliza o toca las fichas contiguas al espacio vacío para moverlas y formar la imagen original o colocar los números en orden.",
                icon = Icons.Default.Extension
            ),
            OnboardingStep(
                title = "Herramientas e IA",
                description = "Usa el icono del ojo para ver la imagen guía, la varita mágica para auto-resolver con Inteligencia Artificial o guarda tu partida en cualquier momento.",
                icon = Icons.Default.AutoAwesome
            ),
            OnboardingStep(
                title = "Logros y Repetición",
                description = "¡Consigue récords, desbloquea logros en el Salón de la Fama y vuelve a ver tu victoria paso a paso con el modo Replay!",
                icon = Icons.Default.MilitaryTech
            )
        )
    }
    var currentStepIndex by remember { mutableIntStateOf(0) }
    val currentStep = steps[currentStepIndex]
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 10.dp,
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
                        .size(72.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = currentStep.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                AnimatedContent(targetState = currentStep, label = "onboarding_step") { step ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = step.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = step.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    steps.indices.forEach { index ->
                        Box(
                            modifier = Modifier
                                .size(if (index == currentStepIndex) 12.dp else 8.dp)
                                .background(
                                    if (index == currentStepIndex) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant,
                                    CircleShape
                                )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStepIndex > 0) {
                        TextButton(onClick = { currentStepIndex-- }) {
                            Text("Anterior")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }
                    Button(
                        onClick = {
                            if (currentStepIndex < steps.size - 1) {
                                currentStepIndex++
                            } else {
                                onDismiss()
                            }
                        },
                        shape = CircleShape
                    ) {
                        Text(
                            if (currentStepIndex == steps.size - 1) "¡A Jugar!" else "Siguiente",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
