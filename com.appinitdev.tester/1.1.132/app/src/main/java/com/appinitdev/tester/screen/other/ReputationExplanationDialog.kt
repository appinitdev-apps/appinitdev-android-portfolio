
@Composable
fun ReputationExplanationDialog(
    profile: com.appinitdev.tester.model.UserProfile?,
    onDismiss: () -> Unit
) {
    val nivelReal = profile?.nivel ?: 0.0
    val puntosReales = profile?.puntos ?: 0
    val estrellasReales = profile?.promedioEstrellas ?: 5.0
    val numVotos = profile?.calificaciones?.size ?: 0
    val maxAppsPosibles = DevAppsConfig.developerApps.size.toDouble()
    val puntosExtra = profile?.puntosExtra ?: 0.0

    // 1. Nivel (30%)
    val factorNivel = (nivelReal / maxAppsPosibles).coerceAtMost(1.0)
    val porcentajeNivelObtenido = (factorNivel * 30).toInt()

    // 2. Puntos (25%)
    val factorPuntos = (Math.log10(puntosReales.toDouble() + 1.0) / Math.log10(501.0)).coerceAtMost(1.0)
    val porcentajePuntosObtenido = (factorPuntos * 25).toInt()

    // 3. Estrellas (45%)
    val confianza: Double = (numVotos.toDouble() / (numVotos + 5)).coerceIn(0.0, 1.0)
    val estrellasAjustadas: Double = ((estrellasReales.toDouble() * confianza) + (3.0 * (1.0 - confianza)))
    val factorEstrellas = (estrellasAjustadas / 5.0).coerceIn(0.0, 1.0)
    val porcentajeEstrellasObtenido = (factorEstrellas * 45).toInt()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(vertical = 20.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.tu_reputaci_n),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.tu_puntaje_actual_es_de),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = String.format("%.4f", profile?.reputacion ?: 0.0),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- FACTORES CON SIGNIFICADO ---

                ReputationFactorCard(
                    icon = Icons.Default.WorkspacePremium,
                    title = stringResource(R.string.nivel_de_instalaci_n),
                    percentage = 30,
                    userPercentage = porcentajeNivelObtenido,
                    description = stringResource(R.string.representa_tu_compromiso_cuantas_m_s_apps_del_sistema_instales_m_s_cerca_estar_s_del_30),
                    color = Color(0xFF4CAF50)
                )

                ReputationFactorCard(
                    icon = Icons.Default.TrendingUp,
                    title = stringResource(R.string.puntos_de_actividad),
                    percentage = 25,
                    userPercentage = porcentajePuntosObtenido,
                    description = stringResource(R.string.mide_tu_experiencia_los_primeros_puntos_valen_m_s_para_ayudarte_a_subir_r_pido_al_inicio),
                    color = Color(0xFF2196F3)
                )

                ReputationFactorCard(
                    icon = Icons.Default.Star,
                    title = stringResource(R.string.confianza_bayesiana),
                    percentage = 45,
                    userPercentage = porcentajeEstrellasObtenido,
                    description = stringResource(R.string.valora_tu_calidad_combina_tu_promedio_de_estrellas_con_tu_cantidad_de_votos_recibidos),
                    color = Color(0xFFFF9800)
                )

                if (puntosExtra > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        thickness = DividerDefaults.Thickness,
                        color = DividerDefaults.color
                    )

                    Surface(
                        color = Color(0xFFFFD700).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = Color(0xFFFFA000)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    stringResource(R.string.bono_extra) +"${String.format("%.2f", puntosExtra)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFA000)
                                )
                                Text(
                                    stringResource(R.string.este_puntaje_se_suma_directamente_para_completar_tu_reputaci_n_m_xima),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.entendido), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ReputationFactorCard(
    icon: ImageVector,
    title: String,
    percentage: Int,
    userPercentage: Int,
    description: String,
    color: Color
) {
    // Cálculo de la barra relativo al tope de la categoría (30, 25 o 45)
    val progresoRelativo = if (percentage > 0) userPercentage.toFloat() / percentage.toFloat() else 0f

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, modifier = Modifier.size(20.dp), tint = color)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = "$userPercentage% / $percentage%",
                        style = MaterialTheme.typography.labelLarge,
                        color = color,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progresoRelativo.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }
        }

        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 48.dp, top = 4.dp),
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}