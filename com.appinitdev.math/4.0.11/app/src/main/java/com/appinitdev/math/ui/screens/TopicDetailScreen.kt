
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicDetailScreen(
    viewModel: MathViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val topic by viewModel.selectedTopic.collectAsState()
    val markdownContent by viewModel.topicMarkdownContent.collectAsState()
    val selectedTab by viewModel.selectedDetailTab.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()

    val currentTopic = topic ?: return
    var noteText by remember(currentTopic.id) {
        mutableStateOf(viewModel.getTopicNote(currentTopic.id))
    }
    var showNotesSheet by remember { mutableStateOf(false) }

    // Dynamic Tab List: Omit 'EXAMPLES' tab if current topic has no examples
    val availableTabs = remember(currentTopic) {
        val list = mutableListOf(DetailTab.ALL, DetailTab.THEORY)
        if (currentTopic.hasExamples) {
            list.add(DetailTab.EXAMPLES)
        }
        list.add(DetailTab.FORMULAS)
        list.add(DetailTab.CALCULATOR)
        list
    }

    // Modal Dialog / Sheet for Personal Notes & Custom Formula Editor
    if (showNotesSheet) {
        AlertDialog(
            onDismissRequest = { showNotesSheet = false },
            title = {
                Text(
                    text = "📝 " + stringResource(id = R.string.title_personal_notes),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FormulaEditorCard(
                        initialNote = noteText,
                        onNoteChanged = { newNote ->
                            noteText = newNote
                            viewModel.saveTopicNote(currentTopic.id, newNote)
                        },
                        currentLang = currentLang
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showNotesSheet = false },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(id = R.string.save_and_close), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentTopic.title,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text(text = "←", style = MaterialTheme.typography.titleLarge)
                    }
                },
                actions = {
                    val isPinned = viewModel.isTopicPinned(currentTopic.id)
                    IconButton(onClick = { viewModel.togglePinnedTopic(currentTopic) }) {
                        Text(
                            text = if (isPinned) "📌" else "📍",
                            fontSize = 20.sp
                        )
                    }
                    IconButton(onClick = {
                        shareTopic(context, currentTopic.title, currentTopic.summarySnippet)
                    }) {
                        Text(text = "📤", fontSize = 18.sp)
                    }
                    IconButton(onClick = { viewModel.toggleFavorite(currentTopic) }) {
                        Text(
                            text = if (currentTopic.isFavorite) "★" else "☆",
                            fontSize = 22.sp,
                            color = if (currentTopic.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            if (selectedTab != DetailTab.CALCULATOR) {
                FloatingActionButton(
                    onClick = { showNotesSheet = true },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = CircleShape
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "📝", fontSize = 20.sp)
                        if (noteText.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "•",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                AdBanner(
                    adUnitId = "ca-app-pub-8325653088416363/5096006208",
                    modifier = Modifier.fillMaxWidth(),
                    isPremium = isPremium
                )
                BottomAppBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.previousTopic() },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(id = R.string.btn_previous))
                        }

                        OutlinedButton(
                            onClick = { viewModel.nextTopic() },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(id = R.string.btn_next))
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableTabs) { tab ->
                    val tabLabel = when (tab) {
                        DetailTab.ALL -> stringResource(id = R.string.tab_all)
                        DetailTab.THEORY -> stringResource(id = R.string.tab_theory)
                        DetailTab.EXAMPLES -> stringResource(id = R.string.tab_examples)
                        DetailTab.FORMULAS -> stringResource(id = R.string.tab_formulas)
                        DetailTab.CALCULATOR -> stringResource(id = R.string.tab_calculator)
                    }
                    FilterChip(
                        selected = tab == selectedTab,
                        onClick = { viewModel.setDetailTab(tab) },
                        label = {
                            Text(
                                text = "${tab.icon} $tabLabel",
                                fontWeight = if (tab == selectedTab) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            if (selectedTab == DetailTab.CALCULATOR) {
                AdvancedScientificCalculator(
                    currentLang = currentLang,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            } else {
                MathMarkdownViewer(
                    markdownContent = markdownContent,
                    filterTab = selectedTab,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            }
        }
    }
}

private fun shareTopic(context: Context, title: String, snippet: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(
            Intent.EXTRA_TEXT,
            "📐 $title\n\n$snippet\n\nCompartido desde Formulario Matemático"
        )
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Compartir Fórmula")
    context.startActivity(shareIntent)
}
