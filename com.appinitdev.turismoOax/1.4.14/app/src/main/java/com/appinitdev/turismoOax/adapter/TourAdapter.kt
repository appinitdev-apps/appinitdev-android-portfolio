class TourAdapter(
    private var tours: List<Tour>,
    private val listener: (Tour) -> Unit
) : RecyclerView.Adapter<TourAdapter.TourViewHolder>() {
    fun updateData(newTours: List<Tour>) {
        tours = newTours
        notifyDataSetChanged()
    }
    class TourViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nombre: TextView = view.findViewById(R.id.txtNombreTour)
        private val imagen: ImageView = view.findViewById(R.id.imgTour)
        private val btnFav: ImageView = view.findViewById(R.id.btnFav)
        fun bind(tour: Tour, listener: (Tour) -> Unit) {
            nombre.text = tour.getNombreActual()
            val imageSource = com.appinitdev.turismoOax.network.OfflineImageManager.getFileOrUrl(itemView.context, tour.imagenPrincipal)
            Glide.with(itemView.context)
                .load(imageSource)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(imagen)
            val dbHelper = DatabaseHelper(itemView.context)
            updateFavIcon(dbHelper.isFavorito(tour.id))
            btnFav.setOnClickListener {
                val isFavNow = dbHelper.toggleFavorito(tour)
                updateFavIcon(isFavNow)
            }
            itemView.setOnClickListener { listener(tour) }
        }
        private fun updateFavIcon(isFavorite: Boolean) {
            val colorRes = if (isFavorite) R.color.red_500 else R.color.grey_400
            val color = ContextCompat.getColor(itemView.context, colorRes)
            ImageViewCompat.setImageTintList(btnFav, ColorStateList.valueOf(color))
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TourViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tour, parent, false)
        return TourViewHolder(view)
    }
    override fun onBindViewHolder(holder: TourViewHolder, position: Int) {
        holder.bind(tours[position], listener)
    }
    override fun getItemCount() = tours.size
}
