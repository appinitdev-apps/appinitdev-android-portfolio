
@Composable
fun ChatDate(
    items: List<VChat>,
    m: VChat,
    index: Int,
) {
    val dateVisible =
        remember {
            if (index == items.size - 1) {
                true
            } else {
                if (index + 1 < items.size) {
                    items[index + 1].createdAt.formatDate() != m.createdAt.formatDate()
                } else {
                    false
                }
            }
        }
    if (dateVisible) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = m.createdAt.formatDate(),
                color = MaterialTheme.colorScheme.secondaryTextColor,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Normal),
            )
        }
    }
}
