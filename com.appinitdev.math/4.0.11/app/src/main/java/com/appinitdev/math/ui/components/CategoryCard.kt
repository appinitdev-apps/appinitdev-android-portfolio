@Composable
fun CategoryCard(
    category: MathCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (iconRes, gradientColors) = when (category.id) {
        "introduction" -> Pair(R.drawable.ic_introduction, listOf(Color(0xFF3B82F6), Color(0xFF60A5FA)))
        "sets-and-numbers" -> Pair(R.drawable.ic_sets, listOf(Color(0xFF2563EB), Color(0xFF3B82F6)))
        "powers-radicals-logarithms" -> Pair(R.drawable.ic_powers, listOf(Color(0xFF7C3AED), Color(0xFF8B5CF6)))
        "polynomials" -> Pair(R.drawable.ic_polynomials, listOf(Color(0xFF9333EA), Color(0xFFA855F7)))
        "equations" -> Pair(R.drawable.ic_equations, listOf(Color(0xFF1D4ED8), Color(0xFF60A5FA)))
        "inequalities" -> Pair(R.drawable.ic_inequalities, listOf(Color(0xFF0284C7), Color(0xFF38BDF8)))
        "linear-systems" -> Pair(R.drawable.ic_linear_systems, listOf(Color(0xFFD97706), Color(0xFFFBBF24)))
        "complex-numbers" -> Pair(R.drawable.ic_complex, listOf(Color(0xFFDB2777), Color(0xFFEC4899)))
        "vectors-and-matrices" -> Pair(R.drawable.ic_vectors, listOf(Color(0xFFEA580C), Color(0xFFF97316)))
        "algebraic-structures" -> Pair(R.drawable.ic_structures, listOf(Color(0xFFE11D48), Color(0xFFFB7185)))
        "trigonometry" -> Pair(R.drawable.ic_trigonometry, listOf(Color(0xFF059669), Color(0xFF10B981)))
        "functions" -> Pair(R.drawable.ic_functions, listOf(Color(0xFF16A34A), Color(0xFF4ADE80)))
        "lines-planes-conic-sections" -> Pair(R.drawable.ic_conics, listOf(Color(0xFF0D9488), Color(0xFF2DD4BF)))
        "limits" -> Pair(R.drawable.ic_limits, listOf(Color(0xFF0891B2), Color(0xFF22D3EE)))
        "derivatives" -> Pair(R.drawable.ic_derivatives, listOf(Color(0xFF6D28D9), Color(0xFFA78BFA)))
        "differential-calculus-theorems" -> Pair(R.drawable.ic_calculus, listOf(Color(0xFF4338CA), Color(0xFF818CF8)))
        "integrals" -> Pair(R.drawable.ic_integrals, listOf(Color(0xFF0284C7), Color(0xFF06B6D4)))
        "differential-equations" -> Pair(R.drawable.ic_diff_eq, listOf(Color(0xFF0F766E), Color(0xFF14B8A6)))
        "sequences" -> Pair(R.drawable.ic_sequences, listOf(Color(0xFFC2410C), Color(0xFFFB923C)))
        "series" -> Pair(R.drawable.ic_series, listOf(Color(0xFFB45309), Color(0xFFF59E0B)))
        "probability-and-statistics" -> Pair(R.drawable.ic_probability, listOf(Color(0xFFC026D3), Color(0xFFE879F9)))
        "kinematics" -> Pair(R.drawable.ic_kinematics, listOf(Color(0xFFD97706), Color(0xFFFCD34D)))
        "financial-math" -> Pair(R.drawable.ic_financial, listOf(Color(0xFF15803D), Color(0xFF84CC16)))
        "logic" -> Pair(R.drawable.ic_logic, listOf(Color(0xFF9D174D), Color(0xFFF472B6)))
        "various" -> Pair(R.drawable.ic_various, listOf(Color(0xFF475569), Color(0xFF94A3B8)))
        else -> Pair(R.drawable.ic_algebra, listOf(Color(0xFF2563EB), Color(0xFF60A5FA)))
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(gradientColors)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = category.title,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = category.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = androidx.compose.ui.res.stringResource(id = R.string.category_topics_count, category.topicCount),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = category.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
