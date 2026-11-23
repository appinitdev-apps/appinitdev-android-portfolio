class NestedScrollViewItemTouchHelper(
    callback: Callback,
    private val scrollView: NestedScrollView,
) : ItemTouchHelper(callback) {
    private var selectedStartY: Int = -1
    private var selectedStartScrollY: Float = -1f
    private var selectedView: View? = null
    private var dragScrollStartTimeInMs: Long = 0
    private var lastmDy = 0f
    private var lastScrollY = 0
    private var tmpRect: Rect? = null
    override fun select(selected: RecyclerView.ViewHolder?, actionState: Int) {
        super.select(selected, actionState)
        if (selected != null) {
            selectedView = selected.itemView
            selectedStartY = selected.itemView.top
            selectedStartScrollY = scrollView!!.scrollY.toFloat()
        }
    }
    override fun scrollIfNecessary(): Boolean {
        if (mSelected == null) {
            dragScrollStartTimeInMs = Long.MIN_VALUE
            return false
        }
        val now = System.currentTimeMillis()
        val scrollDuration =
            if (dragScrollStartTimeInMs == Long.MIN_VALUE) 0 else now - dragScrollStartTimeInMs
        val lm = mRecyclerView.layoutManager
        if (tmpRect == null) {
            tmpRect = Rect()
        }
        var scrollY = 0
        val currentScrollY = scrollView.scrollY
        val actualShowingHeight =
            scrollView.height - mRecyclerView.top - mRecyclerView.paddingBottom
        lm!!.calculateItemDecorationsForChild(mSelected.itemView, tmpRect!!)
        if (lm.canScrollVertically()) {
            if (lastScrollY != 0 && abs(lastmDy) >= abs(mDy)) {
                scrollY = lastScrollY
            } else {
                val curY = (selectedStartY + mDy - currentScrollY).toInt()
                val checkDy = (mDy + selectedStartScrollY - currentScrollY).toInt()
                val topDiff = curY - tmpRect!!.top - mRecyclerView.paddingTop
                if (checkDy < 0 && topDiff < 0) { 
                    scrollY = topDiff
                } else if (checkDy > 0) { 
                    val bottomDiff = (curY + mSelected.itemView.height - actualShowingHeight) + 10
                    if (bottomDiff >= 0) {
                        scrollY = bottomDiff
                    }
                } else {
                    scrollY = 0
                }
            }
        }
        lastScrollY = scrollY
        lastmDy = mDy
        if (scrollY != 0) {
            scrollY =
                mCallback.interpolateOutOfBoundsScroll(
                    mRecyclerView,
                    mSelected.itemView.height,
                    scrollY,
                    actualShowingHeight,
                    scrollDuration,
                )
        }
        if (scrollY != 0) {
            val maxScrollY = scrollView.childrenHeightsSum - scrollView.height
            if (
                (scrollY < 0 && scrollView.scrollY > 0) ||
                    (scrollY > 0 && scrollView.scrollY < maxScrollY)
            ) {
                if (dragScrollStartTimeInMs == Long.MIN_VALUE) {
                    dragScrollStartTimeInMs = now
                }
                scrollView.scrollBy(0, scrollY)
                selectedView?.translationY = selectedView!!.translationY + scrollY
                return true
            }
        }
        dragScrollStartTimeInMs = Long.MIN_VALUE
        lastScrollY = 0
        lastmDy = 0f
        return false
    }
    private val ViewGroup.childrenHeightsSum
        get() = children.map { it.measuredHeight }.sum()
}
