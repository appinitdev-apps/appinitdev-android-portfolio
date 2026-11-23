@Composable
fun FilePasteBar(
    filesVM: FilesViewModel,
    coroutineScope: CoroutineScope,
    onPasteComplete: () -> Unit
) {
    BottomAppBar {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                filesVM.cutFiles.clear()
                filesVM.copyFiles.clear()
                filesVM.showPasteBar.value = false
            }) {
                Icon(painter = painterResource(Res.drawable.x), contentDescription = "Cancel")
            }
            Text(
                text = if (filesVM.cutFiles.isNotEmpty())
                    pluralStringResource(Res.plurals.moving_items, filesVM.cutFiles.size, filesVM.cutFiles.size)
                else
                    pluralStringResource(Res.plurals.copying_items, filesVM.copyFiles.size, filesVM.copyFiles.size),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Button(onClick = {
                coroutineScope.launch {
                    if (filesVM.cutFiles.isNotEmpty()) {
                        executeCutFiles(filesVM, onPasteComplete)
                    } else if (filesVM.copyFiles.isNotEmpty()) {
                        executeCopyFiles(filesVM, onPasteComplete)
                    }
                }
            }) {
                Text(stringResource(Res.string.paste))
            }
        }
    }
} 