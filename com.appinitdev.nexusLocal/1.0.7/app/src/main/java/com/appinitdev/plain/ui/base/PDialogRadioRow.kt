@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PDialogRadioRow(selected: Boolean, onClick: () -> Unit, text: String) {
    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .clickable {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(
            modifier = Modifier.padding(start = 6.dp),
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                baselineShift = BaselineShift.None,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}