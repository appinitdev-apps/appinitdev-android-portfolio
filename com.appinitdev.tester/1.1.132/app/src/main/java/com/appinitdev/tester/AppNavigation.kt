
@Composable
fun AppNavigation(
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier,
    startAppId: String? = null
) {
    val navController = rememberNavController()
    val isLoggedIn = viewModel.isUserLoggedIn()

    LaunchedEffect(isLoggedIn, startAppId) {
        if (isLoggedIn) {
            viewModel.fetchUserProfile()
            if (!startAppId.isNullOrBlank()) {
                
                navController.navigate("app_detail/$startAppId")
            }
        }
    }

    val startDest = if (viewModel.isUserLoggedIn()) "home" else "login"
    NavHost(navController = navController, startDestination = startDest) {
        composable("login") {
            LoginScreen(navController, viewModel)
        }
        composable("register") {
            RegisterScreen(navController, viewModel)
        }
        composable("forgot_password") {
            ForgotPasswordScreen(navController, viewModel)
        }

        composable("edit_profile") {
            EditProfileScreen(navController, viewModel)
        }

        composable("home") {
            HomeScreen(navController,viewModel)
        }
        composable(
            route = "app_detail/{appId}",
            arguments = listOf(navArgument("appId") { type = NavType.StringType })
        ) { backStackEntry ->
            val appId = backStackEntry.arguments?.getString("appId") ?: ""
            AppDetailScreen(appId, viewModel) { navController.popBackStack() }
        }

        composable("verification_screen") {
            VerificationSection(viewModel = viewModel, onBack = { navController.popBackStack() })
        }

        composable("production_apps") {
            ProductionAppsScreen(viewModel, navController)
        }

        composable("ranking_screen") {
            
            ReputationRankingScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("notifications") {
            com.appinitdev.tester.screen.NotificationCenterScreen(
                viewModel = viewModel,
                navController = navController,
                onBack = { navController.popBackStack() }
            )
        }

        composable("points_history") {
            com.appinitdev.tester.screen.PointsHistoryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }





        composable(
            route = "autorizar_resenas/{appId}",
            arguments = listOf(navArgument("appId") { type = NavType.StringType })
        ) { backStackEntry ->
            val appId = backStackEntry.arguments?.getString("appId") ?: ""

            
            PanelAutorizacionPropietario(
                appId = appId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

    }
}