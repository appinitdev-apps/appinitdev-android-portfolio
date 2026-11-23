@Composable
internal fun SettingsCardItems(navController: NavHostController) {
    val darkTheme = LocalDarkTheme.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    PCard {
        PListItem(
            modifier = Modifier.clickable {
                navController.navigate(Routing.DarkTheme)
            },
            icon = Res.drawable.sun_moon,
            title = stringResource(Res.string.dark_theme),
            subtitle = DarkTheme.entries.find { it.value == darkTheme }?.getText() ?: "",
            separatedActions = true,
        ) {
            PSwitch(
                activated = DarkTheme.isDarkTheme(darkTheme),
            ) {
                scope.launch {
                    withIO {
                        DarkThemePreference.putAsync(if (it) DarkTheme.ON else DarkTheme.OFF)
                    }
                }
            }
        }
    }
    VerticalSpace(dp = 16.dp)
    PCard {
        PListItem(
            modifier = Modifier.clickable {
                navController.navigate(Routing.Language)
            },
            title = stringResource(Res.string.language),
            subtitle = stringResource(Res.string.language_desc),
            icon = Res.drawable.languages,
            showMore = true,
        )
    }
    VerticalSpace(16.dp)
    PCard {
        PListItem(
            modifier = Modifier.clickable {
                navController.navigate(Routing.BackupRestore)
            },
            title = stringResource(Res.string.backup_restore),
            subtitle = stringResource(Res.string.backup_desc),
            icon = Res.drawable.database_backup,
            showMore = true,
        )
    }
}
@Composable
internal fun DeveloperSettingsCard(navController: NavHostController) {
    PCard {
        PListItem(title = stringResource(Res.string.client_id), value = TempData.clientId)
        PListItem(
            modifier = Modifier.clickable { navController.navigate(Routing.WebDev) },
            title = stringResource(Res.string.developer_options),
            icon = Res.drawable.code,
            showMore = true,
        )
        PListItem(
            modifier = Modifier.clickable { navController.navigate(Routing.ComponentShowcase) },
            title = stringResource(Res.string.ui_components),
            icon = Res.drawable.layout_grid,
            showMore = true,
        )
        PListItem(
            modifier = Modifier.clickable { throw RuntimeException("Test crash triggered from Developer Options") },
            title = stringResource(Res.string.simulate_crash),
            subtitle = stringResource(Res.string.simulate_crash_desc),
            icon = Res.drawable.circle_alert,
        )
    }
}
