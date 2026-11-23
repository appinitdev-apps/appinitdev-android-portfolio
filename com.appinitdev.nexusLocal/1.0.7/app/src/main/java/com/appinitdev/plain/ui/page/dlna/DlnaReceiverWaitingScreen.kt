
@Composable
fun DlnaReceiverWaitingScreen() {
    val deviceName = TempData.deviceName.value
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        PCard {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                DlnaRippleAnimation()
                Text(
                    text = stringResource(Res.string.dlna_receiver_waiting_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(Res.string.dlna_receiver_waiting_desc, deviceName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        VerticalSpace(16.dp)
        PCard {
            PListItem(
                title = stringResource(Res.string.dlna_receiver_step1_title),
                subtitle = stringResource(Res.string.dlna_receiver_step1_desc),
                start = { StepNumber(1) },
            )
            PListItem(
                title = stringResource(Res.string.dlna_receiver_step2_title),
                subtitle = stringResource(Res.string.dlna_receiver_step2_desc),
                start = { StepNumber(2) },
            )
            PListItem(
                title = stringResource(Res.string.dlna_receiver_step3_title),
                subtitle = stringResource(Res.string.dlna_receiver_step3_desc, deviceName),
                start = { StepNumber(3) },
            )
        }
        Tips(text = stringResource(Res.string.dlna_receiver_protocol_note))
    }
}

@Composable
private fun DlnaRippleAnimation() {
    val transition = rememberInfiniteTransition(label = "dlna_ripple")
    val pulse = @Composable { delayMs: Int ->
        val scale by transition.animateFloat(
            initialValue = 0.3f, targetValue = 1.1f,
            animationSpec = infiniteRepeatable(tween(2000, delayMs, LinearEasing), RepeatMode.Restart),
            label = "scale_$delayMs",
        )
        val alpha by transition.animateFloat(
            initialValue = 0.55f, targetValue = 0f,
            animationSpec = infiniteRepeatable(tween(2000, delayMs, LinearEasing), RepeatMode.Restart),
            label = "alpha_$delayMs",
        )
        Pair(scale, alpha)
    }
    val (s1, a1) = pulse(0); val (s2, a2) = pulse(650); val (s3, a3) = pulse(1300)
    val color = MaterialTheme.colorScheme.primary
    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(140.dp)) {
            fun DrawScope.ring(scale: Float, alpha: Float) =
                drawCircle(color.copy(alpha = alpha), radius = (size.minDimension / 2) * scale)
            ring(s1, a1); ring(s2, a2); ring(s3, a3)
            drawCircle(color, radius = 28.dp.toPx())
        }
        Icon(painter = painterResource(Res.drawable.cast), contentDescription = null, modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.onPrimary)
    }
}

