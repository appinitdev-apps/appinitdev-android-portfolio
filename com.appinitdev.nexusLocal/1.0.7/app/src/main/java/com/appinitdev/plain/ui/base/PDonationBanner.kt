
@Composable
fun PDonationBanner(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val locale = LocaleHelper.currentLocale()
    val isZhCN = locale.language == "zh" && locale.country == "CN"
    var showWeChatDialog by remember { mutableStateOf(false) }

    if (showWeChatDialog) {
        WeChatDonateDialog { showWeChatDialog = false }
    }

    val effectiveOnClick: () -> Unit = if (isZhCN) {
        { showWeChatDialog = true }
    } else {
        onClick
    }

    PFeatureBanner(
        modifier = modifier,
        tag = stringResource(Res.string.donation),
        title = stringResource(Res.string.donation_title),
        description = stringResource(Res.string.donation_desc),
        buttonText = stringResource(Res.string.buy_me_a_coffee),
        onClick = effectiveOnClick,
        tagIcon = Res.drawable.rocket,
        buttonIcon = Res.drawable.rocket,
    )
}

@Composable
private fun WeChatDonateDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("微信赞赏") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AsyncImage(
                    model = "file:///android_asset/donate_wechat.webp",
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "请用微信扫码",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        },
    )
}
