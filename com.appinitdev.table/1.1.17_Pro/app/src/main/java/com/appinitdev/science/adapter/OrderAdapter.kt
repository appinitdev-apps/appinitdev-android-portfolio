class OrderAdapter(dataSet: List<String> = emptyList()) : DragDropSwipeAdapter<String, OrderAdapter.ViewHolder>(dataSet) {
    class ViewHolder(itemView: View) : DragDropSwipeAdapter.ViewHolder(itemView) {
        val itemText: TextView = itemView.findViewById(R.id.title_order)
        val dragIcon: ImageView = itemView.findViewById(R.id.handle_order)
    }
    override fun getViewHolder(itemLayout: View) = OrderAdapter.ViewHolder(itemLayout)
    override fun onBindViewHolder(item: String, viewHolder: OrderAdapter.ViewHolder, position: Int) {
        viewHolder.itemText.text = item
    }
    override fun getViewToTouchToStartDraggingItem(item: String, viewHolder: OrderAdapter.ViewHolder, position: Int): View? {
        return viewHolder.dragIcon
    }
}
