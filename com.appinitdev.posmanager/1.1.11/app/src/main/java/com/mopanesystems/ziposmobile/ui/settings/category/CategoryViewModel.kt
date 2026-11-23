
@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            val existingCategories = repository.getAllCategories().first()
            if (existingCategories.isEmpty()) {
                insertDefaultCategories()
            }
        }
    }

    private suspend fun insertDefaultCategories() {
        val defaultCategories = listOf(
            Category(id = "Default1", name = "All"),
            Category(id = "Default2", name = "Food"),
            Category(id = "Default3", name = "Beverages"),
            Category(id = "Default4", name = "Snacks"),
            Category(id = "Default5", name = "Hygiene"),
            Category(id = "Default6", name = "Electronics"),
            Category(id = "Default7", name = "Clothing"),
            Category(id = "Default8", name = "Home & Kitchen"),
            Category(id = "Default9", name = "Books"),
            Category(id = "Default10", name = "Toys & Games"),
            Category(id = "Default11", name = "Sports"),
            Category(id = "Default12", name = "Beauty"),
            Category(id = "Default13", name = "Health"),
            Category(id = "Default14", name = "Automotive"),
            Category(id = "Default15", name = "Stationery")
        )
        repository.insertCategories(defaultCategories)
    }

    val allCategories: StateFlow<List<Category>> =
        repository.getAllCategories()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val activeCategories: StateFlow<List<Category>> =
        repository.getActiveCategories()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun insertCategory(category: Category) {
        viewModelScope.launch {
            repository.insertCategory(category)
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            repository.updateCategory(category)
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }
}
