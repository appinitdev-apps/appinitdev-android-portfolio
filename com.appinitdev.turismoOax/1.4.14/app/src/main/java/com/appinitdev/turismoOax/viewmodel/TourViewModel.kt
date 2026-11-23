
class TourViewModel : ViewModel() {
    private val _tours = MutableLiveData<List<Tour>>()
    val tours: LiveData<List<Tour>> get() = _tours

    init {
        cargarTours()
    }

    private fun cargarTours() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getTours()
                _tours.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}