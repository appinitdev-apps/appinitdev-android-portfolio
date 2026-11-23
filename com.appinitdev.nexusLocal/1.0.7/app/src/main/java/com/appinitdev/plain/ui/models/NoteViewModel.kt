
@OptIn(SavedStateHandleSaveableApi::class)
class NoteViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    var editMode by savedStateHandle.saveable { mutableStateOf(false) }
    val item = mutableStateOf<DNote?>(null)
    var content by savedStateHandle.saveable { mutableStateOf("") }
    val showSelectTagsDialog = mutableStateOf(false)
}
