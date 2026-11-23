
@Composable
fun TesterProfileModal(
    testerData: TesterData,
    viewModel: AuthViewModel,
    onDismiss: () -> Unit
) {
    var fullProfile by remember { mutableStateOf<UserProfile?>(null) }

    
    LaunchedEffect(testerData.idUser) {
        fullProfile = viewModel.getProfileById(testerData.idUser)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        title = { Text(stringResource(R.string.perfil_de_desarrollador), fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                if (fullProfile == null) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    val profile = fullProfile!!
                    Text(profile.devName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    Text(
                        text = profile.bio.ifBlank { stringResource(R.string.sin_biograf_a) },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(8.dp)) {
                        Text(
                            text = "${stringResource(R.string.d_as_en_la_app)} ${testerData.dias}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    androidx.compose.material3.HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "${"%.1f".format(profile.promedioEstrellas)} (${profile.calificaciones.size} ${stringResource(R.string.votos)})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(stringResource(R.string.calificar_este_tester), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                        (1..5).forEach { star ->
                            Surface(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clickable {
                                        viewModel.rateTester(testerData.idUser, star)
                                        onDismiss()
                                    },
                                shape = androidx.compose.foundation.shape.CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (star <= profile.promedioEstrellas) Color(0xFFFFC107) else Color.LightGray,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(12.dp)) { 
                Text(stringResource(R.string.cerrar), fontWeight = FontWeight.Bold) 
            }
        }
    )
}

