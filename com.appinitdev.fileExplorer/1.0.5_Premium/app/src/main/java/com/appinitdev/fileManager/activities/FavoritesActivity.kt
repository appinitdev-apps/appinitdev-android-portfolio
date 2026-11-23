class FavoritesActivity : SimpleActivity(), RefreshRecyclerViewListener {
    private val binding by viewBinding(ActivityFavoritesBinding::inflate)
    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupOptionsMenu()
        updateFavorites()
        binding.apply {
            updateMaterialActivityViews(manageFavoritesCoordinator, manageFavoritesList, useTransparentNavigation = true, useTopSearchMenu = false)
            setupMaterialScrollListener(manageFavoritesList, manageFavoritesToolbar)
        }
    }
    override fun onResume() {
        super.onResume()
        setupToolbar(binding.manageFavoritesToolbar, NavigationIcon.Arrow)
    }
    private fun setupOptionsMenu() {
        binding.manageFavoritesToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.add_favorite -> addFavorite()
                else -> return@setOnMenuItemClickListener false
            }
            return@setOnMenuItemClickListener true
        }
    }
    private fun updateFavorites() {
        binding.apply {
            val favorites = ArrayList<String>()
            config.favorites.mapTo(favorites) { it }
            manageFavoritesPlaceholder.beVisibleIf(favorites.isEmpty())
            manageFavoritesPlaceholder.setTextColor(getProperTextColor())
            manageFavoritesPlaceholder2.apply {
                paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
                beVisibleIf(favorites.isEmpty())
                setTextColor(getProperPrimaryColor())
                setOnClickListener {
                    addFavorite()
                }
            }
            ManageFavoritesAdapter(this@FavoritesActivity, favorites, this@FavoritesActivity, manageFavoritesList) { }.apply {
                manageFavoritesList.adapter = this
            }
        }
    }
    override fun refreshItems() {
        updateFavorites()
    }
    private fun addFavorite() {
        FilePickerDialog(this, pickFile = false, showHidden = config.shouldShowHidden(), canAddShowHiddenButton = true) {
            config.addFavorite(it)
            updateFavorites()
        }
    }
}
