@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun MdEditorBottomAppBar(
    mdEditorVM: MdEditorViewModel,
) {
    val scrollState = rememberScrollState()
    val scrollState2 = rememberScrollState()
    val context = LocalContext.current
    if (mdEditorVM.showSettings) {
        MdEditorSettingsDialog(mdEditorVM = mdEditorVM)
    }
    if (mdEditorVM.showInsertImage) {
        MdEditorInsertImageDialog(mdEditorVM = mdEditorVM)
    }
    if (mdEditorVM.showColorPicker) {
        ColorPickerDialog(
            stringResource(Res.string.pick_color),
            initValue = "FFFFFFFF",
            onDismiss = {
                mdEditorVM.showColorPicker = false
            }, onConfirm = {
                val hex = it.checkColorHex()
                if (hex != null) {
                    mdEditorVM.insertColor("#$hex")
                } else {
                    DialogHelper.showMessage(LocaleHelper.getString(Res.string.invalid_value))
                }
            })
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.cardBackgroundNormal)
            .navigationBarsPadding()
    ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (mdEditorVM.level == 0) {
            Row(
                modifier =
                Modifier
                    .weight(1f)
                    .horizontalScroll(scrollState),
            ) {
                mdAccessoryItems.forEach { button ->
                    TextButton(onClick = {
                        mdEditorVM.textFieldState.edit { inlineWrap(button.before, button.after) }
                    }) {
                        Text(button.text, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp))
                    }
                }
            }
        } else {
            Row(
                modifier =
                Modifier
                    .weight(1f)
                    .horizontalScroll(scrollState2),
            ) {
                mdAccessoryItems2.forEach { button ->
                    PIconButton(
                        icon = button.icon,
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.primary,
                        click = {
                            button.click(mdEditorVM)
                        },
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.CenterEnd
        ) {
            PIconButton(
                icon = if (mdEditorVM.level == 0) Res.drawable.looks_one else Res.drawable.looks_two,
                contentDescription = "",
                tint = MaterialTheme.colorScheme.onPrimary,
                click = {
                    mdEditorVM.toggleLevel(context)
                },
            )
        }
    }
    } 
}