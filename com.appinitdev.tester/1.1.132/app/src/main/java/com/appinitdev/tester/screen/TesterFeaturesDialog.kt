data class Feature(val icon: ImageVector, val title: String, val desc: String)
@Composable
fun TesterFeaturesDialog(onDismiss: () -> Unit) {
    val features = listOf(
        Feature(Icons.Default.Analytics,
            stringResource(R.string.monitorizaci_n),
            stringResource(R.string.controla_instalaciones_y_uso_en_tiempo_real)
        ),
        Feature(Icons.Default.MonetizationOn,
            stringResource(R.string.gana_puntos),
            stringResource(R.string.prueba_otras_apps_y_sube_las_tuyas)
        ),
        Feature(Icons.Default.Star,
            stringResource(R.string.valora_testers),
            stringResource(R.string.califica_la_calidad_del_feedback_recibido)
        ),
        Feature(Icons.Default.Groups,
            stringResource(R.string.comunidad_real),
            stringResource(R.string.usuarios_aleatorios_e_imparciales)
        ),
        Feature(Icons.Default.Assignment,
            stringResource(R.string.informes),
            stringResource(R.string.reportes_de_errores_y_rendimiento_detallados)
        )
    )
    Dialog(
        onDismissRequest = {   },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.bienvenido_a_testerapp),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.lanza_tu_app_con_total_confianza),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                features.forEach { feature ->
                    FeatureRow(feature)
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.entendido_y_listo))
                }
            }
        }
    }
}
@Composable
fun FeatureRow(feature: Feature) {
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = feature.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = feature.title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = feature.desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}