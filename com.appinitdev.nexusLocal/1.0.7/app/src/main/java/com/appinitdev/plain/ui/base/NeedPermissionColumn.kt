
@Composable
fun NeedPermissionColumn(
    icon: DrawableResource,
    permission: DFeaturePermission
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(800)) + expandVertically(
                animationSpec = tween(800, easing = FastOutSlowInEasing)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(26.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                AnimatedIconContainer(icon)

                Text(
                    text = permission.permission.getGrantAccessText(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp)
                )

                Button(
                    onClick = {
                        sendEvent(RequestPermissionsEvent(*permission.permissions.toTypedArray()))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                        .shadow(8.dp, RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp, horizontal = 16.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.grant_access),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}
