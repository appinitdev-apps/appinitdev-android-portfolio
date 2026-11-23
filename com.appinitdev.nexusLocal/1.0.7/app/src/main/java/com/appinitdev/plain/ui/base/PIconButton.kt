@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PIconButton(
    icon: DrawableResource,
    modifier: Modifier = Modifier,
    iconSize: Dp = 24.dp,
    tint: Color = LocalContentColor.current,
    contentDescription: String? = null,
    showBadge: Boolean = false,
    isHaptic: Boolean? = false,
    isSound: Boolean? = false,
    enabled: Boolean = true,
    click: () -> Unit = {},
) {
    val view = LocalView.current
    IconButton(
        modifier = modifier,
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors().copy(
            contentColor = tint,
            disabledContentColor = tint.copy(alpha = 0.38f)
        ),
        onClick = {
            if (isHaptic == true) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            if (isSound == true) view.playSoundEffect(SoundEffectConstants.CLICK)
            click()
        },
    ) {
        if (showBadge) {
            BadgedBox(
                badge = {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .offset(x = 4.dp, y = (-6).dp)
                            .background(MaterialTheme.colorScheme.badgeBorderColor, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(MaterialTheme.colorScheme.red, shape = CircleShape)
                        )
                    }
                }
            ) {
                PIcon(
                    modifier = Modifier.size(iconSize),
                    icon = painterResource(icon),
                    contentDescription = contentDescription,
                )
            }
        } else {
            PIcon(
                modifier = Modifier.size(iconSize),
                icon = painterResource(icon),
                contentDescription = contentDescription,
            )
        }
    }
}
