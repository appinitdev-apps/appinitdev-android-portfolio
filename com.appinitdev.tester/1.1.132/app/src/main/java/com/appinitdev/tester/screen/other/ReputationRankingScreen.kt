@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReputationRankingScreen(viewModel: AuthViewModel, onBack: () -> Unit) {
    var showReputationInfo by remember { mutableStateOf(false) }
    var selectedUserForInfo by remember { mutableStateOf<UserProfile?>(null) }
    val usuarios by viewModel.allUsers.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.loadRanking()
    }
    val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
    val sortedUsers = remember(usuarios) {
        usuarios.sortedByDescending { it.reputacion }
    }
    val miPosicion = remember(sortedUsers) {
        val index = sortedUsers.indexOfFirst { it.uid == currentUserUid }
        if (index != -1) "#"+(index + 1).toString() else " "
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ranking_de_reputaci_n)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.volver))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .padding(16.dp)) {
            Text(
                text = stringResource(R.string.top_desarrolladores) +" $miPosicion",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black
            )
            Text(
                text = stringResource(R.string.basado_en_nivel_puntos_y_calificaciones_de_la_comunidad),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (sortedUsers.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(sortedUsers) { index, usuario ->
                        RankingUserItem(
                            usuario = usuario,
                            posicion = index + 1,
                            onReputationClick = {
                                selectedUserForInfo = usuario
                                showReputationInfo = true
                            }
                        )
                    }
                }
            }
        }
    }
    if (showReputationInfo && selectedUserForInfo != null) {
        ReputationExplanationDialog(
            profile = selectedUserForInfo,
            onDismiss = {
                showReputationInfo = false
                selectedUserForInfo = null
            }
        )
    }
}
@Composable
fun RankingUserItem(usuario: UserProfile, posicion: Int,onReputationClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (posicion) {
                1 -> Color(0xFFFFD700).copy(alpha = 0.1f) 
                2 -> Color(0xFFC0C0C0).copy(alpha = 0.1f) 
                3 -> Color(0xFFCD7F32).copy(alpha = 0.1f) 
                else -> Color(0xFFE8EAF6).copy(alpha = 0.1f)
            }
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(32.dp),
                contentAlignment = Alignment.Center
            ) {
                if (posicion <= 3) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = when (posicion) {
                            1 -> Color(0xFFFFD700)
                            2 -> Color(0xFFC0C0C0)
                            else -> Color(0xFFCD7F32)
                        }
                    )
                }
                Text(
                    text = posicion.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (posicion <= 3) Color.Black else MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = usuario.devName.ifEmpty { "Tester" }.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = " ${stringResource(R.string.nivel_lbl)} ${usuario.nivel.toInt()}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFFFFA000)
                    )
                    Text(
                        text = " ${usuario.puntos} ${stringResource(R.string.puntos_lbl)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFFFFB100) 
                    )
                    Text(
                        text = " ${String.format("%.1f", usuario.promedioEstrellas)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB8860B) 
                    )
                }
            }
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.clickable { onReputationClick() }
            ) {
                Text(
                    text = "${stringResource(R.string.reputacion_lbl)}: ${String.format("%.5f", usuario.reputacion)}",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}