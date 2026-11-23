
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebSecurityPage(navController: NavHostController) {
    WebSettingsProvider {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val passwordType = LocalPasswordType.current
        val password = LocalPassword.current
        val authTwoFactor = LocalAuthTwoFactor.current
        val rotateUrlTokenOnRestart = LocalRotateUrlTokenOnRestart.current
        var urlToken by remember { mutableStateOf(Base64.encodeToString(TempData.urlToken, Base64.NO_WRAP)) }
        var keyStorePassword by remember { mutableStateOf("") }
        var sslSignature by remember { mutableStateOf("") }
        val editPassword = remember { mutableStateOf("") }

        LaunchedEffect(password) {
            if (editPassword.value != password) editPassword.value = password
            scope.launch(Dispatchers.IO) {
                keyStorePassword = KeyStorePasswordPreference.getAsync()
                try {
                    sslSignature = HttpServerManager.getSSLSignature(context, keyStorePassword).joinToString(" ") { "%02x".format(it).uppercase() }
                } catch (ex: Exception) {
                    LogCat.e("Failed to get SSL signature: ${ex.message}"); ex.printStackTrace()
                }
            }
        }

        PScaffold(
            topBar = { PTopAppBar(navController = navController, title = stringResource(Res.string.security)) },
            content = { paddingValues ->
                LazyColumn(modifier = Modifier.padding(top = paddingValues.calculateTopPadding())) {
                    item { TopSpace() }
                    item {
                        PCard {
                            PListItem(modifier = Modifier.clickable {
                                scope.launch(Dispatchers.IO) {
                                    PasswordTypePreference.putAsync(
                                        context,
                                        if (passwordType == PasswordType.NONE.value) PasswordType.FIXED.value else PasswordType.NONE.value
                                    )
                                }
                            }, title = stringResource(Res.string.require_password)) {
                                PSwitch(activated = passwordType != PasswordType.NONE.value) {
                                    scope.launch(Dispatchers.IO) {
                                        PasswordTypePreference.putAsync(
                                            context,
                                            if (passwordType == PasswordType.NONE.value) PasswordType.FIXED.value else PasswordType.NONE.value
                                        )
                                    }
                                }
                            }
                            if (passwordType != PasswordType.NONE.value) {
                                PasswordTextField(
                                    value = editPassword.value, isChanged = { editPassword.value != password },
                                    onValueChange = { editPassword.value = it }, onConfirm = { scope.launch(Dispatchers.IO) { PasswordPreference.putAsync(it) } })
                                OutlinedButton(
                                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 16.dp),
                                    onClick = { scope.launch(Dispatchers.IO) { editPassword.value = HttpServerManager.resetPasswordAsync() } }) {
                                    Text(stringResource(Res.string.generate_password))
                                }
                            }
                        }
                    }
                    item {
                        VerticalSpace(dp = 16.dp)
                        PCard {
                            PListItem(
                                modifier = Modifier.clickable { scope.launch(Dispatchers.IO) { AuthTwoFactorPreference.putAsync(!authTwoFactor) } },
                                title = stringResource(Res.string.require_confirmation)
                            ) {
                                PSwitch(activated = authTwoFactor) { scope.launch(Dispatchers.IO) { AuthTwoFactorPreference.putAsync(it) } }
                            }
                        }
                        Tips(text = stringResource(Res.string.two_factor_auth_tips)); VerticalSpace(dp = 24.dp)
                    }
                    item {
                        Subtitle(text = stringResource(Res.string.https_certificate_signature))
                        ClipboardCard(label = stringResource(Res.string.https_certificate_signature), text = sslSignature)
                        VerticalSpace(dp = 16.dp)
                        PFilledButton(text = stringResource(Res.string.reset_ssl_certificate), type = ButtonType.DANGER, modifier = Modifier.padding(horizontal = 16.dp), onClick = {
                            scope.launch(Dispatchers.IO) {
                                DialogHelper.showLoading(); KeyStorePasswordPreference.resetAsync()
                                keyStorePassword = KeyStorePasswordPreference.getAsync()
                                HttpServerManager.generateSSLKeyStore(File(context.filesDir, Constants.KEY_STORE_FILE_NAME), keyStorePassword)
                                DialogHelper.hideLoading()
                                DialogHelper.showConfirmDialog("", LocaleHelper.getStringAsync(Res.string.ssl_certificate_reset)) { sendEvent(RestartAppEvent()) }
                            }
                        })
                        VerticalSpace(dp = 24.dp); Subtitle(text = stringResource(Res.string.url_token))
                        ClipboardCard(label = stringResource(Res.string.url_token), text = urlToken)
                        Tips(text = stringResource(Res.string.url_token_tips)); VerticalSpace(dp = 16.dp)
                        PCard {
                            PListItem(modifier = Modifier.clickable {
                                scope.launch(Dispatchers.IO) { RotateUrlTokenOnRestartPreference.putAsync(!rotateUrlTokenOnRestart) }
                            }, title = stringResource(Res.string.rotate_url_token_on_restart)) {
                                PSwitch(activated = rotateUrlTokenOnRestart) {
                                    scope.launch(Dispatchers.IO) { RotateUrlTokenOnRestartPreference.putAsync(it) }
                                }
                            }
                        }
                        Tips(text = stringResource(Res.string.rotate_url_token_on_restart_tips)); VerticalSpace(dp = 16.dp)
                        PFilledButton(text = stringResource(Res.string.reset_token), type = ButtonType.DANGER, modifier = Modifier.padding(horizontal = 16.dp), onClick = {
                            scope.launch(Dispatchers.IO) {
                                UrlTokenPreference.resetAsync(); urlToken = Base64.encodeToString(TempData.urlToken, Base64.NO_WRAP); DialogHelper.showMessage(Res.string.the_token_is_reset)
                            }
                        })
                        BottomSpace(paddingValues)
                    }
                }
            })
    }
}
