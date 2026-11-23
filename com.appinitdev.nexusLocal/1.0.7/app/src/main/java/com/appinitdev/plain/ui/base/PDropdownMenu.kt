
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier.defaultMinSize(minWidth = 160.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier.background(MaterialTheme.colorScheme.inverseOnSurface),
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PDropdownMenuItem(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
) {
    DropdownMenuItem(
        text = text,
        onClick = onClick,
        modifier = Modifier.defaultMinSize(minHeight = 56.dp),
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        contentPadding = PaddingValues(
            horizontal = 24.dp,
            vertical = 0.dp,
        ),
        enabled = enabled,
    )
}

@Composable
fun PDropdownMenuItemCreateFolder(onClick: () -> Unit) {
    PDropdownMenuItem(text = { Text(stringResource(Res.string.create_folder)) }, leadingIcon = {
        Icon(
            painter = painterResource(Res.drawable.folder_plus),
            contentDescription = stringResource(Res.string.create_folder)
        )
    }, onClick = onClick)
}

@Composable
fun PDropdownMenuItemCreateFile(onClick: () -> Unit) {
    PDropdownMenuItem(text = { Text(stringResource(Res.string.create_file)) }, leadingIcon = {
        Icon(
            painter = painterResource(Res.drawable.file_plus),
            contentDescription = stringResource(Res.string.create_file)
        )
    }, onClick = onClick)
}

@Composable
fun PDropdownMenuItemCast(onClick: () -> Unit) {
    PDropdownMenuItem(text = { Text(stringResource(Res.string.cast_mode)) }, leadingIcon = {
        Icon(
            painter = painterResource(Res.drawable.cast),
            contentDescription = stringResource(Res.string.cast_mode)
        )
    }, onClick = onClick)
}

@Composable
fun PDropdownMenuItemSort(onClick: () -> Unit) {
    PDropdownMenuItem(text = { Text(stringResource(Res.string.sort)) }, leadingIcon = {
        Icon(
            painter = painterResource(Res.drawable.sort),
            contentDescription = stringResource(Res.string.sort)
        )
    }, onClick = onClick)
}

@Composable
fun PDropdownMenuItemTags(onClick: () -> Unit) {
    PDropdownMenuItem(text = { Text(stringResource(Res.string.tags)) }, leadingIcon = {
        Icon(
            painter = painterResource(Res.drawable.tags),
            contentDescription = stringResource(Res.string.tags)
        )
    }, onClick = onClick)
}

@Composable
fun PDropdownMenuItemSettings(onClick: () -> Unit) {
    PDropdownMenuItem(text = { Text(stringResource(Res.string.settings)) }, leadingIcon = {
        Icon(
            painter = painterResource(Res.drawable.settings),
            contentDescription = stringResource(Res.string.settings)
        )
    }, onClick = onClick)
}