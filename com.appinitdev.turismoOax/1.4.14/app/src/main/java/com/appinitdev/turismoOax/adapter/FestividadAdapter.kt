class FestividadAdapter(
    private var list: List<Festividad>
) : RecyclerView.Adapter<FestividadAdapter.FestividadViewHolder>() {
    fun updateData(newList: List<Festividad>) {
        list = newList
        notifyDataSetChanged()
    }
    class FestividadViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val emoji: TextView = view.findViewById(R.id.tvFestividadEmoji)
        val nombre: TextView = view.findViewById(R.id.tvFestividadNombre)
        val lugar: TextView = view.findViewById(R.id.tvFestividadLugar)
        val fechaBadge: TextView = view.findViewById(R.id.tvFestividadFechaBadge)
        val hoyTag: TextView = view.findViewById(R.id.tvFestividadHoyTag)
        val descripcion: TextView = view.findViewById(R.id.tvFestividadDescripcion)
        val tip: TextView = view.findViewById(R.id.tvFestividadTip)
        val btnAgendar: MaterialButton = view.findViewById(R.id.btnFestividadAgendar)
        val btnCompartir: MaterialButton = view.findViewById(R.id.btnFestividadCompartir)
        val btnCotizar: MaterialButton = view.findViewById(R.id.btnFestividadCotizar)
        fun bind(item: Festividad) {
            emoji.text = item.emoji
            nombre.text = item.nombre
            lugar.text = "📍 ${item.municipio}"
            fechaBadge.text = item.fecha
            descripcion.text = item.descripcion
            tip.text = "💡 Recomendación: ${item.recomendacion}"
            val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1
            if (item.mesNum == currentMonth) {
                hoyTag.visibility = View.VISIBLE
            } else {
                hoyTag.visibility = View.GONE
            }
            btnAgendar.setOnClickListener {
                val context = itemView.context
                val intent = Intent(Intent.ACTION_INSERT).apply {
                    data = CalendarContract.Events.CONTENT_URI
                    putExtra(CalendarContract.Events.TITLE, "🎉 ${item.nombre} (Oaxaca)")
                    putExtra(CalendarContract.Events.EVENT_LOCATION, item.municipio)
                    putExtra(CalendarContract.Events.DESCRIPTION, "${item.descripcion}\n\nRecomendación: ${item.recomendacion}")
                }
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                } else {
                    context.startActivity(Intent.createChooser(intent, "Agendar Evento"))
                }
            }
            btnCompartir.setOnClickListener {
                val context = itemView.context
                val text = "🎉 ¡Mira este evento cultural en Oaxaca!\n\n*${item.nombre}*\n📍 ${item.municipio}\n📅 ${item.fecha}\n\n${item.descripcion}\n\n¡Descubre más en la app Turismo Oaxaca! 🇲🇽"
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, text)
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "Compartir Festividad"))
            }
            btnCotizar.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, CotizacionActivity::class.java)
                intent.putExtra("tour", "Visita a Festividad: ${item.nombre}")
                context.startActivity(intent)
            }
            itemView.setOnClickListener {
                val context = itemView.context
                MaterialAlertDialogBuilder(context)
                    .setTitle("${item.emoji} ${item.nombre}")
                    .setMessage("📍 Ubicación: ${item.municipio}\n📅 Fecha: ${item.fecha}\n\n${item.descripcion}\n\n💡 Consejos:\n${item.recomendacion}")
                    .setPositiveButton("Cotizar Viaje") { _, _ ->
                        val intent = Intent(context, CotizacionActivity::class.java)
                        intent.putExtra("tour", "Visita a Festividad: ${item.nombre}")
                        context.startActivity(intent)
                    }
                    .setNegativeButton("Cerrar", null)
                    .show()
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FestividadViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_festividad, parent, false)
        return FestividadViewHolder(view)
    }
    override fun onBindViewHolder(holder: FestividadViewHolder, position: Int) {
        holder.bind(list[position])
    }
    override fun getItemCount() = list.size
}
