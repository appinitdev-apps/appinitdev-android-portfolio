
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(navController: NavController, viewModel: ChatViewModel) {
    val verificationRequests = viewModel.verificationRequests
    val reportRequests = viewModel.reportRequests
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.observeVerificationRequests()
        viewModel.observeReports()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.panel_administracion), color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A1C1E))
            )
        },
        containerColor = Color(0xFF131416)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF1A1C1E),
                contentColor = Color(0xFF238CDD)
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("${stringResource(R.string.verificaciones)} (${verificationRequests.size})") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("${stringResource(R.string.reportes)} (${reportRequests.size})") })
            }

            when (selectedTab) {
                0 -> VerificationsList(verificationRequests, viewModel)
                1 -> ReportsList(reportRequests, viewModel)
            }
        }
    }
}

@Composable
fun VerificationsList(requests: List<VerificationRequest>, viewModel: ChatViewModel) {
    if (requests.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.no_hay_verificaciones_pendientes), color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(requests) { request ->
                VerificationRequestItem(request) { approved ->
                    viewModel.updateVerificationStatus(request.userId, request.requestId, approved)
                }
            }
        }
    }
}

@Composable
fun ReportsList(reports: List<ReportRequest>, viewModel: ChatViewModel) {
    if (reports.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.no_hay_reportes_pendientes), color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(reports) { report ->
                ReportRequestItem(report) { dismissed ->
                    if (dismissed) viewModel.dismissReport(report.reportId)
                }
            }
        }
    }
}

@Composable
fun ReportRequestItem(report: ReportRequest, onDismiss: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C1E))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(R.string.reportado_formato, report.reportedUsername), style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Text(text = stringResource(R.string.motivo_formato, report.reason), color = Color(0xFFFF5252), style = MaterialTheme.typography.bodyMedium)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (report.details.isNotEmpty()) {
                Text(text = report.details, color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MainGradientButton(
                    text = stringResource(R.string.descartar),
                    onClick = { onDismiss(true) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun VerificationRequestItem(request: VerificationRequest, onAction: (Boolean) -> Unit) {
    val formatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C1E))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = request.username, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(text = request.email, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(text = stringResource(R.string.id_formato, request.userId), style = MaterialTheme.typography.labelSmall, color = Color.DarkGray)
                }
                Text(
                    text = request.timestamp?.toDate()?.let { formatter.format(it) } ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (request.message.isNotEmpty()) {
                Text(
                    text = request.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(request.imageUrls) { imageUrl ->
                    var showFullImage by remember { mutableStateOf(false) }
                    Box {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = stringResource(R.string.prueba_identidad),
                            modifier = Modifier
                                .width(200.dp)
                                .height(300.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { showFullImage = true },
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    if (showFullImage) {
                        Dialog(onDismissRequest = { showFullImage = false }) {
                            Box(modifier = Modifier.fillMaxSize().clickable { showFullImage = false }) {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SecondaryButton(
                    text = stringResource(R.string.rechazar),
                    onClick = { onAction(false) },
                    modifier = Modifier.weight(1f),
                    contentColor = Color(0xFFFF5252)
                )

                MainGradientButton(
                    text = stringResource(R.string.aprobar),
                    onClick = { onAction(true) },
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.Check
                )
            }
        }
    }
}
