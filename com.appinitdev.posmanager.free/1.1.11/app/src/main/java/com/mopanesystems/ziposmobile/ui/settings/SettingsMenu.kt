
@Composable
fun SettingsMenu() {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.Settings, contentDescription = "Settings")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Cuenta") },
                onClick = {
                    expanded = false
                    // acción, ej. navController.navigate("account")
                }
            )
            DropdownMenuItem(
                text = { Text("Cerrar sesión") },
                onClick = {
                    expanded = false
                    // acción logout
                }
            )
        }
    }
}
