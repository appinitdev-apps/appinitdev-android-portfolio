@Composable
fun RequestList(app: AppModel, viewModel: AuthViewModel) {
    var testerParaVer by remember { mutableStateOf<TesterData?>(null) }
    val context = LocalContext.current
    val solicitudes = remember(app.testers) {
        app.testers.values.filter { it.validado == "Solicitud" }
    }
    testerParaVer?.let {
        TesterProfileModal(
            testerData = it,
            viewModel = viewModel,
            onDismiss = { testerParaVer = null }
        )
    }
    if (solicitudes.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.sin_solicitudes_por_el_momento),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(solicitudes) { tester ->
                ListItem(
                    headlineContent = {
                        Text(
                            text = tester.devName,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { testerParaVer = tester }
                        )
                    },
                    supportingContent = { Text(stringResource(R.string.d_as) + " ${tester.dias} | ${tester.correoUser}") },
                    trailingContent = {
                        Row {
                            IconButton(onClick = { viewModel.responseToTester(app, tester.idUser, true, context) }) {
                                Icon(Icons.Default.Check, "Aceptar", tint = Color.Green)
                            }
                            IconButton(onClick = { viewModel.responseToTester(app, tester.idUser, false, context) }) {
                                Icon(Icons.Default.Close, "Rechazar", tint = Color.Red)
                            }
                        }
                    }
                )
            }
        }
    }
}