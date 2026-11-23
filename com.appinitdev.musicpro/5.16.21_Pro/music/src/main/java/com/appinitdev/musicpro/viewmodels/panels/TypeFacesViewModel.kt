class TypeFacesViewModel(application: Application) : WrappedViewModel(application) {
    private val _typefaceCache = MutableLiveData<Map<String, Typeface>>()
    val typefaceCache: LiveData<Map<String, Typeface>> = _typefaceCache
    init {
        preloadTypefaces()
    }
    private fun preloadTypefaces() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = mutableMapOf<String, Typeface>()
            TypeFace.list.forEach { model ->
                TypefaceStyle.entries.forEach { style ->
                    val typeface = TypeFace.getTypeFace(model.name, style.style, context)
                    val weight = style.name
                    result["${model.name}-$weight"] = typeface
                }
            }
            _typefaceCache.postValue(result)
        }
    }
    override fun onCleared() {
        super.onCleared()
        TypeFace.clearAllPreloadedCache()
    }
}
