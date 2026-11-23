
 














class Content(
    private var liveData: LiveData<List<BaseNote>>,
    private val transform: (List<BaseNote>) -> List<Item>,
) : LiveData<List<Item>>() {
    private var observer: Observer<List<BaseNote>>? = null

    init {
        setObserver(liveData)
    }

    fun setObserver(liveData: LiveData<List<BaseNote>>) {
        observer?.let { this.liveData.removeObserver(it) }
        this.liveData = liveData
        observer = Observer { list -> value = transform(list) }
        this.liveData.observeForever(observer!!)
    }
}
