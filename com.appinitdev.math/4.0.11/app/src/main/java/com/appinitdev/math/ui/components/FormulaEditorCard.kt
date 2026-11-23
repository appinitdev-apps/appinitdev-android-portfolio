
data class FormulaSymbolBtn(
    val label: String,
    val latexSnippet: String
)

@Composable
fun FormulaEditorCard(
    initialNote: String,
    onNoteChanged: (String) -> Unit,
    currentLang: String,
    modifier: Modifier = Modifier
) {
    var textValue by remember(initialNote) { mutableStateOf(initialNote) }
    var showPreview by remember { mutableStateOf(true) }

    val formulaToolbar = remember {
        listOf(
            FormulaSymbolBtn("a/b", "\\frac{a}{b}"),
            FormulaSymbolBtn("√x", "\\sqrt{x}"),
            FormulaSymbolBtn("xⁿ", "x^{n}"),
            FormulaSymbolBtn("x₁", "x_{1}"),
            FormulaSymbolBtn("∫", "\\int_{a}^{b}"),
            FormulaSymbolBtn("∑", "\\sum_{i=1}^{n}"),
            FormulaSymbolBtn("sin", "\\sin(\\theta)"),
            FormulaSymbolBtn("cos", "\\cos(\\theta)"),
            FormulaSymbolBtn("π", "\\pi"),
            FormulaSymbolBtn("θ", "\\theta"),
            FormulaSymbolBtn("±", "\\pm"),
            FormulaSymbolBtn("∞", "\\infty"),
            FormulaSymbolBtn("lim", "\\lim_{x \\to 0}")
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📝 " + stringResource(id = R.string.custom_notes_editor),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                FilterChip(
                    selected = showPreview,
                    onClick = { showPreview = !showPreview },
                    label = { Text(if (showPreview) "👁️ " + stringResource(id = R.string.preview) else "✏️ " + stringResource(id = R.string.edit)) }
                )
            }

            
            Text(
                text = stringResource(id = R.string.insert_symbol_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(formulaToolbar) { btn ->
                    FilterChip(
                        selected = false,
                        onClick = {
                            val newText = if (textValue.isBlank()) {
                                "$${btn.latexSnippet}$"
                            } else {
                                textValue + " " + btn.latexSnippet + " "
                            }
                            textValue = newText
                            onNoteChanged(newText)
                        },
                        label = { Text(btn.label, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            if (!showPreview) {
                OutlinedTextField(
                    value = textValue,
                    onValueChange = {
                        textValue = it
                        onNoteChanged(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    placeholder = { Text(stringResource(id = R.string.placeholder_personal_notes)) },
                    shape = RoundedCornerShape(12.dp)
                )
            } else {
                if (textValue.isNotBlank()) {
                    val previewContent = remember(textValue) {
                        val trimmed = textValue.trim()
                        if (trimmed.contains("$") || trimmed.contains("#") || trimmed.contains("<")) {
                            trimmed
                        } else {
                            "$$ $trimmed $$"
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            MathMarkdownViewer(
                                markdownContent = previewContent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                            )
                        }
                    }
                } else {
                    Text(
                        text = "✍️ " + stringResource(id = R.string.placeholder_personal_notes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
