

@Composable
fun NearbyDeviceItem(
    item: DNearbyDevice,
    isPaired: Boolean = false,
    isPairing: Boolean = false,
    onPairClick: () -> Unit,
    onUnpairClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Surface(
        modifier = PlainTheme.getCardModifier(selected = isPaired),
        color = Color.Unspecified,
    ) {
        PListItem(
            title = item.name,
            subtitle = item.getBestIp(),
            icon = item.deviceType.getIcon(),
            action = {
                when {
                    isPairing -> {
                        POutlinedButton(
                            text = stringResource(Res.string.cancel),
                            onClick = onCancelClick,
                            type = ButtonType.DANGER,
                            buttonSize = ButtonSize.SMALL,
                            isLoading = true
                        )
                    }

                    isPaired -> {
                        POutlinedButton(
                            text = stringResource(Res.string.unpair),
                            onClick = onUnpairClick,
                            type = ButtonType.DANGER,
                            buttonSize = ButtonSize.SMALL,
                        )
                    }

                    else -> {
                        POutlinedButton(
                            text = stringResource(Res.string.pair),
                            onClick = onPairClick,
                            buttonSize = ButtonSize.SMALL,
                        )
                    }
                }
            }
        )
    }
}

