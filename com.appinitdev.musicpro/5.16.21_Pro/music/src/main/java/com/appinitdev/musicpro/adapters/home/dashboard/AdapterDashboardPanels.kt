
 








class AdapterDashboardPanels(
        private val panels: List<Panel>
) : RecyclerView.Adapter<AdapterDashboardPanels.PanelHolder>() {

    private var callbacks: AdapterDashboardPanelsCallbacks? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PanelHolder {
        return PanelHolder(
                AdapterDashboardPanelBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int = panels.size

    override fun onBindViewHolder(holder: PanelHolder, position: Int) {
        val panel = panels[position]
        holder.binding.title.setStartDrawable(panel.iconResId)
        holder.binding.title.text = holder.itemView.context.getString(panel.titleResId)
        holder.binding.title.setOnClickListener {
            callbacks?.onPanelClicked(panels[holder.bindingAdapterPosition])
        }
    }

     




    fun setCallbacks(callbacks: AdapterDashboardPanelsCallbacks) {
        this.callbacks = callbacks
    }

    inner class PanelHolder(val binding: AdapterDashboardPanelBinding) :
            VerticalListViewHolder(binding.root)

    companion object {
         


        interface AdapterDashboardPanelsCallbacks {
             
            fun onPanelClicked(panel: Panel)
        }
    }
}
