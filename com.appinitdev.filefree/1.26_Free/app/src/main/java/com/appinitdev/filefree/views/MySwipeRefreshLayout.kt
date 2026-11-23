class MySwipeRefreshLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : SwipeRefreshLayout(context, attrs) {
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (!isEnabled) {
            return false
        }
        return super.onInterceptTouchEvent(ev)
    }
    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean {
        return if (isEnabled) {
            return false
        } else {
            super.onStartNestedScroll(child, target, nestedScrollAxes)
        }
    }
    override fun canChildScrollUp(): Boolean {
        val directChild = getChildAt(0)
        return when (directChild) {
            is RecyclerViewFastScroller -> {
                val innerRecyclerView = directChild.getChildAt(0)
                innerRecyclerView?.canScrollVertically(-1) == true
            }
            else -> super.canChildScrollUp()
        }
    }
}
