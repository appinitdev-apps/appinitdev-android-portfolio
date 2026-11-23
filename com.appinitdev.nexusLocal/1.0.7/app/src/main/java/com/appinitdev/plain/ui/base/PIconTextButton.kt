@Composable
fun PIconTextButton(
    icon: DrawableResource,
    text: String,
    click: () -> Unit,
) {
    Button(
        onClick = click,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
            )
        }
    }
}
@Composable
fun PIconTextActionButton(
    icon: DrawableResource,
    text: String,
    click: () -> Unit,
) {
    Column(
        modifier = Modifier.widthIn(max = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FilledTonalIconButton(
            modifier = Modifier.size(48.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors()
                .copy(containerColor = MaterialTheme.colorScheme.cardBackgroundNormal),
            onClick = click,
        ) {
            PIcon(
                icon = painterResource(icon),
                contentDescription = text,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
        )
    }
}
