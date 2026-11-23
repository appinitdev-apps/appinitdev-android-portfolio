@Composable
fun BreadcrumbView(
    breadcrumbs: List<BreadcrumbItem>,
    selectedIndex: Int,
    onItemClick: (BreadcrumbItem) -> Unit
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        breadcrumbs.forEachIndexed { index, item ->
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (index == selectedIndex) FontWeight.Bold else FontWeight.Normal,
                    color = if (index == selectedIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .clickable { onItemClick(item) }
                    .padding(vertical = 4.dp)
            )
            if (index < breadcrumbs.size - 1) {
                Text(
                    text = " / ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
} 