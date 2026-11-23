
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen() {
    var selectedPeriod by remember { mutableStateOf("Today") }
    var totalSales by remember { mutableStateOf(0.0) }
    var transactionCount by remember { mutableStateOf(0) }
    var averageTransaction by remember { mutableStateOf(0.0) }
    var topProducts by remember { mutableStateOf(listOf<TopProduct>()) }
    var customerAnalytics by remember { mutableStateOf(listOf<com.mopanesystems.ziposmobile.data.model.CustomerAnalytics>()) }

    val periods = listOf("Today", "This Week", "This Month", "This Year")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreenCompose(
    viewModel: AnalyticsViewModel,
) {
    val dailySales by viewModel.dailySales.observeAsState(emptyList())
    val topProducts by viewModel.topProducts.observeAsState(emptyList())
    val customerAnalytics by viewModel.customerAnalytics.observeAsState(emptyList())
    val totalSales by viewModel.totalSales.observeAsState(BigDecimal.ZERO)
    val transactionCount by viewModel.transactionCount.observeAsState(0)
    val averageTransaction by viewModel.averageTransaction.observeAsState(BigDecimal.ZERO)
    val totalRefunds by viewModel.totalRefunds.observeAsState(0)
    val isLoading by viewModel.isLoading.observeAsState(false)
    val errorMessage by viewModel.errorMessage.observeAsState()


    val todayStr = stringResource(R.string.today)
    val thisWeekStr = stringResource(R.string.this_week)
    val thisMonthStr = stringResource(R.string.this_month)
    val thisYearStr = stringResource(R.string.this_year)

    var selectedPeriod by remember { mutableStateOf("daily") }
    val periods = listOf(
        "daily" to todayStr,
        "weekly" to thisWeekStr,
        "monthly" to thisMonthStr,
        "yearly" to thisYearStr
    )
    var num=0
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.analytics_reporting),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.manage_your_analytics),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(periods) { (key, label) ->
                FilterChip(
                    onClick = {
                        selectedPeriod = key
                        viewModel.setPeriod(key)
                    },
                    label = { Text(label) },
                    selected = selectedPeriod == key
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        

         







        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.sales_summary),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =Arrangement.spacedBy(8.dp)
                        ) {
                            MetricCard(
                                title = stringResource(R.string.total_sales),
                                value = formatCurrency(totalSales),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                            MetricCard(
                                title = stringResource(R.string.transactions),
                                value = transactionCount.toString(),
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetricCard(
                                title = stringResource(R.string.avg_transaction),
                                value = formatCurrency(averageTransaction),
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                            MetricCard(
                                title = stringResource(R.string.products_sold),
                                value = totalRefunds.toString(),
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                val lineData = when (selectedPeriod) {
                    "daily" -> null
                    "weekly" -> viewModel.createWeeklySalesChartData()
                    "monthly" -> viewModel.createMonthlySalesChartData()
                    else  -> viewModel.createAnnualSalesChartData()
                }

                lineData?.let {
                    SalesTrendChart(lineData = it)
                }




            }

            item {
                Text(
                    text = stringResource(R.string.top_products),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }


            items(topProducts) { product ->
                num=num+1
                TopProductCardCompose(product, "#${num}  ")
            }
            num=0

             










        }
        

        Spacer(modifier = Modifier.height(16.dp))

        


        
        errorMessage?.let { message ->
            LaunchedEffect(message) {
                
            }
        }

    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,

    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun TopProductCard(product: TopProduct) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Quantity: ${product.quantitySold}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = "$${String.format("%.2f", product.revenue)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun CustomerAnalyticsCard(customer: com.mopanesystems.ziposmobile.data.model.CustomerAnalytics) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = customer.customerId, 
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Transactions: ${customer.totalTransactions}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Tier: ${customer.loyaltyTier}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "$${String.format("%.2f", customer.totalSpent)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

data class TopProduct(
    val id: String,
    val name: String,
    val quantitySold: Int,
    val revenue: Double,
)

@Composable
fun TopProductCardCompose(
    product: com.mopanesystems.ziposmobile.data.model.ProductPerformance,
    number: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = number, 
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.data, 
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.quantity) +" ${product.quantitySold}",
                    style = MaterialTheme.typography.bodyMedium
                )

            }

            Text(
                text = formatCurrency(product.revenue),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun CustomerAnalyticsCardCompose(customer: com.mopanesystems.ziposmobile.data.model.CustomerAnalytics) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = customer.customerId, 
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Transactions: ${customer.totalTransactions}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Tier: ${customer.loyaltyTier}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = formatCurrency(customer.totalSpent),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

fun formatCurrency(amount: BigDecimal): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    return formatter.format(amount)
}

