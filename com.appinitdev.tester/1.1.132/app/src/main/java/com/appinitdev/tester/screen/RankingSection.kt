@Composable
fun RankingSection(profile: UserProfile) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("${stringResource(R.string.nivel)}: ${profile.nivel}", style = MaterialTheme.typography.titleLarge)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "${stringResource(R.string.calificacion)}: ${String.format("%.1f", profile.promedioEstrellas)}")
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFB300)
                )
                Text(text = " (${profile.calificaciones.size} ${stringResource(R.string.votos)})")
            }
            Text("${stringResource(R.string.puntos_totales)}: ${profile.puntos}")
        }
    }
}