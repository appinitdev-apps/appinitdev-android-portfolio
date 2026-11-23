data class CustomSecretMessage(
    val text: String = "",
    val fontStyle: String = "Normal",
    val textColorHex: String = "#3F51B5",
    val emoji: String = "❤️",
    val theme: String = "Amor ❤️"
) {
    fun toEncodedString(): String {
        return "SMSG::$text::$fontStyle::$textColorHex::$emoji::$theme"
    }
    companion object {
        fun fromEncodedString(encoded: String): CustomSecretMessage {
            if (!encoded.startsWith("SMSG::")) {
                return CustomSecretMessage(text = encoded)
            }
            val parts = encoded.substring(6).split("::")
            return CustomSecretMessage(
                text = parts.getOrElse(0) { "" },
                fontStyle = parts.getOrElse(1) { "Normal" },
                textColorHex = parts.getOrElse(2) { "#3F51B5" },
                emoji = parts.getOrElse(3) { "❤️" },
                theme = parts.getOrElse(4) { "Amor ❤️" }
            )
        }
    }
}
fun getFontFamily(fontStyle: String): FontFamily {
    return when (fontStyle) {
        "Cursiva" -> FontFamily.Cursive
        "Elegante" -> FontFamily.Serif
        "Código" -> FontFamily.Monospace
        else -> FontFamily.Default
    }
}
fun getFontWeight(fontStyle: String): FontWeight {
    return when (fontStyle) {
        "Negrita" -> FontWeight.ExtraBold
        else -> FontWeight.Bold
    }
}
data class MessageThemeStyle(
    val containerColor: Color,
    val borderColor: Color,
    val textColor: Color
)
fun getThemeStyle(theme: String): MessageThemeStyle {
    return when (theme) {
        "Amor ❤️" -> MessageThemeStyle(
            containerColor = Color(0xFFFFF0F2),
            borderColor = Color(0xFFFF4081),
            textColor = Color(0xFFD81B60)
        )
        "Fiesta 🎉" -> MessageThemeStyle(
            containerColor = Color(0xFFFFFDE7),
            borderColor = Color(0xFFFFB300),
            textColor = Color(0xFFE65100)
        )
        "Ciberpunk ⚡" -> MessageThemeStyle(
            containerColor = Color(0xFF1A1A2E),
            borderColor = Color(0xFF00E5FF),
            textColor = Color(0xFF00E5FF)
        )
        "Esmeralda 🌿" -> MessageThemeStyle(
            containerColor = Color(0xFFE8F5E9),
            borderColor = Color(0xFF00C853),
            textColor = Color(0xFF1B5E20)
        )
        "Noche Gold 🏆" -> MessageThemeStyle(
            containerColor = Color(0xFF212121),
            borderColor = Color(0xFFFFD700),
            textColor = Color(0xFFFFD700)
        )
        else -> MessageThemeStyle(
            containerColor = Color(0xFFE8EAF6),
            borderColor = Color(0xFF3F51B5),
            textColor = Color(0xFF1A237E)
        )
    }
}
@Composable
fun SecretMessageInputDialog(
    initialMessage: String,
    onDismiss: () -> Unit,
    onMessageSaved: (String) -> Unit
) {
    val initialParsed = remember(initialMessage) { CustomSecretMessage.fromEncodedString(initialMessage) }
    var text by remember { mutableStateOf(initialParsed.text) }
    var fontStyle by remember { mutableStateOf(initialParsed.fontStyle) }
    var selectedEmoji by remember { mutableStateOf(initialParsed.emoji) }
    var selectedTheme by remember { mutableStateOf(initialParsed.theme) }
    var showQRModal by remember { mutableStateOf(false) }
    val presetMessages = listOf(
        "¡Feliz Cumpleaños! 🎂🎉",
        "¡Eres una persona increíble! 🌟",
        "¡Te Quiero Mucho! ❤️",
        "¿Quieres salir por un café? ☕",
        "¡Lo lograste, Campeón! 🏆"
    )
    val emojis = listOf("❤️", "🎉", "🎂", "☕", "🏆", "🌟", "🤫", "💎", "🚀", "🎁")
    val fontStyles = listOf("Normal", "Cursiva", "Negrita", "Elegante", "Código")
    val themes = listOf("Amor ❤️", "Fiesta 🎉", "Ciberpunk ⚡", "Esmeralda 🌿", "Noche Gold 🏆", "Clásico")
    val currentCustomMessage = CustomSecretMessage(
        text = text,
        fontStyle = fontStyle,
        textColorHex = "#3F51B5",
        emoji = selectedEmoji,
        theme = selectedTheme
    )
    if (showQRModal) {
        QRCodeDialog(
            qrData = currentCustomMessage.toEncodedString(),
            title = "Código QR de tu Desafío Secreto",
            subtitle = "Escanea este código desde otro teléfono para jugar este desafío y descubrir el mensaje.",
            onDismiss = { showQRModal = false }
        )
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
                modifier = Modifier
                    .padding(20.dp)
                    .heightIn(max = 600.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.personalizar_mensaje_secreto),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.configura_el_texto),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Ej: ¡Tienes un regalo esperándote!") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                val themeStyle = getThemeStyle(selectedTheme)
                Card(
                    colors = CardDefaults.cardColors(containerColor = themeStyle.containerColor),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, themeStyle.borderColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(selectedEmoji, fontSize = 22.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (text.isBlank()) "Previsualización de tu mensaje" else text,
                            fontFamily = getFontFamily(fontStyle),
                            fontWeight = getFontWeight(fontStyle),
                            color = themeStyle.textColor,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Emoji:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(emojis) { emo ->
                            FilterChip(
                                selected = selectedEmoji == emo,
                                onClick = { selectedEmoji = emo },
                                label = { Text(emo) },
                                shape = CircleShape
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.tipo_de_letra),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(fontStyles) { fStyle ->
                            FilterChip(
                                selected = fontStyle == fStyle,
                                onClick = { fontStyle = fStyle },
                                label = {
                                    Text(
                                        fStyle,
                                        fontFamily = getFontFamily(fStyle),
                                        fontWeight = getFontWeight(fStyle)
                                    )
                                },
                                shape = CircleShape
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.tema_color),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(themes) { thm ->
                            FilterChip(
                                selected = selectedTheme == thm,
                                onClick = { selectedTheme = thm },
                                label = { Text(thm) },
                                shape = CircleShape
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = CircleShape,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Text(stringResource(R.string.cerrar), maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = { showQRModal = true },
                        shape = CircleShape,
                        modifier = Modifier.weight(1.2f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Compartir QR", maxLines = 1, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            onMessageSaved(currentCustomMessage.toEncodedString())
                            onDismiss()
                        },
                        shape = CircleShape,
                        modifier = Modifier.weight(1.2f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Text(stringResource(R.string.guardar), maxLines = 1, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
@Composable
fun SecretMessageRevelationDialog(
    message: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val parsedMsg = remember(message) { CustomSecretMessage.fromEncodedString(message) }
    val themeStyle = remember(parsedMsg.theme) { getThemeStyle(parsedMsg.theme) }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
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
                        imageVector = Icons.Default.MarkEmailRead,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(38.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "¡Mensaje Secreto Revelado!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = themeStyle.containerColor),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(2.dp, themeStyle.borderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = parsedMsg.emoji, fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "\"${parsedMsg.text}\"",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = getFontFamily(parsedMsg.fontStyle),
                            fontWeight = getFontWeight(parsedMsg.fontStyle),
                            textAlign = TextAlign.Center,
                            color = themeStyle.textColor
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = CircleShape,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                    ) {
                        Text("Aceptar", maxLines = 1, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val shareText = "💌 Mensaje Secreto Revelado:\n\n${parsedMsg.emoji} \"${parsedMsg.text}\"\n\n¡Descarga la app de Rompecabezas!"
                            SocialAndHaptics.shareVictoryText(context, shareText, 0, 0)
                        },
                        shape = CircleShape,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Compartir", maxLines = 1, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
