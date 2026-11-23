
data class MdAccessoryItem(val text: String, val before: String, val after: String = "")
data class MdAccessoryItem2(val icon: DrawableResource, val click: (MdEditorViewModel) -> Unit = {})

@OptIn(ExperimentalFoundationApi::class, SavedStateHandleSaveableApi::class)
class MdEditorViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    val textFieldState = TextFieldState("")
    var showSettings by savedStateHandle.saveable { mutableStateOf(false) }
    var showInsertImage by savedStateHandle.saveable { mutableStateOf(false) }
    var showColorPicker by savedStateHandle.saveable { mutableStateOf(false) }
    var wrapContent by savedStateHandle.saveable { mutableStateOf(true) }
    var showLineNumbers by savedStateHandle.saveable { mutableStateOf(true) }
    var syntaxHighLight by savedStateHandle.saveable { mutableStateOf(true) }
    var linesText by savedStateHandle.saveable { mutableStateOf("1") }
    var level by savedStateHandle.saveable { mutableIntStateOf(0) }

    fun load(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            level = EditorAccessoryLevelPreference.getAsync()
            wrapContent = EditorWrapContentPreference.getAsync()
            showLineNumbers = EditorShowLineNumbersPreference.getAsync()
            syntaxHighLight = EditorSyntaxHighlightPreference.getAsync()
        }
    }

    fun toggleLevel(context: Context) {
        level = if (level == 1) 0 else 1
        viewModelScope.launch(Dispatchers.IO) {
            EditorAccessoryLevelPreference.putAsync(level)
        }
    }

    fun toggleLineNumbers(context: Context) {
        showLineNumbers = !showLineNumbers
        viewModelScope.launch(Dispatchers.IO) {
            EditorShowLineNumbersPreference.putAsync(showLineNumbers)
        }
    }

    fun toggleWrapContent(context: Context) {
        wrapContent = !wrapContent
        viewModelScope.launch(Dispatchers.IO) {
            EditorWrapContentPreference.putAsync(wrapContent)
        }
    }

    fun insertColor(color: String) {
        textFieldState.edit { inlineWrap("<font color=\"$color\">", "</font>") }
        showColorPicker = false
    }
}
