@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WebSettingsPage(navController: NavHostController, webVM: WebConsoleViewModel = viewModel()) {
    WebSettingsProvider {
        val context = LocalContext.current
        val keepAwake = LocalKeepAwake.current
        val scope = rememberCoroutineScope()
        val enabledPermissions = LocalApiPermissions.current
        val permissionList = remember { mutableStateOf(Permissions.getWebList(context)) }
        val shouldIgnoreOptimize = remember { mutableStateOf(!powerManager.isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID)) }
        val systemAlertWindow = remember { mutableStateOf(Permission.SYSTEM_ALERT_WINDOW.can(context)) }
        val notificationListenerGranted = remember { mutableStateOf(Permission.NOTIFICATION_LISTENER.can(context)) }
        WebSettingsEffects(permissionList, shouldIgnoreOptimize, systemAlertWindow, notificationListenerGranted)
        PScaffold(topBar = {
            PTopAppBar(navController = navController, title = stringResource(Res.string.web_settings))
        }, content = { paddingValues ->
            LazyColumn(modifier = Modifier.padding(top = paddingValues.calculateTopPadding())) {
                item {
                    TopSpace()
                    PCard {
                        PListItem(
                            modifier = Modifier.clickable { navController.navigate(Routing.Connections) },
                            icon = Res.drawable.devices, title = stringResource(Res.string.connections), showMore = true
                        )
                        PListItem(
                            modifier = Modifier.clickable { navController.navigate(Routing.WebSecurity) },
                            icon = Res.drawable.lock, title = stringResource(Res.string.security), showMore = true
                        )
                        PListItem(
                            modifier = Modifier.clickable { navController.navigate(Routing.HowToUse) },
                            icon = Res.drawable.info, title = stringResource(Res.string.how_to_use), showMore = true
                        )
                    }
                    VerticalSpace(dp = 16.dp)
                }
                item { Subtitle(text = stringResource(Res.string.permissions)) }
                itemsIndexed(permissionList.value) { index, m ->
                    val permission = m.permission
                    PListItem(
                        modifier = PlainTheme
                            .getCardModifier(index = index, size = permissionList.value.size)
                            .clickable { togglePermission(scope, context, m, !enabledPermissions.contains(permission.name)) },
                        icon = m.icon, title = permission.getText(),
                        subtitle = stringResource(if (m.granted) Res.string.system_permission_granted else Res.string.system_permission_not_granted)
                    ) {
                        PSwitch(activated = enabledPermissions.contains(permission.name)) { enable -> togglePermission(scope, context, m, enable) }
                    }
                }
                if (AppFeatureType.NOTIFICATIONS.has()) {
                    item {
                        VerticalSpace(dp = 16.dp)
                        PCard {
                            val m = PermissionItem.create(context, Res.drawable.bell, Permission.NOTIFICATION_LISTENER)
                            val permission = m.permission
                            val enabled = notificationListenerGranted.value && enabledPermissions.contains(permission.name)
                            PListItem(
                                modifier = Modifier.clickable { togglePermission(scope, context, m, !enabled) },
                                icon = m.icon, title = permission.getText(),
                                subtitle = stringResource(if (notificationListenerGranted.value) Res.string.system_permission_granted else Res.string.system_permission_not_granted)
                            ) {
                                PSwitch(activated = enabled) { enable -> togglePermission(scope, context, m, enable) }
                            }
                            if (enabled) {
                                PListItem(
                                    modifier = Modifier.clickable { navController.navigate(Routing.NotificationSettings) },
                                    icon = Res.drawable.settings, title = stringResource(Res.string.notification_filter_settings),
                                    subtitle = stringResource(Res.string.notification_filter_settings_desc), showMore = true
                                )
                            }
                        }
                    }
                }
                item {
                    VerticalSpace(dp = 16.dp)
                    val m = PermissionItem(null, Permission.NONE, setOf(Permission.NONE))
                    PCard {
                        PListItem(modifier = Modifier.clickable {
                            val intent = Intent(if (context.isTV()) Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS else Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            intent.addCategory(Intent.CATEGORY_DEFAULT); intent.data = Uri.fromParts("package", context.packageName, null)
                            if (intent.resolveActivity(packageManager) != null) context.startActivity(intent) else DialogHelper.showMessage(Res.string.not_supported_error)
                        }, icon = m.icon, title = m.permission.getText(), showMore = true)
                    }
                }
                item {
                    VerticalSpace(dp = 16.dp); Subtitle(text = stringResource(Res.string.performance))
                    PCard {
                        PListItem(modifier = Modifier.clickable { webVM.enableKeepAwake(context, !keepAwake) }, title = stringResource(Res.string.keep_awake)) {
                            PSwitch(activated = keepAwake) { enable -> webVM.enableKeepAwake(context, enable) }
                        }
                    }
                    Tips(stringResource(Res.string.keep_awake_tips))
                    VerticalSpace(dp = 16.dp)
                    PCard {
                        PListItem(modifier = Modifier.clickable {
                            if (shouldIgnoreOptimize.value) webVM.requestIgnoreBatteryOptimization()
                            else {
                                val intent = Intent(); intent.action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS; context.startActivity(intent)
                            }
                        }, title = stringResource(if (shouldIgnoreOptimize.value) Res.string.disable_battery_optimization else Res.string.battery_optimization_disabled), showMore = true)
                    }
                    Tips(stringResource(Res.string.battery_optimization_tips))
                }
                item {
                    VerticalSpace(dp = 16.dp)
                    PCard {
                        PListItem(
                            modifier = Modifier.clickable { navController.navigate(Routing.WebDev) },
                            icon = Res.drawable.code, title = stringResource(Res.string.developer_options), showMore = true
                        )
                    }
                }
                item { BottomSpace(paddingValues) }
            }
        })
    }
}
