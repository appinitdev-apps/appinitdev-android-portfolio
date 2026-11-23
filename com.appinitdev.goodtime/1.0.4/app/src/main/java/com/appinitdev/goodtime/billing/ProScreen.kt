@Composable
fun ProScreen(
    billing: GoogleBilling = koinInject<BillingAbstract>() as GoogleBilling,
    onNavigateBack: () -> Unit,
) {
    val activity = LocalActivity.current
    val productDetails by billing.productDetails.collectAsStateWithLifecycle()
    val isPro by billing.isPro.collectAsStateWithLifecycle(false)
    val isPending by billing.purchasePending.collectAsStateWithLifecycle(false)
    LaunchedEffect(isPending, isPro) {
        if (isPending || isPro) {
            onNavigateBack()
        }
    }
    productDetails.let {
        val offerDetails = it?.oneTimePurchaseOfferDetails
        if (it == null || activity == null || offerDetails == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val priceString = offerDetails.formattedPrice
            ProScreenContent(onNavigateBack, priceString) { billing.buy(activity) }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProScreenContent(
    onNavigateBack: () -> Unit,
    priceString: String,
    onClick: () -> Unit,
) {
    val listState = rememberScrollState()
    val color = MaterialTheme.colorScheme.primary
    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(R.string.unlock_premium),
                icon = Icons.Default.Close,
                onNavigateBack = { onNavigateBack() },
                showSeparator = listState.canScrollBackward,
            )
        },
        bottomBar = {
            if (listState.canScrollForward) {
                SubtleHorizontalDivider()
            }
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    stringResource(R.string.unlock_premium_tagline),
                    style = MaterialTheme.typography.labelSmall.copy(textAlign = TextAlign.Center),
                )
                ProListItem(subtitle = priceString, centered = true) {
                    onClick()
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            Column(Modifier.verticalScroll(listState)) {
                val productName = stringResource(R.string.product_name_long)
                Text(
                    modifier = Modifier.padding(12.dp),
                    text =
                        stringResource(R.string.unlock_premium_desc1, productName) + "\n" +
                            "\n" +
                            stringResource(R.string.unlock_premium_desc2) +
                            "\n" +
                            stringResource(R.string.unlock_premium_desc3),
                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                )
                Spacer(modifier = Modifier.height(16.dp))
                ProFeatureListItem(
                    title = stringResource(R.string.feature_labels_title),
                    subtitle =
                        listOf(
                            stringResource(R.string.feature_labels_desc1),
                            stringResource(R.string.feature_labels_desc2),
                        ),
                    icon = Icons.AutoMirrored.Outlined.Label,
                    color = color,
                )
                ProFeatureListItem(
                    title = stringResource(R.string.feature_timer_customization_title),
                    subtitle =
                        listOf(
                            stringResource(R.string.feature_timer_customization_desc1),
                            stringResource(R.string.feature_timer_customization_desc2),
                        ),
                    icon = EvaIcons.Outline.ColorPalette,
                    color = color,
                )
                ProFeatureListItem(
                    title = stringResource(R.string.feature_notifications_title),
                    subtitle =
                        listOf(
                            stringResource(R.string.feature_notifications_desc1),
                        ),
                    icon = EvaIcons.Outline.Bell,
                    color = color,
                )
                ProFeatureListItem(
                    title = stringResource(R.string.feature_stats_title),
                    subtitle =
                        listOf(
                            stringResource(R.string.feature_stats_desc1),
                            stringResource(R.string.feature_stats_desc2),
                            stringResource(R.string.feature_stats_desc3),
                        ),
                    icon = EvaIcons.Outline.PieChart,
                    color = color,
                )
                ProFeatureListItem(
                    title = stringResource(R.string.feature_backup_title),
                    subtitle =
                        listOf(
                            stringResource(R.string.feature_backup_desc1),
                            stringResource(R.string.feature_backup_desc2),
                        ),
                    icon = EvaIcons.Outline.Sync,
                    color = color,
                )
                ProFeatureListItem(
                    title = stringResource(R.string.feature_support_title),
                    subtitle =
                        listOf(
                            stringResource(R.string.feature_support_desc1),
                        ),
                    icon = EvaIcons.Outline.Heart,
                    color = color,
                )
            }
        }
    }
}
@Composable
fun ProFeatureListItem(
    title: String,
    subtitle: List<String>,
    icon: ImageVector,
    color: Color,
) {
    val background = color.copy(alpha = 0.38f)
    ListItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        leadingContent = {
            Icon(
                modifier =
                    Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(background)
                        .padding(4.dp),
                imageVector = icon,
                tint = color,
                contentDescription = title,
            )
        },
        headlineContent = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                )
                subtitle.forEach {
                    Text(text = it, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
    )
}
@Preview
@Composable
fun ProScreenPreview() {
    ApplicationTheme(darkTheme = true) {
        ProScreenContent(onNavigateBack = {}, onClick = {}, priceString = "42 USD")
    }
}
