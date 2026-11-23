@HiltViewModel
class StoreViewModel @Inject constructor(
    private val repository: StoreRepository
) : ViewModel() {
    private val _store = MutableStateFlow<Store?>(null)
    val store: StateFlow<Store?> = _store.asStateFlow()
    init {
        ensureStoreExists()
    }
    private fun ensureStoreExists() {
        viewModelScope.launch {
            repository.getStore().collect { currentStore ->
                if (currentStore == null) {
                    val defaultStore = Store(
                        id = "default_store", 
                        name = "POS Manager",
                        address = "appinitdevgmail.com",
                        city = "Ciudad",
                        state = "Estado",
                        zipCode = "00000",
                        country = "México",
                        phone = null, 
                        email = null, 
                        website = null, 
                        taxId = null, 
                        currency = "MXN",
                        timezone = "America/Mexico_City",
                        isActive = true,
                        settings = null, 
                        createdAt = java.time.LocalDateTime.now(),
                        updatedAt = java.time.LocalDateTime.now()
                    )
                    repository.insertStore(defaultStore)
                } else {
                    _store.value = currentStore
                }
            }
        }
    }
    fun updateStore(store: Store) {
        viewModelScope.launch {
            repository.updateStore(
                store.copy(updatedAt = LocalDateTime.now())
            )
        }
    }
}