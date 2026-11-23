@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation(navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    val viewModelPOS: POSViewModel = hiltViewModel()
    val viewModelCustomer: CustomerViewModel = hiltViewModel()
    val itemCount by viewModelPOS.itemCount.collectAsStateWithLifecycle()
    val lowStockCount by viewModelPOS.lowStockCount.observeAsState(0)
    val outOfStockCount by viewModelPOS.outOfStockCount.observeAsState(0)
    val barcodeScannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val barcode = result.data?.getStringExtra(BarcodeScannerActivity.EXTRA_BARCODE_RESULT)
            if (!barcode.isNullOrEmpty()) {
                Log.e("codelab", barcode)
                viewModelPOS.findProductByBarcode(barcode)
            }
        }
    }
    val startBarcodeScanner = {
        val intent = Intent(context, BarcodeScannerActivity::class.java)
        barcodeScannerLauncher.launch(intent)
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    BadgedBox(
                        badge = {
                            if ((lowStockCount+outOfStockCount) > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary,  
                                    contentColor = MaterialTheme.colorScheme.onPrimary   
                                ) {
                                    Text((lowStockCount+outOfStockCount).toString())
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = stringResource(R.string.warning),
                            modifier = Modifier.clickable {
                                navController.navigate("inventory")
                            }
                        )
                    }
                    Spacer(modifier = Modifier.width(18.dp))
                    BadgedBox(
                        badge = {
                            if (itemCount > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary,  
                                    contentColor = MaterialTheme.colorScheme.onPrimary   
                                ) {
                                    Text(itemCount.toString())
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = stringResource(R.string.cart),
                            modifier = Modifier.clickable {
                                navController.navigate("pos")
                            }
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.configuraciones))
                    }
                }
            )
        },
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            if (currentRoute !in listOf("settings", "categories", "store", "explorer"))
                BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "pos",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("settings") {
                SettingsScreenCompose(
                    onBack = { navController.popBackStack() },
                    onNavigateToCategories = { navController.navigate("categories") },
                    onNavigateToStore = { navController.navigate("store") },
                    onNavigateToExplorer = { navController.navigate("explorer") }
                )
            }
            composable("categories") {
                CategoryCrudScreen(
                    onBack = { navController.popBackStack() },
                    viewModel = hiltViewModel(),
                )
            }
            composable("store") {
                val storeViewModel: StoreViewModel = hiltViewModel()
                StoreScreen(
                    viewModel = storeViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("explorer") {
                FileExplorerWithViewer(context = context, onBack = { navController.popBackStack() })
            }
            composable("pos") {
                POSScreenCompose(
                    viewModel = viewModelPOS,
                    onScanBarcode = startBarcodeScanner,
                    onProcessPayment = { result ->
                        viewModelPOS.processTransaction(
                            result.paymentMethod,
                            paymentResultInfo = result
                        )
                    },
                    onClearCart = { viewModelPOS.clearCart() },
                    currencyFormatter = NumberFormat.getCurrencyInstance(),
                    viewModelCustomerViewModel = viewModelCustomer,
                )
            }
            composable("products") {
                ProductsScreenCompose(
                    viewModel = hiltViewModel(),
                    viewModelPOS = viewModelPOS
                )
            }
            composable("customers") {
                CustomersScreenCompose(
                    viewModel = hiltViewModel()
                )
            }
            composable("inventory") {
                InventoryScreenCompose(
                    viewModel = hiltViewModel()
                )
            }
            composable("analytics") {
                AnalyticsScreenCompose(
                    viewModel = hiltViewModel()
                )
            }
        }
    }
}
@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("pos", "POS", Icons.Default.PointOfSale),
        BottomNavItem("products", stringResource(R.string.productst), Icons.Default.Inventory),
        BottomNavItem("customers", stringResource(R.string.customerst), Icons.Default.People),
        BottomNavItem("inventory", stringResource(R.string.inventoryt), Icons.Default.Storage),
        BottomNavItem("analytics", stringResource(R.string.analyticst), Icons.Default.Analytics)
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
)
