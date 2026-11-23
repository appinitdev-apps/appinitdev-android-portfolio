
@Composable
fun MessageBubble(
    message: Message,
    isMe: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {

        Surface(
            color = if (isMe)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomEnd = if (isMe) 0.dp else 16.dp,
                bottomStart = if (isMe) 16.dp else 0.dp
            ),
            tonalElevation = 2.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {

                if (!isMe) {
                    Text(
                        text = message.nameDev,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                SmartChatText(
                    text = message.text,
                    isMe = isMe
                )
            }
        }
    }
}

@Composable
fun SmartChatText(text: String, isMe: Boolean) {
    val uriHandler = LocalUriHandler.current

    // Color del texto base dependiendo de si es mi mensaje o no
    val textColor = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val linkColor = if (isMe) Color(0xFF80D8FF) else Color(0xFF007BFF) // Azul más claro en fondo oscuro

    val annotatedString = remember(text) {
        buildAnnotatedString {
            // 1. Aplicamos Estilos de Markdown (*B*, _I_, ~S~, ```C```)
            append(text)

            val boldRegex = Regex("\\*(.*?)\\*")
            val italicRegex = Regex("_(.*?)_")
            val strikeRegex = Regex("~(.*?)~")
            val codeRegex = Regex("```(.*?)```")
            val mentionRegex = Regex("@(\\w+)")

            // Negrita
            boldRegex.findAll(text).forEach { addStyle(SpanStyle(fontWeight = FontWeight.Bold), it.range.first, it.range.last + 1) }
            // Cursiva
            italicRegex.findAll(text).forEach { addStyle(SpanStyle(fontStyle = FontStyle.Italic), it.range.first, it.range.last + 1) }
            // Tachado
            strikeRegex.findAll(text).forEach { addStyle(SpanStyle(textDecoration = TextDecoration.LineThrough), it.range.first, it.range.last + 1) }
            // Código monoespaciado
            codeRegex.findAll(text).forEach {
                addStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = Color.Black.copy(alpha = 0.1f)), it.range.first, it.range.last + 1)
            }
            // Menciones (@usuario)
            mentionRegex.findAll(text).forEach {
                addStyle(SpanStyle(fontWeight = FontWeight.Bold, color = if(isMe) Color.Yellow else Color(0xFF25D366)), it.range.first, it.range.last + 1)
            }

            // 2. Detección automática de Links (URL, Email, Teléfono)
            val spannable = android.text.SpannableString(text)
            LinkifyCompat.addLinks(spannable, Linkify.ALL)
            val spans = spannable.getSpans(0, text.length, android.text.style.URLSpan::class.java)

            for (span in spans) {
                val start = spannable.getSpanStart(span)
                val end = spannable.getSpanEnd(span)

                addStyle(
                    style = SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline),
                    start = start,
                    end = end
                )
                addStringAnnotation(tag = "URL", annotation = span.url, start = start, end = end)
            }
        }
    }

    // SelectionContainer permite que el usuario copie el texto
    SelectionContainer {
        ClickableText(
            text = annotatedString,
            style = MaterialTheme.typography.bodyMedium.copy(color = textColor),
            onClick = { offset ->
                annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                    .firstOrNull()?.let { annotation ->
                        // Abre el link, el marcador de teléfono o el correo
                        uriHandler.openUri(annotation.item)
                    }
            }
        )
    }
}


@Composable
fun InfoBlockExpandable(
    title: String,
    content: String,
    initialExpanded: Boolean = false
) {
    var isExpanded by remember { mutableStateOf(initialExpanded) }
    val uriHandler = LocalUriHandler.current
    val linkColor = MaterialTheme.colorScheme.primary

    // 1. Procesar el texto para detectar links
    val annotatedString = remember(content) {
        buildAnnotatedString {
            append(content)
            val spannable = android.text.SpannableString(content)
            android.text.util.Linkify.addLinks(spannable, android.text.util.Linkify.ALL)
            val spans = spannable.getSpans(0, content.length, android.text.style.URLSpan::class.java)

            for (span in spans) {
                val start = spannable.getSpanStart(span)
                val end = spannable.getSpanEnd(span)
                addStyle(
                    style = SpanStyle(
                        color = linkColor,
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.Medium
                    ),
                    start = start, end = end
                )
                addStringAnnotation(tag = "URL", annotation = span.url, start = start, end = end)
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        // CABECERA: Título + Flecha
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }, // Hacer que todo el título sea clickeable
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = { isExpanded = !isExpanded }) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Ocultar" else "Mostrar",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // CONTENIDO ANIMADO
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
                SelectionContainer {
                    ClickableText(
                        text = annotatedString,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        onClick = { offset ->
                            annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                                .firstOrNull()?.let { annotation ->
                                    try {
                                        uriHandler.openUri(annotation.item)
                                    } catch (_: Exception) { }
                                }
                        }
                    )
                }
                Divider(
                    modifier = Modifier.padding(top = 8.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}

@Composable
fun InfoBlock(title: String, content: String) {
    val uriHandler = LocalUriHandler.current
    val linkColor = MaterialTheme.colorScheme.primary

    // 1. Crear el AnnotatedString con estilos de enlace
    val annotatedString = remember(content) {
        buildAnnotatedString {
            val text = content.take(300)
            append(text)

            // Detección automática de Links (URL, Email, Teléfono) usando Linkify
            val spannable = android.text.SpannableString(text)
            android.text.util.Linkify.addLinks(spannable, android.text.util.Linkify.ALL)
            val spans = spannable.getSpans(0, text.length, android.text.style.URLSpan::class.java)

            for (span in spans) {
                val start = spannable.getSpanStart(span)
                val end = spannable.getSpanEnd(span)

                addStyle(
                    style = SpanStyle(
                        color = linkColor,
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.Medium
                    ),
                    start = start,
                    end = end
                )
                addStringAnnotation(
                    tag = "URL",
                    annotation = span.url,
                    start = start,
                    end = end
                )
            }
        }
    }

    Spacer(Modifier.height(12.dp))
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.secondary
    )

    // 2. Usar ClickableText para que los links funcionen al tocarlos
    SelectionContainer {
        ClickableText(
            text = annotatedString,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            onClick = { offset ->
                annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                    .firstOrNull()?.let { annotation ->
                        try {
                            uriHandler.openUri(annotation.item)
                        } catch (e: Exception) {
                            // Manejar error si no hay app para abrir el link
                        }
                    }
            }
        )
    }
}