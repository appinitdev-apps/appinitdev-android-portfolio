class EmissionAdapter(var list: ArrayList<Element>, var clickListener: EmissionActivity, val context: Context) : RecyclerView.Adapter<EmissionAdapter.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.initialize(list[position], clickListener, context)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.row_emission_list, parent, false)
        return ViewHolder(v)
    }
    override fun getItemCount(): Int { return list.size }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView = itemView.findViewById(R.id.emission_row) as FrameLayout
        private val emissionImg = itemView.findViewById(R.id.emission_img) as ImageView
        private val shortEmi = itemView.findViewById(R.id.emi_short) as TextView
        private val textEmi = itemView.findViewById(R.id.emi_text) as TextView
        private val nameEmi = itemView.findViewById(R.id.emi_name) as TextView
        fun initialize(item: Element, action: OnEmissionClickListener, context: Context) {
            try {
                val jsonObject = ElementDataLoader.loadElementData(context, item.elementKey)
                val url = jsonObject?.optString("short", "---") ?: "---"
                val hUrl = "https://www.jlindemann.se/atomic/emission_lines/"
                val extg = ".gif"
                val fURL = hUrl + url + extg
                try {
                    Picasso.get().load(fURL).into(emissionImg)
                }
                catch(e: ConnectException) {
                }
            }
            catch (e: IOException) { }
            shortEmi.text = item.number.toString()
            nameEmi.text = item.short
            textEmi.text = item.element.capitalize()
            if (item.number > 98) {
                cardView.visibility = View.GONE
            }
            cardView.foreground = ContextCompat.getDrawable(context, R.drawable.toast_card_ripple)
            cardView.isClickable = true
            cardView.isFocusable = true
            cardView.setOnClickListener {
                action.emiClickListener(item, adapterPosition)
            }
        }
    }
    fun filterList(filteredList: ArrayList<Element>) {
        list = filteredList
        notifyDataSetChanged()
    }
    interface OnEmissionClickListener {
        fun emiClickListener(item: Element, position: Int)
    }
}
