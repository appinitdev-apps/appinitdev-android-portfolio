
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(navController: NavController, viewModel: ChatViewModel, state: AppState) {
    val activity = LocalContext.current as com.appinitdev.chatok.MainActivity
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tienda_monedas), color = Color.White) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C1E))
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.tu_saldo_actual), color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = stringResource(R.string.monedas_count, state.userData?.balance ?: 0),
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            
            Text(
                text = stringResource(R.string.suscripcion_vip),
                color = Color(0xFFFF71FF),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp, bottom = 16.dp)
            )

            VipCard(
                title = stringResource(R.string.vip_days, 30),
                price = stringResource(R.string.monedas_count, 50),
                onClick = { viewModel.buyVip(30, 50) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            
            Text(
                text = stringResource(R.string.paquetes_monedas),
                color = Color(0xFF238CDD),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp, bottom = 16.dp)
            )

            CoinPack(
                amount = stringResource(R.string.monedas_count, 100),
                price = "$0.99",
                onClick = { activity.billingManager.launchPurchaseFlow(activity, "chatcoins_100") }
            )
            CoinPack(
                amount = stringResource(R.string.monedas_count, 500),
                price = "$4.49",
                onClick = { activity.billingManager.launchPurchaseFlow(activity, "chatcoins_500") }
            )
            CoinPack(
                amount = stringResource(R.string.monedas_count, 1000),
                price = "$7.99",
                onClick = { activity.billingManager.launchPurchaseFlow(activity, "chatcoins_1000") }
            )
        }
    }
}

@Composable
fun VipCard(title: String, price: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF71FF).copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, Color(0xFFFF71FF).copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFFF71FF).copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFF71FF))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.sin_anuncios_desc), color = Color.Gray, fontSize = 12.sp)
            }
            Text(price, color = Color(0xFFFF71FF), fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun CoinPack(amount: String, price: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFFFD700))
            Spacer(Modifier.width(16.dp))
            Text(text = amount, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text(text = price, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
        }
    }
}
