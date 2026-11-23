 

















@Composable
fun <T : Any> rememberMutableStateListOf(vararg elements: T): SnapshotStateList<T> =
    rememberSaveable(saver = snapshotStateListSaver()) {
        elements.toList().toMutableStateList()
    }

private fun <T : Any> snapshotStateListSaver() =
    listSaver<SnapshotStateList<T>, T>(
        save = { stateList -> stateList.toList() },
        restore = { it.toMutableStateList() },
    )
