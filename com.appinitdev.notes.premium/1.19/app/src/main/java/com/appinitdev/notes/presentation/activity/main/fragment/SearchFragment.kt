
class SearchFragment : NotallyFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // TODO: autofocus and show keyboard
        val initialFolder =
            arguments?.let {
                BundleCompat.getSerializable(it, EXTRA_INITIAL_FOLDER, Folder::class.java)
            }
        binding?.FilterChipGroup?.visibility = View.VISIBLE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding?.MainListView?.scrollIndicators = View.SCROLL_INDICATOR_TOP
        }
        super.onViewCreated(view, savedInstanceState)

        val initialLabel = arguments?.getString(EXTRA_INITIAL_LABEL)
        model.currentLabel = initialLabel
        if (initialLabel?.isEmpty() == true) {
            val checked =
                when (initialFolder ?: model.folder.value) {
                    Folder.NOTES -> R.id.Notes
                    Folder.DELETED -> R.id.Deleted
                    Folder.ARCHIVED -> R.id.Archived
                }

            binding?.FilterChipGroup?.apply {
                setOnCheckedStateChangeListener { _, checkedId ->
                    when (checkedId.firstOrNull()) {
                        R.id.Notes -> model.folder.value = Folder.NOTES
                        R.id.Deleted -> model.folder.value = Folder.DELETED
                        R.id.Archived -> model.folder.value = Folder.ARCHIVED
                    }
                }
                check(checked)
                isVisible = true
            }
        } else binding?.FilterChipGroup?.isVisible = false
        getObservable().observe(viewLifecycleOwner) { items ->
            model.actionMode.updateSelected(items?.filterIsInstance<BaseNote>()?.map { it.id })
        }
    }

    override fun getBackground() = R.drawable.search

    override fun getObservable() = model.searchResults!!

    companion object {
        const val EXTRA_INITIAL_FOLDER = "notes.intent.extra.INITIAL_FOLDER"
        const val EXTRA_INITIAL_LABEL = "notes.intent.extra.INITIAL_LABEL"
    }
}
