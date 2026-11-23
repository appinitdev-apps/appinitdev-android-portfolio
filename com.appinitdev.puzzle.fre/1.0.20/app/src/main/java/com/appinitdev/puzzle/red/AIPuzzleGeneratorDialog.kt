@Composable
fun AIPuzzleGeneratorDialog(
    onDismiss: () -> Unit,
    onImageGenerated: (Bitmap) -> Unit
) {
    var prompt by remember { mutableStateOf("Un dragón de cristal volando sobre un bosque místico") }
    var selectedStyle by remember { mutableStateOf("Ninguno") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val presetPrompts = listOf(
        "Gato astronauta en el espacio 🐱🚀",
        "Castillo de cristal al atardecer 🏰✨",
        "Ciudad futurista ciberpunk 🌆⚡",
        "Dragón neón mágico 🐉🔥",
        "Bosque encantado de hadas 🌲🧚"
    )
    val artStyles = listOf(
        "Ninguno",
        "Realista 📸",
        "Cyberpunk ⚡",
        "Pixel Art 👾",
        "Óleo 🎨",
        "Anime 🌸",
        "Render 3D 🧊",
        "Fantasía Neón 🌌"
    )
    val generateAIImage: () -> Unit = {
        scope.launch {
            if (prompt.isBlank()) return@launch
            isLoading = true
            errorMessage = null
            try {
                val fullPrompt = if (selectedStyle != "Ninguno") {
                    "$prompt, en estilo $selectedStyle"
                } else {
                    prompt
                }
                val encodedPrompt = URLEncoder.encode(fullPrompt, StandardCharsets.UTF_8.toString())
                val seed = (1..999999).random()
                val imageUrl = "https://image.pollinations.ai/prompt/$encodedPrompt?width=800&height=800&nologo=true&seed=$seed"
                val bitmap = downloadUnsplashImage(context, imageUrl)
                if (bitmap != null) {
                    onImageGenerated(bitmap)
                    onDismiss()
                } else {
                    errorMessage = "No se pudo generar la imagen. Inténtalo de nuevo."
                }
            } catch (e: Exception) {
                Log.e("AIPuzzleGenerator", "Error generating image: ${e.message}")
                errorMessage = "Error de conexión al servidor de IA."
            } finally {
                isLoading = false
            }
        }
    }
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
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.generador_de_puzzle_ia),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Escribe cualquier idea y la Inteligencia Artificial creará una imagen única para tu rompecabezas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    placeholder = { Text("Ej: Un gato astronauta en Marte") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.estilo_artistico),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(artStyles) { style ->
                        FilterChip(
                            selected = (selectedStyle == style),
                            onClick = { selectedStyle = style },
                            label = { Text(style, style = MaterialTheme.typography.bodySmall) },
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.ideas_rapidas),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(presetPrompts) { preset ->
                        SuggestionChip(
                            onClick = { prompt = preset },
                            label = { Text(preset, style = MaterialTheme.typography.bodySmall) },
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (isLoading) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.creando_imagen_con_ia),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = CircleShape,
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.cerrar))
                    }
                    Button(
                        onClick = { generateAIImage() },
                        shape = CircleShape,
                        enabled = !isLoading && prompt.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Generar IA", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
