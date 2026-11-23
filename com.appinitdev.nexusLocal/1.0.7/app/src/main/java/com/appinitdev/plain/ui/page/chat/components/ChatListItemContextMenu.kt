@Composable
fun ChatListItemContextMenu(
    navController: NavHostController,
    chatVM: ChatViewModel,
    m: VChat,
    showContextMenu: MutableState<Boolean>,
    onForward: (VChat) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    PDropdownMenu(
        expanded = showContextMenu.value && chatVM.selectedItem.value == m,
        onDismissRequest = {
            chatVM.selectedItem.value = null
            showContextMenu.value = false
        },
    ) {
        PDropdownMenuItem(
            text = { Text(stringResource(Res.string.select)) },
            onClick = {
                chatVM.enterSelectMode()
                chatVM.select(m.id)
                chatVM.selectedItem.value = null
                showContextMenu.value = false
            },
        )
        PDropdownMenuItem(
            text = { Text(stringResource(Res.string.forward)) },
            onClick = {
                chatVM.selectedItem.value = null
                showContextMenu.value = false
                onForward(m)
            },
        )
        if (m.value is DMessageText) {
            PDropdownMenuItem(
                text = { Text(stringResource(Res.string.copy_text)) },
                onClick = {
                    chatVM.selectedItem.value = null
                    showContextMenu.value = false
                    val text = (m.value as DMessageText).text
                    val clip = ClipData.newPlainText(LocaleHelper.getString(Res.string.message), text)
                    clipboardManager.setPrimaryClip(clip)
                    DialogHelper.showTextCopiedMessage(text)
                },
            )
            if (m.fromId == "me") {
                PDropdownMenuItem(
                    text = { Text(stringResource(Res.string.edit_text)) },
                    onClick = {
                        chatVM.selectedItem.value = null
                        showContextMenu.value = false
                        val content = (m.value as DMessageText).text
                        navController.navigateChatEditText(m.id, content)
                    },
                )
            }
        }
        PDropdownMenuItem(
            text = { Text(stringResource(Res.string.delete)) },
            onClick = {
                chatVM.selectedItem.value = null
                showContextMenu.value = false
                chatVM.delete(context, setOf(m.id))
            },
        )
    }
}
