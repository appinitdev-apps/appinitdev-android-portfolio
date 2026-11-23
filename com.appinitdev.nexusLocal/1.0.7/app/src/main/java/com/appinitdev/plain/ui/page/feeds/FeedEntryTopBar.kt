@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun FeedEntryTopBar(
    navController: NavHostController, feedEntryVM: FeedEntryViewModel,
    scrollBehavior: TopAppBarScrollBehavior, scope: CoroutineScope, context: Context,
    onScrollToTop: () -> Unit,
) {
    PTopAppBar(
        modifier = Modifier.combinedClickable(onClick = {}, onDoubleClick = { onScrollToTop() }),
        navController = navController, title = "", scrollBehavior = scrollBehavior,
        actions = {
            PIconButton(icon = Res.drawable.label, contentDescription = stringResource(Res.string.select_tags), tint = MaterialTheme.colorScheme.onSurface) {
                feedEntryVM.showSelectTagsDialog.value = true
            }
            PIconButton(icon = Res.drawable.chrome, contentDescription = stringResource(Res.string.open_in_web), tint = MaterialTheme.colorScheme.onSurface) {
                val m = feedEntryVM.item.value ?: return@PIconButton; WebHelper.open(context, m.url)
            }
            PIconButton(icon = Res.drawable.share_2, contentDescription = stringResource(Res.string.share), tint = MaterialTheme.colorScheme.onSurface) {
                val m = feedEntryVM.item.value ?: return@PIconButton; ShareHelper.shareText(context, m.title.let { it + "\n" } + m.url)
            }
            ActionButtonMoreWithMenu { dismiss ->
                PDropdownMenuItem(text = { Text(stringResource(Res.string.save_to_notes)) },
                    leadingIcon = { Icon(painter = painterResource(Res.drawable.save), contentDescription = stringResource(Res.string.save_to_notes)) },
                    onClick = {
                        dismiss(); val m = feedEntryVM.item.value ?: return@PDropdownMenuItem
                        scope.launch(Dispatchers.IO) {
                            val c = "# ${m.title}\n\n" + m.content.ifEmpty { m.description }
                            NoteHelper.saveToNotesAsync(m.id) { title = c.cut(250).replace("\n", ""); content = c }
                            DialogHelper.showMessage(Res.string.saved)
                        }
                    })
                PDropdownMenuItem(text = { Text(stringResource(Res.string.copy_link)) },
                    leadingIcon = { Icon(painter = painterResource(Res.drawable.link), contentDescription = stringResource(Res.string.copy_link)) },
                    onClick = {
                        dismiss(); val m = feedEntryVM.item.value ?: return@PDropdownMenuItem
                        val clip = ClipData.newPlainText(LocaleHelper.getString(Res.string.link), m.url)
                        clipboardManager.setPrimaryClip(clip); DialogHelper.showTextCopiedMessage(m.url)
                    })
            }
        },
    )
}
