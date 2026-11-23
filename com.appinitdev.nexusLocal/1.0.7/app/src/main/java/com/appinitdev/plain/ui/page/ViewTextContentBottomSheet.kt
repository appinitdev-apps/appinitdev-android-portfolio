
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ViewTextContentBottomSheet(
    textFileVM: TextFileViewModel,
    content: String,
) {
    val context = LocalContext.current
    val onDismiss = {
        textFileVM.showMoreActions.value = false
    }

    PModalBottomSheet(
        onDismissRequest = {
            onDismiss()
        },
    ) {
        LazyColumn {
            item {
                VerticalSpace(32.dp)
            }
            item {
                ActionButtons {
                    IconTextShareButton {
                        ShareHelper.shareText(context, content)
                        onDismiss()
                    }
                    IconTextToTopButton {
                        textFileVM.gotoTop()
                        onDismiss()
                    }
                    IconTextToBottomButton {
                        textFileVM.gotoEnd()
                        onDismiss()
                    }
                }
            }
            item {
                VerticalSpace(dp = 24.dp)
                PCard {
                    PListItem(title = stringResource(Res.string.wrap_content), action = {
                        PSwitch(
                            activated = textFileVM.wrapContent.value,
                        ) {
                            textFileVM.toggleWrapContent(context)
                        }
                    })
                }
            }

            item {
                BottomSpace()
            }
        }
    }
}


