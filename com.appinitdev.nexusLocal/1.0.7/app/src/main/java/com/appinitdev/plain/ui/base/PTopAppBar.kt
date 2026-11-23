
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PTopAppBar(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    navigationIcon: (@Composable () -> Unit)? = {
        NavigationBackIcon { navController.navigateUp() }
    },
    title: String,
    subtitle: String = "",
    containerColor: Color? = null,
    subtitleColor: Color? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    val topBarColor = containerColor ?: MaterialTheme.colorScheme.background
    val topBarSubtitleColor = subtitleColor ?: MaterialTheme.colorScheme.onSurfaceVariant
    TopAppBar(
        title = {
            if (subtitle.isEmpty()) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                        color = topBarSubtitleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        navigationIcon = { navigationIcon?.invoke() },
        actions = {
            actions?.invoke(this)
            HorizontalSpace(8.dp)
        },
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = topBarColor,
            scrolledContainerColor = topBarColor,
        ),
        scrollBehavior = scrollBehavior,
    )
}
