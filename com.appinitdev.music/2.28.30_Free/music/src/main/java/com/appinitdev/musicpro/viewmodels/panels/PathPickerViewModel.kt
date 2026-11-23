
 








@HiltViewModel
class PathPickerViewModel @Inject constructor(
        application: Application
) : WrappedViewModel(application) {

     



    private val supportedAudioExtensions = setOf(
            "mp3", "flac", "ogg", "opus", "wav", "aac", "m4a", "wma",
            "ape", "alac", "aiff", "aif", "dsf", "dsd", "mka", "webm",
            "3gp", "amr", "mid", "midi", "xm", "mod", "s3m", "it"
    )

     
    val items: MutableLiveData<List<PathItem>> = MutableLiveData(emptyList())

     
    val currentPath: MutableLiveData<String?> = MutableLiveData(null)

     



    private val pathStack: ArrayDeque<String?> = ArrayDeque()

    init {
        loadStorageRoots()
    }

     



    private fun loadStorageRoots() {
        viewModelScope.launch(Dispatchers.IO) {
            val roots = mutableListOf<PathItem>()
            val removableVolumes = RemovableStorageDetector.getAllStorageVolumes(context)
            removableVolumes.forEach { storageInfo ->
                val path = storageInfo.path() ?: return@forEach
                if (!path.exists() || !path.canRead()) return@forEach

                roots.add(
                        PathItem(
                                file = path,
                                isDirectory = true,
                                displayName = storageInfo.description() ?: path.name
                        )
                )
            }

            currentPath.postValue(null)
            items.postValue(roots)
        }
    }

     



    fun navigateTo(path: String) {
        pathStack.addLast(currentPath.value)
        loadDirectory(path)
    }

     



    fun navigateBack(): Boolean {
        if (pathStack.isEmpty()) return false
        val previous = pathStack.removeLast()
        if (previous == null) {
            loadStorageRoots()
        } else {
            loadDirectory(previous)
        }
        return true
    }

     



    fun isAtRoot(): Boolean {
        return pathStack.isEmpty()
    }

     



    private fun loadDirectory(path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val dir = File(path)
            if (!dir.exists() || !dir.canRead()) {
                items.postValue(emptyList())
                currentPath.postValue(path)
                return@launch
            }

            val contents = dir.listFiles() ?: emptyArray()
            val result = contents
                .filter { file ->
                    when {
                        
                        file.isDirectory -> !file.name.startsWith(".")
                        
                        file.isFile -> file.extension.lowercase() in supportedAudioExtensions
                        else -> false
                    }
                }
                .sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
                .map { file ->
                    PathItem(
                            file = file,
                            isDirectory = file.isDirectory,
                            displayName = file.name
                    )
                }

            currentPath.postValue(path)
            items.postValue(result)
        }
    }

     


    data class PathItem(
            val file: File,
            val isDirectory: Boolean,
            val displayName: String
    )
}

