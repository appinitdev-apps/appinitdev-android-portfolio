@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigationSheet(
    navController: NavController,
    onHideSheet: () -> Unit,
    actionBadgeCount: Int,
    showPro: Boolean,
    isUpdateAvailable: Boolean,
    wasNotificationPermissionDenied: Boolean,
    onUpdateClicked: () -> Unit,
    onNotificationPermissionGranted: (Boolean) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onHideSheet,
        sheetState = sheetState,
        dragHandle = null,
    ) {
        MainNavigationSheetContent(
            settingsBadgeItemCount = actionBadgeCount,
            onUpdateClicked = onUpdateClicked,
            showPro = showPro,
            isUpdateAvailable = isUpdateAvailable,
            wasNotificationPermissionDenied = wasNotificationPermissionDenied,
            onNotificationPermissionGranted = onNotificationPermissionGranted,
            navigateToLabels = {
                navController.navigate(LabelsDest)
                onHideSheet()
            },
            navigateToStats = {
                navController.navigate(StatsDest)
                onHideSheet()
            },
            navigateToSettings = {
                navController.navigate(SettingsDest)
                onHideSheet()
            },
            navigateToBackup = {
                navController.navigate(BackupDest)
                onHideSheet()
            },
            navigateToAbout = {
                navController.navigate(AboutDest)
                onHideSheet()
            },
            navigateToPro = {
                navController.navigate(ProDest)
                onHideSheet()
            },
            onHideSheet = onHideSheet
        )
    }
}
@Composable
fun MainNavigationSheetContent(
    settingsBadgeItemCount: Int,
    onUpdateClicked: () -> Unit,
    showPro: Boolean,
    isUpdateAvailable: Boolean,
    wasNotificationPermissionDenied: Boolean,
    onNotificationPermissionGranted: (Boolean) -> Unit,
    navigateToLabels: () -> Unit,
    navigateToStats: () -> Unit,
    navigateToSettings: () -> Unit,
    navigateToBackup: () -> Unit,
    navigateToAbout: () -> Unit,
    navigateToPro: () -> Unit,
    onHideSheet: () -> Unit,
) {
    val context = LocalContext.current
    val mainViewModel = koinInject<MainViewModel>()
    Column(
        modifier =
            Modifier
                .padding(horizontal = 4.dp, vertical = 8.dp)
                .animateContentSize()
                .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top,
    ) {
        if (showPro) {
            Spacer(Modifier.height(12.dp))
            ProListItem { navigateToPro() }
        }
        IconTextButton(
            title = stringResource(R.string.labels_title),
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Label,
                    contentDescription = stringResource(R.string.labels_title),
                )
            },
            onClick = navigateToLabels,
        )
        IconTextButton(
            title = stringResource(R.string.stats_title),
            icon = {
                Icon(
                    imageVector = EvaIcons.Outline.PieChart,
                    contentDescription = stringResource(R.string.stats_title),
                )
            },
            onClick = navigateToStats,
        )
        if (settingsBadgeItemCount != 0) {
            ActionSection(
                wasNotificationPermissionDenied = wasNotificationPermissionDenied,
                isUpdateAvailable = isUpdateAvailable,
                onNotificationPermissionGranted = onNotificationPermissionGranted,
                onUpdateClicked = onUpdateClicked,
            )
        } else {
        }
        IconTextButton(
            title = stringResource(R.string.settings_title),
            icon = {
                Icon(
                    imageVector = EvaIcons.Outline.Settings,
                    contentDescription = stringResource(R.string.settings_title),
                )
            },
            onClick = navigateToSettings,
        )
        IconTextButton(
            title = stringResource(R.string.backup_and_restore_title),
            icon = {
                Icon(
                    imageVector = EvaIcons.Outline.Sync,
                    contentDescription = stringResource(R.string.backup_and_restore_title),
                )
            },
            onClick = {
                navigateToBackup()
            },
        )
        IconTextButton(
            title =  stringResource(R.string.tutorial_title),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Preview,
                    contentDescription = stringResource(R.string.tutorial_title),
                )
            },
            onClick = {
                mainViewModel.setShowTutorial(true)
                onHideSheet()
            },
        )
        IconTextButton(
            title = stringResource(R.string.about_app_intro),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Flag,
                    contentDescription = stringResource(R.string.about_app_intro),
                )
            },
            onClick = {
                mainViewModel.setShowOnboarding(true)
                onHideSheet()
            },
            )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 1.dp, vertical = 1.dp),
            horizontalArrangement = Arrangement.End, 
        ) {
            Text(
                text = "v${context.getVersionName()}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            )
        }
    }
}
