
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarChat(
    navController: NavHostController,
    channelVM: ChannelViewModel,
    onNavigateBack: () -> Unit,
) {
    val showMenu = remember { mutableStateOf(false) }

    PTopAppBar(
        navController = navController,
        navigationIcon = {
            NavigationBackIcon(onClick = onNavigateBack)
        },
        title = stringResource(Res.string.chat),
        actions = {
            ActionButtonFolders {
                navController.navigateAppFiles()
            }
            ActionButtonAddWithMenu { dismiss ->
                PDropdownMenuItem(
                    text = { Text(stringResource(Res.string.new_channel)) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.hash),
                            contentDescription = null,
                        )
                    },
                    onClick = {
                        dismiss()
                        channelVM.showCreateChannelDialog.value = true
                    },
                )
                PDropdownMenuItem(
                    text = { Text(stringResource(Res.string.add_device)) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.plus),
                            contentDescription = null,
                        )
                    },
                    onClick = {
                        dismiss()
                        navController.navigate(Routing.Nearby())
                    },
                )
            }
        }
    )
} 