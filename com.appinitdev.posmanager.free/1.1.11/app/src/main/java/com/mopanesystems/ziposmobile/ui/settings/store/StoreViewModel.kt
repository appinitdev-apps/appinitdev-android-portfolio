
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
                        id = "default_store", // solo tendrás un registro
                        name = "POS Manager",
                        address = "appinitdevgmail.com",
                        city = "Ciudad",
                        state = "Estado",
                        zipCode = "00000",
                        country = "México",
                        phone = null, // opcional
                        email = null, // opcional
                        website = null, // opcional
                        taxId = null, // opcional
                        currency = "MXN",
                        timezone = "America/Mexico_City",
                        isActive = true,
                        settings = null, // si no tienes StoreSettings todavía
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