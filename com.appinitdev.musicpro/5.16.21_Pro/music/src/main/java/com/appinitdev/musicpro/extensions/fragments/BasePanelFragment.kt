
 











abstract class BasePanelFragment : PanelFragment() {

     




    protected var gridLayoutManager: GridLayoutManager? = null

    override fun onDestroyView() {
        gridLayoutManager = null
        super.onDestroyView()
    }

     






    protected fun RecyclerView.setupGridLayoutManager(spanCount: Int): GridLayoutManager {
        return GridLayoutManager(requireContext(), spanCount).also { manager ->
            layoutManager = manager
            gridLayoutManager = manager
        }
    }

     







    protected fun <T> Flow<T>.collectWhenStarted(onEach: (T) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                collect { value -> onEach(value) }
            }
        }
    }

     










    protected fun <T> Flow<List<T>>.collectListWhenStarted(
            hasActiveAdapter: () -> Boolean,
            onEach: (List<T>) -> Unit
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                collect { items ->
                    if (items.isNotEmpty() || hasActiveAdapter()) {
                        onEach(items)
                    }
                }
            }
        }
    }

     







    protected fun applyGridSizeUpdate(recyclerView: SpacingRecyclerView, newSpanCount: Int) {
        gridLayoutManager?.spanCount = newSpanCount
        recyclerView.beginDelayedTransition()
        recyclerView.adapter?.notifyItemRangeChanged(0, recyclerView.adapter?.itemCount ?: 0)
    }
}

